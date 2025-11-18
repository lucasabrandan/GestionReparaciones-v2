package com.example.gestionreparacionesapp.data.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.gestionreparacionesapp.data.db.entity.Usuario;

import java.util.List;

@Dao
public interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(Usuario usuario);

    @Update
    void update(Usuario usuario); // CLAVE: Necesario para actualizar el campo 'recordarme'

    // CLAVE PARA LOGIN: Busca usuario por email.
    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    Usuario getUsuarioByEmail(String email);

    // CLAVE PARA PERSISTENCIA: Busca usuario recordado.
    @Query("SELECT * FROM usuarios WHERE recordarme = 1 LIMIT 1")
    Usuario getUsuarioRecordado();

    // CLAVE PARA PERSISTENCIA: Limpia el estado recordado.
    @Query("UPDATE usuarios SET recordarme = 0")
    void limpiarRecordarme();

    // CLAVE PARA PERSISTENCIA (¡EL QUE FALTABA!): Obtiene usuario por ID.
    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    Usuario getUsuarioById(int id);

    // Mantenido para el Registro
    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    Usuario getByEmail(String email);

    // Mantenido para el Registro
    @Query("SELECT * FROM usuarios WHERE usuario = :usuario LIMIT 1")
    Usuario getByUsuario(String usuario);

    @Query("SELECT * FROM usuarios")
    List<Usuario> getAll();
}