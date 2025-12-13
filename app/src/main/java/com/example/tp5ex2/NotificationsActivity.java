package com.example.tp5ex2;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Activité affichant l'historique des notifications
 */
public class NotificationsActivity extends AppCompatActivity {

    private static final String PREF_NOTIFICATIONS = "NotificationsPref";
    private static final String KEY_NOTIFICATIONS_LIST = "notificationsList";

    private RecyclerView recyclerNotifications;
    private LinearLayout emptyState;
    private MaterialButton btnClearAll;
    private List<NotificationItem> notificationList;
    private NotificationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        initViews();
        setupToolbar();
        loadNotifications();
        setupAdapter();
    }

    private void initViews() {
        recyclerNotifications = findViewById(R.id.recyclerNotifications);
        emptyState = findViewById(R.id.emptyState);
        btnClearAll = findViewById(R.id.btnClearAll);

        btnClearAll.setOnClickListener(v -> clearAllNotifications());
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setTitle("Notifications");
        toolbar.setNavigationOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
    }

    private void loadNotifications() {
        notificationList = new ArrayList<>();
        
        SharedPreferences prefs = getSharedPreferences(PREF_NOTIFICATIONS, MODE_PRIVATE);
        String json = prefs.getString(KEY_NOTIFICATIONS_LIST, "[]");
        
        try {
            JSONArray array = new JSONArray(json);
            for (int i = array.length() - 1; i >= 0; i--) { // Afficher les plus récents en premier
                JSONObject obj = array.getJSONObject(i);
                notificationList.add(new NotificationItem(
                    obj.getString("title"),
                    obj.getString("message"),
                    obj.getLong("timestamp"),
                    obj.getString("type")
                ));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Si la liste est vide, ajouter des notifications de démonstration
        if (notificationList.isEmpty()) {
            addDemoNotifications();
        }

        updateEmptyState();
    }

    private void addDemoNotifications() {
        long now = System.currentTimeMillis();
        
        notificationList.add(new NotificationItem(
            "Bienvenue !",
            "Bienvenue sur IT Companies Manager. Explorez les fonctionnalités de l'application.",
            now - 1000 * 60 * 5, // 5 minutes ago
            "welcome"
        ));
        
        notificationList.add(new NotificationItem(
            "Nouvelle entreprise",
            "L'entreprise TechCorp a été ajoutée avec succès.",
            now - 1000 * 60 * 60, // 1 hour ago
            "company_added"
        ));
        
        notificationList.add(new NotificationItem(
            "Favori ajouté",
            "DataFlow Solutions a été ajoutée à vos favoris.",
            now - 1000 * 60 * 60 * 3, // 3 hours ago
            "favorite"
        ));
        
        notificationList.add(new NotificationItem(
            "Mise à jour",
            "Les informations de CloudBase ont été mises à jour.",
            now - 1000 * 60 * 60 * 24, // 1 day ago
            "company_updated"
        ));
    }

    private void setupAdapter() {
        adapter = new NotificationAdapter(notificationList, position -> {
            notificationList.remove(position);
            adapter.notifyItemRemoved(position);
            saveNotifications();
            updateEmptyState();
        });
        
        recyclerNotifications.setLayoutManager(new LinearLayoutManager(this));
        recyclerNotifications.setAdapter(adapter);
    }

    private void clearAllNotifications() {
        notificationList.clear();
        adapter.notifyDataSetChanged();
        saveNotifications();
        updateEmptyState();
    }

    private void saveNotifications() {
        try {
            JSONArray array = new JSONArray();
            for (NotificationItem item : notificationList) {
                JSONObject obj = new JSONObject();
                obj.put("title", item.title);
                obj.put("message", item.message);
                obj.put("timestamp", item.timestamp);
                obj.put("type", item.type);
                array.put(obj);
            }
            
            SharedPreferences prefs = getSharedPreferences(PREF_NOTIFICATIONS, MODE_PRIVATE);
            prefs.edit().putString(KEY_NOTIFICATIONS_LIST, array.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateEmptyState() {
        if (notificationList.isEmpty()) {
            recyclerNotifications.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
            btnClearAll.setVisibility(View.GONE);
        } else {
            recyclerNotifications.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
            btnClearAll.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    /**
     * Ajoute une notification à l'historique (méthode statique utilitaire)
     */
    public static void addNotification(android.content.Context context, String title, String message, String type) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NOTIFICATIONS, MODE_PRIVATE);
        String json = prefs.getString(KEY_NOTIFICATIONS_LIST, "[]");
        
        try {
            JSONArray array = new JSONArray(json);
            JSONObject obj = new JSONObject();
            obj.put("title", title);
            obj.put("message", message);
            obj.put("timestamp", System.currentTimeMillis());
            obj.put("type", type);
            array.put(obj);
            
            prefs.edit().putString(KEY_NOTIFICATIONS_LIST, array.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Inner class for notification item
    static class NotificationItem {
        String title;
        String message;
        long timestamp;
        String type;

        NotificationItem(String title, String message, long timestamp, String type) {
            this.title = title;
            this.message = message;
            this.timestamp = timestamp;
            this.type = type;
        }
    }

    // Adapter for RecyclerView
    static class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
        
        private List<NotificationItem> items;
        private OnItemDeleteListener deleteListener;

        interface OnItemDeleteListener {
            void onDelete(int position);
        }

        NotificationAdapter(List<NotificationItem> items, OnItemDeleteListener listener) {
            this.items = items;
            this.deleteListener = listener;
        }

        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_notification, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            NotificationItem item = items.get(position);
            
            holder.tvTitle.setText(item.title);
            holder.tvMessage.setText(item.message);
            holder.tvTime.setText(getRelativeTime(item.timestamp));
            
            // Set icon based on type
            int iconRes;
            switch (item.type) {
                case "company_added":
                    iconRes = android.R.drawable.ic_menu_add;
                    break;
                case "company_updated":
                    iconRes = android.R.drawable.ic_menu_edit;
                    break;
                case "company_deleted":
                    iconRes = android.R.drawable.ic_menu_delete;
                    break;
                case "favorite":
                    iconRes = android.R.drawable.btn_star_big_on;
                    break;
                case "welcome":
                default:
                    iconRes = android.R.drawable.ic_popup_reminder;
                    break;
            }
            holder.ivIcon.setImageResource(iconRes);
            
            holder.btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDelete(holder.getAdapterPosition());
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        private String getRelativeTime(long timestamp) {
            long now = System.currentTimeMillis();
            long diff = now - timestamp;
            
            if (diff < 60 * 1000) {
                return "À l'instant";
            } else if (diff < 60 * 60 * 1000) {
                int minutes = (int) (diff / (60 * 1000));
                return "Il y a " + minutes + " min";
            } else if (diff < 24 * 60 * 60 * 1000) {
                int hours = (int) (diff / (60 * 60 * 1000));
                return "Il y a " + hours + " h";
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                return sdf.format(new Date(timestamp));
            }
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            android.widget.ImageView ivIcon;
            TextView tvTitle;
            TextView tvMessage;
            TextView tvTime;
            android.widget.ImageButton btnDelete;

            ViewHolder(View itemView) {
                super(itemView);
                ivIcon = itemView.findViewById(R.id.ivNotificationIcon);
                tvTitle = itemView.findViewById(R.id.tvNotificationTitle);
                tvMessage = itemView.findViewById(R.id.tvNotificationMessage);
                tvTime = itemView.findViewById(R.id.tvNotificationTime);
                btnDelete = itemView.findViewById(R.id.btnDeleteNotification);
            }
        }
    }
}
