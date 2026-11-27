package me.spica27.spicamusic.automix

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

private val AUTOMIX_ZIP_MIME_TYPES =
    arrayOf("application/zip", "application/x-zip-compressed", "application/octet-stream")

/**
 * Returns a function that opens the system file picker for an AutoMix result zip.
 * [onResult] gets the chosen file, or null if the picker was cancelled.
 */
@Composable
fun rememberAutoMixImportPicker(onResult: (Uri?) -> Unit): () -> Unit {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument(), onResult)
    return { launcher.launch(AUTOMIX_ZIP_MIME_TYPES) }
}
