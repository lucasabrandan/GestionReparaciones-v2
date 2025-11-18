package com.example.gestionreparacionesapp.ui.reparaciones;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

// --- ¡IMPORTS NECESARIOS! ---
import com.example.gestionreparacionesapp.data.db.AppDatabase;
import com.example.gestionreparacionesapp.data.db.dao.ReparacionDao;
// ---

import com.example.gestionreparacionesapp.data.repository.ReparacionRepository;
import com.example.gestionreparacionesapp.data.util.ResultadoRegistro;
import com.example.gestionreparacionesapp.data.db.entity.Reparacion;

import java.util.List;

public class ReparacionesViewModel extends AndroidViewModel {

    private final ReparacionRepository repository;
    private final MutableLiveData<List<Reparacion>> listaReparaciones = new MutableLiveData<>();
    private final MutableLiveData<ResultadoRegistro> resultadoOperacion = new MutableLiveData<>();

    public ReparacionesViewModel(@NonNull Application application) {
        super(application);

        // --- CORRECCIÓN FINAL AQUÍ ---
        // Se instancia el DAO y se pasa al constructor del Repositorio como es requerido.
        ReparacionDao reparacionDao = AppDatabase.getInstance(application).reparacionDao();
        repository = new ReparacionRepository(reparacionDao, application);
    }

    public LiveData<List<Reparacion>> getListaReparaciones() { return listaReparaciones; }
    public LiveData<ResultadoRegistro> getResultadoOperacion() { return resultadoOperacion; }

    public void cargarReparaciones() {
        // Llamamos al método del repositorio que usa un callback.
        repository.obtenerTodasLasReparaciones(listaReparaciones::postValue);
    }

    public void buscarReparaciones(String query) {
        repository.buscarReparacionesPorTermino(query, listaReparaciones::postValue);
    }

    public void insertarReparacion(int clienteId, String productoNombre, String descripcion, String presupuestoStr, String estado) {
        // La lógica de crear el objeto está en el Repositorio, el ViewModel solo pasa los datos.
        repository.insertarReparacion(clienteId, productoNombre, descripcion, presupuestoStr, estado, result -> {
            resultadoOperacion.postValue(result);
            if (result.isSuccess) cargarReparaciones();
        });
    }

    public void actualizarReparacion(int reparacionId, int clienteId, String productoNombre, String descripcion, String presupuestoStr, String estado) {
        repository.actualizarReparacion(reparacionId, clienteId, productoNombre, descripcion, presupuestoStr, estado, result -> {
            resultadoOperacion.postValue(result);
            if (result.isSuccess) cargarReparaciones();
        });
    }

    public void eliminarReparacion(int reparacionId) {
        repository.eliminarReparacionPorId(reparacionId, result -> {
            resultadoOperacion.postValue(result);
            if (result.isSuccess) cargarReparaciones();
        });
    }
}