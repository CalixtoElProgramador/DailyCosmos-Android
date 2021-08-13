package com.listocalixto.dailycosmos.data.remote.translator

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.listocalixto.dailycosmos.R
import java.util.*

@Suppress("DEPRECATION")
class TranslatorDataSource {

    @SuppressLint("ShowToast")
    fun downloadEnglishToOwnerLanguageModel(
        context: Context,
        activity: Activity
    ): Translator? {

        val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val primaryLocale: Locale = context.resources.configuration.locales[0]
            primaryLocale.language
        } else {
            val primaryLocale: Locale = context.resources.configuration.locale
            primaryLocale.language
        }

        val translator: Translator?

        if (!TranslateLanguage.getAllLanguages().contains(locale)) {
            translator = null
        } else {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(locale)
                .build()
            translator = Translation.getClient(options)
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
                    } else {
                        Snackbar.make(
                            activity.findViewById(R.id.bottom_navigation),
                            "Translating...",
                            Snackbar.LENGTH_SHORT
                        ).show()
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
                    } else {
                        Snackbar.make(
                            activity.findViewById(R.id.bottom_navigation),
                            "An error has occurred with the download",
                            Snackbar.LENGTH_LONG
                        ).setAction("Retry") {
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
                    } else {
                        Snackbar.make(
                            activity.findViewById(R.id.bottom_navigation),
                            "Download cancelled",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
        }
        return translator
    }
}