package com.example.gestionreparacionesapp.data.db.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "productos_venta",
        foreignKeys = {
                @ForeignKey(entity = Venta.class,
                        parentColumns = "id",
                        childColumns = "ventaId",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Producto.class,
                        parentColumns = "id",
                        childColumns = "productoId",
                        onDelete = ForeignKey.NO_ACTION)
        },
        indices = {@Index("ventaId"), @Index("productoId")})
public class ProductoVenta {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private int ventaId;
    private int productoId;
    private String nombreProductoSnapshot; // Nombre guardado por si se borra el original
    private int cantidad;
    private double precioUnitarioSnapshot; // Precio al momento de la venta

    // Campo auxiliar para la UI (Ignore para que Room no intente guardarlo en esta tabla)
    @androidx.room.Ignore
    private Producto producto;

    public ProductoVenta() {}

    public ProductoVenta(int ventaId, int productoId, String nombreProductoSnapshot, int cantidad, double precioUnitarioSnapshot) {
        this.ventaId = ventaId;
        this.productoId = productoId;
        this.nombreProductoSnapshot = nombreProductoSnapshot;
        this.cantidad = cantidad;
        this.precioUnitarioSnapshot = precioUnitarioSnapshot;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getVentaId() { return ventaId; }
    public void setVentaId(int ventaId) { this.ventaId = ventaId; }
    public int getProductoId() { return productoId; }
    public void setProductoId(int productoId) { this.productoId = productoId; }
    public String getNombreProductoSnapshot() { return nombreProductoSnapshot; }
    public void setNombreProductoSnapshot(String nombreProductoSnapshot) { this.nombreProductoSnapshot = nombreProductoSnapshot; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public double getPrecioUnitarioSnapshot() { return precioUnitarioSnapshot; }
    public void setPrecioUnitarioSnapshot(double precioUnitarioSnapshot) { this.precioUnitarioSnapshot = precioUnitarioSnapshot; }

    public double getSubtotal() { return cantidad * precioUnitarioSnapshot; }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) {
        this.producto = producto;
        if (producto != null) {
            this.productoId = producto.getId();
            this.nombreProductoSnapshot = producto.getNombre();
            this.precioUnitarioSnapshot = producto.getPrecio();
        }
    }
}