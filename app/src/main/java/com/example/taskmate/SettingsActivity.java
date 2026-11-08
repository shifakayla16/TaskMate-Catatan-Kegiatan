
package com.example.taskmate;

import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class SettingsActivity extends AppCompatActivity {

    private RadioGroup radioGroupTema;
    private RadioButton rbTemaTerang, rbTemaGelap;
    private Button btnSimpanTema;

    private PrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefsManager = new PrefsManager(this);

        radioGroupTema = findViewById(R.id.radioGroupTema);
        rbTemaTerang = findViewById(R.id.rbTemaTerang);
        rbTemaGelap = findViewById(R.id.rbTemaGelap);
        btnSimpanTema = findViewById(R.id.btnSimpanTema);

        // Menampilkan tema yang tersimpan
        if (prefsManager.isTemaGelap()) {
            rbTemaGelap.setChecked(true);
        } else {
            rbTemaTerang.setChecked(true);
        }

        btnSimpanTema.setOnClickListener(v -> simpanPengaturan());
    }

    private void simpanPengaturan() {

        int pilihan = radioGroupTema.getCheckedRadioButtonId();

        if (pilihan == -1) {
            Toast.makeText(
                    this,
                    "Silakan pilih tema terlebih dahulu",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String tema;

        if (pilihan == R.id.rbTemaGelap) {
            tema = "gelap";
        } else {
            tema = "terang";
        }

        // Menyimpan pilihan tema dengan SharedPreferences
        prefsManager.simpanTema(tema);

        // Mengaktifkan tema yang dipilih
        AppCompatDelegate.setDefaultNightMode(
                tema.equals("gelap")
                        ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO
        );

        Toast.makeText(
                this,
                "Pengaturan tema berhasil disimpan 🌷",
                Toast.LENGTH_SHORT
        ).show();

        finish();
    }
}
