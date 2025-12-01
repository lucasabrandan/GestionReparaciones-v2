package com.example.gestionreparacionesapp.data.db.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "ventas",
        foreignKeys = @ForeignKey(entity = Usuario.class,
                parentColumns = "id",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE))
public class Venta {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private int userId;
    private int clienteId;
    private String fecha;
    private double subtotal;
    private double total;
    private String productosJson;

    // --- NUEVO CAMPO: Ruta del PDF ---
    private String pdfPath;

    public Venta(int userId, int clienteId, String fecha, double subtotal, double total, String productosJson) {
        this.userId = userId;
        this.clienteId = clienteId;
        this.fecha = fecha;
        this.subtotal = subtotal;
        this.total = total;
        this.productosJson = productosJson;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public String getProductosJson() { return productosJson; }
    public void setProductosJson(String productosJson) { this.productosJson = productosJson; }

    public String getPdfPath() { return pdfPath; }
    public void setPdfPath(String pdfPath) { this.pdfPath = pdfPath; }
}