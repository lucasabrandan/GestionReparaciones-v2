package com.example.gestionreparacionesapp.model;

import com.example.gestionreparacionesapp.data.db.entity.Reparacion;

import java.util.List;

/**
 * Clase POJO (Plain Old Java Object) que funciona como un simple "contenedor".
 * Su propósito es agrupar una Reparación y su lista de productos (que se obtienen
 * al parsear el campo JSON de la reparación).
 *
 * NO es una entidad de Room y no usa anotaciones de base de datos. Sirve
 * exclusivamente para organizar los datos para la interfaz de usuario (Fragment/ViewModel).
 */
public class ReparacionConProductos {

    public final Reparacion reparacion;
    public final List<ProductoVenta> productos;

    /**
     * Constructor para crear un objeto que contiene una reparación y su lista de productos asociada.
     * @param reparacion El objeto Reparacion de la base de datos.
     * @param productos La lista de objetos ProductoVenta que se generó al leer el JSON de la reparación.
     */
    public ReparacionConProductos(Reparacion reparacion, List<ProductoVenta> productos) {
        this.reparacion = reparacion;
        this.productos = productos;
    }
}
