package com.example.customerangkot.utils

import android.content.Context
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.customerangkot.R
import com.example.customerangkot.domain.entity.TrayekItem
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.NumberFormat
import java.util.Locale

object Utils {

    fun showConfirmationDialog(context : Context, navigateToNextFragment: () -> Unit,
                               textTitle : String, textMessage : String, textPositive : String,
                               textNegative : String, ) {
        MaterialAlertDialogBuilder(context, R.style.CustomAlertDialog).apply {
            setTitle(textTitle)
            setMessage(textMessage)
            setPositiveButton(textPositive) { _, _ ->
                navigateToNextFragment()
            }
            setNegativeButton(textNegative) { dialog, _ ->
                dialog.dismiss()
            }
        }.show()
    }


    fun extractAmountFromRadioButton(radioText: String): String {
        // Contoh: "Rp. 200.000" -> "200000"
        return radioText
            .replace("Rp.", "")
            .replace(".", "")
            .trim()
            .toInt()
            .toString() // Konversi ke string tanpa format

        // Atau jika ingin tetap mempertahankan format:
        // return radioText.replace("Rp.", "").trim()
    }

    fun getTrayekList(context: Context): List<TrayekItem> {
        val trayekArray = context.resources.getStringArray(R.array.trayeks)
        return trayekArray.mapNotNull { trayekString ->
            val parts = trayekString.split(":", limit = 2)
            if (parts.size == 2) {
                try {
                    TrayekItem(
                        trayekId = parts[0].toInt(),
                        name = parts[1].trim(),
                        description = null, // Baris 8: Inisialisasi description
                        imageUrl = null,    // Baris 9: Inisialisasi imageUrl
                        angkotIds = emptyList(),
                        longitudes = emptyList(),
                        latitudes = emptyList(),
                    )
                } catch (e: NumberFormatException) {
                    null
                }
            } else {
                null
            }
        }
    }

    fun String.capitalizeWords(): String =
        split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }

    fun formatNumber(number: Int): String {
        val formatter = NumberFormat.getNumberInstance(Locale("in", "ID"))
        return "Rp. ${formatter.format(number)}"
    }


}