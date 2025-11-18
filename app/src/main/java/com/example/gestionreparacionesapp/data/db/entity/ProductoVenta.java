package com.example.gestionreparacionesapp.ui.ventas;

import com.example.gestionreparacionesapp.data.db.entity.Producto;

/**
 * Clase auxiliar para manejar la lógica de negocio de un producto en una venta o reparación.
 */
public class ProductoVenta {
    private Producto producto;
    private int cantidad;

    public ProductoVenta(Producto producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getSubtotal() {
        if (producto != null) {
            return producto.getPrecio() * cantidad;
        }
        return 0.0;
    }
}