package com.bearkingsoftware.loddboka.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bearkingsoftware.loddboka.SplashScreen
import com.bearkingsoftware.loddboka.ui.theme.LoddbokaTheme

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    LoddbokaTheme {
        SplashScreen()
    }
}
