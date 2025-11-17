package com.example.gestionreparacionesapp.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.gestionreparacionesapp.data.db.AppDatabase;
import com.example.gestionreparacionesapp.data.db.dao.UsuarioDao;
import com.example.gestionreparacionesapp.data.db.entity.Usuario;
import com.example.gestionreparacionesapp.data.repository.UsuarioRepository;
import com.example.gestionreparacionesapp.data.util.ResultadoLogin; // Necesario para la estructura

/**
 * ViewModel para la pantalla principal (Dashboard).
 * Se encarga de la lógica de presentación, como obtener el nombre del usuario y manejar el logout.
 */
public class DashboardViewModel extends AndroidViewModel {

    private final UsuarioRepository repository;

    // LiveData que expone el nombre del usuario a la vista
    private final MutableLiveData<String> userName = new MutableLiveData<>();
    public LiveData<String> getUserName() { return userName; }

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        // Inicialización de la cadena de dependencias: DAO -> Repository
        UsuarioDao usuarioDao = AppDatabase.getInstance(application).usuarioDao();
        this.repository = new UsuarioRepository(usuarioDao);
    }

    /**
     * Carga el nombre del usuario logueado.
     * En una implementación real, esto obtendría el nombre del usuario guardado en sesión.
     */
    public void loadUserName() {
        // Usa el repositorio para buscar el usuario recordado (que es el que está logueado)
        repository.getUsuarioRecordado(usuario -> {
            if (usuario != null && usuario.getNombreCompleto() != null) {
                userName.postValue(usuario.getNombreCompleto());
            } else {
                userName.postValue("Usuario");
            }
        });
    }

    /**
     * Limpia el estado de 'recordarme' en la BBDD. Usado al cerrar sesión (Logout).
     * El HomeActivity usará este método antes de navegar de vuelta al Login.
     */
    public void clearRememberMeState() {
        repository.limpiarRecordarme();
    }
}