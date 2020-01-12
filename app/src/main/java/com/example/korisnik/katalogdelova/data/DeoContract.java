package com.example.korisnik.katalogdelova.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Korisnik on 23-May-19.
 */

public class DeoContract {
    //Conntent Autority
    public static final String CONTENT_AUTORITY = "com.example.korisnik.katalogdelova";
    //Base content , ovde spajamo CA sa shemom i Base content stvara osnovu URI-a koji ce aplikacija koristiti da kontaktira provajder
    //koristimo parse() metod da bi se napravio upotrebljiv uri.Uzima String i uri i vraca uri
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTORITY);

    // Ova konstanta skladisti put do dabele  i bice prikacena BCU
    public static final String PATH_DELOVI = "delovi";

    //prazan metod sprecava se slucajnu inicijalizaiju ove klase
    private DeoContract() {
    }

    //unutrasnja klasa se pravi za svaku tabelu u basi
    //definise konstante za svaki deo u tabeli
    public static final class DeoEntry implements BaseColumns {


        //MIME tip za CONTENT_URI za celu listu delaova
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTORITY + "/" + PATH_DELOVI;


        //MIME tip za CONTENT_URI za pojedinacan deo
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTORITY + "/" + PATH_DELOVI;



        //ime moje tabele
        public static final String NAZIV_TABELE = "delovi";

        //jedinstveni id broj za svaki deo i koristi se samo u tabeli baze,
        //korisnik nece videti ovu informaciju
        public static final String _ID = BaseColumns._ID;

        //ime dela
        public static final String NAZIV_DELA = "deo";

        //modeli kola gde se ovaj deo moze ugraditi
        public static final String MODELI_AUTOMOBILA = "modeli_kola";

        //cena tog dela
        public static final String CENA_DELA = "cena";

        //preostala kolicina tog dela
        public static final String PREOSTALA_KOLICINA = "kolicina";

        public static final String SLIKA_DELA = "slika";

        //sa withAppendedPath() kreiramo punu URI za parametre uzima BCU koji u sebi sadrzi shemu i CA
        //spaja sa putem do tabele
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI,PATH_DELOVI);
    }
}
