
package com.example.taskmate;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "TaskMate.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_TASK = "kegiatan";
    public static final String COL_ID = "id";
    public static final String COL_NAMA = "nama";
    public static final String COL_DESKRIPSI = "deskripsi";
    public static final String COL_TANGGAL = "tanggal";
    public static final String COL_SELESAI = "selesai";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_TASK + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAMA + " TEXT NOT NULL, " +
                COL_DESKRIPSI + " TEXT, " +
                COL_TANGGAL + " TEXT, " +
                COL_SELESAI + " INTEGER DEFAULT 0)";

        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASK);
        onCreate(db);
    }

    // Menambahkan kegiatan baru
    public long tambahKegiatan(String nama, String deskripsi, String tanggal) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_NAMA, nama);
        values.put(COL_DESKRIPSI, deskripsi);
        values.put(COL_TANGGAL, tanggal);
        values.put(COL_SELESAI, 0);

        return db.insert(TABLE_TASK, null, values);
    }

    // Mengambil semua kegiatan
    public Cursor ambilSemuaKegiatan() {
        SQLiteDatabase db = this.getReadableDatabase();

        return db.query(
                TABLE_TASK,
                null,
                null,
                null,
                null,
                null,
                COL_ID + " DESC"
        );
    }

    // Mengubah kegiatan
    public int ubahKegiatan(int id, String nama, String deskripsi, String tanggal) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_NAMA, nama);
        values.put(COL_DESKRIPSI, deskripsi);
        values.put(COL_TANGGAL, tanggal);

        return db.update(
                TABLE_TASK,
                values,
                COL_ID + "=?",
                new String[]{String.valueOf(id)}
        );
    }

    // Mengubah status selesai
    public int ubahStatus(int id, boolean selesai) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_SELESAI, selesai ? 1 : 0);

        return db.update(
                TABLE_TASK,
                values,
                COL_ID + "=?",
                new String[]{String.valueOf(id)}
        );
    }

    // Menghapus kegiatan
    public int hapusKegiatan(int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        return db.delete(
                TABLE_TASK,
                COL_ID + "=?",
                new String[]{String.valueOf(id)}
        );
    }
}
