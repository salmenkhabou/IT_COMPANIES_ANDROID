package com.example.tp5ex2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "itcompanies.db";
    public static final int DB_VERSION = 3; // Incrémenté pour favoris

    public static final String TABLE_COMPANY = "company";
    public static final String COL_ID       = "_id";
    public static final String COL_NAME     = "name";
    public static final String COL_DESC     = "description";
    public static final String COL_SERVICES = "services";   // chaîne séparée par ;
    public static final String COL_PHONE    = "phone";    public static final String COL_URL      = "url";
    public static final String COL_LAT      = "latitude";   // updated column name
    public static final String COL_LONG     = "longitude";  // updated column name
    public static final String COL_IMAGE    = "image_path"; // image path column
    public static final String COL_FAVORITE = "is_favorite"; // colonne favoris

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {        String sql = "CREATE TABLE " + TABLE_COMPANY + " (" +
                COL_ID       + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME     + " TEXT NOT NULL, " +
                COL_DESC     + " TEXT, " +
                COL_SERVICES + " TEXT, " +
                COL_PHONE    + " TEXT, " +
                COL_URL      + " TEXT, " +
                COL_LAT      + " REAL, " +   // latitude as REAL
                COL_LONG     + " REAL, " +   // longitude as REAL
                COL_IMAGE    + " TEXT, " +   // image path as TEXT
                COL_FAVORITE + " INTEGER DEFAULT 0 " + // favoris (0=non, 1=oui)
                ");";
        db.execSQL(sql);

        // Insertion initiale des 3 entreprises
        insertCompany(db,
                "ZETA BOX",
                "Alpha IT accompagne les PME dans leurs apps mobiles, back-end Java et migration Cloud.",
                "Développement mobile;Back-end Java;Cloud",
                "+21671000111",
                "https://zeta-box.com",
                34.7400F,
                10.7600F
        );

        insertCompany(db,
                "SOFTTODO",
                "Beta Data est spécialisée en IA/ML, pipelines de données et plateformes DevOps.",
                "IA/ML;Data Engineering;DevOps",
                "+21671000222",
                "https://www.softtodo.com/en",
                34.7707F,
                10.6929F
        );

        insertCompany(db,
                "Next-IT",
                "Gamma Secure propose du pentest, audit et supervision 24/7 via un SOC managé.",
                "Cybersécurité;Pentest;SOC",
                "+21671000333",
                "https://www.nextit-tn.com",
                34.7398F,
                10.7600F
        );
    }    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Migration pour ajouter la colonne favoris
        if (oldVersion < 3) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_COMPANY + " ADD COLUMN " + COL_FAVORITE + " INTEGER DEFAULT 0");
            } catch (Exception e) {
                // Si la colonne existe déjà, ignorer l'erreur
            }
        }
    }

    // Méthodes CRUD
    private void insertCompany(SQLiteDatabase db,
                               String name,
                               String desc,
                               String services,
                               String phone,
                               String url,
                               float latitude,
                               float longitude) {
        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_DESC, desc);
        values.put(COL_SERVICES, services);
        values.put(COL_PHONE, phone);
        values.put(COL_URL, url);
        values.put(COL_LAT, latitude);
        values.put(COL_LONG, longitude);
        db.insert(TABLE_COMPANY, null, values);
    }
    public long insertCompany(String name,
                              String desc,
                              String services,
                              String phone,
                              String url,
                              float latitude,
                              float longitude) {

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_DESC, desc);
        values.put(COL_SERVICES, services);
        values.put(COL_PHONE, phone);
        values.put(COL_URL, url);
        values.put(COL_LAT, latitude);
        values.put(COL_LONG, longitude);
        return db.insert(TABLE_COMPANY, null, values);
    }
    public Cursor getAllCompanies() {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(
                TABLE_COMPANY,
                null,   // toutes les colonnes
                null,
                null,
                null,
                null,
                COL_NAME + " ASC"
        );
    }
    // ---------- SELECT by name ----------
    public Cursor getCompanyByName(String name) {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(
                TABLE_COMPANY,
                null,
                COL_NAME + " = ?",
                new String[]{ name },
                null,
                null,
                null
        );
    }

    // ---------- UPDATE (par nom) ----------
    public int updateCompanyByName(String oldName,
                                   String newName,
                                   String desc,
                                   String services,
                                   String phone,
                                   String url,
                                   float latitude,
                                   float longitude) {

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, newName);
        values.put(COL_DESC, desc);
        values.put(COL_SERVICES, services);
        values.put(COL_PHONE, phone);
        values.put(COL_URL, url);
        values.put(COL_LAT, latitude);
        values.put(COL_LONG, longitude);

        return db.update(TABLE_COMPANY, values,
                COL_NAME + " = ?",
                new String[]{ oldName });
    }

    // ---------- DELETE (par nom) ----------
    public int deleteCompanyByName(String name) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_COMPANY,
                COL_NAME + " = ?",
                new String[]{ name });
    }

    // Méthode pour supprimer une entreprise
    public boolean deleteCompany(long companyId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_COMPANY, COL_ID + " = ?", new String[]{String.valueOf(companyId)});
        db.close();
        return result > 0;
    }

    // Méthode pour mettre à jour une entreprise
    public boolean updateCompany(long companyId, String name, String description, 
                                String services, String phone, String url, 
                                double latitude, double longitude) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_DESC, description);
        values.put(COL_SERVICES, services);
        values.put(COL_PHONE, phone);
        values.put(COL_URL, url);
        values.put(COL_LAT, latitude);
        values.put(COL_LONG, longitude);

        int result = db.update(TABLE_COMPANY, values, COL_ID + " = ?", 
                              new String[]{String.valueOf(companyId)});
        db.close();
        return result > 0;
    }    // Méthode pour ajouter une nouvelle entreprise
    public boolean addCompany(String name, String description, String services, 
                             String phone, String url, double latitude, double longitude) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_DESC, description);
        values.put(COL_SERVICES, services);
        values.put(COL_PHONE, phone);
        values.put(COL_URL, url);
        values.put(COL_LAT, latitude);
        values.put(COL_LONG, longitude);

        long result = db.insert(TABLE_COMPANY, null, values);
        db.close();
        return result != -1;
    }

    // Méthode pour récupérer une entreprise par ID
    public Cursor getCompanyById(long companyId) {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(
                TABLE_COMPANY,
                null,
                COL_ID + " = ?",
                new String[]{String.valueOf(companyId)},
                null,
                null,
                null
        );
    }

    // Méthode pour ajouter une nouvelle entreprise avec image
    public long addCompanyWithImage(String name, String description, String services, 
                                   String phone, String url, double latitude, double longitude, String imagePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_DESC, description);
        values.put(COL_SERVICES, services);
        values.put(COL_PHONE, phone);
        values.put(COL_URL, url);
        values.put(COL_LAT, latitude);
        values.put(COL_LONG, longitude);
        values.put(COL_IMAGE, imagePath);

        long result = db.insert(TABLE_COMPANY, null, values);
        db.close();
        return result;
    }

    // Méthode pour mettre à jour une entreprise avec image
    public boolean updateCompanyWithImage(long companyId, String name, String description, 
                                         String services, String phone, String url, 
                                         double latitude, double longitude, String imagePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_DESC, description);
        values.put(COL_SERVICES, services);
        values.put(COL_PHONE, phone);
        values.put(COL_URL, url);
        values.put(COL_LAT, latitude);
        values.put(COL_LONG, longitude);
        values.put(COL_IMAGE, imagePath);        int result = db.update(TABLE_COMPANY, values, COL_ID + " = ?", 
                              new String[]{String.valueOf(companyId)});
        db.close();
        return result > 0;
    }

    // Méthode pour rechercher des entreprises par nom
    public Cursor searchCompaniesByName(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        String searchQuery = "SELECT * FROM " + TABLE_COMPANY + 
                            " WHERE " + COL_NAME + " LIKE ? OR " + 
                            COL_DESC + " LIKE ? OR " +
                            COL_SERVICES + " LIKE ? ORDER BY " + COL_NAME;
        return db.rawQuery(searchQuery, new String[]{"%" + query + "%", "%" + query + "%", "%" + query + "%"});
    }    // Méthode pour filtrer par service
    public Cursor getCompaniesByService(String service) {
        SQLiteDatabase db = this.getReadableDatabase();
        String searchQuery = "SELECT * FROM " + TABLE_COMPANY + 
                            " WHERE " + COL_SERVICES + " LIKE ? ORDER BY " + COL_NAME;
        return db.rawQuery(searchQuery, new String[]{"%" + service + "%"});
    }

    // ==================== MÉTHODES FAVORIS ====================

    /**
     * Bascule l'état favori d'une entreprise
     * @param companyId L'ID de l'entreprise
     * @return true si l'entreprise est maintenant favorite, false sinon
     */
    public boolean toggleFavorite(long companyId) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        // D'abord, récupérer l'état actuel
        Cursor cursor = db.query(TABLE_COMPANY, new String[]{COL_FAVORITE}, 
                COL_ID + " = ?", new String[]{String.valueOf(companyId)}, 
                null, null, null);
        
        boolean isFavorite = false;
        if (cursor.moveToFirst()) {
            int favoriteIndex = cursor.getColumnIndex(COL_FAVORITE);
            if (favoriteIndex != -1) {
                isFavorite = cursor.getInt(favoriteIndex) == 1;
            }
        }
        cursor.close();
        
        // Inverser l'état
        ContentValues values = new ContentValues();
        values.put(COL_FAVORITE, isFavorite ? 0 : 1);
        db.update(TABLE_COMPANY, values, COL_ID + " = ?", new String[]{String.valueOf(companyId)});
        db.close();
        
        return !isFavorite; // Retourne le nouvel état
    }

    /**
     * Vérifie si une entreprise est favorite
     * @param companyId L'ID de l'entreprise
     * @return true si favorite, false sinon
     */
    public boolean isFavorite(long companyId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_COMPANY, new String[]{COL_FAVORITE}, 
                COL_ID + " = ?", new String[]{String.valueOf(companyId)}, 
                null, null, null);
        
        boolean isFavorite = false;
        if (cursor.moveToFirst()) {
            int favoriteIndex = cursor.getColumnIndex(COL_FAVORITE);
            if (favoriteIndex != -1) {
                isFavorite = cursor.getInt(favoriteIndex) == 1;
            }
        }
        cursor.close();
        db.close();
        return isFavorite;
    }

    /**
     * Récupère toutes les entreprises favorites
     * @return Cursor contenant les entreprises favorites
     */
    public Cursor getFavoriteCompanies() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_COMPANY, null, 
                COL_FAVORITE + " = 1", null, 
                null, null, COL_NAME);
    }

    /**
     * Définit l'état favori d'une entreprise
     * @param companyId L'ID de l'entreprise
     * @param isFavorite true pour marquer comme favori, false sinon
     */
    public void setFavorite(long companyId, boolean isFavorite) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_FAVORITE, isFavorite ? 1 : 0);
        db.update(TABLE_COMPANY, values, COL_ID + " = ?", new String[]{String.valueOf(companyId)});
        db.close();
    }
}
