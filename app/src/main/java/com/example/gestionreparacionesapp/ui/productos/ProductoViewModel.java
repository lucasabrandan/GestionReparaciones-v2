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
// --- 1. AÑADIR LA IMPORTACIÓN DE SINGLELIVEEVENT ---
import com.example.gestionreparacionesapp.util.SingleLiveEvent;

import java.util.List;

public class ProductoViewModel extends AndroidViewModel {

    private final ProductoRepository repository;
    private final MutableLiveData<List<Producto>> listaProductos = new MutableLiveData<>();
    public LiveData<List<Producto>> getListaProductos() { return listaProductos; }

    private final MutableLiveData<ResultadoRegistro> operationResult = new MutableLiveData<>();
    public LiveData<ResultadoRegistro> getOperationResult() { return operationResult; }


    // --- 2. DECLARAR EL NUEVO SINGLELIVEEVENT ---
    // Este evento se disparará solo cuando se cree un producto con éxito.
    private final SingleLiveEvent<Producto> nuevoProductoCreadoEvent = new SingleLiveEvent<>();

    // --- 3. CREAR EL GETTER PÚBLICO PARA EL EVENTO ---
    public LiveData<Producto> getNuevoProductoCreadoEvent() {
        return nuevoProductoCreadoEvent;
    }


    public ProductoViewModel(@NonNull Application application) {
        super(application);
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
     * Un método más simple para guardar/insertar un producto desde diálogos rápidos.
     */
    public void guardarProducto(String sku, String nombre, String precioStr, String cantidadStr) {
        // Llama al método principal de inserción pasando null para la imagen.
        // --- 4. MÉTODO MODIFICADO ---
        // Ahora el método `insertarProducto` se encarga de la lógica del SingleLiveEvent
        insertarProducto(sku, nombre, precioStr, cantidadStr, null);
    }

    /**
     * Inserta un nuevo producto.
     * --- 4. MÉTODO MODIFICADO ---
     */
    public void insertarProducto(String sku, String nombre, String precioStr, String cantidadStr, @Nullable String imageUri) {
        if (nombre.isEmpty()) { // Simplificamos la validación para diálogos rápidos
            operationResult.setValue(new ResultadoRegistro(false, "El nombre es obligatorio"));
            return;
        }
        try {
            double precio = 0.0;
            int cantidad = 0;

            if (precioStr != null && !precioStr.isEmpty()) {
                precio = Double.parseDouble(precioStr);
            }
            if (cantidadStr != null && !cantidadStr.isEmpty()) {
                cantidad = Integer.parseInt(cantidadStr);
            }

            Producto producto = new Producto(sku, nombre, precio, cantidad, imageUri);
            repository.insertProducto(producto, result -> {
                operationResult.postValue(result);
                if (result.isSuccess) {
                    // Si la inserción fue exitosa, el ID viene en el mensaje del resultado.
                    try {
                        long nuevoId = Long.parseLong(result.message);
                        producto.setId((int) nuevoId);

                        // ¡AQUÍ LA MAGIA!
                        // Disparamos nuestro SingleLiveEvent con el objeto Producto ya completo.
                        nuevoProductoCreadoEvent.postValue(producto);

                    } catch (NumberFormatException e) {
                        // El repositorio no devolvió un ID válido en el mensaje.
                        // Igualmente, el producto se creó.
                    }
                    // Recargamos la lista principal para que esté al día en todas partes.
                    cargarProductos();
                }
            });
        } catch (NumberFormatException e) {
            operationResult.setValue(new ResultadoRegistro(false, "Precio o cantidad inválidos"));
        }
    }

    /**
     * Actualiza un producto existente (Este método no necesita el SingleLiveEvent).
     */
    public void actualizarProducto(int productoId, String sku, String nombre, String precioStr, String cantidadStr, @Nullable String imageUri) {
        if (sku.isEmpty() || nombre.isEmpty() || precioStr.isEmpty() || cantidadStr.isEmpty()) {
            operationResult.setValue(new ResultadoRegistro(false, "Todos los campos son obligatorios"));
            return;
        }
        try {
            double precio = Double.parseDouble(precioStr);
            int cantidad = Integer.parseInt(cantidadStr);

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