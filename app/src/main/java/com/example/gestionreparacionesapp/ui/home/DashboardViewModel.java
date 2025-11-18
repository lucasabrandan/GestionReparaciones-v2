package com.example.gestionreparacionesapp.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable; // Importar Nullable
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.gestionreparacionesapp.data.db.AppDatabase;
import com.example.gestionreparacionesapp.data.db.dao.UsuarioDao;
import com.example.gestionreparacionesapp.data.db.entity.Usuario;
import com.example.gestionreparacionesapp.data.repository.UsuarioRepository;
import com.example.gestionreparacionesapp.data.util.ResultadoLogin;

public class DashboardViewModel extends AndroidViewModel {

    private final UsuarioRepository repository;

    private final MutableLiveData<String> userName = new MutableLiveData<>();
    public LiveData<String> getUserName() { return userName; }

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        UsuarioDao usuarioDao = AppDatabase.getInstance(application).usuarioDao();
        this.repository = new UsuarioRepository(usuarioDao);
    }

    /**
     * ¡LÓGICA CORREGIDA!
     * Carga el nombre del usuario.
     * Prioriza el nombre recibido desde el Intent (si venimos del Login).
     * Si no, busca en la base de datos (si el usuario abrió la app directamente).
     */
    public void loadUserName(@Nullable String userNameFromIntent) {
        if (userNameFromIntent != null) {
            // 1. El nombre viene del Intent (Login Exitoso)
            userName.setValue(userNameFromIntent);
        } else {
            // 2. El nombre NO viene del Intent (App abierta en frío)
            // Buscamos si hay un usuario con "Recordarme"
            repository.getUsuarioRecordado(usuario -> {
                if (usuario != null && usuario.getNombreCompleto() != null) {
                    userName.postValue(usuario.getNombreCompleto());
                } else {
                    // 3. No hay nombre del Intent NI usuario recordado
                    userName.postValue("Usuario"); // El valor por defecto
                }
            });
        }
    }

    /**
     * Limpia el estado de 'recordarme' en la BBDD. Usado al cerrar sesión (Logout).
     */
    public void clearRememberMeState() {
        repository.limpiarRecordarme();
    }
}