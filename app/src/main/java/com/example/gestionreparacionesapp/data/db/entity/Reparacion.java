package com.example.gestionreparacionesapp.data.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "reparaciones",
        foreignKeys = {
                @ForeignKey(entity = Cliente.class,
                        parentColumns = "id",
                        childColumns = "cliente_id",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Usuario.class,
                        parentColumns = "id",
                        childColumns = "user_id",
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index("cliente_id"), @Index("user_id")}
)
public class Reparacion {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "user_id")
    private int userId;

    @ColumnInfo(name = "cliente_id")
    private int clienteId;

    private String fecha;
    private String descripcion;
    private String estado;
    private String productosJson;
    private double subtotal; // Coste de los productos

    @ColumnInfo(name = "coste_servicio")
    private double costeServicio; // Coste de la mano de obra

    private double total; // Coste total (productos + servicio)

    // --- ¡NUEVO CONSTRUCTOR VACÍO AÑADIDO! ---
    /**
     * Constructor vacío requerido para crear objetos temporales, como en el ViewModel
     * para la operación de borrado. Room puede usar el otro constructor.
     */
    public Reparacion() {
        // El cuerpo puede estar vacío.
    }

    // Room usará este constructor. No es necesario tener otros constructores con @Ignore.
    public Reparacion(int userId, int clienteId, String fecha, String descripcion, String estado, String productosJson, double subtotal, double costeServicio, double total) {
        this.userId = userId;
        this.clienteId = clienteId;
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.estado = estado;
        this.productosJson = productosJson;
        this.subtotal = subtotal;
        this.costeServicio = costeServicio;
        this.total = total;
    }

    // --- Getters y Setters (Sin cambios) ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getProductosJson() { return productosJson; }
    public void setProductosJson(String productosJson) { this.productosJson = productosJson; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public double getCosteServicio() { return costeServicio; }
    public void setCosteServicio(double costeServicio) { this.costeServicio = costeServicio; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
}
