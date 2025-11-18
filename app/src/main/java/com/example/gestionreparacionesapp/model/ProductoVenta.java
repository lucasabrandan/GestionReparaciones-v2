package com.example.gestionreparacionesapp.model; // O el paquete donde tengas el archivo (ej: ui.ventas)

import com.example.gestionreparacionesapp.data.db.entity.Producto;

/**
 * Clase contenedora (POJO) que representa un producto dentro de una venta o reparación.
 * Guarda el objeto Producto completo y la cantidad seleccionada en un momento dado.
 * No es una entidad de la base de datos, es un modelo para la interfaz de usuario.
 */
public class ProductoVenta {

    private final Producto producto;
    private final int cantidad;

    public ProductoVenta(Producto producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
    }

    public Producto getProducto() {
        return producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    /**
     * Calcula el subtotal para este renglón (precio del producto * cantidad).
     * @return El subtotal calculado.
     */
    public double getSubtotal() {
        if (producto != null) {
            return producto.getPrecio() * cantidad;
        }
        return 0;
    }

    /**
     * Método de conveniencia para obtener el nombre del producto de forma segura.
     * @return El nombre del producto o un texto por defecto si es nulo.
     */
    public String getNombreProducto() {
        return producto != null ? producto.getNombre() : "Producto desconocido";
    }

    // --- ¡ESTE ES EL MÉTODO QUE RESUELVE EL ERROR DE COMPILACIÓN! ---
    /**
     * Método de conveniencia para obtener directamente el ID del producto contenido.
     * Es necesario para la lógica de edición en los diálogos, para poder encontrar
     * y seleccionar el producto correcto en el Spinner.
     * @return el ID del producto, o -1 si el producto es nulo para evitar errores.
     */
    public int getProductoId() {
        if (producto != null) {
            // Llama al método getId() del objeto Producto que tiene dentro.
            return producto.getId();
        }
        // Devolvemos -1 para manejar de forma segura el caso de que el producto sea nulo.
        return -1;
    }
}