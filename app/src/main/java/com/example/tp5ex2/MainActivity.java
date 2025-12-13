package com.example.tp5ex2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class MainActivity extends AppCompatActivity {
    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private TextView tvCreateAccount;
    private View root;
    private UserDatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialiser le gestionnaire de session
        sessionManager = new SessionManager(this);
        
        // Appliquer le thème sauvegardé
        sessionManager.applySavedTheme();
        
        // Vérifier si l'utilisateur est déjà connecté
        if (sessionManager.checkLogin()) {
            // Rediriger directement vers MainActivity2
            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            intent.putExtra("username", sessionManager.getUserName());
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
            return;
        }
        
        setContentView(R.layout.activity_main);
        
        // Initialiser la base de données
        dbHelper = new UserDatabaseHelper(this);
        
        // Initialiser les vues
        initViews();
        
        // Configurer les listeners
        setupListeners();
    }
    
    private void initViews() {
        root = findViewById(R.id.root);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvCreateAccount = findViewById(R.id.tvCreateAccount);
    }
    
    private void setupListeners() {
        btnLogin.setOnClickListener(v -> login());
        
        tvCreateAccount.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CreateAccountActivity.class);
            startActivity(intent);
        });
    }
    
    private void login() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";

        // Réinitialiser les erreurs
        tilEmail.setError(null);
        tilPassword.setError(null);

        // Validation des champs
        boolean isValid = true;

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("L'email est requis");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Format d'email invalide");
            isValid = false;
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Le mot de passe est requis");
            isValid = false;
        }

        if (!isValid) return;

        // Vérifier les informations de connexion avec la base de données
        if (dbHelper.checkUser(email, password)) {
            // Créer la session utilisateur
            sessionManager.createLoginSession(email, email.split("@")[0]);
            
            String welcomeMsg = "Bienvenue, " + email + " !";
            Snackbar.make(root, welcomeMsg, Snackbar.LENGTH_LONG).show();

            // Rediriger vers MainActivity2
            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            intent.putExtra("username", email.split("@")[0]);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        } else {
            Snackbar.make(root, "Email ou mot de passe incorrect", Snackbar.LENGTH_LONG).show();
        }
    }
}