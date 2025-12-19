package com.example.tp5ex2;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

/**
 * Tableau de bord Administrateur
 * Permet la gestion complète des entreprises (CRUD)
 */
public class AdminDashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private SessionManager sessionManager;
    private DatabaseHelper dbHelper;
    
    private TextView tvTotalCompanies;
    private TextView tvTotalFavorites;
    private TextView tvWelcomeAdmin;
    private MaterialCardView cardManageCompanies;
    private MaterialCardView cardAddCompany;
    private MaterialCardView cardViewStats;
    private MaterialCardView cardSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        sessionManager = new SessionManager(this);
        dbHelper = new DatabaseHelper(this);

        // Vérifier que l'utilisateur est bien admin
        if (!sessionManager.isAdmin()) {
            Toast.makeText(this, "Accès non autorisé", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupDrawer();
        loadDashboardData();
        setupClickListeners();
    }

    private void initViews() {
        tvTotalCompanies = findViewById(R.id.tvTotalCompanies);
        tvTotalFavorites = findViewById(R.id.tvTotalFavorites);
        tvWelcomeAdmin = findViewById(R.id.tvWelcomeAdmin);
        cardManageCompanies = findViewById(R.id.cardManageCompanies);
        cardAddCompany = findViewById(R.id.cardAddCompany);
        cardViewStats = findViewById(R.id.cardViewStats);
        cardSettings = findViewById(R.id.cardSettings);
        
        FloatingActionButton fabAdd = findViewById(R.id.fabAddCompany);
        fabAdd.setOnClickListener(v -> openAddCompany());
    }

    private void setupDrawer() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);

        toolbar.setTitle("Admin Dashboard");

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.app_name, R.string.app_name);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        updateNavigationHeader();
    }

    private void updateNavigationHeader() {
        View headerView = navigationView.getHeaderView(0);
        if (headerView != null) {
            TextView tvUserName = headerView.findViewById(R.id.nav_header_name);
            TextView tvUserEmail = headerView.findViewById(R.id.nav_header_email);

            if (tvUserName != null) {
                tvUserName.setText(sessionManager.getUserName() + " (Admin)");
            }
            if (tvUserEmail != null) {
                tvUserEmail.setText(sessionManager.getUserEmail());
            }
        }
    }

    private void loadDashboardData() {
        // Message de bienvenue
        String adminName = sessionManager.getUserName();
        tvWelcomeAdmin.setText("Bienvenue, " + (adminName != null ? adminName : "Admin") + " 👋");

        // Compter les entreprises
        Cursor cursor = dbHelper.getAllCompanies();
        int totalCompanies = cursor != null ? cursor.getCount() : 0;
        if (cursor != null) cursor.close();
        tvTotalCompanies.setText(String.valueOf(totalCompanies));

        // Compter les favoris
        Cursor favCursor = dbHelper.getFavoriteCompanies();
        int totalFavorites = favCursor != null ? favCursor.getCount() : 0;
        if (favCursor != null) favCursor.close();
        tvTotalFavorites.setText(String.valueOf(totalFavorites));
    }

    private void setupClickListeners() {
        // Gérer les entreprises
        cardManageCompanies.setOnClickListener(v -> {
            Intent intent = new Intent(this, CompaniesActivity.class);
            intent.putExtra("IS_ADMIN", true);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // Ajouter une entreprise
        cardAddCompany.setOnClickListener(v -> openAddCompany());

        // Voir les statistiques
        cardViewStats.setOnClickListener(v -> showStatsDialog());

        // Paramètres
        cardSettings.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    private void openAddCompany() {
        Intent intent = new Intent(this, AddUpdateCompanyActivity.class);
        intent.putExtra("MODE", "ADD");
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void showStatsDialog() {
        Cursor cursor = dbHelper.getAllCompanies();
        int total = cursor != null ? cursor.getCount() : 0;
        if (cursor != null) cursor.close();

        Cursor favCursor = dbHelper.getFavoriteCompanies();
        int favorites = favCursor != null ? favCursor.getCount() : 0;
        if (favCursor != null) favCursor.close();

        String stats = " Statistiques de l'application\n\n" +
                "• Total entreprises: " + total + "\n" +
                "• Entreprises favorites: " + favorites + "\n" +
                "• Utilisateur connecté: " + sessionManager.getUserName() + "\n" +
                "• Rôle: Administrateur";

        new MaterialAlertDialogBuilder(this)
                .setTitle("Statistiques")
                .setMessage(stats)
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (id == R.id.nav_companies) {
            Intent intent = new Intent(this, CompaniesActivity.class);
            intent.putExtra("IS_ADMIN", true);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } else if (id == R.id.nav_favorites) {
            startActivity(new Intent(this, FavoritesActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } else if (id == R.id.nav_add_company) {
            openAddCompany();
        } else if (id == R.id.nav_notifications) {
            startActivity(new Intent(this, NotificationsActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } else if (id == R.id.nav_theme) {
            showThemeDialog();
        } else if (id == R.id.nav_about) {
            showAboutDialog();
        } else if (id == R.id.nav_logout) {
            logout();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showThemeDialog() {
        int currentTheme = sessionManager.getThemeMode();
        String[] themeOptions = {"Thème clair ", "Thème sombre ", "Suivre le système "};

        new MaterialAlertDialogBuilder(this)
                .setTitle("Choisir le thème")
                .setSingleChoiceItems(themeOptions, currentTheme, (dialog, which) -> {
                    sessionManager.setThemeMode(which);
                    dialog.dismiss();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void showAboutDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("À propos")
                .setMessage("IT Companies Manager\nVersion 1.0\n\n" +
                        "Application de gestion d'entreprises IT\n" +
                        "Mode: Administrateur\n\n" +
                        "Fonctionnalités Admin:\n" +
                        "• Gestion CRUD des entreprises\n" +
                        "• Statistiques\n" +
                        "• Configuration")
                .setPositiveButton("OK", null)
                .show();
    }

    private void logout() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Déconnexion")
                .setMessage("Êtes-vous sûr de vouloir vous déconnecter ?")
                .setPositiveButton("Déconnexion", (dialog, which) -> {
                    sessionManager.logout();
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
