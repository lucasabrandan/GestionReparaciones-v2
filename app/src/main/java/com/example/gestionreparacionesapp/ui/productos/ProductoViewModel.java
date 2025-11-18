package com.example.gestionreparacionesapp.ui.productos;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.gestionreparacionesapp.data.db.AppDatabase;
import com.example.gestionreparacionesapp.data.db.entity.Producto;
import com.example.gestionreparacionesapp.data.repository.ProductoRepository;
import com.example.gestionreparacionesapp.data.util.ResultadoRegistro;

import java.util.List;

public class ProductoViewModel extends AndroidViewModel {

    private final ProductoRepository repository;
    private final MutableLiveData<List<Producto>> listaProductos = new MutableLiveData<>();
    public LiveData<List<Producto>> getListaProductos() { return listaProductos; }

    private final MutableLiveData<ResultadoRegistro> operationResult = new MutableLiveData<>();
    public LiveData<ResultadoRegistro> getOperationResult() { return operationResult; }

    public ProductoViewModel(@NonNull Application application) {
        super(application);
        // CAMBIO: Pasamos el contexto (Application) al Repositorio
        this.repository = new ProductoRepository(
                AppDatabase.getInstance(application).productoDao(),
                application
        );
    }

    public void cargarProductos() {
        repository.getAllProductos(result -> listaProductos.postValue(result));
    }

    public void buscarProductos(String query) {
        if (query == null || query.trim().isEmpty()) {
            cargarProductos();
        } else {
            repository.buscarProductosPorNombre(query, result -> listaProductos.postValue(result));
        }
    }

    /**
     * Inserta un nuevo producto (YA NO NECESITA userId, lo toma de la sesión).
     */
    public void insertarProducto(String sku, String nombre, String precioStr, String cantidadStr, @Nullable String imageUri) {
        if (sku.isEmpty() || nombre.isEmpty() || precioStr.isEmpty() || cantidadStr.isEmpty()) {
            operationResult.setValue(new ResultadoRegistro(false, "Todos los campos son obligatorios"));
            return;
        }
        try {
            double precio = Double.parseDouble(precioStr);
            int cantidad = Integer.parseInt(cantidadStr);

            // CAMBIO: El constructor ya no lleva userId, el Repo lo asigna
            Producto producto = new Producto(sku, nombre, precio, cantidad, imageUri);
            repository.insertProducto(producto, result -> {
                operationResult.postValue(result);
                if (result.isSuccess) cargarProductos();
            });
        } catch (NumberFormatException e) {
            operationResult.setValue(new ResultadoRegistro(false, "Precio o cantidad inválidos"));
        }
    }

    /**
     * Actualiza un producto existente (YA NO NECESITA userId).
     */
    public void actualizarProducto(int productoId, String sku, String nombre, String precioStr, String cantidadStr, @Nullable String imageUri) {
        if (sku.isEmpty() || nombre.isEmpty() || precioStr.isEmpty() || cantidadStr.isEmpty()) {
            operationResult.setValue(new ResultadoRegistro(false, "Todos los campos son obligatorios"));
            return;
        }
        try {
            double precio = Double.parseDouble(precioStr);
            int cantidad = Integer.parseInt(cantidadStr);

            // CAMBIO: El constructor ya no lleva userId, el Repo lo asigna
            Producto producto = new Producto(sku, nombre, precio, cantidad, imageUri);
            producto.setId(productoId);

            repository.updateProducto(producto, result -> {
                operationResult.postValue(result);
                if (result.isSuccess) cargarProductos();
            });
        } catch (NumberFormatException e) {
            operationResult.setValue(new ResultadoRegistro(false, "Precio o cantidad inválidos"));
        }
    }

    /**
     * Elimina un producto.
     */
    public void eliminarProducto(Producto producto) {
        repository.deleteProducto(producto, result -> {
            operationResult.postValue(result);
            if (result.isSuccess) {
                cargarProductos(); // Recargar la lista después de eliminar
            }
        });
    }
}