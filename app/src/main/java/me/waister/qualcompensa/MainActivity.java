package me.waister.qualcompensa;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences mPreferences;
    private Activity mActivity = MainActivity.this;

    private EditText fieldPriceFirst;
    private EditText fieldSizeFirst;
    private EditText fieldPriceSecond;
    private EditText fieldSizeSecond;

    private TextView textResultFirst;
    private TextView textResultSecond;
    private TextView textResultPercentage;

    private CardView cardResult;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setElevation(8);
        }

        mPreferences = Pref.getPreferences(mActivity);

        AdView mAdView = findViewById(R.id.adView);

        fieldPriceFirst = findViewById(R.id.field_price_first);
        fieldSizeFirst = findViewById(R.id.field_size_first);
        fieldPriceSecond = findViewById(R.id.field_price_second);
        fieldSizeSecond = findViewById(R.id.field_size_second);
        AppCompatButton buttonSubmit = findViewById(R.id.button_submit);
        cardResult = findViewById(R.id.card_result);
        scrollView = findViewById(R.id.scroll_view);

        textResultFirst = findViewById(R.id.text_result_first);
        textResultSecond = findViewById(R.id.text_result_second);
        textResultPercentage = findViewById(R.id.text_result_percentage);

        if (mAdView != null) {
            AdRequest.Builder adBuilder = new AdRequest.Builder();
            adBuilder.addTestDevice("C2AC4FB310839CF4EE6D0BCDD82DBE21");
            adBuilder.addTestDevice("EBC2B36195F5D447E9499D20DC391FC0");
            mAdView.loadAd(adBuilder.build());

            fieldPriceFirst.addTextChangedListener(new MaskMoney(fieldPriceFirst));
            fieldPriceSecond.addTextChangedListener(new MaskMoney(fieldPriceSecond));

            fieldPriceFirst.addTextChangedListener(textChangedListener());
            fieldSizeFirst.addTextChangedListener(textChangedListener());
            fieldPriceSecond.addTextChangedListener(textChangedListener());
            fieldSizeSecond.addTextChangedListener(textChangedListener());

            buttonSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    actionSubmit(true);
                }
            });

            fieldSizeSecond.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEND) {
                        actionSubmit(true);
                        hideKeyboard();
                        return true;
                    }

                    return false;
                }
            });
        }

        alertFirstAccess();
    }

    private void alertFirstAccess() {
        boolean firstAccess = mPreferences.getBoolean(Pref.FIRST_ACCESS, true);

        if (firstAccess) {
            Intent intent = new Intent(mActivity, HowWorksActivity.class);
            mActivity.startActivity(intent);
        }
    }

    private TextWatcher textChangedListener() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                cardResult.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        };
    }

    private void actionSubmit(boolean showToast) {
        double priceFirst = getPrice(fieldPriceFirst);
        double sizeFirst = getNumber(fieldSizeFirst);
        double priceSecond = getPrice(fieldPriceSecond);
        double sizeSecond = getNumber(fieldSizeSecond);

        if (priceFirst > 0 && sizeFirst > 0) {
            double realFirst = priceFirst / sizeFirst;

            String resultFirst = getString(R.string.result_first, formatPrice(realFirst));

            textResultFirst.setText(fromHtml(resultFirst));

            if (priceSecond > 0 && sizeSecond> 0) {
                double realSecond = priceSecond / sizeSecond;

                String resultSecond = getString(R.string.result_second, formatPrice(realSecond));
                textResultSecond.setText(fromHtml(resultSecond));

                boolean firstBiggest = realFirst > realSecond;
                double larger = firstBiggest ? realFirst : realSecond;
                double less = firstBiggest ? realSecond : realFirst;

                if (larger == less) {
                    textResultPercentage.setText(R.string.result_equals);
                } else {
                    double percentage = ((larger - less) / larger);
                    String formatted = formatPercent(percentage);

                    String word = firstBiggest ? getString(R.string.second) : getString(R.string.first);
                    String result = getString(R.string.result_percentage, word, formatted);
                    textResultPercentage.setText(fromHtml(result));
                }

                textResultSecond.setVisibility(View.VISIBLE);
                textResultPercentage.setVisibility(View.VISIBLE);
            } else {
                textResultSecond.setVisibility(View.GONE);
                textResultPercentage.setVisibility(View.GONE);
            }

            cardResult.setVisibility(View.VISIBLE);

            scrollBottom();
        } else {
            if (showToast) {
                int message = R.string.error_empty_price;

                if (priceFirst > 0) {
                    message = R.string.error_empty_size;
                }

                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private double getNumber(EditText field) {
        if (field == null) return 0;
        String value = field.getText().toString();
        if (value.isEmpty()) return 0;
        return Double.parseDouble(value);
    }

    private double getPrice(EditText field) {
        if (field == null) return 0;
        String value = field.getText().toString();
        if (value.isEmpty()) return 0;
        String result = value.replaceAll("[R$. ]", "").replaceAll("[,]", ".");
        if (result.isEmpty()) return 0;
        return Double.parseDouble(result);
    }

    private String getValue(EditText field) {
        if (field == null) return "";
        return field.getText().toString();
    }

    private String formatPrice(double value) {
        DecimalFormatSymbols locale = new DecimalFormatSymbols(new Locale("pt", "BR"));
        DecimalFormat formatter = new DecimalFormat("##,###,###,##0.00", locale);
        formatter.setMinimumFractionDigits(3);
        formatter.setParseBigDecimal (true);
        return formatter.format(value);
    }

    private String formatPercent(double value) {
        NumberFormat percentFormat = NumberFormat.getPercentInstance();
        percentFormat.setMaximumFractionDigits(2);
        return percentFormat.format(value);
    }

    @SuppressWarnings("deprecation")
    private Spanned fromHtml(String value) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return Html.fromHtml(value, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(value);
        }
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        if (view != null && inputManager != null) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void scrollBottom() {
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (getValue(fieldPriceFirst).isEmpty()) {
            fieldPriceFirst.setText(mPreferences.getString(Pref.PRICE_FIRST, ""));
        }
        if (getValue(fieldSizeFirst).isEmpty()) {
            fieldSizeFirst.setText(mPreferences.getString(Pref.SIZE_FIRST, ""));
        }
        if (getValue(fieldPriceSecond).isEmpty()) {
            fieldPriceSecond.setText(mPreferences.getString(Pref.PRICE_SECOND, ""));
        }
        if (getValue(fieldSizeSecond).isEmpty()) {
            fieldSizeSecond.setText(mPreferences.getString(Pref.SIZE_SECOND, ""));
        }

        actionSubmit(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        String priceFirst = getValue(fieldPriceFirst);
        String sizeFirst = getValue(fieldSizeFirst);
        String priceSecond = getValue(fieldPriceSecond);
        String sizeSecond = getValue(fieldSizeSecond);

        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(Pref.PRICE_FIRST, priceFirst);
        editor.putString(Pref.SIZE_FIRST, sizeFirst);
        editor.putString(Pref.PRICE_SECOND, priceSecond);
        editor.putString(Pref.SIZE_SECOND, sizeSecond);
        editor.apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(mActivity);
        dialog.setTitle(R.string.confirmation);
        dialog.setMessage(R.string.confirmation_message);
        dialog.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                fieldPriceFirst.setText("");
                fieldSizeFirst.setText("");
                fieldPriceSecond.setText("");
                fieldSizeSecond.setText("");

                fieldPriceFirst.requestFocus();
            }
        });
        dialog.setNegativeButton(R.string.cancel, null);
        dialog.show();
        return true;
    }

}
