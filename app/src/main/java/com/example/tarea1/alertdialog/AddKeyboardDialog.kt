package com.example.tarea1.alertdialog

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.EditText
import com.example.tarea1.R

// Helper para abrir el diálogo de alta de teclados desde ListFragment.
// Lo dejo como object porque no necesito estado ni instancias.
object AddKeyboardDialog {

    // Muestra el diálogo, valida campos y devuelve los datos por callback.
    fun show(
        context: Context,
        inflater: LayoutInflater,
        onAddKeyboard: (title: String, description: String, fav: Boolean) -> Unit
    ) {
        val dialogView = inflater.inflate(R.layout.dialog_add_keyboard, null)

        val etTitle = dialogView.findViewById<EditText>(R.id.etDialogTitle)
        val etDescription = dialogView.findViewById<EditText>(R.id.etDialogDescription)
        val cbFav = dialogView.findViewById<CheckBox>(R.id.cbDialogFav)

        val dialog = AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.dialog_add_title))
            .setView(dialogView)
            .setPositiveButton(context.getString(R.string.dialog_add_button), null)
            .setNegativeButton(context.getString(R.string.dialog_cancel_button), null)
            .create()

        dialog.setOnShowListener {
            // Sobrescribo el botón positivo para evitar que se cierre si hay errores.
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val title = etTitle.text.toString().trim()
                val description = etDescription.text.toString().trim()
                val fav = cbFav.isChecked

                if (title.isBlank()) {
                    etTitle.error = context.getString(R.string.dialog_error_title_required)
                    return@setOnClickListener
                }

                if (description.isBlank()) {
                    etDescription.error = context.getString(R.string.dialog_error_description_required)
                    return@setOnClickListener
                }

                // Si t odo está bien, devuelvo datos al caller y cierro diálogo.
                onAddKeyboard(title, description, fav)
                dialog.dismiss()
            }
        }

        dialog.show()
    }
}
