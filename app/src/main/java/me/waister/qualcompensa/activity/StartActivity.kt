package me.waister.qualcompensa.activity

import android.annotation.SuppressLint
import android.content.IntentSender
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.orhanobut.hawk.Hawk
import me.waister.qualcompensa.R
import me.waister.qualcompensa.application.CustomApplication
import me.waister.qualcompensa.databinding.ActivityStartBinding
import me.waister.qualcompensa.utils.PREF_DEVICE_ID
import me.waister.qualcompensa.utils.appLog
import me.waister.qualcompensa.utils.hide
import me.waister.qualcompensa.utils.isDebug
import org.jetbrains.anko.intentFor

class StartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartBinding

    private var appUpdateManager: AppUpdateManager? = null
    private var updateFlowResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            appLog("UPDATE", "Launcher result code: ${result.resultCode} |  ${result.resultCode == RESULT_OK}")

            if (result.resultCode == RESULT_OK) {
                initApp()
            } else {
                binding.loading.hide()

                AlertDialog.Builder(this)
                    .setTitle(R.string.error_on_update)
                    .setMessage(R.string.error_on_update_message)
                    .setCancelable(false)
                    .setPositiveButton(R.string.try_again) { dialog, _ ->
                        checkAppVersion()

                        dialog.dismiss()
                    }
                    .setNegativeButton(R.string.update_later) { dialog, _ ->
                        initApp()

                        dialog.dismiss()
                    }
                    .create()
                    .show()
            }
        }

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Hawk.get(PREF_DEVICE_ID, "").isEmpty()) {
            Hawk.put(PREF_DEVICE_ID, Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID))
            CustomApplication().updateFuelParams()
        }

        if ((application as CustomApplication).isCheckUpdatesNeeded) {
            (application as CustomApplication).isCheckUpdatesNeeded = false

            checkAppVersion()
        } else {
            initApp()
        }
    }

    override fun onResume() {
        super.onResume()

        appUpdateManager
            ?.appUpdateInfo
            ?.addOnFailureListener {
                it.printStackTrace()
            }
            ?.addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    startUpdate(appUpdateInfo)
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()

        updateFlowResultLauncher.unregister()
    }

    private fun checkAppVersion() {
        binding.loading.visibility = View.VISIBLE

        appUpdateManager = AppUpdateManagerFactory.create(this)

        appUpdateManager
            ?.appUpdateInfo
            ?.addOnFailureListener {
                initApp()

                appLog("UPDATE", "Error - addOnFailureListener: ${it.message}")

                it.printStackTrace()
            }
            ?.addOnSuccessListener { appUpdateInfo ->
                val updateAvailable =
                    appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                val isImmediate = appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)

                appLog("UPDATE", "Success | updateAvailable: $updateAvailable | isImmediate: $isImmediate")

                if (updateAvailable && isImmediate) {
                    startUpdate(appUpdateInfo)
                } else {
                    initApp()
                }
            }
    }

    private fun startUpdate(appUpdateInfo: AppUpdateInfo) {
        appLog("UPDATE", "startUpdate() called")

        try {
            appUpdateManager?.startUpdateFlowForResult(
                appUpdateInfo,
                updateFlowResultLauncher,
                AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
            )
        } catch (e: IntentSender.SendIntentException) {
            if (isDebug()) e.printStackTrace()
        }
    }

    private fun initApp() {
        startActivity(intentFor<MainActivity>())
        finish()
    }

}
