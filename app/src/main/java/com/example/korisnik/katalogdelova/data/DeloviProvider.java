package com.example.korisnik.katalogdelova.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


public class DeloviProvider extends ContentProvider {

    //URI matcher za sadrzaj cele tabele
    private static final int DELOVI = 100;

    //URI matcher za sadrzaj samo pojedinih podataka iz tabele
    private static final int DELOVI_ID = 101;

    //kreiranje URIMatcher objekta koji utvrdjuje koja je vrsta URIa proslednjena.
    //da li za sadrzaj samo pojedinih podtaka ili a celu tabelu
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    //staticni inicijaliator koji se pokrece prilikom pokretanje klase.
    //posto je staticki tip on je vezan za klasu
    static {
        //Provajder ce prihvatiti patern ako je isparavan URI i zatim dodjemo jednistveni id koji odredjuje da li se zahteva
        //pojedinacni podaci ili cela tabla,ovde se zahteva pristup celoj tabli
        uriMatcher.addURI(DeoContract.CONTENT_AUTORITY, DeoContract.PATH_DELOVI, DELOVI);

        //ovaj uri se koristi za pritup samo jednog Reda iz table.
        //znak # je neka vrsta palceholdera koji se moze zameniti sa brojem reda i tako dolazomo do
        // do reda kojim zelimo da pristupimo
        uriMatcher.addURI(DeoContract.CONTENT_AUTORITY, DeoContract.PATH_DELOVI + "/#", DELOVI_ID);
    }

    //omogucuje pristup mojoj bazi
    DeloviDBHelper deloviDBHelper;

    @Override
    public boolean onCreate() {
        //insanciramo klasu
        deloviDBHelper = new DeloviDBHelper(getContext());
        return true;
    }

    //ovaj metod pomocu ContentREsolvera trazi neke informacije iz nase baze

