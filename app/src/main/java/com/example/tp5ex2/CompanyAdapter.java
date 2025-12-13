package com.example.tp5ex2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class CompanyAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Company> companiesList;
    private LayoutInflater inflater;
    private OnCompanyActionListener listener;    // Interface pour gérer les actions sur les entreprises
    public interface OnCompanyActionListener {
        void onDeleteCompany(int position, Company company);
        void onUpdateCompany(int position, Company company);
        void onFavoriteCompany(int position, Company company);
    }

    public CompanyAdapter(Context context, ArrayList<Company> companiesList) {
        this.context = context;
        this.companiesList = companiesList;
        this.inflater = LayoutInflater.from(context);
    }

    public CompanyAdapter(Context context, ArrayList<Company> companiesList, OnCompanyActionListener listener) {
        this.context = context;
        this.companiesList = companiesList;
        this.inflater = LayoutInflater.from(context);
        this.listener = listener;
    }

    public void setOnCompanyActionListener(OnCompanyActionListener listener) {
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return companiesList.size();
    }

    @Override
    public Object getItem(int position) {
        return companiesList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return companiesList.get(position).id;
    }    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        // Utilisation du pattern ViewHolder pour optimiser les performances
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_company, parent, false);
            holder = new ViewHolder();
            holder.tvCompanyName = convertView.findViewById(R.id.tvCompanyName);
            holder.btnUpdate = convertView.findViewById(R.id.btnUpdate);
            holder.btnDelete = convertView.findViewById(R.id.btnDelete);
            holder.btnFavorite = convertView.findViewById(R.id.btnFavorite);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Récupérer l'entreprise à la position actuelle
        Company company = companiesList.get(position);

        // Afficher uniquement le nom de l'entreprise
        holder.tvCompanyName.setText(company.name);

        // Mettre à jour l'icône de favori
        if (company.isFavorite) {
            holder.btnFavorite.setImageResource(R.drawable.ic_favorite);
        } else {
            holder.btnFavorite.setImageResource(R.drawable.ic_favorite_border);
        }

        // Configurer le bouton favori
        holder.btnFavorite.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFavoriteCompany(position, company);
            }
        });

        // Configurer les boutons
        holder.btnUpdate.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUpdateCompany(position, company);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteCompany(position, company);
            }
        });

        return convertView;
    }

    // Classe ViewHolder pour optimiser les performances
    static class ViewHolder {
        TextView tvCompanyName;
        MaterialButton btnUpdate;
        MaterialButton btnDelete;
        ImageButton btnFavorite;
    }

    // Méthode pour mettre à jour les données de l'adaptateur
    public void updateData(ArrayList<Company> newCompaniesList) {
        this.companiesList = newCompaniesList;
        notifyDataSetChanged();
    }

    // Méthode pour ajouter une nouvelle entreprise
    public void addCompany(Company company) {
        companiesList.add(company);
        notifyDataSetChanged();
    }

    // Méthode pour supprimer une entreprise
    public void removeCompany(int position) {
        if (position >= 0 && position < companiesList.size()) {
            companiesList.remove(position);
            notifyDataSetChanged();
        }
    }
}
