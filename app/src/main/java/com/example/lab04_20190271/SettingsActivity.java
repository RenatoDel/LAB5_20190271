package com.example.lab04_20190271;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lab04_20190271.utils.NotificationHelper;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "HabitsAppPrefs";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_MOTIVATIONAL_MESSAGE = "motivational_message";
    private static final String KEY_NOTIFICATION_HOURS = "notification_hours";
    private static final String KEY_MOTIVATIONAL_ENABLED = "motivational_enabled";

    private EditText etUserName;
    private EditText etMotivationalMessage;
    private EditText etNotificationHours;
    private Button btnSave;
    private Button btnCancel;

    private SharedPreferences sharedPreferences;
    private NotificationHelper notificationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initializeViews();
        initializeData();
        loadCurrentSettings();
        setupClickListeners();
    }

    private void initializeViews() {
        etUserName = findViewById(R.id.etUserName);
        etMotivationalMessage = findViewById(R.id.etMotivationalMessage);
        etNotificationHours = findViewById(R.id.etNotificationHours);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void initializeData() {
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        notificationHelper = new NotificationHelper(this);
    }

    private void loadCurrentSettings() {
        // Cargar configuraciones actuales
        String userName = sharedPreferences.getString(KEY_USER_NAME, "");
        String motivationalMessage = sharedPreferences.getString(KEY_MOTIVATIONAL_MESSAGE,
                "¡Hoy es un gran día para formar buenos hábitos!");
        int notificationHours = sharedPreferences.getInt(KEY_NOTIFICATION_HOURS, 2);

        etUserName.setText(userName);
        etMotivationalMessage.setText(motivationalMessage);
        etNotificationHours.setText(String.valueOf(notificationHours));
    }

    private void setupClickListeners() {
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void saveSettings() {
        String userName = etUserName.getText().toString().trim();
        String motivationalMessage = etMotivationalMessage.getText().toString().trim();
        String notificationHoursStr = etNotificationHours.getText().toString().trim();

        // Validaciones
        if (userName.isEmpty()) {
            etUserName.setError("Por favor ingresa tu nombre");
            etUserName.requestFocus();
            return;
        }

        if (motivationalMessage.isEmpty()) {
            etMotivationalMessage.setError("Por favor ingresa un mensaje motivacional");
            etMotivationalMessage.requestFocus();
            return;
        }

        int notificationHours = 2; // Valor por defecto
        try {
            notificationHours = Integer.parseInt(notificationHoursStr);
            if (notificationHours <= 0 || notificationHours > 24) {
                etNotificationHours.setError("Ingresa un número entre 1 y 24");
                etNotificationHours.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etNotificationHours.setError("Ingresa un número válido");
            etNotificationHours.requestFocus();
            return;
        }

        // Cancelar notificaciones motivacionales anteriores
        cancelPreviousMotivationalNotifications();

        // Guardar en SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_NAME, userName);
        editor.putString(KEY_MOTIVATIONAL_MESSAGE, motivationalMessage);
        editor.putInt(KEY_NOTIFICATION_HOURS, notificationHours);
        editor.putBoolean(KEY_MOTIVATIONAL_ENABLED, true);
        editor.apply();

        // Programar nuevas notificaciones motivacionales
        scheduleMotivationalNotifications(motivationalMessage, notificationHours);

        Toast.makeText(this, "Configuración guardada correctamente", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void cancelPreviousMotivationalNotifications() {
        try {
            notificationHelper.cancelMotivationalNotifications();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void scheduleMotivationalNotifications(String message, int hours) {
        try {
            notificationHelper.scheduleMotivationalNotifications(message, hours);
            Toast.makeText(this, "Notificaciones motivacionales programadas cada " + hours + " horas",
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al programar notificaciones motivacionales",
                    Toast.LENGTH_SHORT).show();
        }
    }
}