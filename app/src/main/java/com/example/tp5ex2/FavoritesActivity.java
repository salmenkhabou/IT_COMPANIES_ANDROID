package com.example.tp5ex2;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;

/**
 * Activité affichant la liste des entreprises favorites
 */
public class FavoritesActivity extends AppCompatActivity implements CompanyAdapter.OnCompanyActionListener {

    private ListView listViewFavorites;
    private LinearLayout emptyState;
    private DatabaseHelper dbHelper;
    private ArrayList<Company> favoriteCompanies;
    private CompanyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        dbHelper = new DatabaseHelper(this);
        
        initViews();
        setupToolbar();
        loadFavorites();
    }

    private void initViews() {
        listViewFavorites = findViewById(R.id.listViewFavorites);
        emptyState = findViewById(R.id.emptyState);

        listViewFavorites.setOnItemClickListener((parent, view, position, id) -> {
            Company company = favoriteCompanies.get(position);
            openDetails(company);
        });

        // Bouton pour parcourir les entreprises (dans l'état vide)
        com.google.android.material.button.MaterialButton btnBrowse = findViewById(R.id.btnBrowseCompanies);
        if (btnBrowse != null) {
            btnBrowse.setOnClickListener(v -> {
                startActivity(new Intent(this, CompaniesActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            });
        }
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setTitle("Mes Favoris ⭐");
        toolbar.setNavigationOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
    }

    private void loadFavorites() {
        favoriteCompanies = new ArrayList<>();
        
        Cursor cursor = dbHelper.getFavoriteCompanies();
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
                int isFavorite = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FAVORITE));
                
                String imagePath = null;
                int imageColumnIndex = cursor.getColumnIndex(DatabaseHelper.COL_IMAGE);
                if (imageColumnIndex != -1) {
                    imagePath = cursor.getString(imageColumnIndex);
                }

                ArrayList<String> serviceList = new ArrayList<>();
                if (servicesStr != null && !servicesStr.isEmpty()) {
                    for (String s : servicesStr.split(";")) {
                        serviceList.add(s.trim());
                    }
                }

                Company company = new Company(id, name, desc, serviceList, phone, url, latitude, longitude, imagePath, isFavorite == 1);
                favoriteCompanies.add(company);
            }
            cursor.close();
        }

        updateUI();
    }

    private void updateUI() {
        if (favoriteCompanies.isEmpty()) {
            listViewFavorites.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            listViewFavorites.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
            
            adapter = new CompanyAdapter(this, favoriteCompanies, this);
            listViewFavorites.setAdapter(adapter);
        }
    }

    private void openDetails(Company company) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(MainActivity2.EXTRA_IMAGE_RES, R.drawable.it1);
        intent.putExtra("COMPANY_NAME", company.name);
        intent.putStringArrayListExtra(MainActivity2.EXTRA_SERVICES, company.services);
        intent.putExtra(MainActivity2.EXTRA_PHONE, company.phone);
        intent.putExtra(MainActivity2.EXTRA_URL, company.url);
        intent.putExtra(MainActivity2.EXTRA_DESC, company.description);
        intent.putExtra(String.valueOf(MainActivity2.EXTRA_LAT), company.latitude);
        intent.putExtra(String.valueOf(MainActivity2.EXTRA_LONG), company.longitude);
        startActivity(intent);
        overridePendingTransition(R.anim.scale_up, R.anim.fade_out);
    }

    @Override
    public void onDeleteCompany(int position, Company company) {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Supprimer l'entreprise")
                .setMessage("Êtes-vous sûr de vouloir supprimer \"" + company.name + "\" ?")
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    boolean success = dbHelper.deleteCompany(company.id);
                    if (success) {
                        Toast.makeText(this, "Entreprise supprimée", Toast.LENGTH_SHORT).show();
                        loadFavorites();
                    } else {
                        Toast.makeText(this, "Erreur lors de la suppression", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    @Override
    public void onUpdateCompany(int position, Company company) {
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

    @Override
    public void onFavoriteCompany(int position, Company company) {
        dbHelper.toggleFavorite(company.id);
        Toast.makeText(this, "Retiré des favoris", Toast.LENGTH_SHORT).show();
        loadFavorites();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavorites();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
