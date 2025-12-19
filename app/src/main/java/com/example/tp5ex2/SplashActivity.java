package com.example.tp5ex2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Écran de démarrage (Splash Screen) avec animations
 * Affiche le logo et le nom de l'application pendant 2 secondes
 */
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2000; // 2 secondes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Appliquer le thème sauvegardé
        SessionManager sessionManager = new SessionManager(this);
        sessionManager.applySavedTheme();
        
        setContentView(R.layout.activity_splash);

        // Masquer la barre de navigation système
        hideSystemUI();

        // Animer les éléments
        animateElements();

        // Rediriger après le délai
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            navigateToNextScreen();
        }, SPLASH_DURATION);
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    private void animateElements() {
        ImageView logo = findViewById(R.id.splashLogo);
        TextView appName = findViewById(R.id.splashAppName);
        TextView tagline = findViewById(R.id.splashTagline);

        // Animation du logo (scale + rotation)
        Animation scaleAnim = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        logo.startAnimation(scaleAnim);

        // Animation du nom de l'app (fade in avec délai)
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fadeIn.setStartOffset(500);
        appName.startAnimation(fadeIn);

        // Animation du tagline (fade in avec délai plus long)
        Animation fadeInTagline = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fadeInTagline.setStartOffset(800);
        tagline.startAnimation(fadeInTagline);
    }

    private void navigateToNextScreen() {
        SessionManager sessionManager = new SessionManager(this);
        
        Intent intent;
        if (sessionManager.checkLogin()) {
            // Utilisateur connecté → rediriger selon le rôle
            if (sessionManager.isAdmin()) {
                intent = new Intent(this, AdminDashboardActivity.class);
            } else {
                intent = new Intent(this, ClientHomeActivity.class);
            }
            intent.putExtra("username", sessionManager.getUserName());
        } else {
            // Non connecté → aller à l'écran de connexion
            intent = new Intent(this, MainActivity.class);
        }
        
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }
}
