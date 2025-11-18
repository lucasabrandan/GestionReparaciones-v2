package com.example.gestionreparacionesapp.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.gestionreparacionesapp.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.bottomnavigation.BottomNavigationView; // Importar

public class DashboardFragment extends Fragment {

    private TextView tvBienvenida;
    private MaterialButton btnVentas, btnReparaciones, btnClientes, btnProductos;
    private DashboardViewModel dashboardViewModel;

    public static DashboardFragment newInstance(String userName) {
        DashboardFragment fragment = new DashboardFragment();
        Bundle args = new Bundle();
        args.putString("USER_NAME", userName);
        fragment.setArguments(args);
        return fragment;
    }

    public DashboardFragment() {
        // Constructor público requerido
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        // Vinculación de Vistas
        tvBienvenida = view.findViewById(R.id.tvBienvenida);
        btnVentas = view.findViewById(R.id.btnVentas);
        btnReparaciones = view.findViewById(R.id.btnReparaciones);
        btnClientes = view.findViewById(R.id.btnClientes);
        btnProductos = view.findViewById(R.id.btnProductos);

        // Observar datos
        dashboardViewModel.getUserName().observe(getViewLifecycleOwner(), userName -> {
            if (userName != null && !userName.isEmpty()) {
                tvBienvenida.setText("¡Bienvenido, " + userName + "!");
            } else {
                tvBienvenida.setText("¡Bienvenido!");
            }
        });

        // Carga inicial de datos
        String userNameFromArgs = null;
        if (getArguments() != null) {
            userNameFromArgs = getArguments().getString("USER_NAME");
        }
        dashboardViewModel.loadUserName(userNameFromArgs);

        // Configurar Listeners (¡AHORA CON NAVEGACIÓN!)
        setupListeners();
    }

    /**
     * ¡LÓGICA DE NAVEGACIÓN CORREGIDA!
     * Estos botones ahora controlan la barra de navegación inferior de la HomeActivity.
     */
    private void setupListeners() {

        // 1. Obtener la BottomNavigationView de la Activity "padre"
        BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);

        if (bottomNav != null) {
            // 2. Asignar los clics de los botones a los items del menú inferior
            btnVentas.setOnClickListener(v -> {
                bottomNav.setSelectedItemId(R.id.nav_ventas);
            });

            btnReparaciones.setOnClickListener(v -> {
                bottomNav.setSelectedItemId(R.id.nav_reparaciones);
            });

            btnClientes.setOnClickListener(v -> {
                bottomNav.setSelectedItemId(R.id.nav_clientes);
            });

            btnProductos.setOnClickListener(v -> {
                bottomNav.setSelectedItemId(R.id.nav_productos);
            });
        }
    }
}