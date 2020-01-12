package com.example.korisnik.katalogdelova;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.FileProvider;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.korisnik.katalogdelova.data.DeloviDBHelper;
import com.example.korisnik.katalogdelova.data.DeoContract;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Edit extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    /**
     * boolean promenjiva koja vodi racuna o tome da li je deo izmenjen ili ne
     */
    private boolean mProductHasChanged = false;
    public static int EXISTING_DEO_LOADER = 0;


    private Uri currentDeloviUri;
    private EditText unosDela;          //ime dela
    private EditText unosModelaKola;    //modeli automobila gde se ovaj deo moe ugraditi/prodati
    private EditText unosCene;          //cena dela
    private EditText unosKolicine;      // preostala kolicina
    private ImageView imageViewSlika;
    private Uri mImageUri;
    //prati da li je deo izmenjen ili nije
    private boolean deoJePromenjen;

    //OnTouchListener koji slusa da li je kliknuto na neko View polje,ako jeste to implicira da je neka promena izvrsena
    //i onda menjamo deoJePromenjen true
    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            deoJePromenjen = true;
            return false;

        }
    };
    DeloviDBHelper deloviDBHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edituj_deo);

        Intent intent = getIntent();



        //ukoliko intent ne sadrzi uri dela onda za sigurno znamo da kreiramo novi deo
        if (intent != null) {
            currentDeloviUri = intent.getData();
            if (currentDeloviUri == null) {

                //Ovo je novi deo ,pa se zato ime aktivnosti menja u "Dodaj novi deo"
                setTitle(getString(R.string.naslov_za_dodavanje_dela));


                //Proglasiti nevazeci meni opcija tako da se "Obrisati deo: skloni/sakrije,zato sto ne mozemo obrisati deo koji nije ni naprvljen
                invalidateOptionsMenu();
            } else {


                //ukoliko je ovo postojeci deo,promeniti ime aktivnosti u "Izmeni deo"
                setTitle(getString(R.string.naslov_za_editovanje_dela));

                //inicijalizujemo loader da iscita informacije o delu iz baze
                //i prikazuje trnutne vrednosi u editoru dela
                getSupportLoaderManager().initLoader(EXISTING_DEO_LOADER, null, this);
            }
        }

        FloatingActionButton floatingActionButtonSave = findViewById(R.id.save);
        floatingActionButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sacuvajDeo();
                Toast toast = Toast.makeText(getApplicationContext(), "Deo je ubacen", Toast.LENGTH_SHORT);
                toast.show();
                finish();
            }
        });

        FloatingActionButton floatingActionButtonDelete = findViewById(R.id.delete);
        floatingActionButtonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteConfirmationDialog();
                Toast toast = Toast.makeText(getApplicationContext(), "Deo obrisan", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
        // editview polja iz edituj_deo.xml u kojem upisujemo informacije koje zelimo da skladistimo
        unosDela = findViewById(R.id.naziv_dela);
        unosModelaKola = findViewById(R.id.modeli_kola);
        unosCene = findViewById(R.id.cena);
        unosKolicine = findViewById(R.id.kolicina_preostala);
        imageViewSlika = findViewById(R.id.slika_dela);


        unosDela.setOnTouchListener(onTouchListener);
        unosModelaKola.setOnTouchListener(onTouchListener);
        unosCene.setOnTouchListener(onTouchListener);
        unosKolicine.setOnTouchListener(onTouchListener);
        imageViewSlika.setOnTouchListener(onTouchListener);

        imageViewSlika.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                trySelector();
                deoJePromenjen = true;
            }
        });
    }
    public void trySelector() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            return;
        }
        openSelector();
    }

    private void openSelector() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 23) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType(getString(R.string.intent_type));
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image)), 0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openSelector();
                }


        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                mImageUri = data.getData();
                imageViewSlika.setImageURI(mImageUri);
                imageViewSlika.invalidate();
            }
        }
    }
    private void sacuvajDeo() {
        String imeDelaString = unosDela.getText().toString().trim();
        String unosModelaKolaString = unosModelaKola.getText().toString().trim();
        String unosCeneString = unosCene.getText().toString().trim();
        String unosKolicineString = unosKolicine.getText().toString().trim();

        if (currentDeloviUri == null && TextUtils.isEmpty(imeDelaString) && TextUtils.isEmpty(unosModelaKolaString)
                && TextUtils.isEmpty(unosCeneString) && TextUtils.isEmpty(unosKolicineString) && mImageUri == null) {
            return ;
        }

        deloviDBHelper = new DeloviDBHelper(this);
        ContentValues contentValues = new ContentValues();
        contentValues.put(DeoContract.DeoEntry.NAZIV_DELA, imeDelaString);
        contentValues.put(DeoContract.DeoEntry.MODELI_AUTOMOBILA, unosModelaKolaString);
        contentValues.put(DeoContract.DeoEntry.SLIKA_DELA, mImageUri.toString());


        int cenaJeNula = 0;
        if (!TextUtils.isEmpty(unosCeneString)) {
            cenaJeNula = Integer.parseInt(unosCeneString);
        }
        contentValues.put(DeoContract.DeoEntry.CENA_DELA, cenaJeNula);

        int kolicinaJeNula = 0;
        if (!TextUtils.isEmpty(unosKolicineString)) {
            kolicinaJeNula = Integer.parseInt(unosKolicineString);
        }
        contentValues.put(DeoContract.DeoEntry.PREOSTALA_KOLICINA, kolicinaJeNula);


        if (currentDeloviUri == null) {
            Uri newUri = getContentResolver().insert(DeoContract.DeoEntry.CONTENT_URI, contentValues);

            if (newUri != null) {
                Toast toast = Toast.makeText(getApplicationContext(), "Deo uspesno unesen", Toast.LENGTH_SHORT);
                toast.show();
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), "Deo nije unesen,greska!", Toast.LENGTH_SHORT);
                toast.show();
            }
        } else {
            int redoviPogodnjeniPomenom = getContentResolver().update(currentDeloviUri, contentValues, null, null);
            if (redoviPogodnjeniPomenom == 0) {
                Toast toast = Toast.makeText(this, "Deo je neuspesno azuriran", Toast.LENGTH_SHORT);
                toast.show();
            } else {
                Toast toast = Toast.makeText(this, "Deo je usesno azuriran", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }
    /**
     * Izbacuje na ekranu prozor u kojem se proverava da li je korisniksiguran da zeli da obrise deo
     */
    private void showDeleteConfirmationDialog() {

        //Kreira AlertDialog.Builder ko prikazuje poruku.Klik liseneri za poitivan ili negativan odgocor korisnika
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.poruka_o_brisanju_dela);
        builder.setPositiveButton(R.string.obrisano, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //Korisnik je kliknuo na "Obrisi"  poziva se meroda obrisiDeo() nakon toga finish() sa kojim se zatvara aktivnost i ide na pocetnu stranicu
                obrisiDeo();;
                finish();
            }
        });
        builder.setNegativeButton(R.string.odustani, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                //KOrisnik je kliknuo na odustani i nastavio da edituje deo
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });


        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    /**
     *
     * Prikazuje dialog koji upozorava korisnika da postoje nesacuvane izmene koje ce biti izgubljene ako izadjemo iz editora
     * discardButtonClickListener je klik lisener koji odlucuje sta ce biti dalje ako korisnik zaista potvrdi da zeli da izadje iz editora
     */
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {

        //postavljamo AlertdDialog.builder i prikazujem poruku,i klik lisener za pozitivan ili negativan odgovor

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.poruka_o_nesacuvanoj_izmeni);//prikazuje poruku u allert boxu
        builder.setPositiveButton(R.string.odbacivanje, discardButtonClickListener);//ukoliko pozitivno odgovorim da zelim da odbacim sve promene
        builder.setNegativeButton(R.string.nastavak_editovanja, new DialogInterface.OnClickListener() {//zeli da nastavi da edituje ljubmca
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //KOrisnik je kliknuo na nastavi sa editovanjem zato odbacujemo dialog i nastavljamo sa editovanjem dela
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //1) ovaj metod je pozvan kada je back dugme kliknuto
    @Override
    public void onBackPressed() {

        //ako deo nije promenjen nastaviti sa sa back dugmetom
        if (!deoJePromenjen) {
            super.onBackPressed();
            return;
        }


        //2) Ako postoje nesacuvane izmene,prikazi dialog da upozori korisnika
        //kreira klik lisener koji hendluje dalje portvdu korisnika da ne bi trbalo da se sacuvaju izmene

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //3) Korinik je kliknuo na odbaci dugme i zatvaramo trenutnu aktivnost i vracamo se na pocetni ekran
                        finish();
                    }
                };

        //prikazuje dialog da postoje nesacuvane izmene
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {
                DeoContract.DeoEntry._ID,
                DeoContract.DeoEntry.NAZIV_DELA,
                DeoContract.DeoEntry.MODELI_AUTOMOBILA,
                DeoContract.DeoEntry.CENA_DELA,
                DeoContract.DeoEntry.PREOSTALA_KOLICINA,
                DeoContract.DeoEntry.SLIKA_DELA
        };

        return new CursorLoader(this,
                currentDeloviUri,
                projection,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null || data.getCount() < 1) {
            return;
        }

        if (data.moveToFirst()) {
            int nazivDelaIndex = data.getColumnIndex(DeoContract.DeoEntry.NAZIV_DELA);
            int modeliAutomobilaIndex = data.getColumnIndex(DeoContract.DeoEntry.MODELI_AUTOMOBILA);
            int cenaDelaIndex = data.getColumnIndex(DeoContract.DeoEntry.CENA_DELA);
            int preostalaKolicinaIndex = data.getColumnIndex(DeoContract.DeoEntry.PREOSTALA_KOLICINA);
            int pictureColumnIndex = data.getColumnIndex(DeoContract.DeoEntry.SLIKA_DELA);

            String imeDela = data.getString(nazivDelaIndex);
            String modeliAutimobila = data.getString(modeliAutomobilaIndex);
            int cenaDela = data.getInt(cenaDelaIndex);
            int preostalaKolicina = data.getInt(preostalaKolicinaIndex);
            String imageUriString = data.getString(pictureColumnIndex);
            if (imageUriString!=null){
                mImageUri = Uri.parse(imageUriString);
                imageViewSlika.setImageURI(mImageUri);
            }


            unosDela.setText(imeDela);
            unosModelaKola.setText(modeliAutimobila);
            unosCene.setText(Integer.toString(cenaDela));
            unosKolicine.setText(Integer.toString(preostalaKolicina));


        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // ako nista nije uneto u polja podesiti ovo kao defoult vrednosti
        unosDela.setText("");
        imageViewSlika.setImageResource(R.mipmap.placeholder);
        unosModelaKola.setText("");
        unosCene.setText("");
        unosKolicine.setText("");
    }

    public void obrisiDeo() {
        if (currentDeloviUri != null) {
            int obrisaniRed = getContentResolver().delete(currentDeloviUri, null, null);


            //ukoliko je red obrisan prikazati toast
            if (obrisaniRed == 0) {

                //ako ni jedan red nije obrisan isoisati ovaj toiast
                Toast.makeText(this, "Brisanje nije uspelo", Toast.LENGTH_SHORT).show();

            } else {

                //brisanje je uspesno
                Toast.makeText(this, "Deo je uspesno obrisan", Toast.LENGTH_SHORT).show();
            }
        }

    }



}



