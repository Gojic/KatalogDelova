package com.example.korisnik.katalogdelova.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class DeloviDBHelper extends SQLiteOpenHelper {
    //verzija baze
    private static final int DATABASE_VERSION = 1;

    //ime fajla baze podataka
    private static final String DATABASE_IME = "polovni_delovi.db";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + DeoContract.DeoEntry.NAZIV_TABELE;

    //konstruktor
    public DeloviDBHelper(Context context) {
        super(context, DATABASE_IME, null, DATABASE_VERSION);
    }

    //ovaj meod je pozvan kada se baza prvi put kreira
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //Kreiram String u koji odredjujem koja vrednost ide pod koju kolonu
        //String sadrzava SQL naredbe pod znacima navodnika kojima kreiram tabelu autodelova
        String SQL_KREIRAJ_TABELU_AUTODELOVA = " CREATE TABLE " + DeoContract.DeoEntry.NAZIV_TABELE + " ( "
                + DeoContract.DeoEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DeoContract.DeoEntry.NAZIV_DELA + " TEXT NOT NULL, "
                + DeoContract.DeoEntry.MODELI_AUTOMOBILA + " TEXT, "
                + DeoContract.DeoEntry.CENA_DELA + " INTEGER NOT NULL DEFAULT 0, "
                + DeoContract.DeoEntry.PREOSTALA_KOLICINA + " INTEGER DEFAULT 0, "
                + DeoContract.DeoEntry.SLIKA_DELA + " TEXT );";

        //ovom linijom koda izvrsavam gore navedene SQL knaredbe koji su skladisteni kao String
        sqLiteDatabase.execSQL(SQL_KREIRAJ_TABELU_AUTODELOVA);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
//        if (oldVersion < 2)
//            sqLiteDatabase.execSQL("ALTER TABLE "+ DeoContract.DeoEntry.NAZIV_TABELE +" ADD "+ DeoContract.DeoEntry._ID +" INTEGER PRIMARY KEY AUTOINCREMENT");
//    }
    }
}

