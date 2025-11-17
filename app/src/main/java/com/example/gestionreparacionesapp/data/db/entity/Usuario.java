package com.example.gestionreparacionesapp.data.db.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.Ignore; // ¡NUEVO IMPORT!

/**
 * Entidad principal para la tabla de usuarios.
 */
@Entity(tableName = "usuarios",
        indices = {@Index(value = {"email"}, unique = true)})
public class Usuario {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String nombreCompleto;
    private String usuario;

    @NonNull
    private String email;

    @NonNull
    private String password;

    private String telefono;
    private boolean recordarme;

    // CONSTRUCTOR VACÍO (MARCADO CON @Ignore)
    // Room ya no te advertirá sobre múltiples constructores, ya que forzamos a usar el otro.
    @Ignore
    public Usuario() {
    }

    // CONSTRUCTOR PRINCIPAL (El que Room usará para crear la Entidad)
    public Usuario(String nombreCompleto, String usuario, @NonNull String email, @NonNull String password, String telefono) {
        this.nombreCompleto = nombreCompleto;
        this.usuario = usuario;
        this.email = email;
        this.password = password;
        this.telefono = telefono;
        this.recordarme = false;
    }

    // --- Getters y Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    @NonNull public String getEmail() { return email; }
    public void setEmail(@NonNull String email) { this.email = email; }
    @NonNull public String getPassword() { return password; }
    public void setPassword(@NonNull String password) { this.password = password; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public boolean isRecordarme() { return recordarme; }
    public void setRecordarme(boolean recordarme) { this.recordarme = recordarme; }
}