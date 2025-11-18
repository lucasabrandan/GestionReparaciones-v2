package com.example.gestionreparacionesapp.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.gestionreparacionesapp.R;
import com.example.gestionreparacionesapp.ui.clientes.ClientesFragment;
import com.example.gestionreparacionesapp.ui.login.LoginActivity;
import com.example.gestionreparacionesapp.ui.productos.ProductosFragment;
import com.example.gestionreparacionesapp.ui.reparaciones.ReparacionesFragment;
import com.example.gestionreparacionesapp.ui.ventas.VentasFragment;
import com.example.gestionreparacionesapp.util.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class HomeActivity extends AppCompatActivity {

    private DashboardViewModel dashboardViewModel;
    private BottomNavigationView bottomNavigationView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Inicializar ViewModel
        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        initViews();
        setupToolbar();
        setupBottomNavigation();

        if (savedInstanceState == null) {
            // ¡BUG FIX! Esto soluciona el "Bienvenido Usuario"
            String userName = getIntent().getStringExtra("USER_NAME");
            Fragment initialFragment = DashboardFragment.newInstance(userName);
            loadFragment(initialFragment);
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.app_name));
        }
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment selectedFragment = null;

            if (id == R.id.nav_home) {
                // Volvemos a pasar el nombre de usuario al Dashboard
                String userName = getIntent().getStringExtra("USER_NAME");
                selectedFragment = DashboardFragment.newInstance(userName);
            } else if (id == R.id.nav_ventas) {
                selectedFragment = new VentasFragment();
            } else if (id == R.id.nav_reparaciones) {
                selectedFragment = new ReparacionesFragment();
            } else if (id == R.id.nav_clientes) {
                selectedFragment = new ClientesFragment();
            } else if (id == R.id.nav_productos) {
                selectedFragment = new ProductosFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }
            return false;
        });
    }

    // ===============================================
    // LÓGICA DEL MENÚ DE OPCIONES (TOOLBAR)
    // ===============================================

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Usa el menu_dashboard.xml CONSOLIDADO
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // ¡CAMBIO! Ahora solo hay un botón (Ajustes)
        if (id == R.id.action_settings) {
            mostrarDialogoDeAjustes(); // Mostramos el popup
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Muestra el diálogo de Ajustes (SIN "Cancelar").
     */
    private void mostrarDialogoDeAjustes() {
        final CharSequence[] options = {"Cambiar Tema (Light/Dark)", "Cerrar Sesión"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ajustes de la App");
        builder.setItems(options, (dialog, which) -> {
            if (options[which].equals("Cambiar Tema (Light/Dark)")) {
                toggleTheme(); // Llama a la función de cambiar tema
            } else if (options[which].equals("Cerrar Sesión")) {
                cerrarSesion(); // Llama a la función de cerrar sesión
            }
        });
        builder.show();
    }

    /**
     * Lógica real de Cerrar Sesión
     */
    private void cerrarSesion() {
        // 1. Limpia el "Recordarme" de la base de datos
        dashboardViewModel.clearRememberMeState();

        // 2. Limpia el ID de sesión guardado
        SessionManager.clearSession(this);

        // 3. Vuelve al Login
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Borra el historial
        startActivity(intent);
        finish();
    }

    /**
     * Lógica real de Cambiar Tema
     */
    private void toggleTheme() {
        int currentNightMode = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        int newMode = (currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES)
                ? AppCompatDelegate.MODE_NIGHT_NO
                : AppCompatDelegate.MODE_NIGHT_YES;

        AppCompatDelegate.setDefaultNightMode(newMode);
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}