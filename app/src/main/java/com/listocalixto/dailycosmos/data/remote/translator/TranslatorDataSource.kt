package com.listocalixto.dailycosmos.data.remote.translator

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.listocalixto.dailycosmos.R
import com.listocalixto.dailycosmos.presentation.translator.TranslatorDataStoreViewModel
import java.util.*

class TranslatorDataSource {

    @SuppressLint("ShowToast")
    @RequiresApi(Build.VERSION_CODES.N)
    fun downloadEnglishToOwnerLanguageModel(
        context: Context,
        activity: Activity
    ): Translator {
        val primaryLocale: Locale = context.resources.configuration.locales[0]
        val locale: String = primaryLocale.language
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(locale)
            .build()
        val translator = Translation.getClient(options)
        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()
        translator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                // Model downloaded successfully. Okay to start translating.
                // (Set a flag, unhide the translation UI, etc.)
                Log.d("Translator", "Translator was downloaded successfully ")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    Snackbar.make(
                        activity.findViewById(R.id.bottom_navigation),
                        "Translating...",
                        Snackbar.LENGTH_SHORT
                    )
                        .setAnchorView(activity.requireViewById(R.id.bottom_navigation))
                        .show()
                }
            }
            .addOnFailureListener { exception ->
                Log.d("Translator", "Was happen an error: $exception ")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    Snackbar.make(
                        activity.findViewById(R.id.bottom_navigation),
                        "An error has occurred with the download",
                        Snackbar.LENGTH_LONG
                    )
                        .setAnchorView(activity.requireViewById(R.id.bottom_navigation))
                        .setAction("Retry") {
                            downloadEnglishToOwnerLanguageModel(context, activity)
                        }
                        .show()
                }
            }
            .addOnCanceledListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    Snackbar.make(
                        activity.findViewById(R.id.bottom_navigation),
                        "Download cancelled",
                        Snackbar.LENGTH_LONG
                    )
                        .setAnchorView(activity.requireViewById(R.id.bottom_navigation))
                        .show()
                }
            }
        return translator
    }
}