package com.example.gestionreparacionesapp.ui.configuracion;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gestionreparacionesapp.databinding.ActivityConfiguracionBinding;

/**
 * Activity para gestionar las opciones de configuración de la aplicación.
 * Por ahora, es una pantalla de ejemplo.
 */
public class ConfiguracionActivity extends AppCompatActivity {

    private ActivityConfiguracionBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflamos el layout usando View Binding para acceder a las vistas de forma segura
        binding = ActivityConfiguracionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configuramos la Toolbar como la ActionBar de la actividad
        setSupportActionBar(binding.toolbarConfiguracion);

        // Habilitamos el botón de "atrás" (flecha) en la Toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    /**
     * Este método se llama cuando el usuario presiona el botón de "atrás" en la Toolbar.
     * Cierra la actividad actual y regresa a la anterior (Dashboard).
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Simula el comportamiento del botón "atrás" del dispositivo
        return true;
    }
}
