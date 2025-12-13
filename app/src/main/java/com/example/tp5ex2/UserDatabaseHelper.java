package com.example.tp5ex2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UserDatabaseHelper extends SQLiteOpenHelper {

    // Constantes pour la base de données
    private static final String DATABASE_NAME = "users.db";
    private static final int DATABASE_VERSION = 1;
    
    // Nom de la table et colonnes
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";

    // Constructeur
    public UserDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Méthode appelée lors de la création de la base de données
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_EMAIL + " TEXT UNIQUE, " +
                COLUMN_USERNAME + " TEXT, " +
                COLUMN_PASSWORD + " TEXT" + ")";
        db.execSQL(createTable);
    }

    // Méthode appelée lors de la mise à jour de la base de données
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }    // Méthode pour ajouter un utilisateur
    public boolean addUser(String email, String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_EMAIL, email);
        contentValues.put(COLUMN_USERNAME, username);
        // Hacher le mot de passe avant de le stocker
        contentValues.put(COLUMN_PASSWORD, PasswordUtils.hashPassword(password));
        
        long result = db.insert(TABLE_USERS, null, contentValues);
        db.close();
        
        // Si result == -1, l'insertion a échoué
        return result != -1;
    }

    // Méthode pour vérifier les informations de connexion
    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_PASSWORD};
        String selection = COLUMN_EMAIL + " = ?";
        String[] selectionArgs = {email};
        
        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        
        if (cursor.moveToFirst()) {
            String storedHashedPassword = cursor.getString(0);
            cursor.close();
            db.close();
            
            // Vérifier si le mot de passe haché correspond
            return PasswordUtils.verifyPassword(password, storedHashedPassword);
        }
        
        cursor.close();
        db.close();
        return false;
    }

    // Méthode pour vérifier si un email existe déjà
    public boolean checkEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_ID};
        String selection = COLUMN_EMAIL + " = ?";
        String[] selectionArgs = {email};
        
        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        int cursorCount = cursor.getCount();
        cursor.close();
        db.close();
        
        return cursorCount > 0;
    }
}
