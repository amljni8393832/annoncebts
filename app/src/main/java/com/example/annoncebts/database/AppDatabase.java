package com.example.annoncebts.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AppDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "annonce_bts_db";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    public static final String TABLE_ANNONCE = "annonces";
    public static final String TABLE_UTILISATEUR = "utilisateurs";
    public static final String TABLE_FILIERE = "filieres";
    public static final String TABLE_NIVEAU = "niveaux";
    public static final String TABLE_NOTIFICATION = "notifications";

    // Common column names
    public static final String KEY_ID = "id";

    // ANNONCE Table - column names
    public static final String KEY_ANNONCE_TITRE = "titre";
    public static final String KEY_ANNONCE_DESCRIPTION = "description";
    public static final String KEY_ANNONCE_DATE = "dateAnnonce";
    public static final String KEY_ANNONCE_CATEGORIE = "categorie";
    public static final String KEY_ANNONCE_STATUT = "statut";
    public static final String KEY_ANNONCE_DATE_CREATION = "dateCreation";
    public static final String KEY_ANNONCE_DATE_MODIFICATION = "dateModification";
    public static final String KEY_ANNONCE_DATE_EXPIRATION = "dateExpiration";

    // UTILISATEUR Table - column names
    public static final String KEY_UTILISATEUR_NOM = "nom";
    public static final String KEY_UTILISATEUR_EMAIL = "email";
    public static final String KEY_UTILISATEUR_PASSWORD = "motDePasse";
    public static final String KEY_UTILISATEUR_ROLE = "role";
    public static final String KEY_UTILISATEUR_DATE_CREATION = "dateCreation";

    // FILIERE Table - column names
    public static final String KEY_FILIERE_NOM = "nomFiliere";

    // NIVEAU Table - column names
    public static final String KEY_NIVEAU_LIBELLE = "libelle";

    // NOTIFICATION Table - column names
    public static final String KEY_NOTIFICATION_MESSAGE = "message";
    public static final String KEY_NOTIFICATION_TYPE = "type";
    public static final String KEY_NOTIFICATION_DATE_ENVOI = "dateEnvoi";
    public static final String KEY_NOTIFICATION_ANNONCE_ID = "annonce_id";
    public static final String KEY_NOTIFICATION_UTILISATEUR_ID = "utilisateur_id";

    // Table Create Statements
    private static final String CREATE_TABLE_ANNONCE = "CREATE TABLE " + TABLE_ANNONCE + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_ANNONCE_TITRE + " TEXT,"
            + KEY_ANNONCE_DESCRIPTION + " TEXT,"
            + KEY_ANNONCE_DATE + " INTEGER,"
            + KEY_ANNONCE_CATEGORIE + " TEXT,"
            + KEY_ANNONCE_STATUT + " TEXT,"
            + KEY_ANNONCE_DATE_CREATION + " INTEGER,"
            + KEY_ANNONCE_DATE_MODIFICATION + " INTEGER,"
            + KEY_ANNONCE_DATE_EXPIRATION + " INTEGER" + ")";

    private static final String CREATE_TABLE_UTILISATEUR = "CREATE TABLE " + TABLE_UTILISATEUR + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_UTILISATEUR_NOM + " TEXT,"
            + KEY_UTILISATEUR_EMAIL + " TEXT UNIQUE,"
            + KEY_UTILISATEUR_PASSWORD + " TEXT,"
            + KEY_UTILISATEUR_ROLE + " TEXT,"
            + KEY_UTILISATEUR_DATE_CREATION + " INTEGER" + ")";

    private static final String CREATE_TABLE_FILIERE = "CREATE TABLE " + TABLE_FILIERE + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_FILIERE_NOM + " TEXT" + ")";

    private static final String CREATE_TABLE_NIVEAU = "CREATE TABLE " + TABLE_NIVEAU + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_NIVEAU_LIBELLE + " TEXT" + ")";

    private static final String CREATE_TABLE_NOTIFICATION = "CREATE TABLE " + TABLE_NOTIFICATION + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_NOTIFICATION_MESSAGE + " TEXT,"
            + KEY_NOTIFICATION_TYPE + " TEXT,"
            + KEY_NOTIFICATION_DATE_ENVOI + " INTEGER,"
            + KEY_NOTIFICATION_ANNONCE_ID + " INTEGER,"
            + KEY_NOTIFICATION_UTILISATEUR_ID + " INTEGER" + ")";

    private static AppDatabase instance;

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = new AppDatabase(context.getApplicationContext());
        }
        return instance;
    }

    private AppDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_ANNONCE);
        db.execSQL(CREATE_TABLE_UTILISATEUR);
        db.execSQL(CREATE_TABLE_FILIERE);
        db.execSQL(CREATE_TABLE_NIVEAU);
        db.execSQL(CREATE_TABLE_NOTIFICATION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ANNONCE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_UTILISATEUR);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FILIERE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NIVEAU);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATION);
        onCreate(db);
    }
}