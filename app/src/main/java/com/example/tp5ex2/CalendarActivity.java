package com.example.tp5ex2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.widget.CalendarView;
import android.widget.Toast;

import java.util.Calendar;

public class CalendarActivity extends AppCompatActivity {

    CalendarView calendarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        calendarView = findViewById(R.id.calendarView);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {

            // Créer la date sélectionnée
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth, 10, 0);

            // Intent pour insérer un événement
            Intent intent = new Intent(Intent.ACTION_INSERT);
            intent.setData(CalendarContract.Events.CONTENT_URI);

            intent.putExtra(CalendarContract.Events.TITLE, "Meeting Business");
            intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, calendar.getTimeInMillis());
            intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, calendar.getTimeInMillis() + 60 * 60 * 1000);

            intent.putExtra(CalendarContract.Events.EVENT_LOCATION, "Sfax, Tunisia");

            try {
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Impossible d’ouvrir le calendrier", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
