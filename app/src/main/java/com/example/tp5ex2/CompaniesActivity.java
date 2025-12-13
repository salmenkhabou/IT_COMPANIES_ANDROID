package com.example.tp5ex2;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;

public class CompaniesActivity extends AppCompatActivity implements CompanyAdapter.OnCompanyActionListener {    private ListView listViewCompanies;
    private CompanyAdapter companyAdapter;
    private DatabaseHelper dbHelper;
    private ArrayList<Company> companiesList;
    private ArrayList<Company> allCompaniesList; // Liste complète pour la recherche
    private FloatingActionButton fabAddCompany;
    private TextInputEditText etSearch;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_companies);

        // Initialiser les vues
        initViews();

        // Configurer la toolbar
        setupToolbar();

        // Initialiser la base de données
        dbHelper = new DatabaseHelper(this);

        // Charger les données et configurer l'adaptateur
        loadCompaniesData();
    }    private void initViews() {
        listViewCompanies = findViewById(R.id.listViewCompanies);
        fabAddCompany = findViewById(R.id.fabAddCompany);
        etSearch = findViewById(R.id.etSearch);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        companiesList = new ArrayList<>();
        allCompaniesList = new ArrayList<>();
        
        // Configurer le SwipeRefreshLayout (Pull to Refresh)
        setupSwipeRefresh();
        
        // Configurer le FloatingActionButton
        fabAddCompany.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddUpdateCompanyActivity.class);
            intent.putExtra("MODE", "ADD");
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        
        // Configurer la recherche en temps réel
        setupSearchListener();
    }
    
    private void setupSearchListener() {
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
    
    private void setupSwipeRefresh() {
        // Configurer les couleurs du SwipeRefreshLayout
        swipeRefreshLayout.setColorSchemeResources(
            R.color.brand_primary,
            R.color.brand_secondary,
            R.color.bleu
        );
        
        // Configurer le listener de refresh
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Recharger les données
            loadCompaniesData();
            
            // Réinitialiser la barre de recherche
            if (etSearch.getText() != null) {
                etSearch.getText().clear();
            }
            
            // Afficher un message de confirmation
            Toast.makeText(this, "Liste actualisée", Toast.LENGTH_SHORT).show();
            
            // Arrêter l'animation de refresh
            swipeRefreshLayout.setRefreshing(false);
        });
    }
    
    private void filterCompanies(String query) {
        companiesList.clear();
        
        if (query.isEmpty()) {
            companiesList.addAll(allCompaniesList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Company company : allCompaniesList) {
                if (company.name.toLowerCase().contains(lowerQuery) ||
                    company.description.toLowerCase().contains(lowerQuery) ||
                    String.join(";", company.services).toLowerCase().contains(lowerQuery)) {
                    companiesList.add(company);
                }
            }
        }
        
        if (companyAdapter != null) {
            companyAdapter.notifyDataSetChanged();
        }
        handleEmptyState();
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setTitle("Liste des Entreprises");
        toolbar.setNavigationOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
    }private void loadCompaniesData() {
        // Vider les listes actuelles
        companiesList.clear();
        allCompaniesList.clear();

        // Récupérer les données de la base de données
        Cursor cursor = dbHelper.getAllCompanies();

        if (cursor != null) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NAME));
                String desc = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DESC));
                String servicesStr = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SERVICES));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PHONE));                String url = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_URL));
                double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LAT));
                double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LONG));
                  // Récupérer le chemin de l'image (peut être null)
                String imagePath = null;
                int imageColumnIndex = cursor.getColumnIndex(DatabaseHelper.COL_IMAGE);
                if (imageColumnIndex != -1) {
                    imagePath = cursor.getString(imageColumnIndex);
                }

                // Récupérer l'état favori
                boolean isFavorite = false;
                int favoriteColumnIndex = cursor.getColumnIndex(DatabaseHelper.COL_FAVORITE);
                if (favoriteColumnIndex != -1) {
                    isFavorite = cursor.getInt(favoriteColumnIndex) == 1;
                }

                // Traiter les services
                ArrayList<String> serviceList = new ArrayList<>();
                if (servicesStr != null && !servicesStr.isEmpty()) {
                    for (String service : servicesStr.split(";")) {
                        serviceList.add(service.trim());
                    }
                }                // Créer l'objet Company et l'ajouter à la liste
                Company company = new Company(id, name, desc, serviceList, phone, url, latitude, longitude, imagePath, isFavorite);
                companiesList.add(company);
                allCompaniesList.add(company); // Aussi dans la liste complète pour la recherche
            }
            cursor.close();
        }

        // Log pour débugger
        Log.d("CompaniesActivity", "Nombre d'entreprises chargées: " + companiesList.size());
        for (Company company : companiesList) {
            Log.d("CompaniesActivity", "Entreprise: " + company.name);
        }

        // Gérer l'état vide
        handleEmptyState();// Créer et configurer l'adaptateur
        if (companyAdapter == null) {
            companyAdapter = new CompanyAdapter(this, companiesList);
            companyAdapter.setOnCompanyActionListener(this);
            listViewCompanies.setAdapter(companyAdapter);
        } else {
            companyAdapter.updateData(companiesList);
        }

        // Optionnel: Gérer les clics sur les éléments de la liste
        listViewCompanies.setOnItemClickListener((parent, view, position, id) -> {
            Company selectedCompany = companiesList.get(position);
            Toast.makeText(this, "Entreprise sélectionnée: " + selectedCompany.name, Toast.LENGTH_SHORT).show();
        });
    }

    private void handleEmptyState() {
        android.widget.TextView tvEmptyState = findViewById(R.id.tvEmptyState);
        if (companiesList.isEmpty()) {
            listViewCompanies.setVisibility(android.view.View.GONE);
            tvEmptyState.setVisibility(android.view.View.VISIBLE);
        } else {
            listViewCompanies.setVisibility(android.view.View.VISIBLE);
            tvEmptyState.setVisibility(android.view.View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recharger les données au cas où elles auraient été modifiées
        loadCompaniesData();
    }

    @Override
    public void onDeleteCompany(int position, Company company) {
        // Afficher une boîte de dialogue de confirmation
        new MaterialAlertDialogBuilder(this)
                .setTitle("Supprimer l'entreprise")
                .setMessage("Êtes-vous sûr de vouloir supprimer \"" + company.name + "\" ?")
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    // Supprimer de la base de données
                    boolean success = dbHelper.deleteCompany(company.id);
                    if (success) {
                        // Supprimer de la liste locale et mettre à jour l'adaptateur
                        companiesList.remove(position);
                        companyAdapter.notifyDataSetChanged();
                        handleEmptyState();
                        Toast.makeText(this, "Entreprise supprimée avec succès", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Erreur lors de la suppression", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }    @Override
    public void onUpdateCompany(int position, Company company) {
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
        intent.putExtra("COMPANY_LONG", company.longitude);        if (company.imagePath != null) {
            intent.putExtra("COMPANY_IMAGE", company.imagePath);
        }
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public void onFavoriteCompany(int position, Company company) {
        // Basculer l'état favori dans la base de données
        boolean newFavoriteState = dbHelper.toggleFavorite(company.id);
        
        // Mettre à jour l'objet Company dans les listes
        company.isFavorite = newFavoriteState;
        
        // Mettre à jour aussi dans allCompaniesList
        for (Company c : allCompaniesList) {
            if (c.id == company.id) {
                c.isFavorite = newFavoriteState;
                break;
            }
        }
        
        // Rafraîchir l'affichage
        companyAdapter.notifyDataSetChanged();
        
        // Afficher un message
        String message = newFavoriteState ? 
                company.name + " ajouté aux favoris ⭐" : 
                company.name + " retiré des favoris";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
