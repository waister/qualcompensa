package me.waister.qualcompensa.activity

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.Spanned
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.kittinunf.fuel.httpGet
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.orhanobut.hawk.Hawk
import kotlinx.android.synthetic.main.activity_main.*
import me.waister.qualcompensa.BuildConfig
import me.waister.qualcompensa.R
import me.waister.qualcompensa.utils.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.intentFor
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var mInterstitialAd: InterstitialAd

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val actionBar = supportActionBar

        if (actionBar != null) {
            actionBar.elevation = 8f
        }

        initAdMob()

        field_price_first.addTextChangedListener(MaskMoney(field_price_first))
        field_price_second.addTextChangedListener(MaskMoney(field_price_second))

        field_price_first.addTextChangedListener(textChangedListener())
        field_size_first.addTextChangedListener(textChangedListener())
        field_price_second.addTextChangedListener(textChangedListener())
        field_size_second.addTextChangedListener(textChangedListener())

        button_submit.setOnClickListener {
            actionSubmit()
        }

        field_size_second.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                actionSubmit()
                return@OnEditorActionListener true
            }

            false
        })

        alertFirstAccess()
        checkTokenSent()
    }

    private fun initAdMob() {
        val adBuilder = AdRequest.Builder()
        adBuilder.addTestDevice("738582C779CD9CA43CC0361682874D45")
        adView.loadAd(adBuilder.build())

        MobileAds.initialize(this)
        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = if (BuildConfig.DEBUG)
            "ca-app-pub-3940256099942544/1033173712"
        else
            "ca-app-pub-6521704558504566/2457847036"
        mInterstitialAd.loadAd(AdRequest.Builder().build())
    }

    private fun alertFirstAccess() {
        if (Hawk.get(PREF_FIRST_ACCESS, true))
            startActivity(intentFor<HowWorksActivity>())
    }

    private fun textChangedListener(): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                card_result.visibility = View.GONE
            }

            override fun afterTextChanged(editable: Editable) {}
        }
    }

    private fun actionSubmit() {
        if (mInterstitialAd.isLoaded)
            mInterstitialAd.show()

        hideKeyboard()

        calculate(true)
    }

    private fun calculate(showToast: Boolean) {
        val priceFirst = getPrice(field_price_first)
        val sizeFirst = getNumber(field_size_first)
        val priceSecond = getPrice(field_price_second)
        val sizeSecond = getNumber(field_size_second)

        if (priceFirst > 0 && sizeFirst > 0) {
            val realFirst = priceFirst / sizeFirst

            val resultFirst = getString(R.string.result_first, formatPrice(realFirst))

            text_result_first.text = fromHtml(resultFirst)

            if (priceSecond > 0 && sizeSecond > 0) {
                val realSecond = priceSecond / sizeSecond

                val resultSecond = getString(R.string.result_second, formatPrice(realSecond))
                text_result_second.text = fromHtml(resultSecond)

                val firstBiggest = realFirst > realSecond
                val larger = if (firstBiggest) realFirst else realSecond
                val less = if (firstBiggest) realSecond else realFirst

                if (larger == less) {
                    text_result_percentage.setText(R.string.result_equals)
                } else {
                    val percentage = (larger - less) / larger
                    val formatted = formatPercent(percentage)

                    val word = if (firstBiggest) getString(R.string.second) else getString(R.string.first)
                    val result = getString(R.string.result_percentage, word, formatted)
                    text_result_percentage.text = fromHtml(result)
                }

                text_result_second.visibility = View.VISIBLE
                text_result_percentage.visibility = View.VISIBLE
            } else {
                text_result_second.visibility = View.GONE
                text_result_percentage.visibility = View.GONE
            }

            card_result.visibility = View.VISIBLE

            scrollBottom()
        } else {
            if (showToast) {
                var message = R.string.error_empty_price

                if (priceFirst > 0) {
                    message = R.string.error_empty_size
                }

                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getNumber(field: EditText?): Double {
        if (field == null) return 0.0
        val value = field.text.toString()
        return if (value.isEmpty()) 0.0 else java.lang.Double.parseDouble(value)
    }

    private fun getPrice(field: EditText?): Double {
        if (field == null)
            return 0.0

        val value = field.text.toString()
        val result = value.replace("\\D".toRegex(), "")

        return if (result.isEmpty()) 0.0 else result.toDouble()
    }

    private fun getValue(field: EditText?): String {
        return field?.text?.toString() ?: ""
    }

    private fun formatPrice(value: Double): String {
        val locale = DecimalFormatSymbols(Locale("pt", "BR"))
        val formatter = DecimalFormat("##,###,###,##0.00", locale)
        formatter.minimumFractionDigits = 3
        formatter.isParseBigDecimal = true
        return formatter.format(value)
    }

    private fun formatPercent(value: Double): String {
        val percentFormat = NumberFormat.getPercentInstance()
        percentFormat.maximumFractionDigits = 2
        return percentFormat.format(value)
    }

    private fun fromHtml(value: String): Spanned {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Html.fromHtml(value, Html.FROM_HTML_MODE_LEGACY)
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(value)
        }
    }

    private fun hideKeyboard() {
        val view = currentFocus
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if (view != null)
            inputManager.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    private fun scrollBottom() {
        scroll_view.post { scroll_view.fullScroll(ScrollView.FOCUS_DOWN) }
    }


    private fun checkTokenSent() {
        val token = Hawk.get(PREF_FCM_TOKEN, "")

        if (token.isNotEmpty() && !Hawk.put(PREF_FCM_TOKEN_SENT, false)) {
            val params = listOf(API_TOKEN to token)

            API_ROUTE_IDENTIFY.httpGet(params).responseString { request, response, result ->
                printFuelLog(request, response, result)

                val (data, error) = result

                if (error == null) {
                    val apiObj = data.getValidJSONObject()

                    Hawk.put(PREF_FCM_TOKEN_SENT, apiObj.getBooleanVal(API_SUCCESS))
                }
            }

        }
    }

    override fun onResume() {
        super.onResume()

        if (getValue(field_price_first).isEmpty())
            field_price_first.setText(Hawk.get(PREF_PRICE_FIRST, ""))

        if (getValue(field_size_first).isEmpty())
            field_size_first.setText(Hawk.get(PREF_SIZE_FIRST, ""))

        if (getValue(field_price_second).isEmpty())
            field_price_second.setText(Hawk.get(PREF_PRICE_SECOND, ""))

        if (getValue(field_size_second).isEmpty())
            field_size_second.setText(Hawk.get(PREF_SIZE_SECOND, ""))

        calculate(false)
    }

    override fun onDestroy() {
        super.onDestroy()

        Hawk.put(PREF_PRICE_FIRST, getValue(field_price_first))
        Hawk.put(PREF_SIZE_FIRST, getValue(field_size_first))
        Hawk.put(PREF_PRICE_SECOND, getValue(field_price_second))
        Hawk.put(PREF_SIZE_SECOND, getValue(field_size_second))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        alert(R.string.confirmation_message, R.string.confirmation) {
            positiveButton(R.string.confirm) {
                field_price_first.setText("")
                field_size_first.setText("")
                field_price_second.setText("")
                field_size_second.setText("")

                field_price_first.requestFocus()
            }
            negativeButton(R.string.cancel) {}
        }.show()
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()

        if (mInterstitialAd.isLoaded)
            mInterstitialAd.show()
    }

}
