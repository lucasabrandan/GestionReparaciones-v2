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
import com.example.gestionreparacionesapp.data.util.RepositoryCallback;
import com.example.gestionreparacionesapp.data.util.ResultadoRegistro;
// --- ¡IMPORTANTE! Asegúrate de que la clase SingleLiveEvent existe en este paquete ---
import com.example.gestionreparacionesapp.util.SingleLiveEvent;

import java.util.List;

public class ClientesViewModel extends AndroidViewModel {

    private final ClienteRepository repository;

    private final MutableLiveData<List<Cliente>> listaClientes = new MutableLiveData<>();
    public LiveData<List<Cliente>> getListaClientes() { return listaClientes; }

    private final MutableLiveData<ResultadoRegistro> operationResult = new MutableLiveData<>();
    public LiveData<ResultadoRegistro> getOperationResult() { return operationResult; }

    // --- ¡AQUÍ LA IMPLEMENTACIÓN! ---
    // 1. Se añade el SingleLiveEvent para notificar la creación de un cliente.
    private final SingleLiveEvent<Boolean> clienteCreadoConExito = new SingleLiveEvent<>();

    /**
     * El Fragment observará este LiveData. Se disparará una sola vez cuando un cliente
     * se cree correctamente, para notificar a otras partes de la UI (como ReparacionesFragment).
     */
    public LiveData<Boolean> getClienteCreadoConExito() {
        return clienteCreadoConExito;
    }
    // --- FIN DE LA IMPLEMENTACIÓN ---

    public enum ClienteFilterType {
        TODOS,
        CON_VENTAS,
        CON_REPARACIONES
    }

    public ClientesViewModel(@NonNull Application application) {
        super(application);
        ClienteDao clienteDao = AppDatabase.getInstance(application).clienteDao();
        this.repository = new ClienteRepository(clienteDao, application);
    }

    public void cargarClientes(ClienteFilterType filtro) {
        RepositoryCallback<List<Cliente>> callback = result -> listaClientes.postValue(result);
        switch (filtro) {
            // ... tu lógica de switch
            default:
                repository.getAllClientes(callback);
                break;
        }
    }

    public void buscarClientes(String query) {
        if (query == null || query.trim().isEmpty()) {
            cargarClientes(ClienteFilterType.TODOS);
        } else {
            repository.buscarClientesPorNombre(query, result -> listaClientes.postValue(result));
        }
    }

    /**
     * Inserta un nuevo cliente.
     */
    public void guardarCliente(String dni, String nombre, String direccion, String localidad, String codigoPostal) {
        if (dni.isEmpty() || nombre.isEmpty()) {
            operationResult.setValue(new ResultadoRegistro(false, "DNI y Nombre son obligatorios"));
            return;
        }

        Cliente cliente = new Cliente(dni, nombre, direccion, localidad, codigoPostal);

        // Asumiendo que tu repositorio tiene un callback así.
        repository.insertCliente(cliente, (RepositoryCallback<ResultadoRegistro>) result -> {
            operationResult.postValue(result);
            if (result.isSuccess) {
                // Refrescamos la lista principal para que esté al día en todas partes.
                cargarClientes(ClienteFilterType.TODOS);

                // --- ¡AQUÍ LA MAGIA! ---
                // 2. Disparamos el evento para que el observador en ReparacionesFragment se entere.
                clienteCreadoConExito.postValue(true);
            }
        });
    }

    /**
     * Actualiza un cliente existente.
     */
    public void actualizarCliente(int clienteId, String dni, String nombre, String direccion, String localidad, String codigoPostal) {
        if (dni.isEmpty() || nombre.isEmpty()) {
            operationResult.setValue(new ResultadoRegistro(false, "DNI y Nombre son obligatorios"));
            return;
        }

        Cliente cliente = new Cliente(dni, nombre, direccion, localidad, codigoPostal);
        cliente.setId(clienteId);

        repository.updateCliente(cliente, (RepositoryCallback<ResultadoRegistro>) result -> {
            operationResult.postValue(result);
            if (result.isSuccess) cargarClientes(ClienteFilterType.TODOS);
        });
    }

    /**
     * Elimina un cliente.
     */
    public void eliminarCliente(Cliente cliente) {
        repository.deleteCliente(cliente, (RepositoryCallback<ResultadoRegistro>) result -> {
            operationResult.postValue(result);
            if (result.isSuccess) {
                cargarClientes(ClienteFilterType.TODOS);
            }
        });
    }

    // Constructor vacío por si lo necesitas en algún sitio (no es mala práctica tenerlo)
    public ClientesViewModel() {
        super(null); // Llamada a super con null, no es ideal pero funciona si no usas el contexto.
        // Lo mejor es quitarlo si no se usa.
        this.repository = null;
    }
}