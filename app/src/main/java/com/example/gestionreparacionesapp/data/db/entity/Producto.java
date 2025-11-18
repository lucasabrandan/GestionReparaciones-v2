package com.example.gestionreparacionesapp.data.db.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "productos",
        // AÑADIDO: Clave foránea al Usuario
        foreignKeys = @ForeignKey(entity = Usuario.class,
                parentColumns = "id",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE), // Si se borra el usuario, se borran sus productos
        indices = {
                @Index(value = {"sku", "userId"}, unique = true), // SKU único POR USUARIO
                @Index(value = {"userId"}) // Índice para búsquedas por usuario
        })
public class Producto {

    @PrimaryKey(autoGenerate = true)
    private int id;

    // NUEVO: El ID del usuario dueño de este producto
    private int userId;

    @NonNull
    private String sku;

    @NonNull
    private String nombre;
    private double precio;
    private int cantidad;

    @Nullable
    private String imageUri;

    @Ignore
    public Producto() {
        this.sku = "";
        this.nombre = "";
        this.imageUri = null;
    }

    // Constructor para el ViewModel (no necesita userId, el Repo lo asigna)
    @Ignore
    public Producto(@NonNull String sku, @NonNull String nombre, double precio, int cantidad, @Nullable String imageUri) {
        this.sku = sku;
        this.nombre = nombre;
        this.precio = precio;
        this.cantidad = cantidad;
        this.imageUri = imageUri;
    }

    // Constructor Completo (para Room)
    public Producto(int userId, @NonNull String sku, @NonNull String nombre, double precio, int cantidad, @Nullable String imageUri) {
        this.userId = userId;
        this.sku = sku;
        this.nombre = nombre;
        this.precio = precio;
        this.cantidad = cantidad;
        this.imageUri = imageUri;
    }

    // --- Getters y Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    @NonNull
    public String getSku() { return sku; }
    public void setSku(@NonNull String sku) { this.sku = sku; }

    @NonNull
    public String getNombre() { return nombre; }
    public void setNombre(@NonNull String nombre) { this.nombre = nombre; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    @Nullable
    public String getImageUri() { return imageUri; }
    public void setImageUri(@Nullable String imageUri) { this.imageUri = imageUri; }

    @NonNull
    @Override
    public String toString() {
        return nombre;
    }
}