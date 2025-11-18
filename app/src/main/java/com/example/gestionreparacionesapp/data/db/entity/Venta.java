package com.example.gestionreparacionesapp.data.db.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "ventas",
        foreignKeys = {
                @ForeignKey(entity = Cliente.class,
                        parentColumns = "id",
                        childColumns = "clienteId",
                        onDelete = ForeignKey.CASCADE),
                // AÑADIDO: Clave foránea al Usuario
                @ForeignKey(entity = Usuario.class,
                        parentColumns = "id",
                        childColumns = "userId",
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {
                @Index(value = "clienteId"),
                @Index(value = "userId") // Índice para búsquedas por usuario
        })
public class Venta {

    @PrimaryKey(autoGenerate = true)
    private int id;

    // NUEVO: El ID del usuario dueño de esta venta
    private int userId;

    private int clienteId;
    @NonNull
    private String fecha;
    private double subtotal;
    private double total;
    @NonNull
    private String productosJson;

    @Ignore // <--- AÑADIDO: Soluciona el warning de "multiple constructors"
    public Venta() {
        this.fecha = "";
        this.productosJson = "[]";
    }

    // Constructor principal actualizado (con userId)
    public Venta(int userId, int clienteId, @NonNull String fecha, double subtotal, double total, @NonNull String productosJson) {
        this.userId = userId;
        this.clienteId = clienteId;
        this.fecha = fecha;
        this.subtotal = subtotal;
        this.total = total;
        this.productosJson = productosJson;
    }

    // --- Getters y Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }

    @NonNull public String getFecha() { return fecha; }
    public void setFecha(@NonNull String fecha) { this.fecha = fecha; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    @NonNull public String getProductosJson() { return productosJson; }
    public void setProductosJson(@NonNull String productosJson) { this.productosJson = productosJson; }
}