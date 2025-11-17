package com.example.gestionreparacionesapp.ui.login;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.gestionreparacionesapp.data.db.AppDatabase;
import com.example.gestionreparacionesapp.data.db.dao.UsuarioDao;
import com.example.gestionreparacionesapp.data.db.entity.Usuario;
import com.example.gestionreparacionesapp.data.repository.UsuarioRepository;
import com.example.gestionreparacionesapp.data.util.ResultadoLogin; // Importación de la clase pública
import com.example.gestionreparacionesapp.data.util.ResultadoRegistro; // Importación de la clase pública

/**
 * ViewModel que maneja la lógica de autenticación y el estado de "Recordarme".
 */
public class LoginViewModel extends AndroidViewModel {

    private final UsuarioRepository repository;

    private final MutableLiveData<ResultadoLogin> loginResult = new MutableLiveData<>();
    public LiveData<ResultadoLogin> getLoginResult() { return loginResult; }

    private final MutableLiveData<Boolean> rememberMe = new MutableLiveData<>(false);
    public LiveData<Boolean> getRememberMe() { return rememberMe; }

    private final MutableLiveData<Usuario> rememberedUser = new MutableLiveData<>();
    public LiveData<Usuario> getRememberedUser() { return rememberedUser; }


    public LoginViewModel(@NonNull Application application) {
        super(application);
        UsuarioDao usuarioDao = AppDatabase.getInstance(application).usuarioDao();
        this.repository = new UsuarioRepository(usuarioDao);

        // Inicia la verificación de sesión al arrancar el ViewModel
        checkRememberedUser();
    }

    // ==========================================
    // LÓGICA DE AUTENTICACIÓN
    // ==========================================

    public void login(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            loginResult.setValue(new ResultadoLogin(false, "Ingrese email y contraseña", null));
            return;
        }
        repository.loginUsuario(email, password, result -> {
            // Este result es de tipo ResultadoLogin, que es público
            loginResult.postValue(result);
        });
    }

    // ==========================================
    // GESTIÓN DE PERSISTENCIA (Recordarme)
    // ==========================================

    public void setRememberMe(boolean value) {
        rememberMe.setValue(value);
    }

    public void checkRememberedUser() {
        repository.getUsuarioRecordado(result -> {
            // Este result es de tipo Usuario, que es público
            if (result != null) {
                rememberedUser.postValue(result);
            }
        });
    }

    public void saveRememberMeState(int userId) {
        repository.limpiarRecordarme();
        repository.setRecordarme(userId, true);
    }

    public void clearRememberMeState() {
        repository.limpiarRecordarme();
    }
}