package me.waister.qualcompensa.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.orhanobut.hawk.Hawk
import me.waister.qualcompensa.application.CustomApplication
import me.waister.qualcompensa.databinding.ActivityStartBinding
import me.waister.qualcompensa.utils.PREF_DEVICE_ID
import me.waister.qualcompensa.utils.PREF_DEVICE_ID_OLD
import me.waister.qualcompensa.utils.appLog
import me.waister.qualcompensa.utils.isNotNumeric
import java.util.Calendar
import kotlin.random.Random

class StartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        createDeviceID()

        initApp()
    }

    private fun createDeviceID() {
        val currentDeviceID = Hawk.get(PREF_DEVICE_ID, "")
        val isNotNumeric = currentDeviceID.isNotNumeric()

        if (currentDeviceID.isEmpty() || isNotNumeric) {
            if (isNotNumeric) Hawk.put(PREF_DEVICE_ID_OLD, currentDeviceID)

            val milliseconds = Calendar.getInstance().timeInMillis.toString()
            val random = Random.nextInt(10000, 99999)
            var stringID = "$milliseconds$random"

            if (stringID.length > 18) {
                stringID = stringID.substring(0, 18)
            } else if (stringID.length < 18) {
                stringID = stringID.padEnd(18, '9')
            }

            Hawk.put(PREF_DEVICE_ID, stringID)
            CustomApplication().updateFuelParams()

            appLog("GENERATE_DEVICE_ID", "New device ID: $stringID")
        } else {
            appLog("GENERATE_DEVICE_ID", "Ignored, current ID: $currentDeviceID")
        }
    }

    private fun initApp() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

}
