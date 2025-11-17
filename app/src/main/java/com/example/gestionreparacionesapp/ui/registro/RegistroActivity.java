package com.example.gestionreparacionesapp.ui.registro;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.gestionreparacionesapp.R;

import java.util.regex.Pattern;

public class RegistroActivity extends AppCompatActivity {

    private EditText etNombreCompleto, etCorreo, etConfirmarCorreo, etContrasena, etTelefono;
    private Button btnCancelar, btnContinuar;

    private RegistroViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        viewModel = new ViewModelProvider(this).get(RegistroViewModel.class);

        initViews();
        setupListeners();
        setupObservers();
    }

    private void initViews() {
        etNombreCompleto = findViewById(R.id.etNombreCompleto);
        etCorreo = findViewById(R.id.etCorreo);
        etConfirmarCorreo = findViewById(R.id.etConfirmarCorreo);
        etContrasena = findViewById(R.id.etContrasena);
        etTelefono = findViewById(R.id.etTelefono);
        btnCancelar = findViewById(R.id.btnCancelar);
        btnContinuar = findViewById(R.id.btnContinuar);
    }

    private void setupListeners() {
        btnCancelar.setOnClickListener(v -> finish());
        btnContinuar.setOnClickListener(v -> validarYRegistrar());
    }

    private void setupObservers() {
        viewModel.getRegistroResult().observe(this, result -> {
            Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show();
            if (result.isSuccess) {
                finish();
            }
        });
    }

    private void validarYRegistrar() {
        String nombre = etNombreCompleto.getText().toString().trim();
        String correo = etCorreo.getText().toString().trim();
        String confirmarCorreo = etConfirmarCorreo.getText().toString().trim();
        String contrasena = etContrasena.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();

        // VALIDACIÓN RÁPIDA DE LA VISTA (5 campos principales)
        if (nombre.isEmpty() || correo.isEmpty() || confirmarCorreo.isEmpty() || contrasena.isEmpty() || telefono.isEmpty()) {
            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            Toast.makeText(this, "Ingrese un correo válido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!validarContrasena(contrasena)) {
            Toast.makeText(this, "La contraseña debe tener mínimo 8 caracteres, letras, números y 1 signo", Toast.LENGTH_LONG).show();
            return;
        }

        // LLAMADA CORREGIDA: ENVIANDO 5 ARGUMENTOS (nombre, correo, confirmarCorreo, contrasena, telefono)
        viewModel.registrarUsuario(nombre, correo, confirmarCorreo, contrasena, telefono);
    }

    private boolean validarContrasena(String contrasena) {
        Pattern pattern = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$");
        return pattern.matcher(contrasena).matches();
    }
}