package com.example.tp5ex2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UserDatabaseHelper extends SQLiteOpenHelper {

    // Constantes pour la base de données
    private static final String DATABASE_NAME = "users.db";
    private static final int DATABASE_VERSION = 2; // Version augmentée pour la migration
    
    // Nom de la table et colonnes
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_ROLE = "role"; // Nouveau : admin ou client
    
    // Constantes pour les rôles
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_CLIENT = "client";

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
                COLUMN_PASSWORD + " TEXT, " +
                COLUMN_ROLE + " TEXT DEFAULT '" + ROLE_CLIENT + "'" + ")";
        db.execSQL(createTable);
        
        // Créer un compte admin par défaut
        ContentValues adminValues = new ContentValues();
        adminValues.put(COLUMN_EMAIL, "admin@itcompanies.com");
        adminValues.put(COLUMN_USERNAME, "Admin");
        adminValues.put(COLUMN_PASSWORD, PasswordUtils.hashPassword("Admin@123"));
        adminValues.put(COLUMN_ROLE, ROLE_ADMIN);
        db.insert(TABLE_USERS, null, adminValues);
    }

    // Méthode appelée lors de la mise à jour de la base de données
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Ajouter la colonne role si elle n'existe pas
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_ROLE + " TEXT DEFAULT '" + ROLE_CLIENT + "'");
            
            // Créer un compte admin par défaut si pas déjà existant
            Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_EMAIL + " = ?", 
                    new String[]{"admin@itcompanies.com"});
            if (!cursor.moveToFirst()) {
                ContentValues adminValues = new ContentValues();
                adminValues.put(COLUMN_EMAIL, "admin@itcompanies.com");
                adminValues.put(COLUMN_USERNAME, "Admin");
                adminValues.put(COLUMN_PASSWORD, PasswordUtils.hashPassword("Admin@123"));
                adminValues.put(COLUMN_ROLE, ROLE_ADMIN);
                db.insert(TABLE_USERS, null, adminValues);
            }
            cursor.close();
        }
    }

    // Méthode pour ajouter un utilisateur (par défaut client)
    public boolean addUser(String email, String username, String password) {
        return addUser(email, username, password, ROLE_CLIENT);
    }
    
    // Méthode pour ajouter un utilisateur avec un rôle spécifique
    public boolean addUser(String email, String username, String password, String role) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_EMAIL, email);
        contentValues.put(COLUMN_USERNAME, username);
        contentValues.put(COLUMN_PASSWORD, PasswordUtils.hashPassword(password));
        contentValues.put(COLUMN_ROLE, role);
        
        long result = db.insert(TABLE_USERS, null, contentValues);
        db.close();
        
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
            
            return PasswordUtils.verifyPassword(password, storedHashedPassword);
        }
        
        cursor.close();
        db.close();
        return false;
    }
    
    // Méthode pour récupérer le rôle d'un utilisateur
    public String getUserRole(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_ROLE};
        String selection = COLUMN_EMAIL + " = ?";
        String[] selectionArgs = {email};
        
        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        
        String role = ROLE_CLIENT; // Par défaut
        if (cursor.moveToFirst()) {
            role = cursor.getString(0);
            if (role == null) role = ROLE_CLIENT;
        }
        cursor.close();
        db.close();
        
        return role;
    }
    
    // Méthode pour récupérer le nom d'utilisateur
    public String getUsername(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USERNAME};
        String selection = COLUMN_EMAIL + " = ?";
        String[] selectionArgs = {email};
        
        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        
        String username = "";
        if (cursor.moveToFirst()) {
            username = cursor.getString(0);
        }
        cursor.close();
        db.close();
        
        return username;
    }
    
    // Vérifier si un utilisateur est admin
    public boolean isAdmin(String email) {
        return ROLE_ADMIN.equals(getUserRole(email));
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
    
    // Mettre à jour le mot de passe
    public boolean updatePassword(String email, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PASSWORD, PasswordUtils.hashPassword(newPassword));
        
        int rows = db.update(TABLE_USERS, values, COLUMN_EMAIL + " = ?", new String[]{email});
        db.close();
        
        return rows > 0;
    }
    
    // Supprimer un compte utilisateur
    public boolean deleteUser(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_USERS, COLUMN_EMAIL + " = ?", new String[]{email});
        db.close();
        
        return rows > 0;
    }
}
