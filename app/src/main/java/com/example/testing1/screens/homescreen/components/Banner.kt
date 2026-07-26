package com.example.testing1.screens.homescreen.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.testing1.R

@Composable
fun Banner(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.banner_1),
        contentDescription = "Banner",
        contentScale = ContentScale.Crop,
        modifier = modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(16.dp))
    )
}


@Preview(showBackground = true)
@Composable
fun BannerPreview() {
    Banner(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(16.dp))
    )
}