package me.waister.qualcompensa.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.orhanobut.hawk.Hawk
import kotlinx.android.synthetic.main.activity_splash.*
import me.waister.qualcompensa.R
import me.waister.qualcompensa.application.CustomApplication
import me.waister.qualcompensa.utils.PREF_DEVICE_ID
import org.jetbrains.anko.intentFor

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    companion object {
        const val MY_REQUEST_CODE = 1
    }

    private var appUpdateManager: AppUpdateManager? = null

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        if (Hawk.get(PREF_DEVICE_ID, "").isEmpty()) {
            Hawk.put(PREF_DEVICE_ID, Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID))
            CustomApplication().updateFuelParams()
        }

        checkAppVersion()
    }

    override fun onResume() {
        super.onResume()

        appUpdateManager
                ?.appUpdateInfo
                ?.addOnSuccessListener { appUpdateInfo ->
                    if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                        appUpdateManager?.startUpdateFlowForResult(
                                appUpdateInfo,
                                AppUpdateType.IMMEDIATE,
                                this,
                                MY_REQUEST_CODE
                        )
                    }
                }
    }

    private fun checkAppVersion() {
        ll_loading.visibility = View.VISIBLE

        appUpdateManager = AppUpdateManagerFactory.create(this)

        val appUpdateInfoTask = appUpdateManager?.appUpdateInfo

        appUpdateInfoTask
                ?.addOnFailureListener {
                    initApp()
                }
                ?.addOnSuccessListener { appUpdateInfo ->
                    val updateAvailable = appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    val isImmediate = appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)

                    if (updateAvailable && isImmediate) {

                        appUpdateManager?.startUpdateFlowForResult(
                                appUpdateInfo,
                                AppUpdateType.IMMEDIATE,
                                this,
                                MY_REQUEST_CODE)
                    } else {

                        initApp()

                    }
                }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == MY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                initApp()
            } else {
                ll_loading.visibility = View.GONE

                val dialog = AlertDialog.Builder(this)
                dialog.setCancelable(false)
                dialog.setTitle(R.string.error_on_update)
                dialog.setMessage(R.string.error_on_update_message)
                dialog.setPositiveButton(R.string.try_again) { _, _ ->
                    checkAppVersion()
                }
                dialog.setNegativeButton(R.string.exit_app) { _, _ ->
                    finish()
                }
                dialog.create()
                dialog.show()
            }
        }
    }

    private fun initApp() {
        startActivity(intentFor<MainActivity>())
        finish()
    }

}
