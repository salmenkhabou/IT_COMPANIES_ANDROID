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

/**
 * Adaptateur pour afficher les entreprises côté client
 * Avec boutons de contact (appeler, email, site web) et favoris
 */
public class ClientCompanyAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Company> companies;
    private LayoutInflater inflater;
    private OnClientActionListener listener;

    public interface OnClientActionListener {
        void onCallCompany(Company company);
        void onVisitWebsite(Company company);
        void onEmailCompany(Company company);
        void onToggleFavorite(Company company);
        void onViewDetails(Company company);
    }

    public ClientCompanyAdapter(Context context, ArrayList<Company> companies, OnClientActionListener listener) {
        this.context = context;
        this.companies = companies;
        this.listener = listener;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return companies.size();
    }

    @Override
    public Object getItem(int position) {
        return companies.get(position);
    }

    @Override
    public long getItemId(int position) {
        return companies.get(position).id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_client_company, parent, false);
            holder = new ViewHolder();
            holder.tvName = convertView.findViewById(R.id.tvCompanyName);
            holder.tvServices = convertView.findViewById(R.id.tvCompanyServices);
            holder.tvDescription = convertView.findViewById(R.id.tvCompanyDescription);
            holder.btnCall = convertView.findViewById(R.id.btnCall);
            holder.btnWebsite = convertView.findViewById(R.id.btnWebsite);
            holder.btnEmail = convertView.findViewById(R.id.btnEmail);
            holder.btnFavorite = convertView.findViewById(R.id.btnFavorite);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Company company = companies.get(position);

        // Afficher les informations
        holder.tvName.setText(company.name);
        holder.tvServices.setText(String.join(" • ", company.services));
        
        String description = company.description;
        if (description != null && description.length() > 100) {
            description = description.substring(0, 100) + "...";
        }
        holder.tvDescription.setText(description != null ? description : "");

        // Icône favori
        holder.btnFavorite.setImageResource(
                company.isFavorite ? R.drawable.ic_favorite : R.drawable.ic_favorite_border
        );

        // Listeners
        holder.btnCall.setOnClickListener(v -> {
            if (listener != null) listener.onCallCompany(company);
        });

        holder.btnWebsite.setOnClickListener(v -> {
            if (listener != null) listener.onVisitWebsite(company);
        });

        holder.btnEmail.setOnClickListener(v -> {
            if (listener != null) listener.onEmailCompany(company);
        });

        holder.btnFavorite.setOnClickListener(v -> {
            if (listener != null) listener.onToggleFavorite(company);
        });

        convertView.setOnClickListener(v -> {
            if (listener != null) listener.onViewDetails(company);
        });

        return convertView;
    }

    static class ViewHolder {
        TextView tvName;
        TextView tvServices;
        TextView tvDescription;
        MaterialButton btnCall;
        MaterialButton btnWebsite;
        MaterialButton btnEmail;
        ImageButton btnFavorite;
    }
}
