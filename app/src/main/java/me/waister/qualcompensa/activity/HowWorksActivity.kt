package me.waister.qualcompensa.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.orhanobut.hawk.Hawk
import me.waister.qualcompensa.databinding.ActivityHowWorksBinding
import me.waister.qualcompensa.utils.PREF_FIRST_ACCESS

class HowWorksActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHowWorksBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHowWorksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonSubmit.setOnClickListener {
            Hawk.put(PREF_FIRST_ACCESS, false)

            onBackPressedDispatcher.onBackPressed()
        }
    }

}
