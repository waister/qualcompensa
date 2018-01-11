package me.waister.qualcompensa;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.View;

public class HowWorksActivity extends AppCompatActivity {

    private Activity mActivity = HowWorksActivity.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_how_works);

        AppCompatButton buttonSubmit = (AppCompatButton) findViewById(R.id.button_submit);

        if (buttonSubmit != null) {
            buttonSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedPreferences mPreferences = Pref.getPreferences(mActivity);
                    SharedPreferences.Editor editor = mPreferences.edit();
                    editor.putBoolean(Pref.FIRST_ACCESS, false);
                    editor.apply();

                    onBackPressed();
                }
            });
        }
    }
}
