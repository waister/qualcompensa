package me.waister.qualcompensa.utils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

import java.text.NumberFormat

class MaskMoney(private val mEditText: EditText) : TextWatcher {
    private var isUpdating = false

    private val mNumberFormat = NumberFormat.getCurrencyInstance()

    override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, after: Int) {
        if (isUpdating) {
            isUpdating = false
            return
        }

        isUpdating = true

        var value = charSequence.toString()

        value = value.replace("\\D+".toRegex(), "")

        try {
            value = mNumberFormat.format(java.lang.Double.parseDouble(value) / 100)
            value = value.replace("$", "$ ")

            mEditText.setText(value)
            mEditText.setSelection(value.length)
        } catch (ignored: NumberFormatException) {
        }

    }

    override fun beforeTextChanged(charSequence: CharSequence, start: Int, count: Int, after: Int) {}

    override fun afterTextChanged(editable: Editable) {}
}