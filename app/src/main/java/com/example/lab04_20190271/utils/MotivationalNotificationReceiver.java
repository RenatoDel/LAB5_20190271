package com.example.lab04_20190271.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.example.lab04_20190271.MainActivity;
import com.example.lab04_20190271.R;

import java.util.Random;

public class MotivationalNotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String motivationalMessage = intent.getStringExtra("motivational_message");
        boolean scheduleNext = intent.getBooleanExtra("schedule_next", false);
        int hoursInterval = intent.getIntExtra("hours_interval", 2);

        // Mostrar la notificación motivacional
        showMotivationalNotification(context, motivationalMessage);

        // Si necesita programar la siguiente (para Android 6.0+)
        if (scheduleNext) {
            NotificationHelper notificationHelper = new NotificationHelper(context);
            notificationHelper.scheduleMotivationalNotifications(motivationalMessage, hoursInterval);
        }
    }

    private void showMotivationalNotification(Context context, String message) {
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

        // Construir la notificación motivacional
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context,
                NotificationHelper.CHANNEL_MOTIVATIONAL)
                .setSmallIcon(R.drawable.ic_habit_default)
                .setContentTitle("💪 Mensaje Motivacional")
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setColor(0xFF3498DB); // Azul motivacional

        // Agregar emojis motivacionales aleatorios
        String[] motivationalEmojis = {"💪", "🌟", "🚀", "✨", "🎯", "🌈", "🔥", "⭐"};
        Random random = new Random();
        String emoji = motivationalEmojis[random.nextInt(motivationalEmojis.length)];

        builder.setContentTitle(emoji + " ¡Momento de Motivación! " + emoji);

        // Mostrar notificación con ID único
        int notificationId = 12345; // ID fijo para notificaciones motivacionales
        notificationManager.notify(notificationId, builder.build());
    }
}