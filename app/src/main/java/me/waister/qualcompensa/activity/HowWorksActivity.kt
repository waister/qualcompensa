package me.waister.qualcompensa.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.orhanobut.hawk.Hawk
import kotlinx.android.synthetic.main.activity_how_works.*
import me.waister.qualcompensa.R
import me.waister.qualcompensa.utils.PREF_FIRST_ACCESS

class HowWorksActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_how_works)

        button_submit.setOnClickListener {
            Hawk.put(PREF_FIRST_ACCESS, false)

            onBackPressed()
        }
    }

}
