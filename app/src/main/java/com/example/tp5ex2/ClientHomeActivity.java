package com.example.tp5ex2;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

/**
 * Page d'accueil pour les clients/consommateurs
 * Permet de consulter, rechercher et contacter les entreprises
 */
public class ClientHomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private SessionManager sessionManager;
    private DatabaseHelper dbHelper;

    private ListView listViewCompanies;
    private EditText etSearch;
    private LinearLayout emptyState;
    private TextView tvWelcomeClient;

    private ArrayList<Company> allCompanies;
    private ArrayList<Company> filteredCompanies;
    private ClientCompanyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_home);

        sessionManager = new SessionManager(this);
        dbHelper = new DatabaseHelper(this);

        initViews();
        setupDrawer();
        loadCompanies();
        setupSearch();
    }

    private void initViews() {
        listViewCompanies = findViewById(R.id.listViewCompanies);
        etSearch = findViewById(R.id.etSearch);
        emptyState = findViewById(R.id.emptyState);
        tvWelcomeClient = findViewById(R.id.tvWelcomeClient);

        String userName = sessionManager.getUserName();
        tvWelcomeClient.setText("Bonjour, " + (userName != null ? userName : "Client") + " 👋");

        listViewCompanies.setOnItemClickListener((parent, view, position, id) -> {
            Company company = filteredCompanies.get(position);
            openCompanyDetails(company);
        });
    }

    private void setupDrawer() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);

        toolbar.setTitle("IT Companies");

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.app_name, R.string.app_name);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        
        // Cacher les options admin du menu
        hideAdminMenuItems();
        
        updateNavigationHeader();
    }

    private void hideAdminMenuItems() {
        // Cacher "Ajouter une entreprise" pour les clients
        navigationView.getMenu().findItem(R.id.nav_add_company).setVisible(false);
    }

    private void updateNavigationHeader() {
        View headerView = navigationView.getHeaderView(0);
        if (headerView != null) {
            TextView tvUserName = headerView.findViewById(R.id.nav_header_name);
            TextView tvUserEmail = headerView.findViewById(R.id.nav_header_email);

            if (tvUserName != null) {
                tvUserName.setText(sessionManager.getUserName());
            }
            if (tvUserEmail != null) {
                tvUserEmail.setText(sessionManager.getUserEmail());
            }
        }
    }

    private void loadCompanies() {
        allCompanies = new ArrayList<>();
        filteredCompanies = new ArrayList<>();

        Cursor cursor = dbHelper.getAllCompanies();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NAME));
                String desc = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DESC));
                String servicesStr = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SERVICES));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PHONE));
                String url = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_URL));
                double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LAT));
                double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LONG));

                int isFavorite = 0;
                int favIndex = cursor.getColumnIndex(DatabaseHelper.COL_FAVORITE);
                if (favIndex != -1) {
                    isFavorite = cursor.getInt(favIndex);
                }

                String imagePath = null;
                int imgIndex = cursor.getColumnIndex(DatabaseHelper.COL_IMAGE);
                if (imgIndex != -1) {
                    imagePath = cursor.getString(imgIndex);
                }

                ArrayList<String> serviceList = new ArrayList<>();
                if (servicesStr != null && !servicesStr.isEmpty()) {
                    for (String s : servicesStr.split(";")) {
                        serviceList.add(s.trim());
                    }
                }

                Company company = new Company(id, name, desc, serviceList, phone, url, 
                        latitude, longitude, imagePath, isFavorite == 1);
                allCompanies.add(company);
            }
            cursor.close();
        }

        filteredCompanies.addAll(allCompanies);
        updateListView();
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCompanies(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterCompanies(String query) {
        filteredCompanies.clear();

        if (query.isEmpty()) {
            filteredCompanies.addAll(allCompanies);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Company company : allCompanies) {
                boolean matches = company.name.toLowerCase().contains(lowerQuery) ||
                        (company.description != null && company.description.toLowerCase().contains(lowerQuery)) ||
                        company.services.toString().toLowerCase().contains(lowerQuery);
                if (matches) {
                    filteredCompanies.add(company);
                }
            }
        }

        updateListView();
    }

    private void updateListView() {
        if (filteredCompanies.isEmpty()) {
            listViewCompanies.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            listViewCompanies.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);

            adapter = new ClientCompanyAdapter(this, filteredCompanies, new ClientCompanyAdapter.OnClientActionListener() {
                @Override
                public void onCallCompany(Company company) {
                    callCompany(company);
                }

                @Override
                public void onVisitWebsite(Company company) {
                    visitWebsite(company);
                }

                @Override
                public void onEmailCompany(Company company) {
                    emailCompany(company);
                }

                @Override
                public void onToggleFavorite(Company company) {
                    toggleFavorite(company);
                }

                @Override
                public void onViewDetails(Company company) {
                    openCompanyDetails(company);
                }
            });
            listViewCompanies.setAdapter(adapter);
        }
    }

    private void openCompanyDetails(Company company) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(MainActivity2.EXTRA_IMAGE_RES, R.drawable.it1);
        intent.putExtra("COMPANY_NAME", company.name);
        intent.putStringArrayListExtra(MainActivity2.EXTRA_SERVICES, company.services);
        intent.putExtra(MainActivity2.EXTRA_PHONE, company.phone);
        intent.putExtra(MainActivity2.EXTRA_URL, company.url);
        intent.putExtra(MainActivity2.EXTRA_DESC, company.description);
        intent.putExtra("IS_CLIENT", true); // Pour masquer les boutons d'édition
        startActivity(intent);
        overridePendingTransition(R.anim.scale_up, R.anim.fade_out);
    }

    private void callCompany(Company company) {
        if (company.phone != null && !company.phone.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + company.phone));
            startActivity(intent);
        } else {
            Toast.makeText(this, "Numéro de téléphone non disponible", Toast.LENGTH_SHORT).show();
        }
    }

    private void visitWebsite(Company company) {
        if (company.url != null && !company.url.isEmpty()) {
            String url = company.url;
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url;
            }
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        } else {
            Toast.makeText(this, "Site web non disponible", Toast.LENGTH_SHORT).show();
        }
    }

    private void emailCompany(Company company) {
        // Créer un email générique basé sur le nom de l'entreprise
        String email = "contact@" + company.name.toLowerCase().replace(" ", "") + ".com";
        
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + email));
        intent.putExtra(Intent.EXTRA_SUBJECT, "Contact via IT Companies App");
        intent.putExtra(Intent.EXTRA_TEXT, "Bonjour,\n\nJe vous contacte via l'application IT Companies.\n\nCordialement.");
        
        try {
            startActivity(Intent.createChooser(intent, "Envoyer un email"));
        } catch (Exception e) {
            Toast.makeText(this, "Aucune application email trouvée", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleFavorite(Company company) {
        dbHelper.toggleFavorite(company.id);
        company.isFavorite = !company.isFavorite;
        
        String message = company.isFavorite ? 
                company.name + " ajouté aux favoris ⭐" : 
                company.name + " retiré des favoris";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (id == R.id.nav_companies) {
            // Déjà sur la page d'accueil
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (id == R.id.nav_favorites) {
            startActivity(new Intent(this, FavoritesActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
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
        String[] themeOptions = {"Thème clair ☀️", "Thème sombre 🌙", "Suivre le système 📱"};

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
                        "Découvrez les meilleures entreprises IT.\n\n" +
                        "Fonctionnalités:\n" +
                        "• Recherche en temps réel\n" +
                        "• Contact direct (appel, email, site web)\n" +
                        "• Gestion des favoris")
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
        loadCompanies();
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
