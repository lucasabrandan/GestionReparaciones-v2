package com.example.gestionreparacionesapp.data.db.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "clientes",
        // AÑADIDO: Clave foránea al Usuario
        foreignKeys = @ForeignKey(entity = Usuario.class,
                parentColumns = "id",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE), // Si se borra el usuario, se borran sus clientes
        indices = {
                @Index(value = {"dni", "userId"}, unique = true), // DNI único POR USUARIO
                @Index(value = {"userId"})
        })
public class Cliente {

    @PrimaryKey(autoGenerate = true)
    private int id;

    // NUEVO: El ID del usuario dueño de este cliente
    private int userId;

    @NonNull
    private String dni;

    @NonNull
    private String nombre;
    @NonNull
    private String direccion;
    @NonNull
    private String localidad;
    @NonNull
    private String codigoPostal;

    @Ignore
    public Cliente() {
        this.dni = "";
        this.nombre = "";
        this.direccion = "";
        this.localidad = "";
        this.codigoPostal = "";
    }

    // Constructor para el ViewModel (no necesita userId, el Repo lo asigna)
    @Ignore
    public Cliente(@NonNull String dni, @NonNull String nombre, @NonNull String direccion, @NonNull String localidad, @NonNull String codigoPostal) {
        this.dni = dni;
        this.nombre = nombre;
        this.direccion = direccion;
        this.localidad = localidad;
        this.codigoPostal = codigoPostal;
    }

    // Constructor Completo (para Room)
    public Cliente(int userId, @NonNull String dni, @NonNull String nombre, @NonNull String direccion, @NonNull String localidad, @NonNull String codigoPostal) {
        this.userId = userId;
        this.dni = dni;
        this.nombre = nombre;
        this.direccion = direccion;
        this.localidad = localidad;
        this.codigoPostal = codigoPostal;
    }

    // --- Getters y Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    @NonNull
    public String getDni() { return dni; }
    public void setDni(@NonNull String dni) { this.dni = dni; }

    @NonNull
    public String getNombre() { return nombre; }
    public void setNombre(@NonNull String nombre) { this.nombre = nombre; }

    @NonNull
    public String getDireccion() { return direccion; }
    public void setDireccion(@NonNull String direccion) { this.direccion = direccion; }

    @NonNull
    public String getLocalidad() { return localidad; }
    public void setLocalidad(@NonNull String localidad) { this.localidad = localidad; }

    @NonNull
    public String getCodigoPostal() { return codigoPostal; }
    public void setCodigoPostal(@NonNull String codigoPostal) { this.codigoPostal = codigoPostal; }

    @NonNull
    @Override
    public String toString() {
        return nombre;
    }
}