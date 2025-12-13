package com.example.tp5ex2;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class AddUpdateCompanyActivity extends AppCompatActivity {

    // Mode de l'activité
    public static final String EXTRA_MODE = "EXTRA_MODE";
    public static final String EXTRA_COMPANY_ID = "EXTRA_COMPANY_ID";
    public static final String EXTRA_COMPANY_NAME = "EXTRA_COMPANY_NAME";
    public static final int MODE_ADD = 1;
    public static final int MODE_UPDATE = 2;

    // Vues du formulaire
    private TextInputLayout tilCompanyName, tilDescription, tilServices, tilPhone, tilUrl;
    private TextInputEditText etCompanyName, etDescription, etServices, etPhone, etUrl;
    private ImageView ivCompanyImage;
    private MaterialButton btnSelectImage, btnSaveCompany;
      // Variables
    private DatabaseHelper dbHelper;
    private int currentMode;
    private long companyId;
    private String selectedImagePath;
    private NotificationHelper notificationHelper;
    
    // Launcher pour sélectionner une image
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_update_company);        // Initialiser la base de données
        dbHelper = new DatabaseHelper(this);
        
        // Initialiser les notifications
        notificationHelper = new NotificationHelper(this);
        
        // Récupérer les extras
        Intent intent = getIntent();
        String modeString = intent.getStringExtra("MODE");
        currentMode = "UPDATE".equals(modeString) ? MODE_UPDATE : MODE_ADD;
        companyId = intent.getLongExtra("COMPANY_ID", -1);

        // Initialiser les vues
        initViews();

        // Configurer la toolbar
        setupToolbar();

        // Initialiser le launcher pour la galerie
        setupImagePicker();

        // Configurer les listeners
        setupListeners();

        // Pré-remplir les données si en mode update
        if (currentMode == MODE_UPDATE && companyId != -1) {
            loadCompanyData();
        }
    }

    private void initViews() {
        tilCompanyName = findViewById(R.id.tilCompanyName);
        tilDescription = findViewById(R.id.tilDescription);
        tilServices = findViewById(R.id.tilServices);
        tilPhone = findViewById(R.id.tilPhone);
        tilUrl = findViewById(R.id.tilUrl);

        etCompanyName = findViewById(R.id.etCompanyName);
        etDescription = findViewById(R.id.etDescription);
        etServices = findViewById(R.id.etServices);
        etPhone = findViewById(R.id.etPhone);
        etUrl = findViewById(R.id.etUrl);

        ivCompanyImage = findViewById(R.id.ivCompanyImage);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnSaveCompany = findViewById(R.id.btnSaveCompany);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        
        if (currentMode == MODE_ADD) {
            toolbar.setTitle("Ajouter une entreprise");
            btnSaveCompany.setText("Ajouter");
        } else {
            toolbar.setTitle("Modifier l'entreprise");
            btnSaveCompany.setText("Mettre à jour");
        }
          toolbar.setNavigationOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        try {
                            // Sauvegarder l'image dans le stockage interne
                            selectedImagePath = saveImageToInternalStorage(imageUri);
                            
                            // Afficher l'image dans l'ImageView
                            displayImage(selectedImagePath);
                            
                        } catch (IOException e) {
                            Toast.makeText(this, "Erreur lors du chargement de l'image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        );
    }

    private void setupListeners() {
        btnSelectImage.setOnClickListener(v -> openImagePicker());
        btnSaveCompany.setOnClickListener(v -> saveCompany());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(Intent.createChooser(intent, "Sélectionner une image"));
    }

    private String saveImageToInternalStorage(Uri imageUri) throws IOException {
        // Créer un dossier pour les images d'entreprises
        File imageDir = new File(getFilesDir(), "company_images");
        if (!imageDir.exists()) {
            imageDir.mkdirs();
        }

        // Générer un nom de fichier unique
        String fileName = "company_" + System.currentTimeMillis() + ".jpg";
        File imageFile = new File(imageDir, fileName);

        // Copier l'image depuis l'URI vers le stockage interne
        try (InputStream inputStream = getContentResolver().openInputStream(imageUri);
             FileOutputStream outputStream = new FileOutputStream(imageFile)) {
            
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        }

        return imageFile.getAbsolutePath();
    }

    private void displayImage(String imagePath) {
        if (imagePath != null && !imagePath.isEmpty()) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            if (bitmap != null) {
                ivCompanyImage.setImageBitmap(bitmap);
            }
        }
    }    private void loadCompanyData() {
        Intent intent = getIntent();
        
        // Récupérer les données depuis les extras
        String name = intent.getStringExtra("COMPANY_NAME");
        String description = intent.getStringExtra("COMPANY_DESC");
        String services = intent.getStringExtra("COMPANY_SERVICES");
        String phone = intent.getStringExtra("COMPANY_PHONE");
        String url = intent.getStringExtra("COMPANY_URL");
        String imagePath = intent.getStringExtra("COMPANY_IMAGE");
        
        // Pré-remplir les champs
        if (name != null) etCompanyName.setText(name);
        if (description != null) etDescription.setText(description);
        if (services != null) etServices.setText(services);
        if (phone != null) etPhone.setText(phone);
        if (url != null) etUrl.setText(url);
        
        // Charger l'image si elle existe
        if (imagePath != null && !imagePath.isEmpty()) {
            selectedImagePath = imagePath;
            displayImage(selectedImagePath);
        }
    }

    private void saveCompany() {
        // Récupérer les valeurs des champs
        String name = etCompanyName.getText() != null ? etCompanyName.getText().toString().trim() : "";
        String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
        String services = etServices.getText() != null ? etServices.getText().toString().trim() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        String url = etUrl.getText() != null ? etUrl.getText().toString().trim() : "";

        // Réinitialiser les erreurs
        resetErrors();

        // Validation
        boolean isValid = true;

        if (TextUtils.isEmpty(name)) {
            tilCompanyName.setError("Le nom de l'entreprise est requis");
            isValid = false;
        }

        if (TextUtils.isEmpty(description)) {
            tilDescription.setError("La description est requise");
            isValid = false;
        }

        if (!isValid) return;        // Sauvegarder selon le mode
        boolean success;
        if (currentMode == MODE_ADD) {
            long result = dbHelper.addCompanyWithImage(name, description, services, phone, url, 
                                                      34.7500, 10.7500, selectedImagePath);
            success = result != -1;
        } else {
            success = dbHelper.updateCompanyWithImage(companyId, name, description, services, 
                                                     phone, url, 34.7500, 10.7500, selectedImagePath);
        }        if (success) {
            String message = currentMode == MODE_ADD ? "Entreprise ajoutée avec succès" : "Entreprise mise à jour avec succès";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            
            // Envoyer une notification
            if (currentMode == MODE_ADD) {
                notificationHelper.showCompanyAddedNotification(name);
            } else {
                notificationHelper.showCompanyUpdatedNotification(name);
            }
            
            // Retourner à l'activité précédente
            setResult(RESULT_OK);
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        } else {
            String message = currentMode == MODE_ADD ? "Erreur lors de l'ajout" : "Erreur lors de la mise à jour";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    private void resetErrors() {
        tilCompanyName.setError(null);
        tilDescription.setError(null);
        tilServices.setError(null);
        tilPhone.setError(null);
        tilUrl.setError(null);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
