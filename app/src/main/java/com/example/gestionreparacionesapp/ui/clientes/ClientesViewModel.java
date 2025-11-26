package com.example.gestionreparacionesapp.ui.clientes;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.gestionreparacionesapp.data.db.AppDatabase;
import com.example.gestionreparacionesapp.data.db.dao.ClienteDao;
import com.example.gestionreparacionesapp.data.db.dao.ReparacionDao;
import com.example.gestionreparacionesapp.data.db.entity.Cliente;
import com.example.gestionreparacionesapp.data.db.entity.Reparacion;
import com.example.gestionreparacionesapp.data.repository.ClienteRepository;
import com.example.gestionreparacionesapp.data.util.ResultadoRegistro;
import com.example.gestionreparacionesapp.util.PdfGenerator;
import com.example.gestionreparacionesapp.util.SingleLiveEvent;

import java.io.File;
import java.util.List;
import java.util.concurrent.Executors;

public class ClientesViewModel extends AndroidViewModel {

    private final ClienteRepository repository;

    private final MutableLiveData<List<Cliente>> listaClientes = new MutableLiveData<>();
    public LiveData<List<Cliente>> getListaClientes() { return listaClientes; }

    private final MutableLiveData<ResultadoRegistro> operationResult = new MutableLiveData<>();
    public LiveData<ResultadoRegistro> getOperationResult() { return operationResult; }

    private final SingleLiveEvent<Boolean> clienteCreadoConExito = new SingleLiveEvent<>();
    public LiveData<Boolean> getClienteCreadoConExito() { return clienteCreadoConExito; }

    private final SingleLiveEvent<File> pdfGeneradoEvent = new SingleLiveEvent<>();
    public LiveData<File> getPdfGeneradoEvent() { return pdfGeneradoEvent; }

    public enum ClienteFilterType { TODOS, CON_VENTAS, CON_REPARACIONES }

    public ClientesViewModel(@NonNull Application application) {
        super(application);
        ClienteDao clienteDao = AppDatabase.getInstance(application).clienteDao();
        this.repository = new ClienteRepository(clienteDao, application);
    }

    public void cargarClientes(ClienteFilterType filtro) {
        repository.getAllClientes(result -> listaClientes.postValue(result));
    }

    public void buscarClientes(String query) {
        if (query == null || query.trim().isEmpty()) cargarClientes(ClienteFilterType.TODOS);
        else repository.buscarClientesPorNombre(query, result -> listaClientes.postValue(result));
    }

    public void guardarCliente(String dni, String nombre, String direccion, String localidad, String codigoPostal) {
        if (dni.isEmpty() || nombre.isEmpty()) {
            operationResult.setValue(new ResultadoRegistro(false, "DNI y Nombre obligatorios"));
            return;
        }
        Cliente cliente = new Cliente(dni, nombre, direccion, localidad, codigoPostal);
        repository.insertCliente(cliente, result -> {
            operationResult.postValue(result);
            if (result.isSuccess) {
                cargarClientes(ClienteFilterType.TODOS);
                clienteCreadoConExito.postValue(true);
            }
        });
    }

    public void actualizarCliente(int id, String dni, String nombre, String direccion, String localidad, String codigoPostal) {
        Cliente cliente = new Cliente(dni, nombre, direccion, localidad, codigoPostal);
        cliente.setId(id);
        repository.updateCliente(cliente, result -> {
            operationResult.postValue(result);
            if(result.isSuccess) cargarClientes(ClienteFilterType.TODOS);
        });
    }

    public void eliminarCliente(Cliente cliente) {
        repository.deleteCliente(cliente, result -> {
            operationResult.postValue(result);
            if(result.isSuccess) cargarClientes(ClienteFilterType.TODOS);
        });
    }

    // --- GENERAR PDF Y COMPARTIR ---
    public void generarPdfPresupuesto(Context context, Cliente cliente) {
        Executors.newSingleThreadExecutor().execute(() -> {
            ReparacionDao reparacionDao = AppDatabase.getInstance(context).reparacionDao();
            List<Reparacion> reparaciones = reparacionDao.getAllByCliente(cliente.getId());

            if (reparaciones == null || reparaciones.isEmpty()) {
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(context, "Este cliente no tiene reparaciones para presupuestar.", Toast.LENGTH_SHORT).show());
            } else {
                // Generamos el archivo (Ahora sí retorna File)
                File pdfFile = PdfGenerator.generarPresupuestoPdf(context, cliente, reparaciones);
                // Notificamos para compartir
                if (pdfFile != null) {
                    pdfGeneradoEvent.postValue(pdfFile);
                }
            }
        });
    }
}