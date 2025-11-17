package com.example.gestionreparacionesapp.ui.clientes;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.gestionreparacionesapp.data.db.AppDatabase;
import com.example.gestionreparacionesapp.data.db.dao.ClienteDao;
import com.example.gestionreparacionesapp.data.db.entity.Cliente;
import com.example.gestionreparacionesapp.data.repository.ClienteRepository;
import com.example.gestionreparacionesapp.data.repository.ClienteRepository.InsertResult;

public class ClientesViewModel extends AndroidViewModel {

    private final ClienteRepository repository;
    private final MutableLiveData<InsertResult> insertResult = new MutableLiveData<>();
    public LiveData<InsertResult> getInsertResult() {
        return insertResult;
    }

    public ClientesViewModel(@NonNull Application application) {
        super(application);
        ClienteDao clienteDao = AppDatabase.getInstance(application).clienteDao();
        this.repository = new ClienteRepository(clienteDao);
    }

    /**
     * Guarda un nuevo cliente en el repositorio de forma asíncrona.
     */
    public void guardarCliente(
            String nombre,
            String direccion,
            String localidad,
            String codigoPostal
    ) {
        // Validación de datos simple (la validación completa se haría en el Fragment/Activity)
        if (nombre.isEmpty() || direccion.isEmpty() || localidad.isEmpty() || codigoPostal.isEmpty()) {
            insertResult.setValue(new InsertResult(false, "Por favor complete todos los campos obligatorios."));
            return;
        }

        Cliente nuevoCliente = new Cliente(nombre, direccion, localidad, codigoPostal);

        repository.insertarCliente(nuevoCliente, result -> {
            // Actualiza el LiveData, notificando al Fragment/Activity
            insertResult.postValue(result);
        });
    }
}