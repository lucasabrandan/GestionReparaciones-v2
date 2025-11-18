package com.example.gestionreparacionesapp.data.db.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

// Dejamos la relación con Usuario, que es importante y no causa problemas.
@Entity(tableName = "reparaciones",
        foreignKeys = @ForeignKey(entity = Usuario.class,
                parentColumns = "id",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index(value = {"userId"})}
)
public class Reparacion {

    @PrimaryKey(autoGenerate = true)
    private int id;

    // --- CAMPOS ORIGINALES Y SIMPLES ---
    private int userId;
    private int clienteId;
    private String productoNombre; // Volvemos a un solo nombre de producto
    private String descripcionProblema;
    private double presupuesto;
    private String estado;

    // Constructor vacío (requerido por Room)
    public Reparacion() {}

    // Constructor principal que usaremos
    public Reparacion(int userId, int clienteId, String productoNombre, String descripcionProblema, double presupuesto, String estado) {
        this.userId = userId;
        this.clienteId = clienteId;
        this.productoNombre = productoNombre;
        this.descripcionProblema = descripcionProblema;
        this.presupuesto = presupuesto;
        this.estado = estado;
    }

    // --- Getters y Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }

    public String getProductoNombre() { return productoNombre; }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }

    public String getDescripcionProblema() { return descripcionProblema; }
    public void setDescripcionProblema(String descripcionProblema) { this.descripcionProblema = descripcionProblema; }

    public double getPresupuesto() { return presupuesto; }
    public void setPresupuesto(double presupuesto) { this.presupuesto = presupuesto; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}