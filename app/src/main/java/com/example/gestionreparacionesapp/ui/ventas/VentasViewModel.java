package com.example.gestionreparacionesapp.ui.ventas;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.gestionreparacionesapp.data.db.AppDatabase;
import com.example.gestionreparacionesapp.data.db.entity.Cliente;
import com.example.gestionreparacionesapp.data.db.entity.ProductoVenta;
import com.example.gestionreparacionesapp.data.db.entity.Venta;
import com.example.gestionreparacionesapp.data.repository.VentaRepository;
import com.example.gestionreparacionesapp.data.util.ResultadoRegistro;
import com.example.gestionreparacionesapp.util.PdfGenerator;
import com.example.gestionreparacionesapp.util.SingleLiveEvent;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class VentasViewModel extends AndroidViewModel {

    private final VentaRepository repository;

    private final MutableLiveData<List<Venta>> listaVentas = new MutableLiveData<>();
    public LiveData<List<Venta>> getListaVentas() { return listaVentas; }

    private final MutableLiveData<ResultadoRegistro> operationResult = new MutableLiveData<>();
    public LiveData<ResultadoRegistro> getOperationResult() { return operationResult; }

    private final SingleLiveEvent<File> pdfGeneradoEvent = new SingleLiveEvent<>();
    public LiveData<File> getPdfGeneradoEvent() { return pdfGeneradoEvent; }

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

        Venta venta = new Venta(0, clienteSeleccionado.getId(), fecha, total, total, productosJson);

        repository.insertVenta(venta, productos, result -> {
            operationResult.postValue(result);
            if (result.isSuccess) {
                cargarVentas();
            }
        });
    }

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
        venta.setId(ventaId);

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

    // --- GENERAR COMPROBANTE DE VENTA ---
    public void generarComprobanteVenta(Context context, Venta venta) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(context);

            // 1. Obtener Cliente
            Cliente c = db.clienteDao().getById(venta.getClienteId());

            // 2. Obtener Productos
            List<ProductoVenta> productos = new ArrayList<>();
            try {
                // Asegúrate de que 'productoVentaDao()' exista en tu AppDatabase.java
                productos = db.productoVentaDao().getItemsPorVenta(venta.getId());
            } catch (Exception e) {
                Log.e("VentasViewModel", "Error al obtener productos desde BD, usando lista vacía", e);
            }

            if (c != null) {
                // 3. Generar PDF
                // Asegúrate de que PdfGenerator.java tenga el método generarComprobanteVenta
                File pdf = PdfGenerator.generarComprobanteVenta(context, c, venta, productos);

                if (pdf != null) {
                    pdfGeneradoEvent.postValue(pdf);
                }
            } else {
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(context, "No se encontró el cliente asociado a la venta", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private String convertirProductosAJson(List<ProductoVenta> productos) {
        JSONArray array = new JSONArray();
        try {
            for (ProductoVenta pv : productos) {
                JSONObject productoJson = new JSONObject();
                if (pv.getProducto() != null) {
                    productoJson.put("producto_id", pv.getProducto().getId());
                    productoJson.put("nombre", pv.getProducto().getNombre());
                    productoJson.put("precio", pv.getProducto().getPrecio());
                } else {
                    productoJson.put("nombre", pv.getNombreProductoSnapshot());
                    productoJson.put("precio", pv.getPrecioUnitarioSnapshot());
                }
                productoJson.put("cantidad", pv.getCantidad());
                array.put(productoJson);
            }
        } catch (Exception e) {
            Log.e("VentasViewModel", "Error al crear JSON", e);
        }
        return array.toString();
    }
}