    @Override
    public Cursor query( Uri uri,String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        //sa ovom linijom koda citam podatke iz baze
        SQLiteDatabase sqLiteDatabase = deloviDBHelper.getReadableDatabase();

        //ovaj kursor skladisti rezultate potrage/upita
        Cursor cursor;
        //videti da li URIMatcher moze da spoji URI sa DeoContract.PATH_DELOVI
        //vraca vrednost tipa int.Uporedjuje uri koji je prosledjen kao argumen i zatim
        //sledecim kodom odlucuje sta ce dalje biti
        int match = uriMatcher.match(uri);

        switch (match) {
            case DELOVI:
                //cursor ovde moze selektovatio vise redova od jednom ili celu tablu
                cursor = sqLiteDatabase.query(DeoContract.DeoEntry.NAZIV_TABELE, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case DELOVI_ID:
                //selektuje  kolonu iz tabele
                selection = DeoContract.DeoEntry._ID + "=?";

                //koristi se metod String.valueOf() da bi se podatak koji je vracen kao tip long pretvorili u String
                //ContentUris.parseId(uri) metodom pretvaramo poslednji segment DeoContract.PATH_DELOVI u long
                //dakle id auto dela se ekstraktuje iz URI
                //primer: "content://com.example.android.gojic/milos/5" ovde je to broj 5
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                //cursor ovde seletkuje red sa datim ID-om
                //kursor sadrzi informacije iz tog reda
                cursor = sqLiteDatabase.query(DeoContract.DeoEntry.NAZIV_TABELE, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Nema upita za za nepoznati uri " + uri);
        }

        //Registruje promene u URI i zatim te automatski primenjuje na nas cursor adapter i listu
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    //vraca String koji opisuje tip podtaka koji se nalayi u URI,
    //drugim recima vraca MIME tip
    @Override
    public String getType(Uri uri) {

        final int match = uriMatcher.match(uri);
        switch (match) {
            case DELOVI:
                return DeoContract.DeoEntry.CONTENT_LIST_TYPE;
            case DELOVI_ID:
                return DeoContract.DeoEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Nepoznat uri " + uri + " " + match);
        }
    }

    //ovaj metod sadrzi ContentValues parametar koji sadrzi sta mi zaista zrlim da ubacimo u bazu a uri gde to zelimo

    @Override
    public Uri insert(Uri uri,ContentValues contentValues) {
        int match = uriMatcher.match(uri);
        switch (match) {
            case DELOVI:
                return insertDeo(uri, contentValues);
            default:
                throw new IllegalArgumentException("Unos podataka nije moguc za URI " + uri);
        }
    }

    //umeti odredjeni auto deo sa ddatim contentvalues i vratiti bovi content uri za naznaceni red koji zelimo da ubacimo
    //u bazu
    private Uri insertDeo(Uri uri, ContentValues contentValues) {
        // proveriti da ime dela nije prazno
        String imeDela = contentValues.getAsString(DeoContract.DeoEntry.NAZIV_DELA);
        if (imeDela == null) {
            throw new IllegalArgumentException("Potrebno je ineti deo auta");
        }

        // proveriti da ime modela auta gde se moze ugraditi deo nije przano
        String modeliKola = contentValues.getAsString(DeoContract.DeoEntry.MODELI_AUTOMOBILA);
        if (modeliKola == null) {
            throw new IllegalArgumentException("Potrbno je uneti ime modela kola");
        }

        // Koje je cena navedena proveriti da li je veca ili jendaka  nuli
        Integer cenaDela = contentValues.getAsInteger(DeoContract.DeoEntry.CENA_DELA);
        if (cenaDela != null && cenaDela < 0) {
            throw new IllegalArgumentException("Nije moguce imati cenu manje od 0");
        }
        //proveriri da li je uneta kolicina manja ili jendaka nuli
        Integer preostalaKolicina = contentValues.getAsInteger(DeoContract.DeoEntry.PREOSTALA_KOLICINA);
        if (preostalaKolicina != null && preostalaKolicina < 0) {
            throw new IllegalArgumentException("Nije moguce imati kolicinu manje od 0");
        }

        // unosimo neku vrednost u bazu
        SQLiteDatabase database = deloviDBHelper.getWritableDatabase();

        //unosimo nov deo sa datim vrednostima
        long id = database.insert(DeoContract.DeoEntry.NAZIV_TABELE, null, contentValues);
        //ako je ID -1 onda unos nije uspeo i vraca null
        if (id == -1) {

            return null;
        }
        //obavestava  ContentObserver da su podaci iz ovog URI promenjene
        getContext().getContentResolver().notifyChange(uri, null);

        //vraca novi URI sa ID-om novoubacenig reda koji se dodaje na kraju URI-a
        return ContentUris.withAppendedId(uri, id);
    }

    //kada zelimo da brisemo podatke iz baze koristi se ovaj metod
    @Override
    public int delete(Uri uri,String selection,String[] selectionArgs) {
        // pristupamo bazi radi njene izmene odnosno brisanja
        SQLiteDatabase database = deloviDBHelper.getWritableDatabase();
        //PRATI KOLIKO JE REDOVA OBRISANO
        int rowsDeleted;
        final int match = uriMatcher.match(uri);
        switch (match) {
            case DELOVI:
                // Brisati sve delove koji se podudaraju sa selection i selectionArgs
                rowsDeleted = database.delete(DeoContract.DeoEntry.NAZIV_TABELE, selection, selectionArgs);
                break;
            case DELOVI_ID:
                //Brisati pojedinacni red koji je oznacen ID-em u URI
                selection = DeoContract.DeoEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(DeoContract.DeoEntry.NAZIV_TABELE, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Brisanje nije moguce za uri " + uri);
        }
        ////ako je jedan ili vise redova obrisan obavestava ContentObserver da su podaci izovog URI promenjene
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;

    }

    //ovaj metod azurira pojedinacne podatke iz baze
    @Override
    public int update(Uri uri,ContentValues contentValues,String selection,String[] selectionArgs) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case DELOVI:
                return updateDeo(uri, contentValues, selection, selectionArgs);
            case DELOVI_ID:
                //za DELOVI_ID ,ekstraktovati ID iz uri da bi smo znali koji red da azuriramo
                //section ce biti "_ID=?" a selectionArguments ce biti String array koja sadrzi taj ID
                selection = DeoContract.DeoEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateDeo(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Azuriranje nije moguce za uri  " + uri);
        }
    }

    /**
     * Azurira deo u bazi sa datim content values.Primenju promene redovima koji su naznaceni u section i selectionArguments
     * koji moze biti jedan ili vise redova
     * zatim vraca beoj redova koji su usoesni zurirani
     */
    private int updateDeo(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        //proverava da li je NAZIV_DELA kljuc prisutan,proverava da ime dela nije null
        if (contentValues.containsKey(DeoContract.DeoEntry.NAZIV_DELA)) {
            //uzima vrednost koja je uskladistna pod kljuc NAZIV_DELA i konvertuje u String
            String imeDela = contentValues.getAsString(DeoContract.DeoEntry.NAZIV_DELA);
            //ukoliko nema nikakvu vrednist uskladistenu vraca IllegalArgumentException
            if (imeDela == null) {
                throw new IllegalArgumentException("Potrbni je imate ime dela");
            }
        }

        //Proverava da li je MODELI_AUTOMOBILA kljuc prisutan
        //i proverava da ime modela automobila nije null
        if (contentValues.containsKey(DeoContract.DeoEntry.MODELI_AUTOMOBILA)) {
            String modeliKola = contentValues.getAsString(DeoContract.DeoEntry.MODELI_AUTOMOBILA);
            if (modeliKola == null) {
                throw new IllegalArgumentException("Potrebno je imate imena modela akutomobila");
            }
        }
        //proverava da li je CENA_DELA kljuc prisutan, i proverava da li je uneta vrednost validna
        if (contentValues.containsKey(DeoContract.DeoEntry.CENA_DELA)) {
            // proverava da li je cena veca ili jednaka nuli
            Integer cena = contentValues.getAsInteger(DeoContract.DeoEntry.CENA_DELA);
            if (cena != null && cena < 0) {
                throw new IllegalArgumentException("Potrebno je imati cenu dela");
            }
        }
        //proverava da li je PREOSTALA_KOLICINA kljuc prisutan i da li je uneta vrednost validna
        if (contentValues.containsKey(DeoContract.DeoEntry.PREOSTALA_KOLICINA)) {
            // proverava da li je preostala kolicina veca ili jednaka nuli
            Integer preostalaKolicina = contentValues.getAsInteger(DeoContract.DeoEntry.PREOSTALA_KOLICINA);
            if (preostalaKolicina != null && preostalaKolicina < 0) {
                throw new IllegalArgumentException("Potrebno je imati preostalu kolicinu delova");
            }
        }
        //ako nema vrednosti za azuriranje onda ne pokusavati da se azurira baza uopste
        if (contentValues.size() == 0) {
            return 0;
        }
        //u suprotnom azuriramo vrednost u bazi
        SQLiteDatabase database = deloviDBHelper.getWritableDatabase();

        //izvrsava azuriranje baze i uzima broj redova koji su pogodjeni promenom
        int rowsUpdated = database.update(DeoContract.DeoEntry.NAZIV_TABELE, contentValues, selection, selectionArgs);

        //ako je jedan ili vise redova azurirano obavestava  ContentObserver da su podaci izovog URI promenjene
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

}
