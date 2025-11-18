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
import com.example.gestionreparacionesapp.data.util.ResultadoRegistro;

import java.util.List;

public class ClientesViewModel extends AndroidViewModel {

    private final ClienteRepository repository;

    private final MutableLiveData<List<Cliente>> listaClientes = new MutableLiveData<>();
    public LiveData<List<Cliente>> getListaClientes() { return listaClientes; }

    private final MutableLiveData<ResultadoRegistro> operationResult = new MutableLiveData<>();
    public LiveData<ResultadoRegistro> getOperationResult() {
        return operationResult;
    }

    // Enum para manejar los tipos de filtro
    public enum ClienteFilterType {
        TODOS,
        CON_VENTAS,
        CON_REPARACIONES
    }

    public ClientesViewModel(@NonNull Application application) {
        super(application);
        ClienteDao clienteDao = AppDatabase.getInstance(application).clienteDao();
        // CAMBIO: Pasamos el contexto (Application) al Repositorio
        this.repository = new ClienteRepository(clienteDao, application);
    }

    /**
     * Carga clientes según el filtro aplicado.
     */
    public void cargarClientes(ClienteFilterType filtro) {
        switch (filtro) {
            case CON_VENTAS:
                repository.getClientesConVentas(result -> listaClientes.postValue(result));
                break;
            case CON_REPARACIONES:
                repository.getClientesConReparaciones(result -> listaClientes.postValue(result));
                break;
            case TODOS:
            default:
                repository.getAllClientes(result -> listaClientes.postValue(result));
                break;
        }
    }

    /**
     * Busca clientes por nombre o DNI.
     */
    public void buscarClientes(String query) {
        if (query == null || query.trim().isEmpty()) {
            cargarClientes(ClienteFilterType.TODOS); // Cargar todos si la búsqueda está vacía
        } else {
            repository.buscarClientesPorNombre(query, result -> listaClientes.postValue(result));
        }
    }

    /**
     * Inserta un nuevo cliente (SIN Lat/Lon).
     */
    public void guardarCliente(String dni, String nombre, String direccion, String localidad, String codigoPostal) {
        if (dni.isEmpty() || nombre.isEmpty() || direccion.isEmpty() || localidad.isEmpty() || codigoPostal.isEmpty()) {
            operationResult.setValue(new ResultadoRegistro(false, "DNI, Nombre, Dirección, Localidad y CP son obligatorios"));
            return;
        }

        // CAMBIO: El constructor ya no lleva Lat/Lon
        Cliente cliente = new Cliente(dni, nombre, direccion, localidad, codigoPostal);

        repository.insertCliente(cliente, result -> {
            operationResult.postValue(result);
            if (result.isSuccess) cargarClientes(ClienteFilterType.TODOS); // Recargar la lista
        });
    }

    /**
     * Actualiza un cliente existente (SIN Lat/Lon).
     */
    public void actualizarCliente(int clienteId, String dni, String nombre, String direccion, String localidad, String codigoPostal) {
        if (dni.isEmpty() || nombre.isEmpty() || direccion.isEmpty() || localidad.isEmpty() || codigoPostal.isEmpty()) {
            operationResult.setValue(new ResultadoRegistro(false, "DNI, Nombre, Dirección, Localidad y CP son obligatorios"));
            return;
        }

        // CAMBIO: El constructor ya no lleva Lat/Lon
        Cliente cliente = new Cliente(dni, nombre, direccion, localidad, codigoPostal);
        cliente.setId(clienteId);

        repository.updateCliente(cliente, result -> {
            operationResult.postValue(result);
            if (result.isSuccess) cargarClientes(ClienteFilterType.TODOS); // Recargar la lista
        });
    }

    /**
     * Elimina un cliente.
     */
    public void eliminarCliente(Cliente cliente) {
        repository.deleteCliente(cliente, result -> {
            operationResult.postValue(result);
            if (result.isSuccess) {
                cargarClientes(ClienteFilterType.TODOS); // Recargar la lista
            }
        });
    }
}