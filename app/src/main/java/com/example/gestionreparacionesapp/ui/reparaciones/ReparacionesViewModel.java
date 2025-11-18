package com.example.gestionreparacionesapp.ui.reparaciones;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.gestionreparacionesapp.data.db.AppDatabase;
import com.example.gestionreparacionesapp.data.db.entity.Cliente;
import com.example.gestionreparacionesapp.data.db.entity.Reparacion;
import com.example.gestionreparacionesapp.data.repository.ReparacionRepository;
import com.example.gestionreparacionesapp.data.util.ResultadoRegistro;
import com.example.gestionreparacionesapp.ui.ventas.ProductoVenta;

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
     * Guarda una nueva reparación, incluyendo el coste del servicio.
     */
    public void guardarReparacion(Cliente clienteSeleccionado, String descripcion, List<ProductoVenta> productos, double costeServicio) {
        if (clienteSeleccionado == null) {
            operationResult.setValue(new ResultadoRegistro(false, "Debe seleccionar un cliente"));
            return;
        }
        if (descripcion.isEmpty() && productos.isEmpty()) {
            operationResult.setValue(new ResultadoRegistro(false, "Debe añadir una descripción o al menos un producto"));
            return;
        }

        double subtotalProductos = 0;
        for (ProductoVenta pv : productos) {
            subtotalProductos += pv.getSubtotal();
        }

        // CÁLCULO ACTUALIZADO: El total es la suma de los productos + el coste del servicio.
        double totalFinal = subtotalProductos + costeServicio;

        String productosJson = convertirProductosAJson(productos);
        String fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // Usamos el constructor actualizado de la entidad Reparacion
        Reparacion reparacion = new Reparacion(
                0, // userId lo pone el repo
                clienteSeleccionado.getId(),
                fecha,
                descripcion,
                "Pendiente", // Estado por defecto
                productosJson,
                subtotalProductos,
                costeServicio,
                totalFinal
        );

        repository.insertReparacion(reparacion, result -> {
            operationResult.postValue(result);
            if (result.isSuccess) cargarReparaciones();
        });
    }

    /**
     * Actualiza una reparación existente, incluyendo el coste del servicio.
     */
    public void actualizarReparacion(int reparacionId, Cliente clienteSeleccionado, String descripcion, List<ProductoVenta> productos, double costeServicio) {
        if (clienteSeleccionado == null) {
            operationResult.setValue(new ResultadoRegistro(false, "Debe seleccionar un cliente"));
            return;
        }

        double subtotalProductos = 0;
        for (ProductoVenta pv : productos) {
            subtotalProductos += pv.getSubtotal();
        }

        // CÁLCULO ACTUALIZADO
        double totalFinal = subtotalProductos + costeServicio;

        String productosJson = convertirProductosAJson(productos);
        String fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        Reparacion reparacion = new Reparacion(
                0, // userId
                clienteSeleccionado.getId(),
                fecha,
                descripcion,
                "Actualizado", // Puedes manejar el estado como prefieras
                productosJson,
                subtotalProductos,
                costeServicio,
                totalFinal
        );
        reparacion.setId(reparacionId); // ¡Importante para que sepa cuál actualizar!

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
