package com.example.tp5ex2;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtils {

    /**
     * Méthode pour hacher un mot de passe avec SHA-256
     * @param password Le mot de passe en clair
     * @return Le mot de passe haché en hexadécimal
     */
    public static String hashPassword(String password) {
        try {
            // Créer une instance de MessageDigest pour SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            
            // Hasher le mot de passe
            byte[] hashedBytes = digest.digest(password.getBytes());
            
            // Convertir les bytes en hexadécimal
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashedBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erreur lors du hachage du mot de passe", e);
        }
    }

    /**
     * Méthode pour vérifier si un mot de passe correspond au hash stocké
     * @param password Le mot de passe en clair à vérifier
     * @param hashedPassword Le mot de passe haché stocké en base
     * @return true si les mots de passe correspondent, false sinon
     */
    public static boolean verifyPassword(String password, String hashedPassword) {
        String hashOfInput = hashPassword(password);
        return hashOfInput.equals(hashedPassword);
    }

    /**
     * Vérifie si un mot de passe est suffisamment fort
     * Critères: min 8 caractères, une majuscule, une minuscule, un chiffre, un caractère spécial
     * @param password Le mot de passe à vérifier
     * @return true si le mot de passe est fort, false sinon
     */
    public static boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        boolean hasUppercase = !password.equals(password.toLowerCase());
        boolean hasLowercase = !password.equals(password.toUpperCase());
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");
        
        return hasUppercase && hasLowercase && hasDigit && hasSpecial;
    }

    /**
     * Retourne un message d'erreur détaillé si le mot de passe n'est pas assez fort
     * @param password Le mot de passe à vérifier
     * @return Le message d'erreur ou null si le mot de passe est valide
     */
    public static String getPasswordError(String password) {
        if (password == null || password.isEmpty()) {
            return "Le mot de passe est requis";
        }
        if (password.length() < 8) {
            return "Le mot de passe doit contenir au moins 8 caractères";
        }
        if (password.equals(password.toLowerCase())) {
            return "Le mot de passe doit contenir au moins une majuscule";
        }
        if (password.equals(password.toUpperCase())) {
            return "Le mot de passe doit contenir au moins une minuscule";
        }
        if (!password.matches(".*\\d.*")) {
            return "Le mot de passe doit contenir au moins un chiffre";
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            return "Le mot de passe doit contenir au moins un caractère spécial (!@#$%^&*...)";
        }
        return null; // Mot de passe valide
    }

    /**
     * Vérifie si un email a un format valide
     * @param email L'email à vérifier
     * @return true si l'email est valide, false sinon
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        String emailPattern = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailPattern);
    }
}
