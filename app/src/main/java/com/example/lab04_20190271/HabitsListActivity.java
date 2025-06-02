package com.example.lab04_20190271;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab04_20190271.utils.HabitsStorageManager;
import com.example.lab04_20190271.adapters.HabitsAdapter;
import com.example.lab04_20190271.model.Habit;
import com.example.lab04_20190271.utils.NotificationHelper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class HabitsListActivity extends AppCompatActivity implements HabitsAdapter.OnHabitActionListener {

    private static final String PREFS_NAME = "HabitsAppPrefs";
    private static final String KEY_HABITS_LIST = "habits_list";
    private static final int ADD_HABIT_REQUEST_CODE = 200;

    private RecyclerView recyclerViewHabits;
    private LinearLayout layoutEmptyState;
    private Button btnAddHabit;

    private HabitsStorageManager storageManager;
    private HabitsAdapter habitsAdapter;
    private List<Habit> habitsList;
    private SharedPreferences sharedPreferences;
    private Gson gson;
    private NotificationHelper notificationHelper; // ✅ Nuevo

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habits_list);

        initializeViews();
        initializeData();
        setupRecyclerView();
        setupClickListeners();
        loadHabits();
    }

    private void initializeViews() {
        recyclerViewHabits = findViewById(R.id.recyclerViewHabits);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        btnAddHabit = findViewById(R.id.btnAddHabit);
    }

    private void initializeData() {
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        gson = new Gson();
        habitsList = new ArrayList<>();
        notificationHelper = new NotificationHelper(this);
        storageManager = new HabitsStorageManager(this); // ✅ Nuevo
    }

    private void setupRecyclerView() {
        habitsAdapter = new HabitsAdapter(this, habitsList);
        habitsAdapter.setOnHabitActionListener(this);

        recyclerViewHabits.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewHabits.setAdapter(habitsAdapter);
    }

    private void setupClickListeners() {
        btnAddHabit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HabitsListActivity.this, CreateHabitActivity.class);
                startActivityForResult(intent, ADD_HABIT_REQUEST_CODE);
            }
        });
    }

    private void loadHabits() {
        try {
            Log.d("HabitsListActivity", "Cargando hábitos...");

            // Usar el gestor mejorado de almacenamiento
            List<Habit> loadedHabits = storageManager.loadHabits();

            habitsList.clear();
            habitsList.addAll(loadedHabits);
            habitsAdapter.notifyDataSetChanged();

            updateEmptyStateVisibility();

            Log.d("HabitsListActivity", "Hábitos cargados: " + loadedHabits.size());

            // Mostrar información de almacenamiento en logs
            Log.d("HabitsListActivity", "Info almacenamiento:\n" + storageManager.getStorageInfo());

        } catch (Exception e) {
            Log.e("HabitsListActivity", "Error cargando hábitos: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error al cargar los hábitos", Toast.LENGTH_SHORT).show();

            // En caso de error, asegurar que la lista esté vacía pero funcional
            habitsList.clear();
            habitsAdapter.notifyDataSetChanged();
            updateEmptyStateVisibility();
        }
    }

    private void saveHabits() {
        try {
            Log.d("HabitsListActivity", "Guardando hábitos...");

            boolean success = storageManager.saveHabits(habitsList);

            if (!success) {
                Log.e("HabitsListActivity", "Error al guardar los hábitos");
                Toast.makeText(this, "Error al guardar los hábitos", Toast.LENGTH_SHORT).show();
            } else {
                Log.d("HabitsListActivity", "Hábitos guardados exitosamente");
            }

        } catch (Exception e) {
            Log.e("HabitsListActivity", "Excepción guardando hábitos: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error al guardar los hábitos", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateEmptyStateVisibility() {
        if (habitsList.isEmpty()) {
            recyclerViewHabits.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerViewHabits.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
        }
    }

    @Override
    public void onHabitClick(Habit habit) {
        Toast.makeText(this, "Hábito: " + habit.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteHabit(Habit habit) {
        showDeleteConfirmationDialog(habit);
    }

    private void showDeleteConfirmationDialog(Habit habit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Eliminar Hábito");
        builder.setMessage("¿Estás seguro de que quieres eliminar el hábito \"" + habit.getName() + "\"?");

        builder.setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteHabit(habit);
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // ✅ REEMPLAZAR el método deleteHabit en HabitsListActivity por este:

    private void deleteHabit(Habit habit) {
        try {
            Log.d("HabitsListActivity", "Eliminando hábito: " + habit.getName());

            // Cancelar notificaciones antes de eliminar
            try {
                cancelHabitNotifications(habit);
            } catch (Exception e) {
                Log.e("HabitsListActivity", "Error cancelando notificaciones: " + e.getMessage());
            }

            // Eliminar usando el gestor de almacenamiento
            boolean success = storageManager.deleteHabit(habit.getId(), habitsList);

            if (success) {
                // Actualizar la lista local
                int position = habitsList.indexOf(habit);
                if (position != -1) {
                    habitsList.remove(position);

                    // Actualizar adapter de forma segura
                    try {
                        habitsAdapter.notifyItemRemoved(position);
                        if (position < habitsList.size()) {
                            habitsAdapter.notifyItemRangeChanged(position, habitsList.size() - position);
                        }
                    } catch (Exception e) {
                        habitsAdapter.notifyDataSetChanged();
                    }
                }

                updateEmptyStateVisibility();
                Toast.makeText(this, "Hábito eliminado correctamente", Toast.LENGTH_SHORT).show();
                Log.d("HabitsListActivity", "Hábito eliminado exitosamente");

            } else {
                Toast.makeText(this, "Error al eliminar el hábito", Toast.LENGTH_SHORT).show();
                Log.e("HabitsListActivity", "Error eliminando hábito del almacenamiento");
            }

        } catch (Exception e) {
            Log.e("HabitsListActivity", "Error eliminando hábito: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error al eliminar el hábito", Toast.LENGTH_SHORT).show();

            // Recargar la lista en caso de error para mantener consistencia
            loadHabits();
        }
    }

    private void cancelHabitNotifications(Habit habit) {
        try {
            if (notificationHelper != null && habit != null) {
                notificationHelper.cancelHabitNotification(habit);
                Log.d("HabitsListActivity", "Notificaciones canceladas para: " + habit.getName());
            }
        } catch (Exception e) {
            Log.e("HabitsListActivity", "Error cancelando notificaciones: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_HABIT_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null && data.hasExtra("new_habit")) {
                Habit newHabit = (Habit) data.getSerializableExtra("new_habit");
                if (newHabit != null) {
                    addNewHabit(newHabit);
                }
            }
        }
    }

    private void addNewHabit(Habit habit) {
        try {
            Log.d("HabitsListActivity", "Agregando nuevo hábito: " + habit.getName());

            habitsList.add(habit);
            habitsAdapter.notifyItemInserted(habitsList.size() - 1);

            // Guardar inmediatamente
            boolean saved = storageManager.saveHabits(habitsList);

            if (saved) {
                updateEmptyStateVisibility();

                // Programar notificaciones para el nuevo hábito
                scheduleHabitNotifications(habit);

                Toast.makeText(this, "Hábito creado correctamente", Toast.LENGTH_SHORT).show();
                Log.d("HabitsListActivity", "Nuevo hábito agregado y guardado");
            } else {
                // Si falla el guardado, remover de la lista
                habitsList.remove(habit);
                habitsAdapter.notifyItemRemoved(habitsList.size());
                Toast.makeText(this, "Error al guardar el hábito", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e("HabitsListActivity", "Error agregando hábito: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error al crear el hábito", Toast.LENGTH_SHORT).show();
        }
    }

    private void scheduleHabitNotifications(Habit habit) {
        // ✅ Implementación real
        try {
            notificationHelper.scheduleHabitNotification(habit);
            Toast.makeText(this, "Recordatorios programados para " + habit.getName(),
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al programar recordatorios", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHabits();
    }
}