package com.example.tp5ex2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity2 extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final String EXTRA_IMAGE_RES = "EXTRA_IMAGE_RES";
    public static final String EXTRA_SERVICES  = "EXTRA_SERVICES";
    public static final String EXTRA_PHONE     = "EXTRA_PHONE";
    public static final String EXTRA_URL       = "EXTRA_URL";
    public static final String EXTRA_USERNAME  = "EXTRA_USERNAME";
    public static final String EXTRA_DESC = "EXTRA_DESC";
    public static final double EXTRA_LONG = 0.0;  // Change to double
    public static final double EXTRA_LAT = 0.0;  // Change to double

    private DatabaseHelper dbHelper;
    private ArrayList<Company> companies = new ArrayList<>();
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        
        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        
        loadCompaniesFromDb();
        setupCards();
        setupDrawer();

        // Barre de navigation : afficher le nom utilisateur
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        String username = getIntent().getStringExtra(EXTRA_USERNAME);
        if (username == null || username.trim().isEmpty()) username = "COMPANIES";
        toolbar.setTitle(username);

        // Couleur barre de statut
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window w = getWindow();
            w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            w.setStatusBarColor(getColor(R.color.bleu));
        }

        // Cartes cliquables
        MaterialCardView cardAlpha = findViewById(R.id.cardAlpha);
        MaterialCardView cardBeta  = findViewById(R.id.cardBeta);
        MaterialCardView cardGamma = findViewById(R.id.cardGamma);

        cardAlpha.setOnClickListener(v -> {
            Company company = companies.get(0);
            openDetails(company);
        });

        cardBeta.setOnClickListener(v -> {
            Company company = companies.get(1);
            openDetails(company);
        });

        cardGamma.setOnClickListener(v -> {
            Company company = companies.get(2);
            openDetails(company);
        });

        // Configuration des boutons Update/Delete
        setupButtons();

        // Configuration du FloatingActionButton
        FloatingActionButton fabAddCompany = findViewById(R.id.fabAddCompany);
        fabAddCompany.setOnClickListener(v -> addNewCompany());
    }

    private void setupButtons() {
        // Boutons Alpha
        MaterialButton btnAlphaUpdate = findViewById(R.id.btnAlphaUpdate);
        MaterialButton btnAlphaDelete = findViewById(R.id.btnAlphaDelete);
        
        // Boutons Beta
        MaterialButton btnBetaUpdate = findViewById(R.id.btnBetaUpdate);
        MaterialButton btnBetaDelete = findViewById(R.id.btnBetaDelete);
        
        // Boutons Gamma
        MaterialButton btnGammaUpdate = findViewById(R.id.btnGammaUpdate);
        MaterialButton btnGammaDelete = findViewById(R.id.btnGammaDelete);

        // Sécurité : vérifier qu'on a au moins 3 entrées
        if (companies.size() < 3) return;

        // Listeners pour Alpha
        btnAlphaUpdate.setOnClickListener(v -> launchUpdateActivity(companies.get(0)));
        btnAlphaDelete.setOnClickListener(v -> deleteCompany(0, companies.get(0)));

        // Listeners pour Beta  
        btnBetaUpdate.setOnClickListener(v -> launchUpdateActivity(companies.get(1)));
        btnBetaDelete.setOnClickListener(v -> deleteCompany(1, companies.get(1)));

        // Listeners pour Gamma
        btnGammaUpdate.setOnClickListener(v -> launchUpdateActivity(companies.get(2)));
        btnGammaDelete.setOnClickListener(v -> deleteCompany(2, companies.get(2)));
    }

    private void setupDrawer() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);

        // Configuration du toggle pour le drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.app_name, R.string.app_name);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Listener pour les éléments du menu
        navigationView.setNavigationItemSelectedListener(this);

        // Mettre à jour le header avec les infos utilisateur
        updateNavigationHeader();
    }

    private void updateNavigationHeader() {
        View headerView = navigationView.getHeaderView(0);
        if (headerView != null) {
            TextView tvUserName = headerView.findViewById(R.id.nav_header_name);
            TextView tvUserEmail = headerView.findViewById(R.id.nav_header_email);
            
            if (tvUserName != null) {
                String userName = sessionManager.getUserName();
                tvUserName.setText(userName != null ? userName : "Utilisateur");
            }
            if (tvUserEmail != null) {
                String userEmail = sessionManager.getUserEmail();
                tvUserEmail.setText(userEmail != null ? userEmail : "email@example.com");
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Déjà sur l'accueil, fermer le drawer
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (id == R.id.nav_companies) {
            startActivity(new Intent(this, CompaniesActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } else if (id == R.id.nav_favorites) {
            startActivity(new Intent(this, FavoritesActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } else if (id == R.id.nav_add_company) {
            addNewCompany();
        } else if (id == R.id.nav_notifications) {
            startActivity(new Intent(this, NotificationsActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } else if (id == R.id.nav_theme) {
            showThemeDialog();
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(this, SettingsActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } else if (id == R.id.nav_about) {
            showAboutDialog();
        } else if (id == R.id.nav_logout) {
            logout();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void loadCompaniesFromDb() {
        companies.clear();
        Cursor c = dbHelper.getAllCompanies();
        if (c != null) {
            while (c.moveToNext()) {
                long id   = c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_ID));
                String name = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_NAME));
                String desc = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_DESC));
                String servicesStr = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_SERVICES));
                String phone = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PHONE));
                String url   = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_URL));
                double latitude = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COL_LAT));
                double longitude = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COL_LONG));
                
                // Récupérer le chemin de l'image (peut être null)
                String imagePath = null;
                int imageColumnIndex = c.getColumnIndex(DatabaseHelper.COL_IMAGE);
                if (imageColumnIndex != -1) {
                    imagePath = c.getString(imageColumnIndex);
                }

                ArrayList<String> serviceList = new ArrayList<>();
                if (servicesStr != null && !servicesStr.isEmpty()) {
                    for (String s : servicesStr.split(";")) {
                        serviceList.add(s.trim());
                    }
                }

                companies.add(new Company(id, name, desc, serviceList, phone, url, latitude, longitude, imagePath));
            }
            c.close();
        }
    }

    private void setupCards() {
        MaterialCardView cardAlpha = findViewById(R.id.cardAlpha);
        MaterialCardView cardBeta  = findViewById(R.id.cardBeta);
        MaterialCardView cardGamma = findViewById(R.id.cardGamma);

        // Sécurité : vérifier qu'on a au moins 3 entrées
        if (companies.size() < 3) return;

        bindCard(cardAlpha, companies.get(0));
        bindCard(cardBeta,  companies.get(1));
        bindCard(cardGamma, companies.get(2));
    }

    private void bindCard(MaterialCardView card, Company company) {
        // Find TextViews dynamically based on the card's child views
        TextView tvTitle = null;
        TextView tvSubtitle = null;
        
        // Get the LinearLayout inside the card
        if (card.getChildCount() > 0 && card.getChildAt(0) instanceof LinearLayout) {
            LinearLayout cardLayout = (LinearLayout) card.getChildAt(0);
            if (cardLayout.getChildCount() > 1 && cardLayout.getChildAt(1) instanceof LinearLayout) {
                LinearLayout textLayout = (LinearLayout) cardLayout.getChildAt(1);
                if (textLayout.getChildCount() >= 2) {
                    tvTitle = (TextView) textLayout.getChildAt(0);
                    tvSubtitle = (TextView) textLayout.getChildAt(1);
                }
            }
        }
        
        if (tvTitle != null && tvSubtitle != null) {
            tvTitle.setText(company.name);
            tvSubtitle.setText(String.join(" • ", company.services));
        }

        card.setOnClickListener(v -> openDetails(company));
    }

    private void openDetails(Company company) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(EXTRA_IMAGE_RES, R.drawable.it1); // or a column image if added in the DB
        intent.putExtra("COMPANY_NAME", company.name); // Pour l'export PDF
        intent.putStringArrayListExtra(EXTRA_SERVICES, company.services);
        intent.putExtra(EXTRA_PHONE, company.phone);
        intent.putExtra(EXTRA_URL, company.url);
        intent.putExtra(EXTRA_DESC, company.description);
        intent.putExtra(String.valueOf(EXTRA_LAT), company.latitude);
        intent.putExtra(String.valueOf(EXTRA_LONG), company.longitude);
        startActivity(intent);
        overridePendingTransition(R.anim.scale_up, R.anim.fade_out);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_companies_list) {
            // Naviguer vers l'activité CompaniesActivity
            Intent intent = new Intent(this, CompaniesActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            return true;
        } else if (id == R.id.action_about) {
            // Action pour "À propos" 
            showAboutDialog();
            return true;
        } else if (id == R.id.action_logout) {
            // Déconnexion
            logout();
            return true;
        } else if (id == R.id.action_theme) {
            // Changer le thème
            showThemeDialog();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void showThemeDialog() {
        SessionManager sessionManager = new SessionManager(this);
        int currentTheme = sessionManager.getThemeMode();
        
        String[] themeOptions = {"Thème clair ☀️", "Thème sombre 🌙", "Suivre le système 📱"};
        
        new MaterialAlertDialogBuilder(this)
                .setTitle("Choisir le thème")
                .setSingleChoiceItems(themeOptions, currentTheme, (dialog, which) -> {
                    sessionManager.setThemeMode(which);
                    dialog.dismiss();
                    Toast.makeText(this, "Thème appliqué", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }
    
    private void showAboutDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("À propos")
                .setMessage("IT Companies Manager\nVersion 1.0\n\nApplication de gestion d'entreprises IT avec authentification sécurisée et gestion d'images.")
                .setPositiveButton("OK", null)
                .show();
    }
    
    private void logout() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Déconnexion")
                .setMessage("Êtes-vous sûr de vouloir vous déconnecter ?")
                .setPositiveButton("Déconnexion", (dialog, which) -> {
                    SessionManager sessionManager = new SessionManager(this);
                    sessionManager.logout();
                    
                    Toast.makeText(this, "Déconnexion réussie", Toast.LENGTH_SHORT).show();
                    
                    // Retourner à l'écran de connexion
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
        // Recharger les données au cas où elles auraient été modifiées
        loadCompaniesFromDb();
        setupCards();
        setupButtons();
    }

    private void deleteCompany(int position, Company company) {
        // Afficher une boîte de dialogue de confirmation
        new MaterialAlertDialogBuilder(this)
                .setTitle("Supprimer l'entreprise")
                .setMessage("Êtes-vous sûr de vouloir supprimer \"" + company.name + "\" ?")
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    // Supprimer de la base de données
                    boolean success = dbHelper.deleteCompany(company.id);
                    if (success) {
                        Toast.makeText(this, "Entreprise supprimée avec succès", Toast.LENGTH_SHORT).show();
                        // Recharger les données et l'interface
                        loadCompaniesFromDb();
                        setupCards();
                        setupButtons();
                    } else {
                        Toast.makeText(this, "Erreur lors de la suppression", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void launchUpdateActivity(Company company) {
        // Lancer AddUpdateCompanyActivity en mode UPDATE
        Intent intent = new Intent(this, AddUpdateCompanyActivity.class);
        intent.putExtra("MODE", "UPDATE");
        intent.putExtra("COMPANY_ID", company.id);
        intent.putExtra("COMPANY_NAME", company.name);
        intent.putExtra("COMPANY_DESC", company.description);
        intent.putExtra("COMPANY_SERVICES", String.join(";", company.services));
        intent.putExtra("COMPANY_PHONE", company.phone);
        intent.putExtra("COMPANY_URL", company.url);
        intent.putExtra("COMPANY_LAT", company.latitude);
        intent.putExtra("COMPANY_LONG", company.longitude);
        if (company.imagePath != null) {
            intent.putExtra("COMPANY_IMAGE", company.imagePath);
        }
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void addNewCompany() {
        // Lancer AddUpdateCompanyActivity en mode ADD
        Intent intent = new Intent(this, AddUpdateCompanyActivity.class);
        intent.putExtra("MODE", "ADD");
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}