package com.example.lab04_20190271.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.lab04_20190271.MainActivity;
import com.example.lab04_20190271.R;
import com.example.lab04_20190271.model.Habit;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("NotificationReceiver", "Receiver ejecutado!");

        // Obtener datos del hábito
        String habitId = intent.getStringExtra("habit_id");
        String habitName = intent.getStringExtra("habit_name");
        String habitCategory = intent.getStringExtra("habit_category");
        String suggestedAction = intent.getStringExtra("suggested_action");

        Log.d("NotificationReceiver", "Hábito: " + habitName + ", Categoría: " + habitCategory);

        // Crear la notificación
        showHabitNotification(context, habitId, habitName, habitCategory, suggestedAction);

        // ✅ REPROGRAMAR la siguiente notificación
        rescheduleNotification(context, intent);
    }

    private void showHabitNotification(Context context, String habitId, String habitName,
                                       String habitCategory, String suggestedAction) {

        NotificationHelper notificationHelper = new NotificationHelper(context);
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Intent para abrir la app cuando se toque la notificación
        Intent openAppIntent = new Intent(context, MainActivity.class);
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Obtener canal apropiado y configuración
        String channelId = notificationHelper.getChannelForCategory(habitCategory);
        int iconResource = getCategoryIcon(habitCategory);

        // Construir la notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(iconResource)
                .setContentTitle(habitName)
                .setContentText(suggestedAction)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(suggestedAction))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        // Personalizar según categoría
        customizeNotificationByCategory(builder, habitCategory);

        // Mostrar notificación con ID único
        int notificationId = habitId.hashCode();
        notificationManager.notify(notificationId, builder.build());

        Log.d("NotificationReceiver", "Enviando notificación con ID: " + notificationId);
        notificationManager.notify(notificationId, builder.build());
        Log.d("NotificationReceiver", "Notificación enviada!");
    }

    private void customizeNotificationByCategory(NotificationCompat.Builder builder, String category) {
        switch (category.toLowerCase()) {
            case "ejercicio":
                builder.setColor(0xFFE74C3C); // Rojo
                builder.setLights(0xFFE74C3C, 1000, 1000);
                break;
            case "alimentación":
                builder.setColor(0xFFF39C12); // Naranja
                builder.setLights(0xFFF39C12, 1000, 1000);
                break;
            case "sueño":
                builder.setColor(0xFF8E44AD); // Púrpura
                builder.setPriority(NotificationCompat.PRIORITY_DEFAULT); // Menos intrusivo
                break;
            case "lectura":
                builder.setColor(0xFF27AE60); // Verde
                builder.setLights(0xFF27AE60, 1000, 1000);
                break;
            case "trabajo":
                builder.setColor(0xFF34495E); // Azul oscuro
                builder.setLights(0xFF34495E, 1000, 1000);
                break;
            case "meditación":
                builder.setColor(0xFF16A085); // Verde azulado
                builder.setPriority(NotificationCompat.PRIORITY_DEFAULT); // Menos intrusivo
                break;
            default:
                builder.setColor(0xFF3498DB); // Azul por defecto
                break;
        }
    }

    private int getCategoryIcon(String category) {
        switch (category.toLowerCase()) {
            case "ejercicio":
                return R.drawable.ic_exercise;
            case "alimentación":
                return R.drawable.ic_food;
            case "sueño":
                return R.drawable.ic_sleep;
            case "lectura":
                return R.drawable.ic_book;
            case "trabajo":
                return R.drawable.ic_work;
            case "meditación":
                return R.drawable.ic_meditation;
            default:
                return R.drawable.ic_habit_default;
        }
    }

    private void rescheduleNotification(Context context, Intent originalIntent) {
        try {
            String habitId = originalIntent.getStringExtra("habit_id");

            // Cargar el hábito desde SharedPreferences para obtener su frecuencia
            SharedPreferences prefs = context.getSharedPreferences("HabitsAppPrefs", Context.MODE_PRIVATE);
            String habitsJson = prefs.getString("habits_list", "");

            if (!habitsJson.isEmpty()) {
                // Buscar el hábito específico
                Gson gson = new Gson();
                Type listType = new TypeToken<List<Habit>>(){}.getType();
                List<Habit> habits = gson.fromJson(habitsJson, listType);

                for (com.example.lab04_20190271.model.Habit habit : habits) {
                    if (habit.getId().equals(habitId)) {
                        // Reprogramar la siguiente notificación
                        NotificationHelper helper = new NotificationHelper(context);
                        helper.scheduleHabitNotification(habit);
                        Log.d("NotificationReceiver", "Reprogramada notificación para: " + habit.getName());
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Log.e("NotificationReceiver", "Error reprogramando notificación: " + e.getMessage());
            e.printStackTrace();
        }
    }
}