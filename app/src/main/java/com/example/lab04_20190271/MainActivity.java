package com.example.lab04_20190271;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int GALLERY_REQUEST_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;
    private static final String PREFS_NAME = "HabitsAppPrefs";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_MOTIVATIONAL_MESSAGE = "motivational_message";
    private static final String PROFILE_IMAGE_NAME = "profile_image.jpg";

    private static final int NOTIFICATION_PERMISSION_CODE = 102;

    private TextView tvGreeting;
    private TextView tvMotivationalMessage;
    private ImageView ivProfileImage;
    private Button btnViewHabits;
    private Button btnSettings;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        initializeSharedPreferences();
        loadUserData();
        setupClickListeners();
        loadProfileImage();
        checkNotificationPermissions();
    }

    private void initializeViews() {
        tvGreeting = findViewById(R.id.tvGreeting);
        tvMotivationalMessage = findViewById(R.id.tvMotivationalMessage);
        ivProfileImage = findViewById(R.id.ivProfileImage);
        btnViewHabits = findViewById(R.id.btnViewHabits);
        btnSettings = findViewById(R.id.btnSettings);
    }

    private void initializeSharedPreferences() {
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }

    private void loadUserData() {
        // Cargar nombre del usuario (por defecto "Usuario" si es la primera vez)
        String userName = sharedPreferences.getString(KEY_USER_NAME, "Usuario");
        tvGreeting.setText("¡Hola, " + userName + "!");

        // Cargar mensaje motivacional (mensaje por defecto)
        String motivationalMessage = sharedPreferences.getString(KEY_MOTIVATIONAL_MESSAGE,
                "¡Hoy es un gran día para formar buenos hábitos!");
        tvMotivationalMessage.setText(motivationalMessage);
    }

    private void setupClickListeners() {
        // Click en imagen de perfil - abrir galería
        ivProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkStoragePermissionAndOpenGallery();
            }
        });

        // Click en "Ver mis hábitos"
        btnViewHabits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HabitsListActivity.class);
                startActivity(intent);
            }
        });

        // Click en "Configuraciones"
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
    }


    private void checkNotificationPermissions() {
        // Para Android 13+ (API 33+) necesitamos pedir permisos de notificación
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
            }
        }
    }
    private void checkStoragePermissionAndOpenGallery() {
        // Para Android 13+ (API 33+) ya no necesitamos READ_EXTERNAL_STORAGE para MediaStore
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ puede acceder directamente a MediaStore
            openGallery();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6-12 necesita permisos
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_CODE);
            } else {
                openGallery();
            }
        } else {
            // Android < 6 no necesita permisos en tiempo de ejecución
            openGallery();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Permiso necesario para acceder a la galería", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permisos de notificación concedidos", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Los permisos de notificación son necesarios para los recordatorios",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                try {
                    Log.d("MainActivity", "Imagen seleccionada: " + selectedImageUri);

                    // Obtener bitmap de la imagen seleccionada
                    Bitmap bitmap;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        // Android 9+ (API 28+)
                        ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), selectedImageUri);
                        bitmap = ImageDecoder.decodeBitmap(source);
                    } else {
                        // Android < 9
                        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                    }

                    Log.d("MainActivity", "Bitmap obtenido: " + bitmap.getWidth() + "x" + bitmap.getHeight());

                    // Redimensionar la imagen para optimizar espacio
                    Bitmap resizedBitmap = resizeBitmap(bitmap, 300, 300);

                    // Guardar en internal storage
                    boolean saved = saveImageToInternalStorage(resizedBitmap);

                    if (saved) {
                        // Mostrar en ImageView
                        ivProfileImage.setImageBitmap(resizedBitmap);
                        Toast.makeText(this, "Imagen guardada correctamente", Toast.LENGTH_SHORT).show();
                        Log.d("MainActivity", "Imagen guardada y mostrada correctamente");
                    } else {
                        Toast.makeText(this, "Error al guardar la imagen", Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    Log.e("MainActivity", "Error al procesar la imagen: " + e.getMessage());
                    e.printStackTrace();
                    Toast.makeText(this, "Error al cargar la imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "No se seleccionó ninguna imagen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Bitmap resizeBitmap(Bitmap originalBitmap, int targetWidth, int targetHeight) {
        return Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, false);
    }

    private boolean saveImageToInternalStorage(Bitmap bitmap) {
        try {
            FileOutputStream fos = openFileOutput(PROFILE_IMAGE_NAME, MODE_PRIVATE);
            boolean compressed = bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();

            Log.d("MainActivity", "Imagen guardada: " + compressed);
            return compressed;

        } catch (IOException e) {
            Log.e("MainActivity", "Error guardando imagen: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void loadProfileImage() {
        try {
            File imageFile = new File(getFilesDir(), PROFILE_IMAGE_NAME);
            Log.d("MainActivity", "Buscando imagen en: " + imageFile.getAbsolutePath());
            Log.d("MainActivity", "Archivo existe: " + imageFile.exists());

            if (imageFile.exists()) {
                FileInputStream fis = openFileInput(PROFILE_IMAGE_NAME);
                Bitmap bitmap = BitmapFactory.decodeStream(fis);

                if (bitmap != null) {
                    ivProfileImage.setImageBitmap(bitmap);
                    Log.d("MainActivity", "Imagen de perfil cargada correctamente");
                } else {
                    Log.w("MainActivity", "No se pudo decodificar la imagen");
                }

                fis.close();
            } else {
                Log.d("MainActivity", "No hay imagen de perfil guardada");
            }
        } catch (IOException e) {
            Log.e("MainActivity", "Error cargando imagen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar datos cuando regresamos de configuraciones
        loadUserData();
    }
}