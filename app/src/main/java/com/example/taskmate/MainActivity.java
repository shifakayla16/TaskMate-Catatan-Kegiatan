package com.example.taskmate;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class MainActivity extends AppCompatActivity {

    private Button btnAddTask, btnSettings;
    private Button btnFilterSemua, btnFilterBelum, btnFilterSelesai;
    private TextView txtEmpty, txtProgress;
    private LinearLayout layoutDaftarKegiatan;

    private DatabaseHelper databaseHelper;
    private PrefsManager prefsManager;

    private String filterAktif = "semua";

    private int warna(int resourceId) {
        return getResources().getColor(resourceId, getTheme());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        prefsManager = new PrefsManager(this);

        AppCompatDelegate.setDefaultNightMode(
                prefsManager.isTemaGelap()
                        ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO
        );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseHelper = new DatabaseHelper(this);

        btnAddTask = findViewById(R.id.btnAddTask);
        btnSettings = findViewById(R.id.btnSettings);

        txtEmpty = findViewById(R.id.txtEmpty);
        txtProgress = findViewById(R.id.txtProgress);

        layoutDaftarKegiatan = findViewById(
                R.id.layoutDaftarKegiatan
        );

        btnFilterSemua = findViewById(R.id.btnFilterSemua);
        btnFilterBelum = findViewById(R.id.btnFilterBelum);
        btnFilterSelesai = findViewById(R.id.btnFilterSelesai);

        pindahkanDataLama();

        btnAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    AddTaskActivity.class
            );
            startActivity(intent);
        });

        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    SettingsActivity.class
            );
            startActivity(intent);
        });

        btnFilterSemua.setOnClickListener(v -> {
            filterAktif = "semua";
            tampilkanKegiatan();
        });

        btnFilterBelum.setOnClickListener(v -> {
            filterAktif = "belum";
            tampilkanKegiatan();
        });

        btnFilterSelesai.setOnClickListener(v -> {
            filterAktif = "selesai";
            tampilkanKegiatan();
        });

        aturWarnaFilter();
        tampilkanKegiatan();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (databaseHelper != null) {
            tampilkanKegiatan();
        }
    }

    // Mengatur warna tombol filter
    private void aturWarnaFilter() {

        int warnaAktif = warna(R.color.warna_sage);
        int warnaTidakAktif = warna(R.color.warna_pink);
        int warnaTeks = warna(R.color.warna_teks);
        int putih = warna(R.color.white);

        btnFilterSemua.setBackgroundTintList(
                ColorStateList.valueOf(
                        filterAktif.equals("semua")
                                ? warnaAktif : warnaTidakAktif
                )
        );

        btnFilterBelum.setBackgroundTintList(
                ColorStateList.valueOf(
                        filterAktif.equals("belum")
                                ? warnaAktif : warnaTidakAktif
                )
        );

        btnFilterSelesai.setBackgroundTintList(
                ColorStateList.valueOf(
                        filterAktif.equals("selesai")
                                ? warnaAktif : warnaTidakAktif
                )
        );

        btnFilterSemua.setTextColor(
                filterAktif.equals("semua") ? putih : warnaTeks
        );

        btnFilterBelum.setTextColor(
                filterAktif.equals("belum") ? putih : warnaTeks
        );

        btnFilterSelesai.setTextColor(
                filterAktif.equals("selesai") ? putih : warnaTeks
        );
    }

    // Memindahkan data lama dari SharedPreferences
    private void pindahkanDataLama() {

        SharedPreferences prefs = getSharedPreferences(
                "TaskMateData",
                MODE_PRIVATE
        );

        if (prefs.getBoolean("sudah_dipindahkan", false)) {
            return;
        }

        String nama = prefs.getString("nama_kegiatan", "");
        String deskripsi = prefs.getString("deskripsi", "");
        String tanggal = prefs.getString("tanggal", "");
        boolean selesai = prefs.getBoolean("selesai", false);

        if (!nama.isEmpty()) {

            long id = databaseHelper.tambahKegiatan(
                    nama,
                    deskripsi,
                    tanggal
            );

            if (id != -1 && selesai) {
                databaseHelper.ubahStatus((int) id, true);
            }
        }

        prefs.edit()
                .putBoolean("sudah_dipindahkan", true)
                .apply();
    }

    // Menampilkan daftar kegiatan
    private void tampilkanKegiatan() {

        layoutDaftarKegiatan.removeAllViews();

        Cursor cursor = databaseHelper.ambilSemuaKegiatan();

        int jumlah = 0;
        int jumlahSelesai = 0;

        if (cursor != null) {

            while (cursor.moveToNext()) {

                int id = cursor.getInt(
                        cursor.getColumnIndexOrThrow(
                                DatabaseHelper.COL_ID
                        )
                );

                String nama = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                DatabaseHelper.COL_NAMA
                        )
                );

                String deskripsi = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                DatabaseHelper.COL_DESKRIPSI
                        )
                );

                String tanggal = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                DatabaseHelper.COL_TANGGAL
                        )
                );

                boolean selesai = cursor.getInt(
                        cursor.getColumnIndexOrThrow(
                                DatabaseHelper.COL_SELESAI
                        )
                ) == 1;

                jumlah++;

                if (selesai) {
                    jumlahSelesai++;
                }

                if (filterAktif.equals("belum") && selesai) {
                    continue;
                }

                if (filterAktif.equals("selesai") && !selesai) {
                    continue;
                }

                buatKartuKegiatan(
                        id,
                        nama,
                        deskripsi,
                        tanggal,
                        selesai
                );
            }

            cursor.close();
        }

        txtProgress.setText(
                jumlahSelesai + " dari " + jumlah +
                        " Kegiatan Selesai 🌱"
        );

        aturWarnaFilter();

        if (layoutDaftarKegiatan.getChildCount() == 0) {

            txtEmpty.setVisibility(View.VISIBLE);

            if (jumlah == 0) {
                txtEmpty.setText(
                        "🌷\nBelum ada kegiatan.\n" +
                                "Yuk, tambahkan kegiatan pertamamu!"
                );
            } else if (filterAktif.equals("belum")) {
                txtEmpty.setText(
                        "Semua kegiatan sudah selesai! 🌸"
                );
            } else {
                txtEmpty.setText(
                        "Belum ada kegiatan yang selesai."
                );
            }

        } else {
            txtEmpty.setVisibility(View.GONE);
        }
    }

    // Membuat kartu kegiatan
    private void buatKartuKegiatan(
            int id,
            String nama,
            String deskripsi,
            String tanggal,
            boolean selesai
    ) {

        int warnaKartu = warna(R.color.warna_kartu);
        int warnaTeks = warna(R.color.warna_teks);
        int warnaSekunder = warna(R.color.warna_teks_sekunder);
        int warnaTanggal = warna(R.color.warna_tanggal);
        int warnaSage = warna(R.color.warna_sage);
        int putih = warna(R.color.white);

        LinearLayout kartu = new LinearLayout(this);
        kartu.setOrientation(LinearLayout.VERTICAL);
        kartu.setPadding(22, 18, 22, 18);
        kartu.setBackgroundColor(warnaKartu);

        LinearLayout.LayoutParams kartuParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        kartuParams.setMargins(0, 0, 0, 16);
        kartu.setLayoutParams(kartuParams);

        TextView txtNama = new TextView(this);
        txtNama.setText("🌷 " + nama);
        txtNama.setTextColor(warnaTeks);
        txtNama.setTextSize(18);
        txtNama.setTypeface(null, Typeface.BOLD);

        TextView txtDeskripsi = new TextView(this);
        txtDeskripsi.setText(deskripsi);
        txtDeskripsi.setTextColor(warnaSekunder);
        txtDeskripsi.setTextSize(14);
        txtDeskripsi.setPadding(0, 10, 0, 5);

        TextView txtTanggal = new TextView(this);
        txtTanggal.setText("Tanggal: " + tanggal);
        txtTanggal.setTextColor(warnaTanggal);
        txtTanggal.setTextSize(13);

        CheckBox chkSelesai = new CheckBox(this);
        chkSelesai.setText("Tandai kegiatan selesai");
        chkSelesai.setTextColor(warnaTeks);

        chkSelesai.setButtonTintList(
                ColorStateList.valueOf(warnaSage)
        );

        chkSelesai.setChecked(selesai);

        if (selesai) {
            txtNama.setPaintFlags(
                    txtNama.getPaintFlags() |
                            Paint.STRIKE_THRU_TEXT_FLAG
            );

            chkSelesai.setText("Kegiatan sudah selesai ✓");
        }

        chkSelesai.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    databaseHelper.ubahStatus(id, isChecked);

                    tampilkanKegiatan();

                    Toast.makeText(
                            this,
                            isChecked
                                    ? "Kegiatan selesai! 🌷"
                                    : "Status kegiatan diperbarui",
                            Toast.LENGTH_SHORT
                    ).show();
                }
        );

        LinearLayout barisTombol = new LinearLayout(this);
        barisTombol.setOrientation(LinearLayout.HORIZONTAL);

        Button btnEdit = new Button(this);
        btnEdit.setText("Edit");
        btnEdit.setAllCaps(false);
        btnEdit.setTextColor(putih);
        btnEdit.setBackgroundTintList(
                ColorStateList.valueOf(warnaSage)
        );

        Button btnHapus = new Button(this);
        btnHapus.setText("Hapus");
        btnEdit.setAllCaps(false);
        btnHapus.setTextColor(putih);
        btnHapus.setBackgroundTintList(
                ColorStateList.valueOf(warna(R.color.warna_tanggal))
        );

        LinearLayout.LayoutParams tombolParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );

        tombolParams.setMargins(4, 0, 4, 0);

        barisTombol.addView(btnEdit, tombolParams);
        barisTombol.addView(btnHapus, tombolParams);

        btnEdit.setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    AddTaskActivity.class
            );

            intent.putExtra("id_kegiatan", id);
            intent.putExtra("nama_kegiatan", nama);
            intent.putExtra("deskripsi", deskripsi);
            intent.putExtra("tanggal", tanggal);

            startActivity(intent);
        });

        btnHapus.setOnClickListener(v -> {

            new AlertDialog.Builder(this)
                    .setTitle("Hapus Kegiatan 🌷")
                    .setMessage(
                            "Yakin ingin menghapus kegiatan \"" +
                                    nama + "\"?"
                    )
                    .setPositiveButton("Ya, Hapus", (dialog, which) -> {

                        batalkanPengingat(id);
                        databaseHelper.hapusKegiatan(id);

                        Toast.makeText(
                                this,
                                "Kegiatan berhasil dihapus",
                                Toast.LENGTH_SHORT
                        ).show();

                        tampilkanKegiatan();
                    })
                    .setNegativeButton("Batal", null)
                    .show();
        });

        kartu.addView(txtNama);
        kartu.addView(txtDeskripsi);
        kartu.addView(txtTanggal);
        kartu.addView(chkSelesai);
        kartu.addView(barisTombol);

        layoutDaftarKegiatan.addView(kartu);
    }

    // Membatalkan pengingat kegiatan yang dihapus
    private void batalkanPengingat(int id) {

        Intent intent = new Intent(
                this,
                ReminderReceiver.class
        );

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT |
                        PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager =
                (AlarmManager) getSystemService(ALARM_SERVICE);

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }

        pendingIntent.cancel();
    }
}
