package com.example.tp5ex2;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

/**
 * Utilitaire pour gérer les notifications locales
 */
public class NotificationHelper {

    private static final String CHANNEL_ID = "it_companies_channel";
    private static final String CHANNEL_NAME = "IT Companies";
    private static final String CHANNEL_DESC = "Notifications pour IT Companies Manager";

    private Context context;
    private NotificationManager notificationManager;

    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    /**
     * Crée le canal de notification (requis pour Android 8.0+)
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(CHANNEL_DESC);
            channel.enableVibration(true);
            channel.setShowBadge(true);
            
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Affiche une notification simple
     * @param title Le titre de la notification
     * @param message Le message de la notification
     * @param notificationId L'ID unique de la notification
     */
    public void showNotification(String title, String message, int notificationId) {
        // Intent pour ouvrir l'application au clic
        Intent intent = new Intent(context, SplashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 
                0, 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(notificationId, builder.build());
    }

    /**
     * Affiche une notification pour une nouvelle entreprise ajoutée
     * @param companyName Le nom de l'entreprise
     */
    public void showCompanyAddedNotification(String companyName) {
        showNotification(
                "Nouvelle entreprise ajoutée 🏢",
                companyName + " a été ajoutée avec succès !",
                1001
        );
    }

    /**
     * Affiche une notification pour une entreprise mise à jour
     * @param companyName Le nom de l'entreprise
     */
    public void showCompanyUpdatedNotification(String companyName) {
        showNotification(
                "Entreprise mise à jour ✏️",
                companyName + " a été modifiée.",
                1002
        );
    }

    /**
     * Affiche une notification pour une entreprise supprimée
     * @param companyName Le nom de l'entreprise
     */
    public void showCompanyDeletedNotification(String companyName) {
        showNotification(
                "Entreprise supprimée 🗑️",
                companyName + " a été supprimée.",
                1003
        );
    }

    /**
     * Affiche une notification de bienvenue
     * @param userName Le nom de l'utilisateur
     */
    public void showWelcomeNotification(String userName) {
        showNotification(
                "Bienvenue ! 👋",
                "Bonjour " + userName + ", bon retour sur IT Companies !",
                1004
        );
    }

    /**
     * Affiche une notification pour une entreprise ajoutée aux favoris
     * @param companyName Le nom de l'entreprise
     */
    public void showFavoriteNotification(String companyName) {
        showNotification(
                "Favori ajouté ⭐",
                companyName + " a été ajoutée à vos favoris.",
                1005
        );
    }

    /**
     * Annule une notification spécifique
     * @param notificationId L'ID de la notification à annuler
     */
    public void cancelNotification(int notificationId) {
        notificationManager.cancel(notificationId);
    }

    /**
     * Annule toutes les notifications
     */
    public void cancelAllNotifications() {
        notificationManager.cancelAll();
    }
}
