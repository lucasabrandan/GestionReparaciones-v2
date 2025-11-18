package com.example.gestionreparacionesapp.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.gestionreparacionesapp.R;
import com.example.gestionreparacionesapp.data.db.entity.Usuario;
import com.example.gestionreparacionesapp.data.util.ResultadoLogin;
import com.example.gestionreparacionesapp.ui.home.HomeActivity;
import com.example.gestionreparacionesapp.ui.registro.RegistroActivity;
import com.example.gestionreparacionesapp.util.SessionManager; // <-- ¡IMPORTAMOS EL SESSION MANAGER!

import com.google.android.material.switchmaterial.SwitchMaterial;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnIngresar;
    private TextView tvRegistrate;
    private SwitchMaterial swRecordarme;

    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        initViews();
        setupListeners();
        setupObservers();

        // 1. Verificar si hay un usuario recordado (para inicio rápido)
        viewModel.getRememberedUser().observe(this, usuario -> {
            if (usuario != null) {
                // ¡MEJORA APLICADA! Guardamos la sesión del usuario recordado
                SessionManager.saveUserId(this, usuario.getId());
                handleSuccessfulLogin(usuario);
            }
        });
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnIngresar = findViewById(R.id.btnIngresar);
        tvRegistrate = findViewById(R.id.tvRegistrate);
        swRecordarme = findViewById(R.id.swRecordarme);
    }

    private void setupListeners() {
        btnIngresar.setOnClickListener(v -> attemptLogin());

        tvRegistrate.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegistroActivity.class);
            startActivity(intent);
        });

        swRecordarme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setRememberMe(isChecked);
        });
    }

    private void setupObservers() {
        // Observa el resultado del Login
        viewModel.getLoginResult().observe(this, result -> {
            Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show();
            if (result.isSuccess) {
                // ¡MEJORA APLICADA! Guardamos la sesión del nuevo login
                SessionManager.saveUserId(this, result.usuario.getId());
                handleSuccessfulLogin(result.usuario);
            }
        });

        // Observa el estado de 'Recordarme'
        viewModel.getRememberMe().observe(this, isChecked -> {
            // Lógica de UI si el estado cambia
        });
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Delegar la lógica de autenticación al ViewModel
        viewModel.login(email, password);
    }

    private void handleSuccessfulLogin(Usuario usuario) {
        // Lógica de persistencia de sesión
        boolean shouldRemember = viewModel.getRememberMe().getValue() != null && viewModel.getRememberMe().getValue();

        if (shouldRemember) {
            viewModel.saveRememberMeState(usuario.getId());
        } else {
            viewModel.clearRememberMeState();
        }

        // Navegar a la pantalla principal
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.putExtra("USER_NAME", usuario.getNombreCompleto());

        startActivity(intent);
        finish();
    }
}