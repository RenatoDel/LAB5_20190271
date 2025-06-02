package com.example.lab04_20190271.model;

import com.example.lab04_20190271.R;

import java.io.Serializable;

public class Habit implements Serializable {
    private String id;
    private String name;
    private String category;
    private int frequencyHours;
    private String startDate;
    private String startTime;

    // Constructor vacío
    public Habit() {
    }

    // Constructor completo
    public Habit(String id, String name, String category, int frequencyHours,
                 String startDate, String startTime) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.frequencyHours = frequencyHours;
        this.startDate = startDate;
        this.startTime = startTime;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public int getFrequencyHours() {
        return frequencyHours;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getStartTime() {
        return startTime;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setFrequencyHours(int frequencyHours) {
        this.frequencyHours = frequencyHours;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    // Método para obtener el texto de frecuencia formateado
    public String getFrequencyText() {
        if (frequencyHours == 1) {
            return "Cada minuto";
        } else if (frequencyHours == 60) {
            return "Cada hora";
        } else if (frequencyHours == 1440) {
            return "Diariamente";
        } else if (frequencyHours >= 60) {
            int hours = frequencyHours / 60;
            return "Cada " + hours + (hours == 1 ? " hora" : " horas");
        } else {
            return "Cada " + frequencyHours + (frequencyHours == 1 ? " minuto" : " minutos");
        }
    }

    // Método para obtener el ícono según la categoría
    public int getCategoryIcon() {
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

    // Método para obtener el color según la categoría
    public int getCategoryColor() {
        switch (category.toLowerCase()) {
            case "ejercicio":
                return R.color.exercise_color;
            case "alimentación":
                return R.color.food_color;
            case "sueño":
                return R.color.sleep_color;
            case "lectura":
                return R.color.reading_color;
            case "trabajo":
                return R.color.work_color;
            case "meditación":
                return R.color.meditation_color;
            default:
                return R.color.default_color;
        }
    }

    @Override
    public String toString() {
        return "Habit{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", frequencyHours=" + frequencyHours +
                ", startDate='" + startDate + '\'' +
                ", startTime='" + startTime + '\'' +
                '}';
    }
}
