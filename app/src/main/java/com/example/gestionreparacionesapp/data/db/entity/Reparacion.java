package com.example.gestionreparacionesapp.data.db.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "reparaciones",
        foreignKeys = {
                @ForeignKey(entity = Cliente.class,
                        parentColumns = "id",
                        childColumns = "clienteId",
                        onDelete = ForeignKey.CASCADE),
                // Clave foránea al Usuario (para seguridad de datos)
                @ForeignKey(entity = Usuario.class,
                        parentColumns = "id",
                        childColumns = "userId",
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {
                // Índices para mejorar el rendimiento de las búsquedas
                @Index(value = "clienteId"),
                @Index(value = "userId")
        })
public class Reparacion {

    @PrimaryKey(autoGenerate = true)
    private int id;

    // ID del usuario dueño de esta reparación
    private int userId;

    private int clienteId;
    @NonNull
    private String fecha;
    @NonNull
    private String descripcion;
    private double subtotal;
    private double total;
    @NonNull
    private String productosJson;

    @Ignore // Soluciona el warning de Room
    public Reparacion() {
        this.fecha = "";
        this.descripcion = "";
        this.productosJson = "[]";
    }

    // Constructor principal (ahora con userId)
    public Reparacion(int userId, int clienteId, @NonNull String fecha, @NonNull String descripcion, double subtotal, double total, @NonNull String productosJson) {
        this.userId = userId;
        this.clienteId = clienteId;
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.subtotal = subtotal;
        this.total = total;
        this.productosJson = productosJson;
    }

    // Constructor para el ViewModel (no necesita userId, el Repo lo asigna)
    @Ignore
    public Reparacion(int clienteId, @NonNull String fecha, @NonNull String descripcion, double subtotal, double total, @NonNull String productosJson) {
        this.clienteId = clienteId;
        this.fecha = fecha;
        this.descripcion = descripcion;
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
    @NonNull public String getDescripcion() { return descripcion; }
    public void setDescripcion(@NonNull String descripcion) { this.descripcion = descripcion; }
    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    @NonNull public String getProductosJson() { return productosJson; }
    public void setProductosJson(@NonNull String productosJson) { this.productosJson = productosJson; }
}