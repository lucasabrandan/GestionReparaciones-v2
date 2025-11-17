package com.example.gestionreparacionesapp.ui.home;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.appcompat.widget.Toolbar; // Import para Toolbar

import com.example.gestionreparacionesapp.R;
import com.example.gestionreparacionesapp.ui.login.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

// Importaciones de Fragments (Asegúrate de que estos 4 archivos existan)
import com.example.gestionreparacionesapp.ui.ventas.VentasFragment;
import com.example.gestionreparacionesapp.ui.reparaciones.ReparacionesFragment;
import com.example.gestionreparacionesapp.ui.clientes.ClientesFragment;
import com.example.gestionreparacionesapp.ui.productos.ProductosFragment;


public class HomeActivity extends AppCompatActivity {

    private DashboardViewModel dashboardViewModel;
    private BottomNavigationView bottomNavigationView;
    private Toolbar toolbar; // Declaramos el Toolbar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Establece el Layout
        setContentView(R.layout.activity_home);

        // 2. Inicializar ViewModel
        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        // 3. Vincular Vistas (Toolbar y BottomNavigation)
        initViews();

        // 4. Configurar el Toolbar
        setupToolbar();

        // 5. Configurar la Navegación Inferior
        setupBottomNavigation();

        // 6. Cargar Fragmento Inicial
        if (savedInstanceState == null) {
            loadFragment(new DashboardFragment());
        }
    }

    private void initViews() {
        // Estos IDs DEBEN existir en activity_home.xml
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
                selectedFragment = new DashboardFragment();
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
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            dashboardViewModel.clearRememberMeState();
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return true;
        } else if (id == R.id.action_toggle_theme) {
            toggleTheme();
            return true;
        } else if (id == R.id.action_settings) {
            Toast.makeText(this, "Abriendo diálogo de Ajustes...", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

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