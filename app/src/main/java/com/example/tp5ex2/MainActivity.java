package com.example.tp5ex2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class MainActivity extends AppCompatActivity {
    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private TextView tvCreateAccount, tvAdminHelp, tvLoginTypeLabel;
    private View root;
    private UserDatabaseHelper dbHelper;
    private SessionManager sessionManager;
    
    // Nouveaux éléments pour les onglets
    private TabLayout tabLayoutLoginType;
    private MaterialCardView cardAdminInfo, cardClientInfo;
    private LinearLayout loginTypeIndicator;
    private ImageView ivLoginTypeIcon;
    
    // Mode de connexion: 0 = Client, 1 = Admin
    private int currentLoginMode = 0;
    private static final int MODE_CLIENT = 0;
    private static final int MODE_ADMIN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialiser le gestionnaire de session
        sessionManager = new SessionManager(this);
        
        // Appliquer le thème sauvegardé
        sessionManager.applySavedTheme();
        
        // Vérifier si l'utilisateur est déjà connecté
        if (sessionManager.checkLogin()) {
            // Rediriger selon le rôle
            redirectToHome();
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
        
        // Nouveaux éléments pour les onglets
        tabLayoutLoginType = findViewById(R.id.tabLayoutLoginType);
        cardAdminInfo = findViewById(R.id.cardAdminInfo);
        cardClientInfo = findViewById(R.id.cardClientInfo);
        loginTypeIndicator = findViewById(R.id.loginTypeIndicator);
        tvLoginTypeLabel = findViewById(R.id.tvLoginTypeLabel);
        ivLoginTypeIcon = findViewById(R.id.ivLoginTypeIcon);
        tvAdminHelp = findViewById(R.id.tvAdminHelp);
    }
    
    private void setupListeners() {
        btnLogin.setOnClickListener(v -> login());
        
        tvCreateAccount.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CreateAccountActivity.class);
            startActivity(intent);
        });
        
        // Listener pour les onglets Admin/Client
        tabLayoutLoginType.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentLoginMode = tab.getPosition();
                updateLoginModeUI();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
    
    /**
     * Met à jour l'interface selon le mode de connexion sélectionné
     */
    private void updateLoginModeUI() {
        if (currentLoginMode == MODE_ADMIN) {
            // Mode Admin
            cardAdminInfo.setVisibility(View.VISIBLE);
            cardClientInfo.setVisibility(View.GONE);
            tvCreateAccount.setVisibility(View.GONE);
            tvAdminHelp.setVisibility(View.VISIBLE);
            tvLoginTypeLabel.setText("Connexion Administrateur");
            ivLoginTypeIcon.setImageResource(R.drawable.ic_admin);
            loginTypeIndicator.setBackgroundColor(getResources().getColor(R.color.admin_bg_light, null));
            tvLoginTypeLabel.setTextColor(getResources().getColor(R.color.admin_color, null));
            btnLogin.setText("Se connecter en tant qu'Admin");
            
            // Pré-remplir l'email admin pour faciliter le test
            etEmail.setHint("admin@itcompanies.com");
        } else {
            // Mode Client
            cardAdminInfo.setVisibility(View.GONE);
            cardClientInfo.setVisibility(View.VISIBLE);
            tvCreateAccount.setVisibility(View.VISIBLE);
            tvAdminHelp.setVisibility(View.GONE);
            tvLoginTypeLabel.setText("Connexion Client");
            ivLoginTypeIcon.setImageResource(R.drawable.ic_person);
            loginTypeIndicator.setBackgroundColor(getResources().getColor(R.color.brand_primary_light, null));
            tvLoginTypeLabel.setTextColor(getResources().getColor(R.color.brand_primary, null));
            btnLogin.setText("Se connecter");
            
            etEmail.setHint("");
        }
        
        // Réinitialiser les erreurs
        tilEmail.setError(null);
        tilPassword.setError(null);
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
            // Récupérer le rôle et le nom d'utilisateur
            String role = dbHelper.getUserRole(email);
            String username = dbHelper.getUsername(email);
            if (username == null || username.isEmpty()) {
                username = email.split("@")[0];
            }
            
            // Vérifier si le mode correspond au rôle
            boolean isUserAdmin = UserDatabaseHelper.ROLE_ADMIN.equals(role);
            
            if (currentLoginMode == MODE_ADMIN && !isUserAdmin) {
                // Tentative de connexion admin avec un compte client
                Snackbar.make(root, "❌ Ce compte n'a pas les droits administrateur", Snackbar.LENGTH_LONG).show();
                return;
            }
            
            if (currentLoginMode == MODE_CLIENT && isUserAdmin) {
                // Admin qui se connecte en mode client - on le redirige vers admin
                Snackbar.make(root, "👨‍💼 Compte Admin détecté - Redirection vers le dashboard", Snackbar.LENGTH_LONG).show();
            }
            
            // Créer la session utilisateur avec le rôle
            sessionManager.createLoginSession(email, username, role);
            
            String welcomeMsg = isUserAdmin ? "Bienvenue Admin " + username + " ! 👨‍💼" : "Bienvenue " + username + " ! 👤";
            Snackbar.make(root, welcomeMsg, Snackbar.LENGTH_LONG).show();

            // Rediriger selon le rôle
            redirectToHome();
        } else {
            Snackbar.make(root, "Email ou mot de passe incorrect", Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Redirige l'utilisateur vers la bonne interface selon son rôle
     */
    private void redirectToHome() {
        Intent intent;
        if (sessionManager.isAdmin()) {
            // Interface Admin
            intent = new Intent(MainActivity.this, AdminDashboardActivity.class);
        } else {
            // Interface Client
            intent = new Intent(MainActivity.this, ClientHomeActivity.class);
        }
        intent.putExtra("username", sessionManager.getUserName());
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }
}
