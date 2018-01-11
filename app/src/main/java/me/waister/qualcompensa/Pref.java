package me.waister.qualcompensa;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class Pref {

    public static final String PRICE_FIRST = "PRICE_FIRST";
    public static final String SIZE_FIRST = "SIZE_FIRST";
    public static final String PRICE_SECOND = "PRICE_SECOND";
    public static final String SIZE_SECOND = "SIZE_SECOND";
    public static final String FIRST_ACCESS = "FIRST_ACCESS";

    public static SharedPreferences getPreferences(Activity activity) {
        return activity.getSharedPreferences(Pref.class.getName(), Context.MODE_PRIVATE);
    }

}
