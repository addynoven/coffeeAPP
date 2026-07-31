package com.example.testing1.screens.ui_components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.testing1.ui.theme.Testing1Theme
import com.example.testing1.util.LottieAnimations

@Composable
fun AnimatedFavoriteButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Int = 24
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(LottieAnimations.HEART_FAV))
    
    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = isFavorite,
        restartOnPlay = false
    )

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!isFavorite || composition == null) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (isFavorite) Color.Red else androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size((size * 0.6).dp)
            )
        } else {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .scale(3.5f)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun AnimatedFavoriteButtonPreview() {
    Testing1Theme {
        Box(modifier = Modifier.size(100.dp), contentAlignment = Alignment.Center) {
            AnimatedFavoriteButton(
                isFavorite = true,
                onClick = {},
                size = 48
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun AnimatedFavoriteButtonNotFavoritePreview() {
    Testing1Theme {
        Box(modifier = Modifier.size(100.dp), contentAlignment = Alignment.Center) {
            AnimatedFavoriteButton(
                isFavorite = false,
                onClick = {}
            )
        }
    }
}
