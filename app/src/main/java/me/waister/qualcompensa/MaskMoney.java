package me.waister.qualcompensa;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.text.NumberFormat;

public class MaskMoney implements TextWatcher{

    final EditText mEditText;
    private boolean isUpdating = false;

    // Get the system money format. Example: Brazil (R$), USA ($)...
    private NumberFormat mNumberFormat = NumberFormat.getCurrencyInstance();

    public MaskMoney(EditText editText) {
        super();
        this.mEditText = editText;
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int start, int before, int after) {
        // Prevent infinity loop
        if (isUpdating) {
            isUpdating = false;
            return;
        }

        isUpdating = true;

        // Value from EditText
        String value = charSequence.toString();

        // Only numbers here
        value = value.replaceAll("\\D+", "");

        try {
            // Format the value and put back on EditText
            value = mNumberFormat.format(Double.parseDouble(value) / 100);
            value = value.replace("$", "$ ");

            mEditText.setText(value);
            mEditText.setSelection(value.length());
        } catch (NumberFormatException ignored) {
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
    }

    @Override
    public void afterTextChanged(Editable editable) {
    }
}