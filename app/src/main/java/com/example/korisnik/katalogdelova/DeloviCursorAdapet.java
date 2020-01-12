package com.example.korisnik.katalogdelova;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.korisnik.katalogdelova.data.DeoContract;


public class DeloviCursorAdapet extends CursorAdapter {

    //kostruktor koji sadrzi kontekst i kursor iz kojeg dovlacimo podatke
    public DeloviCursorAdapet(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    //pravi novu praznu listu. Jos uvek nisu postavljeni ili vezani podaci
    //sadrzi kontekst,kursor iz kojeg ce dobiti podatke i on je vec pomeren na odgovarajucu poziciju
    //viewGroup ili roditelj na koju je novi view atacovan i vraca novokreiran list item view
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        //LayoutInflater uzima input moj layout xml fajl odnosno, activity_main i pravi View objekat
        return LayoutInflater.from(context).inflate(R.layout.list_view, viewGroup, false);
    }

    //ovaj metod se koristi da poveze sve podatke sa datim view-om
    //kao sto je povezivanje teksta sa TextView-om
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        //trazi TextView pomocu njihovog id
        TextView imeDelaTextView = view.findViewById(R.id.ime_dela);
        TextView modeliAutomobilaTextView = view.findViewById(R.id.modeli_automobila);
        TextView cenaTextView = view.findViewById(R.id.cena_item);
        TextView preostalaKolicinaTextView = view.findViewById(R.id.preostala_kolicina);
        ImageView slikaDelaVIew = view.findViewById(R.id.slika_item);

        //pronalzi kolone za koje smo zainteresovani koje zelimo da prikazemo u listi
        int imeDelaColumnIndex = cursor.getColumnIndex(DeoContract.DeoEntry.NAZIV_DELA);
        int modeliAutomobilaColumnIndex = cursor.getColumnIndex(DeoContract.DeoEntry.MODELI_AUTOMOBILA);
        int cenaColumnIndex = cursor.getColumnIndex(DeoContract.DeoEntry.CENA_DELA);
        int preostalaKolicinaColumnIndex = cursor.getColumnIndex(DeoContract.DeoEntry.PREOSTALA_KOLICINA);
        int slikaDelaColumnIndex = cursor.getColumnIndex(DeoContract.DeoEntry.SLIKA_DELA);


        //Cita atribute iz cursora za taj autodeo
        String imeDela = cursor.getString(imeDelaColumnIndex);
        String modeliAutomobila = cursor.getString(modeliAutomobilaColumnIndex);
        int cenaDela = cursor.getInt(cenaColumnIndex);
        int preostalaKolicina = cursor.getInt(preostalaKolicinaColumnIndex);
        String imageUriString = cursor.getString(slikaDelaColumnIndex);
        Uri slikaDelaUri = Uri.parse(imageUriString);


        //azurira TextView
        imeDelaTextView.setText(imeDela);
        modeliAutomobilaTextView.setText(modeliAutomobila);
        cenaTextView.setText(String.valueOf(cenaDela));
        preostalaKolicinaTextView.setText(String.valueOf(preostalaKolicina));
        slikaDelaVIew.setImageURI(slikaDelaUri);

    }


    }

