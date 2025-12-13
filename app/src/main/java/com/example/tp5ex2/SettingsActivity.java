package com.example.tp5ex2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Activité de paramètres et configuration de l'application
 */
public class SettingsActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private SwitchMaterial switchNotifications;
    private SwitchMaterial switchDarkMode;
    private SwitchMaterial switchAutoLogin;
    private TextView tvCurrentUser;
    private TextView tvCurrentEmail;
    private MaterialButton btnChangePassword;
    private MaterialButton btnClearCache;
    private MaterialButton btnExportData;
    private MaterialButton btnDeleteAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sessionManager = new SessionManager(this);

        initViews();
        setupToolbar();
        loadSettings();
        setupListeners();
    }

    private void initViews() {
        switchNotifications = findViewById(R.id.switchNotifications);
        switchDarkMode = findViewById(R.id.switchDarkMode);
        switchAutoLogin = findViewById(R.id.switchAutoLogin);
        tvCurrentUser = findViewById(R.id.tvCurrentUser);
        tvCurrentEmail = findViewById(R.id.tvCurrentEmail);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnClearCache = findViewById(R.id.btnClearCache);
        btnExportData = findViewById(R.id.btnExportData);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setTitle("Paramètres");
        toolbar.setNavigationOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
    }

    private void loadSettings() {
        // Charger les informations utilisateur
        String userName = sessionManager.getUserName();
        String userEmail = sessionManager.getUserEmail();
        
        tvCurrentUser.setText(userName != null ? userName : "Utilisateur");
        tvCurrentEmail.setText(userEmail != null ? userEmail : "email@example.com");
        
        // Charger les préférences
        switchNotifications.setChecked(sessionManager.areNotificationsEnabled());
        switchDarkMode.setChecked(sessionManager.getThemeMode() == SessionManager.THEME_DARK);
        switchAutoLogin.setChecked(sessionManager.isAutoLoginEnabled());
    }

    private void setupListeners() {
        // Switch Notifications
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sessionManager.setNotificationsEnabled(isChecked);
            String message = isChecked ? "Notifications activées" : "Notifications désactivées";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });

        // Switch Dark Mode
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int themeMode = isChecked ? SessionManager.THEME_DARK : SessionManager.THEME_LIGHT;
            sessionManager.setThemeMode(themeMode);
        });

        // Switch Auto Login
        switchAutoLogin.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sessionManager.setAutoLoginEnabled(isChecked);
            String message = isChecked ? "Connexion automatique activée" : "Connexion automatique désactivée";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });

        // Bouton Changer le mot de passe
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        // Bouton Vider le cache
        btnClearCache.setOnClickListener(v -> clearCache());

        // Bouton Exporter les données
        btnExportData.setOnClickListener(v -> exportData());

        // Bouton Supprimer le compte
        btnDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());
    }

    private void showChangePasswordDialog() {
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        
        TextInputEditText etCurrentPassword = dialogView.findViewById(R.id.etCurrentPassword);
        TextInputEditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        TextInputEditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Changer le mot de passe")
                .setView(dialogView)
                .setPositiveButton("Confirmer", (dialog, which) -> {
                    String currentPwd = etCurrentPassword.getText().toString();
                    String newPwd = etNewPassword.getText().toString();
                    String confirmPwd = etConfirmPassword.getText().toString();
                    
                    if (currentPwd.isEmpty() || newPwd.isEmpty() || confirmPwd.isEmpty()) {
                        Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    if (!newPwd.equals(confirmPwd)) {
                        Toast.makeText(this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    String passwordError = PasswordUtils.getPasswordError(newPwd);
                    if (passwordError != null) {
                        Toast.makeText(this, passwordError, Toast.LENGTH_LONG).show();
                        return;
                    }
                    
                    // TODO: Implémenter le changement de mot de passe dans la BD
                    Toast.makeText(this, "Mot de passe changé avec succès", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void clearCache() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Vider le cache")
                .setMessage("Êtes-vous sûr de vouloir vider le cache de l'application ?")
                .setPositiveButton("Vider", (dialog, which) -> {
                    try {
                        // Vider le cache de l'application
                        deleteCache();
                        Toast.makeText(this, "Cache vidé avec succès", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(this, "Erreur lors du vidage du cache", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void deleteCache() {
        try {
            java.io.File cacheDir = getCacheDir();
            if (cacheDir != null && cacheDir.isDirectory()) {
                deleteDir(cacheDir);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean deleteDir(java.io.File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    boolean success = deleteDir(new java.io.File(dir, child));
                    if (!success) {
                        return false;
                    }
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        }
        return false;
    }

    private void exportData() {
        Toast.makeText(this, "Export des données en cours...", Toast.LENGTH_SHORT).show();
        
        // Créer un fichier JSON avec les données utilisateur
        try {
            StringBuilder data = new StringBuilder();
            data.append("{\n");
            data.append("  \"user\": \"").append(sessionManager.getUserName()).append("\",\n");
            data.append("  \"email\": \"").append(sessionManager.getUserEmail()).append("\",\n");
            data.append("  \"exportDate\": \"").append(java.time.LocalDateTime.now()).append("\"\n");
            data.append("}");
            
            // Partager les données
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Export de mes données IT Companies");
            shareIntent.putExtra(Intent.EXTRA_TEXT, data.toString());
            startActivity(Intent.createChooser(shareIntent, "Exporter via"));
            
        } catch (Exception e) {
            Toast.makeText(this, "Erreur lors de l'export", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteAccountDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("⚠️ Supprimer le compte")
                .setMessage("Cette action est irréversible. Toutes vos données seront supprimées définitivement.\n\nÊtes-vous sûr de vouloir continuer ?")
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    // TODO: Implémenter la suppression du compte dans la BD
                    sessionManager.logout();
                    Toast.makeText(this, "Compte supprimé", Toast.LENGTH_SHORT).show();
                    
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
