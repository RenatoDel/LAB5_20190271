package com.example.lab04_20190271.utils;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.lab04_20190271.model.Habit;

import java.util.Calendar;

public class NotificationHelper {

    // IDs de canales de notificación
    public static final String CHANNEL_EXERCISE = "exercise_channel";
    public static final String CHANNEL_FOOD = "food_channel";
    public static final String CHANNEL_SLEEP = "sleep_channel";
    public static final String CHANNEL_READING = "reading_channel";
    public static final String CHANNEL_WORK = "work_channel";
    public static final String CHANNEL_MEDITATION = "meditation_channel";
    public static final String CHANNEL_MOTIVATIONAL = "motivational_channel";
    public static final String CHANNEL_DEFAULT = "default_channel";

    private Context context;
    private NotificationManager notificationManager;

    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannels();
    }

    /**
     * Crear todos los canales de notificación (Android 8.0+)
     */
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            // Canal de Ejercicio
            NotificationChannel exerciseChannel = new NotificationChannel(
                    CHANNEL_EXERCISE,
                    "Ejercicio",
                    NotificationManager.IMPORTANCE_HIGH
            );
            exerciseChannel.setDescription("Recordatorios de ejercicio");
            exerciseChannel.enableVibration(true);
            exerciseChannel.setVibrationPattern(new long[]{100, 200, 300, 400});
            notificationManager.createNotificationChannel(exerciseChannel);

            // Canal de Alimentación
            NotificationChannel foodChannel = new NotificationChannel(
                    CHANNEL_FOOD,
                    "Alimentación",
                    NotificationManager.IMPORTANCE_HIGH
            );
            foodChannel.setDescription("Recordatorios de alimentación");
            foodChannel.enableVibration(true);
            foodChannel.setVibrationPattern(new long[]{200, 200, 200});
            notificationManager.createNotificationChannel(foodChannel);

            // Canal de Sueño
            NotificationChannel sleepChannel = new NotificationChannel(
                    CHANNEL_SLEEP,
                    "Sueño",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            sleepChannel.setDescription("Recordatorios de sueño");
            sleepChannel.enableVibration(false); // Sueño sin vibración fuerte
            notificationManager.createNotificationChannel(sleepChannel);

            // Canal de Lectura
            NotificationChannel readingChannel = new NotificationChannel(
                    CHANNEL_READING,
                    "Lectura",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            readingChannel.setDescription("Recordatorios de lectura");
            readingChannel.enableVibration(true);
            notificationManager.createNotificationChannel(readingChannel);

            // Canal de Trabajo
            NotificationChannel workChannel = new NotificationChannel(
                    CHANNEL_WORK,
                    "Trabajo",
                    NotificationManager.IMPORTANCE_HIGH
            );
            workChannel.setDescription("Recordatorios de trabajo");
            workChannel.enableVibration(true);
            notificationManager.createNotificationChannel(workChannel);

            // Canal de Meditación
            NotificationChannel meditationChannel = new NotificationChannel(
                    CHANNEL_MEDITATION,
                    "Meditación",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            meditationChannel.setDescription("Recordatorios de meditación");
            meditationChannel.enableVibration(false); // Meditación sin vibración
            notificationManager.createNotificationChannel(meditationChannel);

            // Canal Motivacional
            NotificationChannel motivationalChannel = new NotificationChannel(
                    CHANNEL_MOTIVATIONAL,
                    "Motivacional",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            motivationalChannel.setDescription("Mensajes motivacionales");
            motivationalChannel.enableVibration(true);
            notificationManager.createNotificationChannel(motivationalChannel);

            // Canal por defecto
            NotificationChannel defaultChannel = new NotificationChannel(
                    CHANNEL_DEFAULT,
                    "General",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            defaultChannel.setDescription("Notificaciones generales");
            notificationManager.createNotificationChannel(defaultChannel);
        }
    }

    /**
     * Obtener el canal apropiado según la categoría del hábito
     */
    public String getChannelForCategory(String category) {
        switch (category.toLowerCase()) {
            case "ejercicio":
                return CHANNEL_EXERCISE;
            case "alimentación":
                return CHANNEL_FOOD;
            case "sueño":
                return CHANNEL_SLEEP;
            case "lectura":
                return CHANNEL_READING;
            case "trabajo":
                return CHANNEL_WORK;
            case "meditación":
                return CHANNEL_MEDITATION;
            default:
                return CHANNEL_DEFAULT;
        }
    }


    public void scheduleHabitNotification(Habit habit) {
        try {
            Log.d("NotificationHelper", "Programando notificación para: " + habit.getName());
            Log.d("NotificationHelper", "Frecuencia: " + habit.getFrequencyHours() + " minutos");

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            // Verificar si podemos programar alarmas exactas
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    Log.w("NotificationHelper", "No se pueden programar alarmas exactas. Redirigiendo a configuración...");
                    requestExactAlarmPermission();
                    return;
                }
            }

            // Crear intent para el receptor de notificaciones
            Intent intent = new Intent(context, com.example.lab04_20190271.utils.NotificationReceiver.class);
            intent.putExtra("habit_id", habit.getId());
            intent.putExtra("habit_name", habit.getName());
            intent.putExtra("habit_category", habit.getCategory());
            intent.putExtra("suggested_action", getSuggestedAction(habit));

            // PendingIntent único para cada hábito
            int requestCode = habit.getId().hashCode();
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // ✅ CALCULAR tiempo usando la frecuencia REAL del hábito
            Calendar nextNotification = Calendar.getInstance();

            // Si la frecuencia es menor a 60 minutos, usar minutos
            if (habit.getFrequencyHours() < 60) {
                nextNotification.add(Calendar.MINUTE, habit.getFrequencyHours());
                Log.d("NotificationHelper", "Próxima notificación en " + habit.getFrequencyHours() + " minutos: " + nextNotification.getTime());
            } else {
                // Si es 60 o más, convertir a horas
                int hours = habit.getFrequencyHours() / 60;
                nextNotification.add(Calendar.HOUR_OF_DAY, hours);
                Log.d("NotificationHelper", "Próxima notificación en " + hours + " horas: " + nextNotification.getTime());
            }

            // Programar la alarma
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        nextNotification.getTimeInMillis(),
                        pendingIntent
                );
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        nextNotification.getTimeInMillis(),
                        pendingIntent
                );
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        nextNotification.getTimeInMillis(),
                        pendingIntent
                );
            }

            Log.d("NotificationHelper", "Alarma programada exitosamente para: " + nextNotification.getTime());

        } catch (SecurityException e) {
            Log.e("NotificationHelper", "SecurityException: " + e.getMessage());
        } catch (Exception e) {
            Log.e("NotificationHelper", "Error programando notificación: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ✅ AGREGAR este nuevo método:
    private void requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.setData(android.net.Uri.parse("package:" + context.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } catch (Exception e) {
                Log.e("NotificationHelper", "Error abriendo configuración de alarmas: " + e.getMessage());
            }
        }
    }



    /**
     * Cancelar notificaciones de un hábito
     */
    public void cancelHabitNotification(Habit habit) {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            Intent intent = new Intent(context, NotificationReceiver.class);
            int requestCode = habit.getId().hashCode();
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            alarmManager.cancel(pendingIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Parsear fecha y hora del hábito
     */
    private Calendar parseHabitDateTime(Habit habit) {
        Calendar calendar = Calendar.getInstance();

        try {
            // Parsear fecha (formato: dd/MM/yyyy)
            String[] dateParts = habit.getStartDate().split("/");
            int day = Integer.parseInt(dateParts[0]);
            int month = Integer.parseInt(dateParts[1]) - 1; // Mes base 0
            int year = Integer.parseInt(dateParts[2]);

            // Parsear hora (formato: HH:mm)
            String[] timeParts = habit.getStartTime().split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            calendar.set(year, month, day, hour, minute, 0);
            calendar.set(Calendar.MILLISECOND, 0);

        } catch (Exception e) {
            e.printStackTrace();
            // Si hay error, usar tiempo actual + frecuencia
            calendar.add(Calendar.HOUR_OF_DAY, habit.getFrequencyHours());
        }

        return calendar;
    }

    /**
     * Generar acción sugerida según la categoría
     */
    private String getSuggestedAction(Habit habit) {
        String category = habit.getCategory().toLowerCase();
        String habitName = habit.getName();

        switch (category) {
            case "ejercicio":
                return "¡Hora de " + habitName + "! Mueve tu cuerpo";
            case "alimentación":
                return "Recuerda: " + habitName;
            case "sueño":
                return "Es momento de " + habitName;
            case "lectura":
                return "Tiempo de " + habitName + " 📚";
            case "trabajo":
                return "Productividad: " + habitName;
            case "meditación":
                return "Momento zen: " + habitName + " 🧘";
            default:
                return "Recordatorio: " + habitName;
        }
    }
    // ✅ AGREGAR estos métodos al final de la clase NotificationHelper

    private static final int MOTIVATIONAL_REQUEST_CODE = 9999;

    /**
     * Programar notificaciones motivacionales
     */
    public void scheduleMotivationalNotifications(String message, int hours) {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            // Crear intent para el receptor de notificaciones motivacionales
            Intent intent = new Intent(context, MotivationalNotificationReceiver.class);
            intent.putExtra("motivational_message", message);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    MOTIVATIONAL_REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Calcular tiempo de la primera notificación (ahora + horas especificadas)
            Calendar startTime = Calendar.getInstance();
            startTime.add(Calendar.HOUR_OF_DAY, hours);

            // Programar alarma repetitiva
            long intervalMillis = hours * 60 * 60 * 1000L; // Horas a milisegundos

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Para Android 6.0+ usamos setExactAndAllowWhileIdle
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        startTime.getTimeInMillis(),
                        pendingIntent
                );

                // Programar la siguiente notificación desde el receiver
                scheduleNextMotivationalNotification(startTime.getTimeInMillis() + intervalMillis,
                        message, hours);
            } else {
                alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        startTime.getTimeInMillis(),
                        intervalMillis,
                        pendingIntent
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Programar la siguiente notificación motivacional (para Android 6.0+)
     */
    private void scheduleNextMotivationalNotification(long triggerAtMillis, String message, int hours) {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            Intent intent = new Intent(context, MotivationalNotificationReceiver.class);
            intent.putExtra("motivational_message", message);
            intent.putExtra("hours_interval", hours);
            intent.putExtra("schedule_next", true);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    MOTIVATIONAL_REQUEST_CODE + 1,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Cancelar notificaciones motivacionales
     */
    public void cancelMotivationalNotifications() {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            // Cancelar notificación principal
            Intent intent = new Intent(context, MotivationalNotificationReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    MOTIVATIONAL_REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            alarmManager.cancel(pendingIntent);

            // Cancelar notificación de repetición
            PendingIntent pendingIntentNext = PendingIntent.getBroadcast(
                    context,
                    MOTIVATIONAL_REQUEST_CODE + 1,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            alarmManager.cancel(pendingIntentNext);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}