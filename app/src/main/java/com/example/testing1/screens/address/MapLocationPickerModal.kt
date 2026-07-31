package com.example.testing1.screens.address

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

data class SearchResultItem(
    val displayName: String,
    val lat: Double,
    val lon: Double
)

@Composable
fun MapLocationPickerModal(
    initialLatitude: Double? = null,
    initialLongitude: Double? = null,
    initialTag: String = "Home",
    onDismiss: () -> Unit,
    onAddressSaved: (tag: String, fullAddress: String, lat: Double, lng: Double) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Configure OSMDroid user agent (required by OSM terms)
    remember {
        Configuration.getInstance().userAgentValue = "CoffeeAPP-Android-Delivery/1.0 (dev@coffeeapp.io)"
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        true
    }

    val cartoDbTileSource = remember {
        XYTileSource(
            "CartoDB-Voyager",
            0, 20, 256, ".png",
            arrayOf(
                "https://a.basemaps.cartocdn.com/rastertiles/voyager/",
                "https://b.basemaps.cartocdn.com/rastertiles/voyager/",
                "https://c.basemaps.cartocdn.com/rastertiles/voyager/"
            )
        )
    }

    // Default starting point (Bhopal / Default fallback)
    val defaultLat = initialLatitude ?: 23.2599
    val defaultLng = initialLongitude ?: 77.4126

    var centerGeoPoint by remember { mutableStateOf(GeoPoint(defaultLat, defaultLng)) }
    var fullAddress by remember { mutableStateOf("Fetching location address...") }
    var selectedTag by remember { mutableStateOf(initialTag) }
    var customTagText by remember { mutableStateOf("") }
    var isGeocoding by remember { mutableStateOf(false) }

    // Auto-suggest search state
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<SearchResultItem>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var showSuggestions by remember { mutableStateOf(false) }
    var searchDebounceJob by remember { mutableStateOf<Job?>(null) }

    var osmMapViewState by remember { mutableStateOf<MapView?>(null) }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Perform place search query via Nominatim (Free, Keyless)
    fun performSearch(query: String) {
        if (query.trim().length < 2) {
            searchResults = emptyList()
            showSuggestions = false
            return
        }
        isSearching = true
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val encodedQuery = URLEncoder.encode(query.trim(), "UTF-8")
                val urlString = "https://nominatim.openstreetmap.org/search?format=json&q=$encodedQuery&limit=6"
                val conn = URL(urlString).openConnection() as HttpURLConnection
                conn.setRequestProperty("User-Agent", "CoffeeAPP-Android-Delivery/1.0 (dev@coffeeapp.io)")
                conn.connectTimeout = 4000
                conn.readTimeout = 4000

                if (conn.responseCode == 200) {
                    val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                    val jsonArray = JSONArray(responseText)
                    val items = mutableListOf<SearchResultItem>()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        items.add(
                            SearchResultItem(
                                displayName = obj.getString("display_name"),
                                lat = obj.getString("lat").toDouble(),
                                lon = obj.getString("lon").toDouble()
                            )
                        )
                    }
                    withContext(Dispatchers.Main) {
                        searchResults = items
                        showSuggestions = items.isNotEmpty()
                        isSearching = false
                    }
                } else {
                    withContext(Dispatchers.Main) { isSearching = false }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { isSearching = false }
            }
        }
    }

    // Reverse geocode LatLng into human-readable address
    fun updateAddressForLocation(lat: Double, lng: Double) {
        centerGeoPoint = GeoPoint(lat, lng)
        isGeocoding = true
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(lat, lng, 1) { addresses ->
                        val first = addresses.firstOrNull()
                        val text = if (first != null) {
                            first.getAddressLine(0) ?: "${first.locality ?: ""}, ${first.countryName ?: ""}".trim(',', ' ')
                        } else {
                            "Lat: %.4f, Lng: %.4f".format(lat, lng)
                        }
                        coroutineScope.launch(Dispatchers.Main) {
                            fullAddress = text.ifBlank { "Lat: %.4f, Lng: %.4f".format(lat, lng) }
                            isGeocoding = false
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(lat, lng, 1)
                    val first = addresses?.firstOrNull()
                    val text = if (first != null) {
                        first.getAddressLine(0) ?: "${first.locality ?: ""}, ${first.countryName ?: ""}".trim(',', ' ')
                    } else {
                        "Lat: %.4f, Lng: %.4f".format(lat, lng)
                    }
                    withContext(Dispatchers.Main) {
                        fullAddress = text.ifBlank { "Lat: %.4f, Lng: %.4f".format(lat, lng) }
                        isGeocoding = false
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    fullAddress = "Location: %.4f, %.4f".format(lat, lng)
                    isGeocoding = false
                }
            }
        }
    }

    // Move map to GPS location
    fun moveToCurrentGpsLocation() {
        if (!hasLocationPermission) return
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val gpsPoint = GeoPoint(location.latitude, location.longitude)
                    osmMapViewState?.controller?.animateTo(gpsPoint, 17.0, 1000L)
                    updateAddressForLocation(location.latitude, location.longitude)
                }
            }
        } catch (e: SecurityException) {
            // Permission missing
        }
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                      permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasLocationPermission = granted
        if (granted) {
            moveToCurrentGpsLocation()
        }
    }

    // Request GPS permission & center on open
    LaunchedEffect(Unit) {
        if (hasLocationPermission) {
            moveToCurrentGpsLocation()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
        updateAddressForLocation(defaultLat, defaultLng)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                topBar = {
                    Surface(
                        tonalElevation = 4.dp,
                        shadowElevation = 4.dp,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = onDismiss,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Select Location",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                },
                bottomBar = {
                    // Bottom Address Card & Label Selector
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding(),
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "DELIVERY ADDRESS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isGeocoding) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(
                                    text = fullAddress,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "LABEL AS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf("Home", "Work", "Other").forEach { tagOption ->
                                    FilterChip(
                                        selected = selectedTag == tagOption,
                                        onClick = { selectedTag = tagOption },
                                        label = { Text(tagOption, fontWeight = FontWeight.Medium) },
                                        leadingIcon = if (selectedTag == tagOption) {
                                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                        } else null
                                    )
                                }
                            }

                            if (selectedTag == "Other") {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = customTagText,
                                    onValueChange = { customTagText = it },
                                    label = { Text("Custom Tag (e.g. Gym, Friend's Place)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    val finalTag = if (selectedTag == "Other" && customTagText.isNotBlank()) {
                                        customTagText.trim()
                                    } else {
                                        selectedTag
                                    }
                                    onAddressSaved(
                                        finalTag,
                                        fullAddress,
                                        centerGeoPoint.latitude,
                                        centerGeoPoint.longitude
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                enabled = !isGeocoding && fullAddress.isNotBlank()
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Confirm & Save Location", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Interactive Map View
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            MapView(ctx).apply {
                                setTileSource(cartoDbTileSource)
                                setMultiTouchControls(true)
                                controller.setZoom(16.0)
                                controller.setCenter(centerGeoPoint)
                                addMapListener(object : MapListener {
                                    override fun onScroll(event: ScrollEvent?): Boolean {
                                        val center = mapCenter
                                        updateAddressForLocation(center.latitude, center.longitude)
                                        return true
                                    }

                                    override fun onZoom(event: ZoomEvent?): Boolean {
                                        val center = mapCenter
                                        updateAddressForLocation(center.latitude, center.longitude)
                                        return true
                                    }
                                })
                                osmMapViewState = this
                            }
                        },
                        update = { mapView ->
                            osmMapViewState = mapView
                        }
                    )

                    // Center Pin Marker fixed at center
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Target Location",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(52.dp)
                            )
                            Spacer(modifier = Modifier.height(26.dp))
                        }
                    }

                    // Floating Map Controls (Right Side: Zoom In, Zoom Out, GPS Location)
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Zoom In Button
                        SmallFloatingActionButton(
                            onClick = {
                                osmMapViewState?.controller?.zoomIn()
                            },
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Zoom In")
                        }

                        // Zoom Out Button
                        SmallFloatingActionButton(
                            onClick = {
                                osmMapViewState?.controller?.zoomOut()
                            },
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Zoom Out")
                        }

                        // GPS Current Location FAB
                        FloatingActionButton(
                            onClick = {
                                if (hasLocationPermission) {
                                    moveToCurrentGpsLocation()
                                } else {
                                    permissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "My GPS Location"
                            )
                        }
                    }

                    // Floating Auto-Suggest Search Bar Overlay
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    ) {
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search place",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(8.dp)
                                )
                                TextField(
                                    value = searchQuery,
                                    onValueChange = { text ->
                                        searchQuery = text
                                        searchDebounceJob?.cancel()
                                        searchDebounceJob = coroutineScope.launch {
                                            delay(350L) // 350ms debounce
                                            performSearch(text)
                                        }
                                    },
                                    placeholder = { Text("Search location or area...", fontSize = 14.sp) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                        unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                        disabledContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                        focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                                        unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                                    )
                                )

                                if (isSearching) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .padding(end = 4.dp),
                                        strokeWidth = 2.dp
                                    )
                                }

                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = {
                                            searchQuery = ""
                                            searchResults = emptyList()
                                            showSuggestions = false
                                        }
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear search", modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }

                        // Auto-suggest suggestions overlay
                        AnimatedVisibility(
                            visible = showSuggestions && searchResults.isNotEmpty(),
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp)
                                    .heightIn(max = 240.dp),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 10.dp),
                                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(vertical = 4.dp)
                                ) {
                                    items(searchResults) { item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    val point = GeoPoint(item.lat, item.lon)
                                                    centerGeoPoint = point
                                                    fullAddress = item.displayName
                                                    osmMapViewState?.controller?.animateTo(point, 17.0, 800L)
                                                    showSuggestions = false
                                                    searchQuery = ""
                                                }
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = item.displayName,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        HorizontalDivider(
                                            modifier = Modifier.padding(horizontal = 16.dp),
                                            thickness = 0.5.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
