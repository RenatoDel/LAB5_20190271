package com.example.lab04_20190271.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab04_20190271.R;
import com.example.lab04_20190271.model.Habit;

import java.util.List;

public class HabitsAdapter extends RecyclerView.Adapter<HabitsAdapter.HabitViewHolder> {

    private Context context;
    private List<Habit> habitsList;
    private OnHabitActionListener listener;

    // Interface para manejar los clicks
    public interface OnHabitActionListener {
        void onHabitClick(Habit habit);
        void onDeleteHabit(Habit habit);
    }

    public HabitsAdapter(Context context, List<Habit> habitsList) {
        this.context = context;
        this.habitsList = habitsList;
    }

    public void setOnHabitActionListener(OnHabitActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_habit, parent, false);
        return new HabitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        Habit habit = habitsList.get(position);
        holder.bind(habit);
    }

    @Override
    public int getItemCount() {
        return habitsList.size();
    }

    public void updateHabits(List<Habit> newHabits) {
        this.habitsList = newHabits;
        notifyDataSetChanged();
    }

    // ✅ REEMPLAZAR el método removeHabit en HabitsAdapter por este:

    public void removeHabit(int position) {
        try {
            if (position >= 0 && position < habitsList.size()) {
                habitsList.remove(position);
                notifyItemRemoved(position);
                // Solo actualizar el rango si hay elementos restantes
                if (position < habitsList.size()) {
                    notifyItemRangeChanged(position, habitsList.size() - position);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Si hay error, actualizar toda la lista
            notifyDataSetChanged();
        }
    }

    class HabitViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivCategoryIcon;
        private TextView tvHabitName;
        private TextView tvCategory;
        private TextView tvFrequency;
        private TextView tvStartDateTime;
        private ImageButton btnDeleteHabit;

        public HabitViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            tvHabitName = itemView.findViewById(R.id.tvHabitName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvFrequency = itemView.findViewById(R.id.tvFrequency);
            tvStartDateTime = itemView.findViewById(R.id.tvStartDateTime);
            btnDeleteHabit = itemView.findViewById(R.id.btnDeleteHabit);

            // Click en el item completo
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onHabitClick(habitsList.get(position));
                        }
                    }
                }
            });

            // Click en botón de eliminar
            btnDeleteHabit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onDeleteHabit(habitsList.get(position));
                        }
                    }
                }
            });
        }

        public void bind(Habit habit) {
            // Establecer nombre del hábito
            tvHabitName.setText(habit.getName());

            // Establecer categoría
            tvCategory.setText(habit.getCategory());

            // Cambiar color del tag según la categoría
            int backgroundColor = getCategoryBackgroundColor(habit.getCategory());
            tvCategory.setBackgroundColor(ContextCompat.getColor(context, backgroundColor));

            // Establecer frecuencia
            tvFrequency.setText(habit.getFrequencyText());

            // Establecer fecha y hora de inicio
            String startDateTime = "Inicio: " + habit.getStartDate() + " - " + habit.getStartTime();
            tvStartDateTime.setText(startDateTime);

            // Establecer ícono de categoría
            int iconResource = getCategoryIcon(habit.getCategory());
            ivCategoryIcon.setImageResource(iconResource);

            // Cambiar color de fondo del ícono
            int iconBackgroundColor = getCategoryIconBackgroundColor(habit.getCategory());
            ivCategoryIcon.setBackgroundTintList(ContextCompat.getColorStateList(context, iconBackgroundColor));
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

        private int getCategoryBackgroundColor(String category) {
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

        private int getCategoryIconBackgroundColor(String category) {
            switch (category.toLowerCase()) {
                case "ejercicio":
                    return R.color.exercise_light_color;
                case "alimentación":
                    return R.color.food_light_color;
                case "sueño":
                    return R.color.sleep_light_color;
                case "lectura":
                    return R.color.reading_light_color;
                case "trabajo":
                    return R.color.work_light_color;
                case "meditación":
                    return R.color.meditation_light_color;
                default:
                    return R.color.default_light_color;
            }
        }
    }
}
