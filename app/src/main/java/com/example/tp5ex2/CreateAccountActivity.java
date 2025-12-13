package com.example.tp5ex2;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import android.widget.TextView;

public class CreateAccountActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilUsername, tilPassword, tilConfirmPassword;
    private TextInputEditText etEmail, etUsername, etPassword, etConfirmPassword;
    private MaterialButton btnCreateAccount;
    private TextView tvLogin;
    private UserDatabaseHelper dbHelper;    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        // Initialiser la base de données
        dbHelper = new UserDatabaseHelper(this);

        // Configurer la toolbar
        setupToolbar();

        // Initialiser les vues
        initViews();

        // Configurer les listeners
        setupListeners();
    }

    private void setupToolbar() {
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initViews() {
        tilEmail = findViewById(R.id.tilEmail);
        tilUsername = findViewById(R.id.tilUsername);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        
        etEmail = findViewById(R.id.etEmail);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        tvLogin = findViewById(R.id.tvLogin);
    }

    private void setupListeners() {
        btnCreateAccount.setOnClickListener(v -> createAccount());
        
        tvLogin.setOnClickListener(v -> {
            // Rediriger vers l'activité de connexion
            Intent intent = new Intent(CreateAccountActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void createAccount() {
        // Récupérer les valeurs des champs
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString() : "";

        // Réinitialiser les erreurs
        resetErrors();

        // Validation des champs
        boolean isValid = true;

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("L'email est requis");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Format d'email invalide");
            isValid = false;
        }

        if (TextUtils.isEmpty(username)) {
            tilUsername.setError("Le nom d'utilisateur est requis");
            isValid = false;
        }        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Le mot de passe est requis");
            isValid = false;
        } else {
            // Validation du mot de passe fort
            String passwordError = PasswordUtils.getPasswordError(password);
            if (passwordError != null) {
                tilPassword.setError(passwordError);
                isValid = false;
            }
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            tilConfirmPassword.setError("Veuillez confirmer le mot de passe");
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Les mots de passe ne correspondent pas");
            isValid = false;
        }

        if (!isValid) return;

        // Vérifier si l'email existe déjà
        if (dbHelper.checkEmailExists(email)) {
            tilEmail.setError("Cet email est déjà utilisé");
            return;
        }

        // Ajouter l'utilisateur à la base de données
        boolean isAdded = dbHelper.addUser(email, username, password);

        if (isAdded) {
            Toast.makeText(this, "Compte créé avec succès!", Toast.LENGTH_SHORT).show();
            // Rediriger vers l'activité de connexion
            Intent intent = new Intent(CreateAccountActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Erreur lors de la création du compte", Toast.LENGTH_SHORT).show();
        }
    }

    private void resetErrors() {
        tilEmail.setError(null);
        tilUsername.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
    }
}
