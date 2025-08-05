package me.waister.qualcompensa.activity

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.orhanobut.hawk.Hawk
import me.waister.qualcompensa.databinding.ActivityHowWorksBinding
import me.waister.qualcompensa.utils.PREF_FIRST_ACCESS

class HowWorksActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHowWorksBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)

        binding = ActivityHowWorksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonSubmit.setOnClickListener {
            Hawk.put(PREF_FIRST_ACCESS, false)

            onBackPressedDispatcher.onBackPressed()
        }

        setupCommonInsets()
    }

    private fun setupCommonInsets() = with(binding) {
        ViewCompat.setOnApplyWindowInsetsListener(layoutMain) { view, insets ->
            val bars =
                insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            view.apply {
                updatePadding(left = bars.left, top = bars.top, right = bars.right, bottom = bars.bottom)
            }
            insets
        }
    }

}
