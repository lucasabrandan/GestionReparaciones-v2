package com.example.gestionreparacionesapp.ui.ventas;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.gestionreparacionesapp.data.db.AppDatabase;
import com.example.gestionreparacionesapp.data.db.entity.Cliente;
import com.example.gestionreparacionesapp.data.db.entity.Venta;
import com.example.gestionreparacionesapp.data.repository.VentaRepository;
import com.example.gestionreparacionesapp.data.util.RepositoryCallback;
import com.example.gestionreparacionesapp.data.util.ResultadoRegistro;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VentasViewModel extends AndroidViewModel {

    private final VentaRepository repository;

    private final MutableLiveData<List<Venta>> listaVentas = new MutableLiveData<>();
    public LiveData<List<Venta>> getListaVentas() { return listaVentas; }

    private final MutableLiveData<ResultadoRegistro> operationResult = new MutableLiveData<>();
    public LiveData<ResultadoRegistro> getOperationResult() { return operationResult; }

    public VentasViewModel(@NonNull Application application) {
        super(application);
        this.repository = new VentaRepository(
                AppDatabase.getInstance(application).ventaDao(),
                application
        );
    }

    public void cargarVentas() {
        repository.getAllVentas(result -> listaVentas.postValue(result));
    }

    public void buscarVentas(String query) {
        if (query == null || query.trim().isEmpty()) {
            cargarVentas();
        } else {
            repository.buscarVentas(query, result -> listaVentas.postValue(result));
        }
    }

    public void guardarVenta(Cliente clienteSeleccionado, List<ProductoVenta> productos) {
        if (clienteSeleccionado == null) {
            operationResult.setValue(new ResultadoRegistro(false, "Debe seleccionar un cliente"));
            return;
        }
        if (productos == null || productos.isEmpty()) {
            operationResult.setValue(new ResultadoRegistro(false, "Debe añadir al menos un producto"));
            return;
        }

        double total = 0;
        for (ProductoVenta pv : productos) {
            total += pv.getSubtotal();
        }

        String productosJson = convertirProductosAJson(productos);
        String fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // Asumiendo que el constructor de Venta es (userId, clienteId, fecha, subtotal, total, productosJson)
        Venta venta = new Venta(0, clienteSeleccionado.getId(), fecha, total, total, productosJson);

        // La lógica para descontar stock debe estar en el repositorio
        repository.insertVenta(venta, productos, result -> {
            operationResult.postValue(result);
            if (result.isSuccess) {
                cargarVentas();
            }
        });
    }

    // --- ¡AQUÍ EL MÉTODO NUEVO PARA ACTUALIZAR! ---
    public void actualizarVenta(int ventaId, Cliente clienteSeleccionado, List<ProductoVenta> productos) {
        if (clienteSeleccionado == null) {
            operationResult.setValue(new ResultadoRegistro(false, "Debe seleccionar un cliente"));
            return;
        }
        if (productos == null || productos.isEmpty()) {
            operationResult.setValue(new ResultadoRegistro(false, "Debe añadir al menos un producto"));
            return;
        }

        double total = 0;
        for (ProductoVenta pv : productos) {
            total += pv.getSubtotal();
        }

        String productosJson = convertirProductosAJson(productos);
        String fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        Venta venta = new Venta(0, clienteSeleccionado.getId(), fecha, total, total, productosJson);
        venta.setId(ventaId); // ¡MUY IMPORTANTE! Esto le indica a Room qué registro actualizar.

        repository.updateVenta(venta, result -> {
            operationResult.postValue(result);
            if (result.isSuccess) cargarVentas();
        });
    }

    public void eliminarVenta(Venta venta) {
        repository.deleteVenta(venta, result -> {
            operationResult.postValue(result);
            if (result.isSuccess) cargarVentas();
        });
    }

    private String convertirProductosAJson(List<ProductoVenta> productos) {
        JSONArray array = new JSONArray();
        try {
            for (ProductoVenta pv : productos) {
                JSONObject productoJson = new JSONObject();
                productoJson.put("producto_id", pv.getProducto().getId()); // Guardamos el ID para la reconstrucción
                productoJson.put("nombre", pv.getProducto().getNombre());
                productoJson.put("precio", pv.getProducto().getPrecio());
                productoJson.put("cantidad", pv.getCantidad());
                array.put(productoJson);
            }
        } catch (Exception e) {
            Log.e("VentasViewModel", "Error al crear JSON de productos", e);
        }
        return array.toString();
    }
}