import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import locale.L
import locale.LanguagePreferences

/**
 * Language selection screen shown on first startup.
 * Allows users to select their preferred language for the application.
 */
@Composable
fun LanguageSelectionScreen(
    onLanguageSelected: (String) -> Unit
) {
    var selectedLanguage by remember { mutableStateOf("english") }
    val availableLanguages = LanguagePreferences.getAvailableLanguages()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        // Title
        Text(
            text = "Select Language / Seleccionar idioma / Choisir la langue",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "Choose your preferred language for BiblePro",
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // Language list
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            elevation = 4.dp
        ) {
            LazyColumn(
                modifier = Modifier.padding(8.dp)
            ) {
                items(availableLanguages.toList()) { (languageCode, displayName) ->
                    LanguageItem(
                        languageCode = languageCode,
                        displayName = displayName,
                        isSelected = selectedLanguage == languageCode,
                        onSelected = { selectedLanguage = it }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Continue button
        Button(
            onClick = {
                LanguagePreferences.setPreferredLanguage(selectedLanguage)
                L.current.language = selectedLanguage
                onLanguageSelected(selectedLanguage)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.primary
            )
        ) {
            Text(
                text = "Continue",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun LanguageItem(
    languageCode: String,
    displayName: String,
    isSelected: Boolean,
    onSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelected(languageCode) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = displayName,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
        
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = MaterialTheme.colors.primary
            )
        }
    }
}