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
// El callback ahora viene de un paquete diferente si lo has creado. Asumimos una estructura simple.
import com.example.gestionreparacionesapp.data.util.RepositoryCallback;
import com.example.gestionreparacionesapp.data.util.ResultadoRegistro;

import org.json.JSONArray;
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
        // La inicialización del repositorio sigue siendo la misma,
        // pero ahora este repositorio es mucho más eficiente por dentro.
        this.repository = new VentaRepository(
                AppDatabase.getInstance(application).ventaDao(),
                application
        );
    }

    public void cargarVentas() {
        // La llamada no cambia, pero ahora usa la implementación moderna.
        repository.getAllVentas(result -> listaVentas.postValue(result));
    }

    public void buscarVentas(String query) {
        if (query == null || query.trim().isEmpty()) {
            cargarVentas();
        } else {
            repository.buscarVentas(query, result -> listaVentas.postValue(result));
        }
    }

    /**
     * Guarda una nueva venta y le pide al repositorio que descuente el stock.
     */
    public void guardarVenta(Cliente clienteSeleccionado, List<ProductoVenta> productos) {
        if (clienteSeleccionado == null) {
            operationResult.setValue(new ResultadoRegistro(false, "Debe seleccionar un cliente"));
            return;
        }
        if (productos == null || productos.isEmpty()) {
            operationResult.setValue(new ResultadoRegistro(false, "Debe añadir al menos un producto"));
            return;
        }

        double subtotal = 0;
        for (ProductoVenta pv : productos) {
            subtotal += pv.getSubtotal();
        }
        double total = subtotal;

        String productosJson = convertirProductosAJson(productos);
        String fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        Venta venta = new Venta(0, clienteSeleccionado.getId(), fecha, subtotal, total, productosJson);

        // --- CAMBIO CLAVE AQUÍ ---
        // Ahora pasamos la lista de 'productos' para que el repositorio pueda descontar el stock.
        repository.insertVenta(venta, productos, result -> {
            operationResult.postValue(result);
            if (result.isSuccess) {
                cargarVentas(); // Recarga la lista de ventas para mostrar la nueva
            }
        });
    }

    /**
     * Actualiza una venta existente.
     * NOTA: La lógica para RE-AJUSTAR el stock al editar no está implementada.
     */
    public void actualizarVenta(int ventaId, Cliente clienteSeleccionado, List<ProductoVenta> productos) {
        if (clienteSeleccionado == null) {
            operationResult.setValue(new ResultadoRegistro(false, "Debe seleccionar un cliente"));
            return;
        }
        if (productos == null || productos.isEmpty()) {
            operationResult.setValue(new ResultadoRegistro(false, "Debe añadir al menos un producto"));
            return;
        }

        double subtotal = 0;
        for (ProductoVenta pv : productos) {
            subtotal += pv.getSubtotal();
        }
        double total = subtotal;

        String productosJson = convertirProductosAJson(productos);
        String fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        Venta venta = new Venta(0, clienteSeleccionado.getId(), fecha, subtotal, total, productosJson);
        venta.setId(ventaId);

        repository.updateVenta(venta, result -> {
            operationResult.postValue(result);
            if (result.isSuccess) cargarVentas();
        });
    }

    public void eliminarVenta(Venta venta) {
        // Eliminar una venta NO restaura el stock por defecto.
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
                productoJson.put("nombre", pv.getProducto().getNombre());
                productoJson.put("precio", pv.getProducto().getPrecio());
                productoJson.put("cantidad", pv.getCantidad()); // La cantidad vendida
                array.put(productoJson);
            }
        } catch (Exception e) {
            Log.e("VentasViewModel", "Error al crear JSON de productos", e);
        }
        return array.toString();
    }
}
