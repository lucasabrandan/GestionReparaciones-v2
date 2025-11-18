package com.example.gestionreparacionesapp.ui.registro;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.gestionreparacionesapp.data.repository.UsuarioRepository;
import com.example.gestionreparacionesapp.data.db.AppDatabase;
import com.example.gestionreparacionesapp.data.db.dao.UsuarioDao;
import com.example.gestionreparacionesapp.data.util.ResultadoRegistro;
import com.example.gestionreparacionesapp.data.db.entity.Usuario;

public class RegistroViewModel extends AndroidViewModel {

    private final UsuarioRepository repository;
    private final MutableLiveData<ResultadoRegistro> registroResult = new MutableLiveData<>();
    public LiveData<ResultadoRegistro> getRegistroResult() {
        return registroResult;
    }

    public RegistroViewModel(@NonNull Application application) {
        super(application);
        UsuarioDao usuarioDao = AppDatabase.getInstance(application).usuarioDao();
        this.repository = new UsuarioRepository(usuarioDao);
    }

    // ACEPTA 5 ARGUMENTOS (nombre, correo, confirmarCorreo, contrasena, telefono)
    public void registrarUsuario(
            String nombre,
            String correo,
            String confirmarCorreo,
            String contrasena,
            String telefono
    ) {
        if (nombre.isEmpty() || correo.isEmpty() || confirmarCorreo.isEmpty() || contrasena.isEmpty() || telefono.isEmpty()) {
            registroResult.setValue(new ResultadoRegistro(false, "Por favor complete todos los campos."));
            return;
        }

        if (!correo.equals(confirmarCorreo)) {
            registroResult.setValue(new ResultadoRegistro(false, "Los correos no coinciden."));
            return;
        }

        // Llamada al Repository: Se usa el correo como NombreUsuario para rellenar la entidad
        repository.insertarUsuario(
                new Usuario(nombre, correo /* Usado como NombreUsuario */, correo, contrasena, telefono),
                result -> {
                    registroResult.postValue(result);
                }
        );
    }
}