
package com.example.taskmate;

import android.content.BroadcastReceiver;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class ReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "taskmate_reminder";

    @Override
    public void onReceive(Context context, Intent intent) {

        String namaKegiatan = intent.getStringExtra("nama_kegiatan");

        if (namaKegiatan == null || namaKegiatan.isEmpty()) {
            namaKegiatan = "Kegiatanmu";
        }

        NotificationManager manager =
                (NotificationManager) context.getSystemService(
                        Context.NOTIFICATION_SERVICE
                );

        if (manager == null) return;

        // Membuat kanal notifikasi untuk Android 8 ke atas
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "Pengingat TaskMate",
                            NotificationManager.IMPORTANCE_HIGH
                    );

            channel.setDescription(
                    "Pengingat tanggal kegiatan TaskMate"
            );

            manager.createNotificationChannel(channel);
        }

        Intent bukaAplikasi = new Intent(
                context,
                MainActivity.class
        );

        bukaAplikasi.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
        );

        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        context,
                        100,
                        bukaAplikasi,
                        PendingIntent.FLAG_UPDATE_CURRENT |
                                PendingIntent.FLAG_IMMUTABLE
                );

        Notification notification;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            notification = new Notification.Builder(
                    context,
                    CHANNEL_ID
            )
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("Pengingat Kegiatan 🌷")
                    .setContentText(
                            "Jangan lupa: " + namaKegiatan
                    )
                    .setStyle(
                            new Notification.BigTextStyle()
                                    .bigText(
                                            "Jangan lupa menyelesaikan kegiatan: "
                                                    + namaKegiatan
                                    )
                    )
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .build();

        } else {

            notification = new Notification.Builder(context)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("Pengingat Kegiatan")
                    .setContentText("Jangan lupa: " + namaKegiatan)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .build();
        }

        manager.notify(
                (int) System.currentTimeMillis(),
                notification
        );
    }
}
