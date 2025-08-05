package me.waister.qualcompensa.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
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
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.orhanobut.hawk.Hawk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.waister.qualcompensa.R
import me.waister.qualcompensa.databinding.ActivityMainBinding
import me.waister.qualcompensa.utils.API_ROUTE_IDENTIFY
import me.waister.qualcompensa.utils.API_SUCCESS
import me.waister.qualcompensa.utils.API_TOKEN
import me.waister.qualcompensa.utils.InAppUpdate
import me.waister.qualcompensa.utils.MaskMoney
import me.waister.qualcompensa.utils.PREF_FCM_TOKEN
import me.waister.qualcompensa.utils.PREF_FCM_TOKEN_SENT
import me.waister.qualcompensa.utils.PREF_FIRST_ACCESS
import me.waister.qualcompensa.utils.PREF_PRICE_FIRST
import me.waister.qualcompensa.utils.PREF_PRICE_SECOND
import me.waister.qualcompensa.utils.PREF_SIZE_FIRST
import me.waister.qualcompensa.utils.PREF_SIZE_SECOND
import me.waister.qualcompensa.utils.alert
import me.waister.qualcompensa.utils.getBooleanVal
import me.waister.qualcompensa.utils.getValidJSONObject
import me.waister.qualcompensa.utils.isDebug
import me.waister.qualcompensa.utils.loadAdBanner
import me.waister.qualcompensa.utils.negativeButton
import me.waister.qualcompensa.utils.positiveButton
import me.waister.qualcompensa.utils.printFuelLog
import me.waister.qualcompensa.utils.setupCommonInsets
import java.text.NumberFormat

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val actionBar = supportActionBar

        if (actionBar != null) {
            actionBar.elevation = 8f
        }

        setupViews()

        if (!isDebug())
            InAppUpdate(this)
    }

    private fun setupViews() = with(binding) {
        setupCommonInsets(appBarLayout, root)

        initAdMob()

        fieldPriceFirst.addTextChangedListener(MaskMoney(fieldPriceFirst))
        fieldPriceSecond.addTextChangedListener(MaskMoney(fieldPriceSecond))

        fieldPriceFirst.addTextChangedListener(textChangedListener())
        fieldSizeFirst.addTextChangedListener(textChangedListener())
        fieldPriceSecond.addTextChangedListener(textChangedListener())
        fieldSizeSecond.addTextChangedListener(textChangedListener())

        buttonSubmit.setOnClickListener {
            hideKeyboard()
            calculate(true)
        }

        fieldSizeSecond.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                buttonSubmit.performClick()
                return@OnEditorActionListener true
            }

            false
        })

        alertFirstAccess()
        checkTokenSent()
    }

    private fun initAdMob() {
        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(this@MainActivity) {}
            runOnUiThread {
                val deviceId = listOf(AdRequest.DEVICE_ID_EMULATOR)
                val configuration = RequestConfiguration.Builder().setTestDeviceIds(deviceId).build()
                MobileAds.setRequestConfiguration(configuration)

                loadAdBanner(
                    adViewContainer = binding.llBanner,
                    adUnitId = "ca-app-pub-6521704558504566/8188995605",
                    adSize = null,
                    collapsible = false,
                )
            }
        }
    }

    private fun alertFirstAccess() {
        if (Hawk.get(PREF_FIRST_ACCESS, true))
            startActivity(Intent(this, HowWorksActivity::class.java))
    }

    private fun textChangedListener(): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                binding.cardResult.visibility = View.GONE
            }

            override fun afterTextChanged(editable: Editable) {}
        }
    }

    private fun calculate(showToast: Boolean) {
        val priceFirst = getPrice(binding.fieldPriceFirst)
        val sizeFirst = getNumber(binding.fieldSizeFirst)
        val priceSecond = getPrice(binding.fieldPriceSecond)
        val sizeSecond = getNumber(binding.fieldSizeSecond)

        if (priceFirst > 0 && sizeFirst > 0) {
            val realFirst = priceFirst / sizeFirst

            val resultFirst = getString(R.string.result_first, realFirst.formatPrice())

            binding.textResultFirst.text = fromHtml(resultFirst)

            if (priceSecond > 0 && sizeSecond > 0) {
                val realSecond = priceSecond / sizeSecond

                val resultSecond = getString(R.string.result_second, realSecond.formatPrice())
                binding.textResultSecond.text = fromHtml(resultSecond)

                val firstBiggest = realFirst > realSecond
                val larger = if (firstBiggest) realFirst else realSecond
                val less = if (firstBiggest) realSecond else realFirst

                if (larger == less) {
                    binding.textResultPercentage.setText(R.string.result_equals)
                } else {
                    val percentage = (larger - less) / larger
                    val formatted = formatPercent(percentage)

                    val word =
                        if (firstBiggest) getString(R.string.second) else getString(R.string.first)
                    val result = getString(R.string.result_percentage, word, formatted)
                    binding.textResultPercentage.text = fromHtml(result)
                }

                binding.textResultSecond.visibility = View.VISIBLE
                binding.textResultPercentage.visibility = View.VISIBLE
            } else {
                binding.textResultSecond.visibility = View.GONE
                binding.textResultPercentage.visibility = View.GONE
            }

            binding.cardResult.visibility = View.VISIBLE

            scrollBottom()
        } else {
            if (showToast) {
                var message = R.string.error_empty_price

                if (priceFirst > 0) {
                    message = R.string.error_empty_size
                }

                Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
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

    private fun Double.formatPrice(): String = NumberFormat.getCurrencyInstance().format(this)

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
            inputManager.hideSoftInputFromWindow(
                view.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
    }

    private fun scrollBottom() = with(binding) {
        scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
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

        binding.apply {
            if (getValue(fieldPriceFirst).isEmpty())
                fieldPriceFirst.setText(Hawk.get(PREF_PRICE_FIRST, ""))

            if (getValue(fieldSizeFirst).isEmpty())
                fieldSizeFirst.setText(Hawk.get(PREF_SIZE_FIRST, ""))

            if (getValue(fieldPriceSecond).isEmpty())
                fieldPriceSecond.setText(Hawk.get(PREF_PRICE_SECOND, ""))

            if (getValue(fieldSizeSecond).isEmpty())
                fieldSizeSecond.setText(Hawk.get(PREF_SIZE_SECOND, ""))
        }

        calculate(false)
    }

    override fun onDestroy() {
        super.onDestroy()

        binding.apply {
            Hawk.put(PREF_PRICE_FIRST, getValue(fieldPriceFirst))
            Hawk.put(PREF_SIZE_FIRST, getValue(fieldSizeFirst))
            Hawk.put(PREF_PRICE_SECOND, getValue(fieldPriceSecond))
            Hawk.put(PREF_SIZE_SECOND, getValue(fieldSizeSecond))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    @SuppressLint("SetTextI18n")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        alert(R.string.confirmation_message, R.string.confirmation) {
            positiveButton(R.string.confirm) {
                binding.apply {
                    fieldPriceFirst.setText("")
                    fieldSizeFirst.setText("")
                    fieldPriceSecond.setText("")
                    fieldSizeSecond.setText("")

                    fieldPriceFirst.requestFocus()
                }
            }
            negativeButton(R.string.cancel) {}
        }.show()
        return true
    }

}
