package com.example.gestionreparacionesapp.ui.reparaciones;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.gestionreparacionesapp.data.db.AppDatabase;
import com.example.gestionreparacionesapp.data.db.entity.Cliente;
import com.example.gestionreparacionesapp.data.db.entity.Producto;
import com.example.gestionreparacionesapp.data.db.entity.Reparacion;
import com.example.gestionreparacionesapp.data.repository.ReparacionRepository;
import com.example.gestionreparacionesapp.data.util.ResultadoRegistro;
import com.example.gestionreparacionesapp.ui.ventas.ProductoVenta; // Reutilizamos la clase de Ventas

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReparacionesViewModel extends AndroidViewModel {

    private final ReparacionRepository repository;

    private final MutableLiveData<List<Reparacion>> listaReparaciones = new MutableLiveData<>();
    public LiveData<List<Reparacion>> getListaReparaciones() { return listaReparaciones; }

    private final MutableLiveData<ResultadoRegistro> operationResult = new MutableLiveData<>();
    public LiveData<ResultadoRegistro> getOperationResult() { return operationResult; }

    public ReparacionesViewModel(@NonNull Application application) {
        super(application);
        // Pasamos el Context al Repositorio para que pueda leer el userId de la sesión
        this.repository = new ReparacionRepository(
                AppDatabase.getInstance(application).reparacionDao(),
                application
        );
    }

    public void cargarReparaciones() {
        repository.getAllReparaciones(result -> listaReparaciones.postValue(result));
    }

    public void buscarReparaciones(String query) {
        if (query == null || query.trim().isEmpty()) {
            cargarReparaciones();
        } else {
            repository.buscarReparaciones(query, result -> listaReparaciones.postValue(result));
        }
    }

    /**
     * Guarda una nueva reparación. (YA NO NECESITA userId, lo toma de la sesión)
     */
    public void guardarReparacion(Cliente clienteSeleccionado, String descripcion, List<ProductoVenta> productos) {
        if (clienteSeleccionado == null) {
            operationResult.setValue(new ResultadoRegistro(false, "Debe seleccionar un cliente"));
            return;
        }
        if (descripcion.isEmpty() && productos.isEmpty()) {
            operationResult.setValue(new ResultadoRegistro(false, "Debe añadir una descripción o al menos un producto"));
            return;
        }

        double subtotal = 0;
        for (ProductoVenta pv : productos) {
            subtotal += pv.getSubtotal();
        }
        double total = subtotal;

        String productosJson = convertirProductosAJson(productos);
        String fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // Usamos el constructor sin userId (el Repo lo asigna)
        Reparacion reparacion = new Reparacion(clienteSeleccionado.getId(), fecha, descripcion, subtotal, total, productosJson);

        repository.insertReparacion(reparacion, result -> {
            operationResult.postValue(result);
            if (result.isSuccess) cargarReparaciones();
        });
    }

    /**
     * Actualiza una reparación (YA NO NECESITA userId).
     */
    public void actualizarReparacion(int reparacionId, Cliente clienteSeleccionado, String descripcion, List<ProductoVenta> productos) {
        if (clienteSeleccionado == null) {
            operationResult.setValue(new ResultadoRegistro(false, "Debe seleccionar un cliente"));
            return;
        }

        double subtotal = 0;
        for (ProductoVenta pv : productos) {
            subtotal += pv.getSubtotal();
        }
        double total = subtotal;

        String productosJson = convertirProductosAJson(productos);
        String fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // Usamos el constructor sin userId (el Repo lo asigna)
        Reparacion reparacion = new Reparacion(clienteSeleccionado.getId(), fecha, descripcion, subtotal, total, productosJson);
        reparacion.setId(reparacionId);

        repository.updateReparacion(reparacion, result -> {
            operationResult.postValue(result);
            if (result.isSuccess) cargarReparaciones();
        });
    }

    public void eliminarReparacion(Reparacion reparacion) {
        repository.deleteReparacion(reparacion, result -> {
            operationResult.postValue(result);
            if (result.isSuccess) cargarReparaciones();
        });
    }

    private String convertirProductosAJson(List<ProductoVenta> productos) {
        JSONArray array = new JSONArray();
        try {
            for (ProductoVenta pv : productos) {
                JSONObject productoJson = new JSONObject();
                productoJson.put("nombre", pv.getProducto().getNombre());
                productoJson.put("precio", pv.getProducto().getPrecio());
                productoJson.put("cantidad", pv.getCantidad());
                array.put(productoJson);
            }
        } catch (Exception e) {
            Log.e("ReparacionViewModel", "Error al crear JSON de productos", e);
        }
        return array.toString();
    }
}