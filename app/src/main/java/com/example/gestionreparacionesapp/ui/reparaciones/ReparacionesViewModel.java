// REEMPLAZA TODO EL ARCHIVO CON ESTE CÓDIGO
package com.example.gestionreparacionesapp.ui.reparaciones;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.gestionreparacionesapp.data.db.AppDatabase;
import com.example.gestionreparacionesapp.data.db.dao.ReparacionDao;
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
        ReparacionDao reparacionDao = AppDatabase.getInstance(application).reparacionDao();
        repository = new ReparacionRepository(reparacionDao, application);
    }

    public LiveData<List<Reparacion>> getListaReparaciones() { return listaReparaciones; }
    public LiveData<ResultadoRegistro> getResultadoOperacion() { return resultadoOperacion; }

    public void cargarReparaciones() {
        repository.obtenerTodasLasReparaciones(listaReparaciones::postValue);
    }

    public void buscarReparaciones(String query) {
        repository.buscarReparacionesPorTermino(query, listaReparaciones::postValue);
    }

    // --- MÉTODO INSERTAR MODIFICADO ---
    public void insertarReparacion(int clienteId, String marca, String modelo, String serie, String descripcion, String repuestos, String costoRepuestosStr, String costoManoObraStr, String estado) {
        repository.insertarReparacion(clienteId, marca, modelo, serie, descripcion, repuestos, costoRepuestosStr, costoManoObraStr, estado, result -> {
            resultadoOperacion.postValue(result);
            if (result.isSuccess) cargarReparaciones();
        });
    }

    // --- MÉTODO ACTUALIZAR MODIFICADO ---
    public void actualizarReparacion(int reparacionId, int clienteId, String marca, String modelo, String serie, String descripcion, String repuestos, String costoRepuestosStr, String costoManoObraStr, String estado) {
        repository.actualizarReparacion(reparacionId, clienteId, marca, modelo, serie, descripcion, repuestos, costoRepuestosStr, costoManoObraStr, estado, result -> {
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
