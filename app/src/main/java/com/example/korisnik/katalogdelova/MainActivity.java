package com.example.korisnik.katalogdelova;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.korisnik.katalogdelova.data.DeloviDBHelper;
import com.example.korisnik.katalogdelova.data.DeoContract;

import java.io.ByteArrayOutputStream;

//ova klasa prikazuje listu unetih auto delova sa svi potrebnim detaljima
public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {


    //identifikator za Loader
    private static final int DELOVI_LOADER = 0;
    //adapter za ListView
    DeloviCursorAdapet deloviCursorAdapet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lista_delova);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast toast = Toast.makeText(getApplicationContext(), "Dodaj deo", Toast.LENGTH_SHORT);
                toast.show();
                Intent intent = new Intent(MainActivity.this, Edit.class);
                startActivity(intent);
            }
        });
        ListView listView = findViewById(R.id.list);

        deloviCursorAdapet = new DeloviCursorAdapet(this, null);

        View emptyView = findViewById(R.id.empty_view);
        listView.setEmptyView(emptyView);

        listView.setAdapter(deloviCursorAdapet);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                //novi intent kojim idemo u Edit
                Intent intent = new Intent(MainActivity.this, Edit.class);

                //formiramo novi uri koji se kreira kada je neki deo kliknut,tako sto dodajemo id koji je
                //prosledjen kao input  DeoContract.DeoEntry.CONTENT_URI
                //npr URI "content://com.example.android.gojic/milos/4" ako je deo sa id-om 4 kliknut
                Uri currentPetUri = ContentUris.withAppendedId(DeoContract.DeoEntry.CONTENT_URI, id);
                intent.setData(currentPetUri);

                Toast toast = Toast.makeText(getApplicationContext(), "Izmeni deo", Toast.LENGTH_SHORT);
                toast.show();
                startActivity(intent);

            }
        });
        getSupportLoaderManager().initLoader(DELOVI_LOADER, null, this);
   }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //ovde su navedene kolone koje zelim da prikazem
        String[] projection = {
                DeoContract.DeoEntry._ID,
                DeoContract.DeoEntry.NAZIV_DELA,
                DeoContract.DeoEntry.MODELI_AUTOMOBILA,
                DeoContract.DeoEntry.CENA_DELA,
                DeoContract.DeoEntry.PREOSTALA_KOLICINA,
                DeoContract.DeoEntry.SLIKA_DELA

        };
        //ovaj loader ce pokrenuti ContentProvider query metod u pozadini a ne na glavnom tredu
        return new CursorLoader(MainActivity.this,      //kontekst
                DeoContract.DeoEntry.CONTENT_URI,               //provajder sadrzi URI iz koga uzimamo neke podatke
                projection,                                     //lista kolona koju zelimo da vratimo,kolone cije vrednosti zelimo da uzmemo
                null,                                  //filter kojim zelimo da izjavimo kpje redove zelimo da vratimo,ovde je to null sto znaci sve
                null,                               //ovo su parametri koji uzmaju vrednosti is selection
                null                                   //redosled sortiranja,ovde je to null sto znaci default redosled
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //azurira DeloviCursorAdapter sa novim kursorom koji sadrzi azuriane informacije o autodelu
        deloviCursorAdapet.swapCursor(data);
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //poziva se kada treba obrisati podatke
        //swapCursor - zamenjuje novi kursor vracajuci stari
        deloviCursorAdapet.swapCursor(null);
    }

}
