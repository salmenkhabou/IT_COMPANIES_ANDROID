package com.example.tp5ex2;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

/**
 * Classe utilitaire pour gérer la session utilisateur
 * Utilise SharedPreferences pour stocker les informations de connexion
 */
public class SessionManager {

    private static final String PREF_NAME = "ITCompaniesSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_LOGIN_TIME = "loginTime";
    private static final String KEY_THEME_MODE = "themeMode";
    private static final String KEY_NOTIFICATIONS_ENABLED = "notificationsEnabled";
    private static final String KEY_AUTO_LOGIN_ENABLED = "autoLoginEnabled";
    
    // Constantes pour le thème
    public static final int THEME_LIGHT = 0;
    public static final int THEME_DARK = 1;
    public static final int THEME_SYSTEM = 2;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    /**
     * Crée une session de connexion pour l'utilisateur
     * @param email L'email de l'utilisateur
     * @param username Le nom d'utilisateur
     */
    public void createLoginSession(String email, String username) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, username);
        editor.putLong(KEY_LOGIN_TIME, System.currentTimeMillis());
        editor.apply();
    }

    /**
     * Vérifie si l'utilisateur est connecté
     * @return true si l'utilisateur est connecté, false sinon
     */
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Récupère l'email de l'utilisateur connecté
     * @return L'email de l'utilisateur ou null si non connecté
     */
    public String getUserEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, null);
    }

    /**
     * Récupère le nom d'utilisateur connecté
     * @return Le nom d'utilisateur ou null si non connecté
     */
    public String getUserName() {
        return sharedPreferences.getString(KEY_USER_NAME, null);
    }

    /**
     * Récupère le temps de connexion
     * @return Le timestamp de connexion ou 0 si non connecté
     */
    public long getLoginTime() {
        return sharedPreferences.getLong(KEY_LOGIN_TIME, 0);
    }

    /**
     * Déconnecte l'utilisateur et efface les données de session
     */
    public void logout() {
        editor.clear();
        editor.apply();
    }

    /**
     * Vérifie si la session a expiré (optionnel: après 7 jours)
     * @return true si la session a expiré, false sinon
     */
    public boolean isSessionExpired() {
        long loginTime = getLoginTime();
        if (loginTime == 0) return true;
        
        long currentTime = System.currentTimeMillis();
        long sevenDaysInMillis = 7 * 24 * 60 * 60 * 1000L;
        
        return (currentTime - loginTime) > sevenDaysInMillis;
    }

    /**
     * Vérifie la session et retourne true si l'utilisateur peut accéder à l'app
     * @return true si la session est valide, false sinon
     */
    public boolean checkLogin() {
        if (!isLoggedIn() || isSessionExpired()) {
            logout();
            return false;
        }
        return true;
    }

    /**
     * Définit le mode de thème (clair, sombre, système)
     * @param themeMode Le mode de thème (THEME_LIGHT, THEME_DARK, THEME_SYSTEM)
     */
    public void setThemeMode(int themeMode) {
        editor.putInt(KEY_THEME_MODE, themeMode);
        editor.apply();
        applyTheme(themeMode);
    }

    /**
     * Récupère le mode de thème actuel
     * @return Le mode de thème actuel
     */
    public int getThemeMode() {
        return sharedPreferences.getInt(KEY_THEME_MODE, THEME_SYSTEM);
    }

    /**
     * Applique le thème selon le mode choisi
     * @param themeMode Le mode de thème
     */
    public void applyTheme(int themeMode) {
        switch (themeMode) {
            case THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case THEME_SYSTEM:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    /**
     * Applique le thème sauvegardé au démarrage de l'application
     */
    public void applySavedTheme() {
        applyTheme(getThemeMode());
    }

    /**
     * Active ou désactive les notifications
     * @param enabled true pour activer, false pour désactiver
     */
    public void setNotificationsEnabled(boolean enabled) {
        editor.putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled);
        editor.apply();
    }

    /**
     * Vérifie si les notifications sont activées
     * @return true si les notifications sont activées
     */
    public boolean areNotificationsEnabled() {
        return sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
    }

    /**
     * Active ou désactive la connexion automatique
     * @param enabled true pour activer, false pour désactiver
     */
    public void setAutoLoginEnabled(boolean enabled) {
        editor.putBoolean(KEY_AUTO_LOGIN_ENABLED, enabled);
        editor.apply();
    }

    /**
     * Vérifie si la connexion automatique est activée
     * @return true si la connexion automatique est activée
     */
    public boolean isAutoLoginEnabled() {
        return sharedPreferences.getBoolean(KEY_AUTO_LOGIN_ENABLED, true);
    }
}
