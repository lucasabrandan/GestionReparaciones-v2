// REEMPLAZA TODO EL ARCHIVO CON ESTE CÓDIGO
package com.example.gestionreparacionesapp.data.db.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

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

    private int userId;
    private int clienteId;

    // --- NUEVOS CAMPOS PARA EL EQUIPO (DISPENSER) ---
    private String equipoMarca;
    private String equipoModelo;
    private String equipoSerie;

    // --- CAMPOS PARA EL TRABAJO REALIZADO ---
    private String descripcionProblema;
    private String repuestosUtilizados; // Guardaremos como texto, separado por comas
    private double costoRepuestos;
    private double costoManoDeObra;

    // --- CAMPO DE ESTADO ---
    private String estado;

    // Constructor vacío requerido por Room
    public Reparacion() {}

    // Constructor que usaremos
    public Reparacion(int userId, int clienteId, String equipoMarca, String equipoModelo, String equipoSerie, String descripcionProblema, String repuestosUtilizados, double costoRepuestos, double costoManoDeObra, String estado) {
        this.userId = userId;
        this.clienteId = clienteId;
        this.equipoMarca = equipoMarca;
        this.equipoModelo = equipoModelo;
        this.equipoSerie = equipoSerie;
        this.descripcionProblema = descripcionProblema;
        this.repuestosUtilizados = repuestosUtilizados;
        this.costoRepuestos = costoRepuestos;
        this.costoManoDeObra = costoManoDeObra;
        this.estado = estado;
    }

    // --- Getters y Setters para todos los campos ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }

    public String getEquipoMarca() { return equipoMarca; }
    public void setEquipoMarca(String equipoMarca) { this.equipoMarca = equipoMarca; }

    public String getEquipoModelo() { return equipoModelo; }
    public void setEquipoModelo(String equipoModelo) { this.equipoModelo = equipoModelo; }

    public String getEquipoSerie() { return equipoSerie; }
    public void setEquipoSerie(String equipoSerie) { this.equipoSerie = equipoSerie; }

    public String getDescripcionProblema() { return descripcionProblema; }
    public void setDescripcionProblema(String descripcionProblema) { this.descripcionProblema = descripcionProblema; }

    public String getRepuestosUtilizados() { return repuestosUtilizados; }
    public void setRepuestosUtilizados(String repuestosUtilizados) { this.repuestosUtilizados = repuestosUtilizados; }

    public double getCostoRepuestos() { return costoRepuestos; }
    public void setCostoRepuestos(double costoRepuestos) { this.costoRepuestos = costoRepuestos; }

    public double getCostoManoDeObra() { return costoManoDeObra; }
    public void setCostoManoDeObra(double costoManoDeObra) { this.costoManoDeObra = costoManoDeObra; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    // Método de ayuda para calcular el presupuesto total
    public double getPresupuestoTotal() {
        return this.costoRepuestos + this.costoManoDeObra;
    }
}
