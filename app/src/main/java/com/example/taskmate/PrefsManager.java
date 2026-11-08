
package com.example.taskmate;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefsManager {

    private static final String PREF_NAME = "TaskMatePreferences";
    private static final String KEY_TEMA = "tema";

    private final SharedPreferences preferences;

    public PrefsManager(Context context) {
        preferences = context.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
        );
    }

    // Mengambil pilihan tema
    public String getTema() {
        return preferences.getString(KEY_TEMA, "terang");
    }

    // Menyimpan pilihan tema
    public void simpanTema(String tema) {
        preferences.edit()
                .putString(KEY_TEMA, tema)
                .apply();
    }

    // Memeriksa apakah tema gelap aktif
    public boolean isTemaGelap() {
        return getTema().equals("gelap");
    }
}
