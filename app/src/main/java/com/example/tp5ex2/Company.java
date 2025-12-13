package com.example.tp5ex2;

import java.util.ArrayList;

public class Company {
    public long   id;
    public String name;
    public String description;
    public ArrayList<String> services;
    public String phone;
    public String url;
    public double latitude;
    public double longitude;
    public String imagePath;
    public boolean isFavorite;

    public Company(long id, String name, String description,
                   ArrayList<String> services, String phone, String url, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.services = services;
        this.phone = phone;
        this.url = url;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imagePath = null;
        this.isFavorite = false;
    }

    public Company(long id, String name, String description,
                   ArrayList<String> services, String phone, String url, double latitude, double longitude, String imagePath) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.services = services;
        this.phone = phone;
        this.url = url;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imagePath = imagePath;
        this.isFavorite = false;
    }

    public Company(long id, String name, String description,
                   ArrayList<String> services, String phone, String url, 
                   double latitude, double longitude, String imagePath, boolean isFavorite) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.services = services;
        this.phone = phone;
        this.url = url;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imagePath = imagePath;
        this.isFavorite = isFavorite;
    }

}
