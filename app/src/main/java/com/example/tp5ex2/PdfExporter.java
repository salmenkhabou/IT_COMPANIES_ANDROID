package com.example.tp5ex2;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utilitaire pour exporter les informations d'entreprise en PDF
 * Utilise l'API native Android PdfDocument
 */
public class PdfExporter {

    private static final int PAGE_WIDTH = 595;  // A4 width in points
    private static final int PAGE_HEIGHT = 842; // A4 height in points
    private static final int MARGIN = 40;

    /**
     * Exporte les informations d'une entreprise en PDF
     * @param context Le contexte de l'application
     * @param company L'entreprise à exporter
     * @return Le fichier PDF créé, ou null en cas d'erreur
     */
    public static File exportCompanyToPdf(Context context, Company company) {
        PdfDocument document = new PdfDocument();
        
        // Créer une page
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // Styles de peinture
        Paint titlePaint = new Paint();
        titlePaint.setColor(Color.parseColor("#1976D2")); // Bleu primary
        titlePaint.setTextSize(28);
        titlePaint.setFakeBoldText(true);

        Paint subtitlePaint = new Paint();
        subtitlePaint.setColor(Color.parseColor("#424242"));
        subtitlePaint.setTextSize(18);
        subtitlePaint.setFakeBoldText(true);

        Paint textPaint = new Paint();
        textPaint.setColor(Color.parseColor("#616161"));
        textPaint.setTextSize(14);

        Paint linePaint = new Paint();
        linePaint.setColor(Color.parseColor("#E0E0E0"));
        linePaint.setStrokeWidth(1);

        int y = MARGIN + 30;

        // En-tête
        canvas.drawText("IT COMPANIES", MARGIN, y, titlePaint);
        y += 15;
        
        // Ligne de séparation
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint);
        y += 40;

        // Nom de l'entreprise
        titlePaint.setTextSize(24);
        canvas.drawText(company.name, MARGIN, y, titlePaint);
        y += 35;

        // Date d'export
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE);
        canvas.drawText("Exporté le: " + sdf.format(new Date()), MARGIN, y, textPaint);
        y += 40;

        // Ligne de séparation
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint);
        y += 30;

        // Description
        canvas.drawText("📝 Description", MARGIN, y, subtitlePaint);
        y += 25;
        
        // Diviser la description en lignes
        String description = company.description != null ? company.description : "Non renseignée";
        String[] descLines = wrapText(description, textPaint, PAGE_WIDTH - 2 * MARGIN);
        for (String line : descLines) {
            canvas.drawText(line, MARGIN, y, textPaint);
            y += 20;
        }
        y += 20;

        // Services
        canvas.drawText("🔧 Services", MARGIN, y, subtitlePaint);
        y += 25;
        
        if (company.services != null && !company.services.isEmpty()) {
            for (String service : company.services) {
                canvas.drawText("• " + service, MARGIN + 20, y, textPaint);
                y += 20;
            }
        } else {
            canvas.drawText("Aucun service renseigné", MARGIN + 20, y, textPaint);
            y += 20;
        }
        y += 20;

        // Coordonnées
        canvas.drawText("📞 Contact", MARGIN, y, subtitlePaint);
        y += 25;
        
        String phone = company.phone != null && !company.phone.isEmpty() ? company.phone : "Non renseigné";
        canvas.drawText("Téléphone: " + phone, MARGIN + 20, y, textPaint);
        y += 20;
        
        String url = company.url != null && !company.url.isEmpty() ? company.url : "Non renseigné";
        canvas.drawText("Site web: " + url, MARGIN + 20, y, textPaint);
        y += 30;

        // Localisation
        canvas.drawText("📍 Localisation", MARGIN, y, subtitlePaint);
        y += 25;
        
        canvas.drawText("Latitude: " + company.latitude, MARGIN + 20, y, textPaint);
        y += 20;
        canvas.drawText("Longitude: " + company.longitude, MARGIN + 20, y, textPaint);
        y += 40;

        // Pied de page
        canvas.drawLine(MARGIN, PAGE_HEIGHT - 60, PAGE_WIDTH - MARGIN, PAGE_HEIGHT - 60, linePaint);
        textPaint.setTextSize(10);
        textPaint.setColor(Color.parseColor("#9E9E9E"));
        canvas.drawText("Généré par IT Companies Manager - Version 1.0", MARGIN, PAGE_HEIGHT - 40, textPaint);

        document.finishPage(page);

        // Sauvegarder le fichier
        String fileName = "company_" + sanitizeFileName(company.name) + "_" + 
                System.currentTimeMillis() + ".pdf";
        
        File pdfFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);
        
        try {
            FileOutputStream fos = new FileOutputStream(pdfFile);
            document.writeTo(fos);
            fos.close();
            document.close();
            return pdfFile;
        } catch (IOException e) {
            e.printStackTrace();
            document.close();
            return null;
        }
    }

    /**
     * Exporte et partage le PDF
     */
    public static void exportAndSharePdf(Context context, Company company) {
        File pdfFile = exportCompanyToPdf(context, company);
        
        if (pdfFile != null && pdfFile.exists()) {
            Toast.makeText(context, "PDF créé: " + pdfFile.getName(), Toast.LENGTH_SHORT).show();
            
            // Partager le PDF
            try {
                Uri uri = FileProvider.getUriForFile(context, 
                        context.getPackageName() + ".fileprovider", pdfFile);
                
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("application/pdf");
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Fiche entreprise: " + company.name);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                
                context.startActivity(Intent.createChooser(shareIntent, "Partager le PDF via"));
            } catch (Exception e) {
                Toast.makeText(context, "Erreur lors du partage: " + e.getMessage(), 
                        Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(context, "Erreur lors de la création du PDF", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Divise le texte en lignes pour tenir dans la largeur spécifiée
     */
    private static String[] wrapText(String text, Paint paint, float maxWidth) {
        if (text == null || text.isEmpty()) {
            return new String[]{""};
        }
        
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        java.util.ArrayList<String> lines = new java.util.ArrayList<>();
        
        for (String word : words) {
            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
            if (paint.measureText(testLine) <= maxWidth) {
                currentLine = new StringBuilder(testLine);
            } else {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                }
                currentLine = new StringBuilder(word);
            }
        }
        
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }
        
        return lines.toArray(new String[0]);
    }

    /**
     * Nettoie le nom de fichier en supprimant les caractères spéciaux
     */
    private static String sanitizeFileName(String name) {
        if (name == null) return "unknown";
        return name.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
    }
}
