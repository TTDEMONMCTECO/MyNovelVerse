package com.example.novelverse.presentation.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.novelverse.core.datastore.ReaderPreferences
import com.example.novelverse.domain.model.Chapter
import com.example.ui.theme.ReaderColors

data class ReaderThemePreset(
    val name: String,
    val backgroundColor: Color,
    val textColor: Color
)

val ReaderThemePresets = listOf(
    ReaderThemePreset("White", ReaderColors.WhiteBg, ReaderColors.WhiteText),
    ReaderThemePreset("Sepia", ReaderColors.SepiaBg, ReaderColors.SepiaText),
    ReaderThemePreset("Cream", ReaderColors.CreamBg, ReaderColors.CreamText),
    ReaderThemePreset("Night", ReaderColors.NightBg, ReaderColors.NightText),
    ReaderThemePreset("Amoled", ReaderColors.AmoledBg, ReaderColors.AmoledText),
    ReaderThemePreset("Forest", ReaderColors.ForestBg, ReaderColors.ForestText)
)

fun getFontFamilyByName(name: String): FontFamily {
    return when (name.lowercase()) {
        "serif" -> FontFamily.Serif
        "sans-serif", "sans_serif" -> FontFamily.SansSerif
        "monospace" -> FontFamily.Monospace
        "cursive" -> FontFamily.Cursive
        else -> FontFamily.Serif
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderSettingsSheet(
    preferences: ReaderPreferences,
    onPreferencesChange: (ReaderPreferences) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Reader Customization",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            // Theme Preset Selector
            Text(text = "Reading Theme", style = MaterialTheme.typography.labelMedium)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(ReaderThemePresets) { preset ->
                    val isSelected = preferences.readerTheme.equals(preset.name, ignoreCase = true)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable {
                                onPreferencesChange(preferences.copy(readerTheme = preset.name))
                            }
                            .testTag("theme_preset_${preset.name}")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(preset.backgroundColor)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Aa",
                                color = preset.textColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = preset.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalDivider()

            // Font Size Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Font Size (${preferences.fontSizeSp}sp)", style = MaterialTheme.typography.bodyMedium)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalIconButton(
                        onClick = {
                            if (preferences.fontSizeSp > 12) {
                                onPreferencesChange(preferences.copy(fontSizeSp = preferences.fontSizeSp - 1))
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease Font Size")
                    }

                    Text(
                        text = "${preferences.fontSizeSp}",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )

                    FilledTonalIconButton(
                        onClick = {
                            if (preferences.fontSizeSp < 32) {
                                onPreferencesChange(preferences.copy(fontSizeSp = preferences.fontSizeSp + 1))
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase Font Size")
                    }
                }
            }

            // Font Family Choice Chips
            Text(text = "Font Family", style = MaterialTheme.typography.labelMedium)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("Serif", "Sans-Serif", "Monospace", "Cursive").forEach { fontName ->
                    val isSelected = preferences.fontFamily.equals(fontName, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onPreferencesChange(preferences.copy(fontFamily = fontName)) },
                        label = { Text(fontName) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            HorizontalDivider()

            // Line Spacing
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Line Height", style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(1.3f to "Compact", 1.6f to "Normal", 2.0f to "Relaxed").forEach { (spacing, label) ->
                        val isSelected = preferences.lineSpacingMultiplier == spacing
                        FilterChip(
                            selected = isSelected,
                            onClick = { onPreferencesChange(preferences.copy(lineSpacingMultiplier = spacing)) },
                            label = { Text(label) }
                        )
                    }
                }
            }

            // Brightness Overlay Slider
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "In-App Brightness", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "${preferences.brightnessPercent}%", style = MaterialTheme.typography.bodySmall)
                }
                Slider(
                    value = preferences.brightnessPercent.toFloat(),
                    onValueChange = { onPreferencesChange(preferences.copy(brightnessPercent = it.toInt())) },
                    valueRange = 20f..100f
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterListSheet(
    chapters: List<Chapter>,
    currentChapterId: String?,
    onChapterSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = chapters.filter {
        it.title.contains(searchQuery, ignoreCase = true) || it.index.toString() == searchQuery
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Table of Contents (${chapters.size} Chapters)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Filter chapters...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                items(filtered, key = { it.id }) { chapter ->
                    val isCurrent = chapter.id == currentChapterId
                    ListItem(
                        headlineContent = {
                            Text(
                                text = chapter.title,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        },
                        trailingContent = {
                            if (isCurrent) {
                                Icon(Icons.Default.MenuBook, contentDescription = "Reading", tint = MaterialTheme.colorScheme.primary)
                            } else if (chapter.isDownloaded) {
                                Icon(Icons.Default.Check, contentDescription = "Downloaded", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onChapterSelect(chapter.id)
                                onDismiss()
                            }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                }
            }
        }
    }
}

@Composable
fun AddBookmarkDialog(
    excerpt: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.BookmarkAdd, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Add Bookmark")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "\"$excerpt\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Personal Note (Optional)") },
                    placeholder = { Text("e.g. Great plot twist here!") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(note) }) {
                Text("Save Bookmark")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
