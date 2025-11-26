package com.example.gestionreparacionesapp.data.db.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "productos_reparacion",
        foreignKeys = {
                @ForeignKey(entity = Reparacion.class,
                        parentColumns = "id",
                        childColumns = "reparacionId",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Producto.class,
                        parentColumns = "id",
                        childColumns = "productoId",
                        onDelete = ForeignKey.NO_ACTION) // No borrar el historial si se borra el producto
        },
        indices = {@Index("reparacionId"), @Index("productoId")})
public class ProductoReparacion {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private int reparacionId;
    private int productoId;
    private String nombreProductoSnapshot; // Guardamos el nombre por si el producto original se borra
    private int cantidad;
    private double precioUnitarioCobrado; // El precio que le cobraste al cliente (editable)

    public ProductoReparacion(int reparacionId, int productoId, String nombreProductoSnapshot, int cantidad, double precioUnitarioCobrado) {
        this.reparacionId = reparacionId;
        this.productoId = productoId;
        this.nombreProductoSnapshot = nombreProductoSnapshot;
        this.cantidad = cantidad;
        this.precioUnitarioCobrado = precioUnitarioCobrado;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getReparacionId() { return reparacionId; }
    public void setReparacionId(int reparacionId) { this.reparacionId = reparacionId; }
    public int getProductoId() { return productoId; }
    public void setProductoId(int productoId) { this.productoId = productoId; }
    public String getNombreProductoSnapshot() { return nombreProductoSnapshot; }
    public void setNombreProductoSnapshot(String nombreProductoSnapshot) { this.nombreProductoSnapshot = nombreProductoSnapshot; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public double getPrecioUnitarioCobrado() { return precioUnitarioCobrado; }
    public void setPrecioUnitarioCobrado(double precioUnitarioCobrado) { this.precioUnitarioCobrado = precioUnitarioCobrado; }

    public double getSubtotal() { return cantidad * precioUnitarioCobrado; }
}