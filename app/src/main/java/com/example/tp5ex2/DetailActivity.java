package com.example.tp5ex2;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;

public class DetailActivity extends AppCompatActivity {

    private int imageResId;
    private ArrayList<String> services;
    private String phone;
    private String url;
    private String description;

    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // --- Récupération des extras ---
        imageResId = getIntent().getIntExtra(MainActivity2.EXTRA_IMAGE_RES, 0);
        services   = getIntent().getStringArrayListExtra(MainActivity2.EXTRA_SERVICES);
        phone      = getIntent().getStringExtra(MainActivity2.EXTRA_PHONE);
        url        = getIntent().getStringExtra(MainActivity2.EXTRA_URL);
        description = getIntent().getStringExtra(MainActivity2.EXTRA_DESC);

        // FIX : correct way to retrieve float extras
        latitude  = getIntent().getFloatExtra(String.valueOf(MainActivity2.EXTRA_LAT), 0f);
        longitude = getIntent().getFloatExtra(String.valueOf(MainActivity2.EXTRA_LONG), 0f);

        // --- Toolbar navigation ---
        MaterialToolbar toolbar = findViewById(R.id.detailTopAppBar);
        toolbar.setNavigationOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.scale_down);
        });

        // --- Image ---
        ImageView companyImage = findViewById(R.id.companyImage);
        if (imageResId != 0) companyImage.setImageResource(imageResId);

        // --- Description ---
        TextView tvDesc = findViewById(R.id.companyDescription);
        tvDesc.setText(description != null ? description : "");

        // --- Chips Services ---
        ChipGroup chipGroup = findViewById(R.id.chipGroup);

        if (services != null) {
            for (String s : services) {
                Chip chip = new Chip(this);
                chip.setText(s);
                chip.setCheckable(false);
                chipGroup.addView(chip);
            }
        }

        // --- BTN CALL ---
        MaterialButton btnCall = findViewById(R.id.btnCall);
        btnCall.setOnClickListener(v -> {
            if (phone != null && !phone.isEmpty()) {
                Intent dial = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
                startActivity(dial);
            }
        });

        // --- BTN WEB ---
        MaterialButton btnWeb = findViewById(R.id.btnWeb);
        btnWeb.setOnClickListener(v -> {
            if (url != null && !url.isEmpty()) {
                String fixed = url.startsWith("http") ? url : "https://" + url;
                Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(fixed));
                startActivity(browser);
            }
        });

        // --- BTN CALENDAR ---
        findViewById(R.id.btnCalendar).setOnClickListener(v ->
                startActivity(new Intent(this, CalendarActivity.class))
        );

        // --- BTN MAP ---
        findViewById(R.id.btnMap).setOnClickListener(v -> showMap());

        // --- BTN EMAIL ---
        findViewById(R.id.btnEmail).setOnClickListener(v -> showEmailDialog());

        // --- BTN RATING ---
        MaterialButton btnRate = findViewById(R.id.btnRate);
        MaterialCardView ratingCard = findViewById(R.id.ratingCard);
        TextView tvRating = findViewById(R.id.tvRating);

        btnRate.setOnClickListener(v -> showRatingDialog(ratingCard, tvRating));
        
        // --- BTN SHARE ---
        MaterialButton btnShare = findViewById(R.id.btnShare);
        btnShare.setOnClickListener(v -> shareCompanyInfo());
        
        // --- BTN EXPORT PDF ---
        MaterialButton btnExportPdf = findViewById(R.id.btnExportPdf);
        btnExportPdf.setOnClickListener(v -> exportToPdf());
    }

    // -----------------------------------------------------------------------

    private void showRatingDialog(MaterialCardView card, TextView tv) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.rating_dialog, null);
        RatingBar ratingBar = view.findViewById(R.id.ratingBar);

        builder.setView(view);
        builder.setCancelable(true);

        builder.setNegativeButton("CANCEL", (d, w) -> d.dismiss());

        builder.setPositiveButton("SUBMIT", (d, w) -> {
            float rating = ratingBar.getRating();
            card.setVisibility(View.VISIBLE);
            tv.setText("Your rating: " + rating + " stars");
        });

        builder.create().show();
    }

    private void showEmailDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 30, 40, 10);

        EditText etSubject = new EditText(this);
        etSubject.setHint("Objet de l’e-mail");

        EditText etMessage = new EditText(this);
        etMessage.setHint("Contenu du message");
        etMessage.setMinLines(3);

        layout.addView(etSubject);
        layout.addView(etMessage);

        new AlertDialog.Builder(this)
                .setTitle("Envoyer un e-mail")
                .setView(layout)
                .setPositiveButton("Envoyer", (dialog, which) -> {
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                    emailIntent.setData(Uri.parse("mailto:contact@company.com"));
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, etSubject.getText().toString());
                    emailIntent.putExtra(Intent.EXTRA_TEXT, etMessage.getText().toString());
                    startActivity(Intent.createChooser(emailIntent, "Envoyer via..."));
                })
                .setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showMap() {
        String geoUri = "geo:" + latitude + "," + longitude + "?q=" + latitude + "," + longitude + "(Company)";
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri));
        mapIntent.setPackage("com.google.android.apps.maps"); // Assurez-vous d'utiliser l'application Google Maps

        // Vérifier si Google Maps est installé sur l'appareil
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            // Si Google Maps n'est pas installé, ouvrir l'URL dans un navigateur
            String url = "https://www.google.com/maps?q=" + latitude + "," + longitude;
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        }
    }

    /**
     * Partager les informations de l'entreprise
     */
    private void shareCompanyInfo() {
        StringBuilder shareText = new StringBuilder();
        shareText.append("🏢 Découvrez cette entreprise !\n\n");
        
        if (description != null && !description.isEmpty()) {
            shareText.append("📝 ").append(description).append("\n\n");
        }
        
        if (services != null && !services.isEmpty()) {
            shareText.append("🔧 Services: ").append(String.join(", ", services)).append("\n\n");
        }
        
        if (phone != null && !phone.isEmpty()) {
            shareText.append("📞 Téléphone: ").append(phone).append("\n");
        }
        
        if (url != null && !url.isEmpty()) {
            shareText.append("🌐 Site web: ").append(url).append("\n");
        }
        
        shareText.append("\n📍 Localisation: ").append(latitude).append(", ").append(longitude);
        
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Entreprise IT à découvrir");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());
        
        startActivity(Intent.createChooser(shareIntent, "Partager via"));
    }

    /**
     * Exporter les informations de l'entreprise en PDF
     */
    private void exportToPdf() {
        // Créer un objet Company temporaire avec les données actuelles
        Company company = new Company(
                0, // ID temporaire
                "Entreprise", // Nom par défaut (on pourrait le passer via intent)
                description,
                services,
                phone,
                url,
                latitude,
                longitude
        );
        
        // Récupérer le nom de l'entreprise depuis l'intent si disponible
        String companyName = getIntent().getStringExtra("COMPANY_NAME");
        if (companyName != null && !companyName.isEmpty()) {
            company.name = companyName;
        }
        
        // Exporter et partager le PDF
        PdfExporter.exportAndSharePdf(this, company);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.scale_down);
    }

}
