
package com.example.taskmate;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddTaskActivity extends AppCompatActivity {

    private EditText edtNamaKegiatan, edtDeskripsi;
    private Button btnTanggal, btnSimpan;

    private DatabaseHelper databaseHelper;

    private int idKegiatan = -1;
    private String tanggalDipilih = "";

    private static final int REQUEST_NOTIFIKASI = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        databaseHelper = new DatabaseHelper(this);

        edtNamaKegiatan = findViewById(R.id.edtNamaKegiatan);
        edtDeskripsi = findViewById(R.id.edtDeskripsi);
        btnTanggal = findViewById(R.id.btnTanggal);
        btnSimpan = findViewById(R.id.btnSimpan);

        // Meminta izin notifikasi Android 13 ke atas
        if (Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                        != android.content.pm.PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{
                            android.Manifest.permission.POST_NOTIFICATIONS
                    },
                    REQUEST_NOTIFIKASI
            );
        }

        idKegiatan = getIntent().getIntExtra(
                "id_kegiatan",
                -1
        );

        if (idKegiatan != -1) {
            tampilkanDataEdit();
        }

        btnTanggal.setOnClickListener(v -> tampilkanDatePicker());

        btnSimpan.setOnClickListener(v -> simpanKegiatan());
    }

    private void tampilkanDataEdit() {

        String nama = getIntent().getStringExtra("nama_kegiatan");
        String deskripsi = getIntent().getStringExtra("deskripsi");
        String tanggal = getIntent().getStringExtra("tanggal");

        edtNamaKegiatan.setText(nama);
        edtDeskripsi.setText(deskripsi);

        if (tanggal != null && !tanggal.isEmpty()) {
            tanggalDipilih = tanggal;
            btnTanggal.setText(tanggal);
        }

        btnSimpan.setText("Simpan Perubahan");
    }

    private void tampilkanDatePicker() {

        Calendar kalender = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {

                    tanggalDipilih = String.format(
                            Locale.getDefault(),
                            "%02d/%02d/%04d",
                            dayOfMonth,
                            month + 1,
                            year
                    );

                    btnTanggal.setText(tanggalDipilih);
                },
                kalender.get(Calendar.YEAR),
                kalender.get(Calendar.MONTH),
                kalender.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    private void simpanKegiatan() {

        String nama = edtNamaKegiatan.getText().toString().trim();
        String deskripsi = edtDeskripsi.getText().toString().trim();

        if (nama.isEmpty()) {
            edtNamaKegiatan.setError("Nama kegiatan harus diisi");
            edtNamaKegiatan.requestFocus();
            return;
        }

        if (tanggalDipilih.isEmpty()) {
            Toast.makeText(
                    this,
                    "Silakan pilih tanggal kegiatan",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (idKegiatan == -1) {

            long hasil = databaseHelper.tambahKegiatan(
                    nama,
                    deskripsi,
                    tanggalDipilih
            );

            if (hasil != -1) {

                idKegiatan = (int) hasil;
                jadwalkanPengingat(idKegiatan, nama, tanggalDipilih);

                Toast.makeText(
                        this,
                        "Kegiatan berhasil ditambahkan 🌷",
                        Toast.LENGTH_SHORT
                ).show();

                finish();

            } else {
                Toast.makeText(
                        this,
                        "Gagal menambahkan kegiatan",
                        Toast.LENGTH_SHORT
                ).show();
            }

        } else {

            int hasil = databaseHelper.ubahKegiatan(
                    idKegiatan,
                    nama,
                    deskripsi,
                    tanggalDipilih
            );

            if (hasil > 0) {

                jadwalkanPengingat(idKegiatan, nama, tanggalDipilih);

                Toast.makeText(
                        this,
                        "Kegiatan berhasil diperbarui 🌷",
                        Toast.LENGTH_SHORT
                ).show();

                finish();

            } else {
                Toast.makeText(
                        this,
                        "Tidak ada perubahan pada kegiatan",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }

    // Menjadwalkan pengingat pukul 09.00 pada tanggal kegiatan
    private void jadwalkanPengingat(
            int id,
            String nama,
            String tanggal
    ) {

        try {
            SimpleDateFormat format = new SimpleDateFormat(
                    "dd/MM/yyyy",
                    Locale.getDefault()
            );

            format.setLenient(false);

            Date tanggalDate = format.parse(tanggal);

            if (tanggalDate == null) return;

            Calendar waktuPengingat = Calendar.getInstance();
            waktuPengingat.setTime(tanggalDate);
            waktuPengingat.set(Calendar.HOUR_OF_DAY, 9);
            waktuPengingat.set(Calendar.MINUTE, 0);
            waktuPengingat.set(Calendar.SECOND, 0);
            waktuPengingat.set(Calendar.MILLISECOND, 0);

            long waktu = waktuPengingat.getTimeInMillis();

            // Jika tanggal sudah lewat, jangan jadwalkan
            if (waktu < System.currentTimeMillis()) {

                Calendar hariIni = Calendar.getInstance();
                Calendar tanggalKegiatan = Calendar.getInstance();
                tanggalKegiatan.setTime(tanggalDate);

                boolean hariYangSama =
                        hariIni.get(Calendar.YEAR) ==
                                tanggalKegiatan.get(Calendar.YEAR) &&
                                hariIni.get(Calendar.DAY_OF_YEAR) ==
                                        tanggalKegiatan.get(Calendar.DAY_OF_YEAR);

                if (hariYangSama) {
                    // Jika pukul 09.00 sudah lewat hari ini,
                    // pengingat muncul sekitar satu menit lagi.
                    waktu = System.currentTimeMillis() + 60 * 1000;
                } else {
                    batalkanPengingat(id);
                    return;
                }
            }

            Intent intent = new Intent(
                    this,
                    ReminderReceiver.class
            );

            intent.putExtra("nama_kegiatan", nama);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    id,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT |
                            PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager =
                    (AlarmManager) getSystemService(
                            Context.ALARM_SERVICE
                    );

            if (alarmManager != null) {
                alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        waktu,
                        pendingIntent
                );
            }

        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "Pengingat gagal dijadwalkan",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // Membatalkan pengingat berdasarkan ID kegiatan
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
                (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }

        pendingIntent.cancel();
    }
}
