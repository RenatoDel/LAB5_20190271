package com.example.lab04_20190271;


import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lab04_20190271.model.Habit;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

public class CreateHabitActivity extends AppCompatActivity {

    private EditText etHabitName;
    private Spinner spinnerCategory;
    private EditText etFrequencyHours;
    private Button btnSelectDate;
    private TextView tvSelectedDate;
    private Button btnSelectTime;
    private TextView tvSelectedTime;
    private Button btnCreateHabit;
    private Button btnCancel;

    private Calendar selectedDateTime;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;

    // Categorías disponibles
    private String[] categories = {
            "Selecciona una categoría",
            "Ejercicio",
            "Alimentación",
            "Sueño",
            "Lectura",
            "Trabajo",
            "Meditación",
            "Estudio",
            "Limpieza",
            "Socialización"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_habit);

        initializeViews();
        initializeData();
        setupSpinner();
        setupClickListeners();
    }

    private void initializeViews() {
        etHabitName = findViewById(R.id.etHabitName);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        etFrequencyHours = findViewById(R.id.etFrequencyHours);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        btnSelectTime = findViewById(R.id.btnSelectTime);
        tvSelectedTime = findViewById(R.id.tvSelectedTime);
        btnCreateHabit = findViewById(R.id.btnCreateHabit);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void initializeData() {
        selectedDateTime = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        // Establecer valores por defecto
        etFrequencyHours.setText("2");
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.spinner_item, categories);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnSelectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        btnSelectTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker();
            }
        });

        btnCreateHabit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createHabit();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void showDatePicker() {
        Calendar currentDate = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        selectedDateTime.set(Calendar.YEAR, year);
                        selectedDateTime.set(Calendar.MONTH, month);
                        selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateDateDisplay();
                    }
                },
                currentDate.get(Calendar.YEAR),
                currentDate.get(Calendar.MONTH),
                currentDate.get(Calendar.DAY_OF_MONTH)
        );

        // No permitir fechas pasadas
        datePickerDialog.getDatePicker().setMinDate(currentDate.getTimeInMillis());
        datePickerDialog.show();
    }

    private void showTimePicker() {
        Calendar currentTime = Calendar.getInstance();

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedDateTime.set(Calendar.MINUTE, minute);
                        updateTimeDisplay();
                    }
                },
                currentTime.get(Calendar.HOUR_OF_DAY),
                currentTime.get(Calendar.MINUTE),
                true
        );

        timePickerDialog.show();
    }

    private void updateDateDisplay() {
        String formattedDate = dateFormat.format(selectedDateTime.getTime());
        tvSelectedDate.setText(formattedDate);
        tvSelectedDate.setTextColor(getResources().getColor(R.color.text_primary));
    }

    private void updateTimeDisplay() {
        String formattedTime = timeFormat.format(selectedDateTime.getTime());
        tvSelectedTime.setText(formattedTime);
        tvSelectedTime.setTextColor(getResources().getColor(R.color.text_primary));
    }

    private void createHabit() {
        if (validateInputs()) {
            Habit newHabit = buildHabitFromInputs();

            // Devolver el hábito a la actividad anterior
            Intent resultIntent = new Intent();
            resultIntent.putExtra("new_habit", newHabit);
            setResult(RESULT_OK, resultIntent);

            Toast.makeText(this, "¡Hábito creado exitosamente!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private boolean validateInputs() {
        // Validar nombre del hábito
        String habitName = etHabitName.getText().toString().trim();
        if (habitName.isEmpty()) {
            etHabitName.setError("Por favor ingresa el nombre del hábito");
            etHabitName.requestFocus();
            return false;
        }

        // Validar categoría
        if (spinnerCategory.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Por favor selecciona una categoría", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validar frecuencia
        String frequencyStr = etFrequencyHours.getText().toString().trim();
        if (frequencyStr.isEmpty()) {
            etFrequencyHours.setError("Por favor ingresa la frecuencia");
            etFrequencyHours.requestFocus();
            return false;
        }

        try {
            int frequency = Integer.parseInt(frequencyStr);
            if (frequency <= 0 || frequency > 10080) { // 10080 minutos = 1 semana
                etFrequencyHours.setError("La frecuencia debe estar entre 1 y 10080 minutos");
                etFrequencyHours.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            etFrequencyHours.setError("Por favor ingresa un número válido");
            etFrequencyHours.requestFocus();
            return false;
        }

        // Validar fecha
        if (tvSelectedDate.getText().toString().equals("No seleccionada")) {
            Toast.makeText(this, "Por favor selecciona una fecha de inicio", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validar hora
        if (tvSelectedTime.getText().toString().equals("No seleccionada")) {
            Toast.makeText(this, "Por favor selecciona una hora de inicio", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validar que la fecha y hora no sean en el pasado
        Calendar now = Calendar.getInstance();
        if (selectedDateTime.before(now)) {
            Toast.makeText(this, "La fecha y hora de inicio no pueden ser en el pasado",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private Habit buildHabitFromInputs() {
        String id = UUID.randomUUID().toString();
        String name = etHabitName.getText().toString().trim();
        String category = categories[spinnerCategory.getSelectedItemPosition()];
        int frequencyHours = Integer.parseInt(etFrequencyHours.getText().toString().trim());
        String startDate = dateFormat.format(selectedDateTime.getTime());
        String startTime = timeFormat.format(selectedDateTime.getTime());

        return new Habit(id, name, category, frequencyHours, startDate, startTime);
    }
}