package com.example.gestionreparacionesapp.data.db.dao;

import androidx.room.Dao;
import androidx.room.Transaction;
import com.example.gestionreparacionesapp.data.db.AppDatabase;
import com.example.gestionreparacionesapp.data.db.entity.Producto;
import com.example.gestionreparacionesapp.data.db.entity.Venta;
import com.example.gestionreparacionesapp.ui.ventas.ProductoVenta;
import java.util.List;

@Dao
public abstract class VentaConProductosDao {

    // Variable estática para guardar el mensaje de error de stock
    private static String mensajeErrorStock = "";

    /**
     * Inserta una venta y descuenta el stock. Este es el método que llamará el Repositorio.
     * Ya NO declara 'throws Exception'.
     * @return El ID de la venta si tiene éxito, o -1L si falla la validación de stock.
     */
    @Transaction
    public long insertarVentaYActualizarStock(Venta venta, List<ProductoVenta> productosVendidos, AppDatabase db) {
        ProductoDao productoDao = db.productoDao();
        mensajeErrorStock = ""; // Limpiamos el mensaje de error anterior

        // --- PASO 1: VALIDACIÓN DE STOCK ---
        for (ProductoVenta pv : productosVendidos) {
            Producto productoEnDB = productoDao.getById(pv.getProducto().getId());

            if (productoEnDB == null) {
                mensajeErrorStock = "Error: El producto con ID " + pv.getProducto().getId() + " no fue encontrado.";
                return -1L; // Falla la transacción
            }

            if (productoEnDB.getCantidad() < pv.getCantidad()) {
                // Guardamos el mensaje de error y devolvemos -1 para indicar el fallo
                mensajeErrorStock = "Stock insuficiente para '" + productoEnDB.getNombre() + "'. Se requieren " + pv.getCantidad() + ", disponibles: " + productoEnDB.getCantidad();
                return -1L; // Falla la transacción
            }
        }
        // Si llegamos aquí, la validación fue exitosa.

        // --- PASO 2: DESCUENTO DE STOCK ---
        for (ProductoVenta pv : productosVendidos) {
            Producto productoEnDB = productoDao.getById(pv.getProducto().getId());
            int nuevoStock = productoEnDB.getCantidad() - pv.getCantidad();
            productoEnDB.setCantidad(nuevoStock);
            productoDao.update(productoEnDB);
        }

        // --- PASO 3: INSERTAR LA VENTA ---
        VentaDao ventaDao = db.ventaDao();
        return ventaDao.insert(venta); // Devuelve el ID de la nueva venta (un número positivo)
    }

    /**
     * Método auxiliar para obtener el mensaje de error específico.
     */
    public String getMensajeErrorStock() {
        return mensajeErrorStock;
    }
}
