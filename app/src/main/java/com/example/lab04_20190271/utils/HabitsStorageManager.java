package com.example.lab04_20190271.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.lab04_20190271.model.Habit;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class HabitsStorageManager {

    private static final String PREFS_NAME = "HabitsAppPrefs";
    private static final String KEY_HABITS_LIST = "habits_list";
    private static final String KEY_HABITS_BACKUP = "habits_list_backup";
    private static final String KEY_LAST_SAVE_TIME = "last_save_time";
    private static final String KEY_DATA_VERSION = "data_version";
    private static final int CURRENT_DATA_VERSION = 1;

    private Context context;
    private SharedPreferences sharedPreferences;
    private Gson gson;

    public HabitsStorageManager(Context context) {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    /**
     * Guardar lista de hábitos con backup automático
     */
    public boolean saveHabits(List<Habit> habits) {
        try {
            Log.d("HabitsStorage", "Guardando " + habits.size() + " hábitos");

            // Crear backup de la versión anterior antes de guardar
            createBackup();

            // Convertir a JSON
            String habitsJson = gson.toJson(habits);

            // Guardar en SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_HABITS_LIST, habitsJson);
            editor.putLong(KEY_LAST_SAVE_TIME, System.currentTimeMillis());
            editor.putInt(KEY_DATA_VERSION, CURRENT_DATA_VERSION);

            boolean success = editor.commit(); // Usar commit para asegurar que se guarde inmediatamente

            if (success) {
                Log.d("HabitsStorage", "Hábitos guardados exitosamente");
                validateSavedData(habits);
            } else {
                Log.e("HabitsStorage", "Error al guardar hábitos en SharedPreferences");
            }

            return success;

        } catch (Exception e) {
            Log.e("HabitsStorage", "Error guardando hábitos: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cargar lista de hábitos con recuperación automática
     */
    public List<Habit> loadHabits() {
        try {
            Log.d("HabitsStorage", "Cargando hábitos...");

            String habitsJson = sharedPreferences.getString(KEY_HABITS_LIST, "");

            if (habitsJson.isEmpty()) {
                Log.d("HabitsStorage", "No hay hábitos guardados");
                return new ArrayList<>();
            }

            // Verificar versión de datos
            int dataVersion = sharedPreferences.getInt(KEY_DATA_VERSION, 0);
            if (dataVersion < CURRENT_DATA_VERSION) {
                Log.d("HabitsStorage", "Migrando datos de versión " + dataVersion + " a " + CURRENT_DATA_VERSION);
                habitsJson = migrateData(habitsJson, dataVersion);
            }

            // Parsear JSON
            Type listType = new TypeToken<List<Habit>>(){}.getType();
            List<Habit> habits = gson.fromJson(habitsJson, listType);

            if (habits == null) {
                Log.w("HabitsStorage", "JSON parseado es null, intentando recuperar backup");
                return loadFromBackup();
            }

            // Validar datos cargados
            List<Habit> validHabits = validateAndCleanHabits(habits);

            Log.d("HabitsStorage", "Cargados " + validHabits.size() + " hábitos válidos");
            return validHabits;

        } catch (JsonSyntaxException e) {
            Log.e("HabitsStorage", "Error de JSON, intentando recuperar backup: " + e.getMessage());
            return loadFromBackup();
        } catch (Exception e) {
            Log.e("HabitsStorage", "Error cargando hábitos: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Crear backup antes de guardar
     */
    private void createBackup() {
        try {
            String currentData = sharedPreferences.getString(KEY_HABITS_LIST, "");
            if (!currentData.isEmpty()) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(KEY_HABITS_BACKUP, currentData);
                editor.apply();
                Log.d("HabitsStorage", "Backup creado");
            }
        } catch (Exception e) {
            Log.e("HabitsStorage", "Error creando backup: " + e.getMessage());
        }
    }

    /**
     * Cargar desde backup si falla la carga principal
     */
    private List<Habit> loadFromBackup() {
        try {
            Log.d("HabitsStorage", "Intentando cargar desde backup");
            String backupJson = sharedPreferences.getString(KEY_HABITS_BACKUP, "");

            if (!backupJson.isEmpty()) {
                Type listType = new TypeToken<List<Habit>>(){}.getType();
                List<Habit> habits = gson.fromJson(backupJson, listType);

                if (habits != null) {
                    Log.d("HabitsStorage", "Backup cargado exitosamente: " + habits.size() + " hábitos");
                    return validateAndCleanHabits(habits);
                }
            }

            Log.w("HabitsStorage", "No hay backup disponible");
            return new ArrayList<>();

        } catch (Exception e) {
            Log.e("HabitsStorage", "Error cargando backup: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Validar y limpiar hábitos cargados
     */
    private List<Habit> validateAndCleanHabits(List<Habit> habits) {
        List<Habit> validHabits = new ArrayList<>();

        for (Habit habit : habits) {
            if (isValidHabit(habit)) {
                validHabits.add(habit);
            } else {
                Log.w("HabitsStorage", "Hábito inválido encontrado: " + habit);
            }
        }

        return validHabits;
    }

    /**
     * Validar que un hábito tenga datos correctos
     */
    private boolean isValidHabit(Habit habit) {
        return habit != null &&
                habit.getId() != null && !habit.getId().isEmpty() &&
                habit.getName() != null && !habit.getName().isEmpty() &&
                habit.getCategory() != null && !habit.getCategory().isEmpty() &&
                habit.getFrequencyHours() > 0 &&
                habit.getStartDate() != null && !habit.getStartDate().isEmpty() &&
                habit.getStartTime() != null && !habit.getStartTime().isEmpty();
    }

    /**
     * Validar que los datos guardados coincidan con los originales
     */
    private void validateSavedData(List<Habit> originalHabits) {
        try {
            List<Habit> savedHabits = loadHabits();
            if (savedHabits.size() != originalHabits.size()) {
                Log.w("HabitsStorage", "Advertencia: Tamaño de datos guardados no coincide");
            }
        } catch (Exception e) {
            Log.e("HabitsStorage", "Error validando datos guardados: " + e.getMessage());
        }
    }

    /**
     * Migrar datos de versiones anteriores
     */
    private String migrateData(String oldData, int fromVersion) {
        // Placeholder para futuras migraciones
        // Por ahora simplemente retorna los datos originales
        return oldData;
    }

    /**
     * Eliminar un hábito específico
     */
    public boolean deleteHabit(String habitId, List<Habit> currentHabits) {
        try {
            List<Habit> updatedHabits = new ArrayList<>();
            boolean found = false;

            for (Habit habit : currentHabits) {
                if (!habit.getId().equals(habitId)) {
                    updatedHabits.add(habit);
                } else {
                    found = true;
                    Log.d("HabitsStorage", "Eliminando hábito: " + habit.getName());
                }
            }

            if (found) {
                return saveHabits(updatedHabits);
            } else {
                Log.w("HabitsStorage", "Hábito no encontrado para eliminar: " + habitId);
                return false;
            }

        } catch (Exception e) {
            Log.e("HabitsStorage", "Error eliminando hábito: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtener información de almacenamiento
     */
    public String getStorageInfo() {
        long lastSave = sharedPreferences.getLong(KEY_LAST_SAVE_TIME, 0);
        int version = sharedPreferences.getInt(KEY_DATA_VERSION, 0);
        boolean hasBackup = !sharedPreferences.getString(KEY_HABITS_BACKUP, "").isEmpty();

        return "Último guardado: " + new java.util.Date(lastSave) + "\n" +
                "Versión de datos: " + version + "\n" +
                "Backup disponible: " + (hasBackup ? "Sí" : "No");
    }

    /**
     * Limpiar todos los datos (para testing)
     */
    public void clearAllData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_HABITS_LIST);
        editor.remove(KEY_HABITS_BACKUP);
        editor.remove(KEY_LAST_SAVE_TIME);
        editor.apply();
        Log.d("HabitsStorage", "Todos los datos eliminados");
    }
}