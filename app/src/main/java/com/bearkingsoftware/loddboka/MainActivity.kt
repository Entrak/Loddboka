package com.bearkingsoftware.loddboka

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.core.os.LocaleListCompat
import com.bearkingsoftware.loddboka.ui.theme.BrownText
import com.bearkingsoftware.loddboka.ui.theme.LoddbokaTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Locale

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val localeManager = LocaleManager(this)

        runBlocking {
            val locale = localeManager.localeFlow.first()
            val appLocale = LocaleListCompat.forLanguageTags(locale.toLanguageTag())
            AppCompatDelegate.setApplicationLocales(appLocale)
        }
        
        setContent {
            val currentLocale by localeManager.localeFlow.collectAsState(initial = Locale.getDefault())

            LoddbokaTheme(darkTheme = isSystemInDarkTheme(), dynamicColor = false) {
                MainScreen(locale = currentLocale)
            }
        }
    }
}

@Composable
fun SplashScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.bearking),
            contentDescription = "Bear King Software Logo",
            modifier = Modifier.fillMaxWidth(0.4f)
        )
        Text(text = "Bear King", style = MaterialTheme.typography.headlineMedium, color = BrownText, fontWeight = FontWeight.Bold)
        Text(text = "Software", style = MaterialTheme.typography.headlineMedium, color = BrownText, fontWeight = FontWeight.Bold)
    }
}