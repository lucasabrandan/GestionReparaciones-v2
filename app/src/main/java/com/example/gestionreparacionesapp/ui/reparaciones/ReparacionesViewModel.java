package com.example.gestionreparacionesapp.ui.reparaciones;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.gestionreparacionesapp.data.db.AppDatabase;
import com.example.gestionreparacionesapp.data.db.dao.ClienteDao;
import com.example.gestionreparacionesapp.data.db.dao.ReparacionDao;
import com.example.gestionreparacionesapp.data.db.entity.Cliente;
import com.example.gestionreparacionesapp.data.db.entity.ProductoReparacion;
import com.example.gestionreparacionesapp.data.db.entity.Reparacion;
import com.example.gestionreparacionesapp.data.repository.ReparacionRepository;
import com.example.gestionreparacionesapp.data.util.ResultadoRegistro;
import com.example.gestionreparacionesapp.util.PdfGenerator;
import com.example.gestionreparacionesapp.util.SingleLiveEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class ReparacionesViewModel extends AndroidViewModel {

    private final ReparacionRepository repository;
    private final MutableLiveData<List<Reparacion>> listaReparaciones = new MutableLiveData<>();

    // --- GESTIÓN DE SESIÓN (LOTE DE REPARACIONES) ---
    // Lista de reparaciones pendientes de guardar en esta sesión
    private final List<Reparacion> sessionReparaciones = new ArrayList<>();
    // Mapa para vincular cada reparación con sus repuestos
    private final Map<Reparacion, List<ProductoReparacion>> sessionRepuestos = new HashMap<>();

    private final MutableLiveData<Integer> sessionCount = new MutableLiveData<>(0); // Para saber cuántas llevamos cargadas
    private final SingleLiveEvent<File> pdfGeneradoEvent = new SingleLiveEvent<>();
    private final SingleLiveEvent<String> mensajeToast = new SingleLiveEvent<>();

    // Repuestos temporales de la máquina actual (antes de agregarla al lote)
    private final MutableLiveData<List<ProductoReparacion>> repuestosMaquinaActual = new MutableLiveData<>(new ArrayList<>());

    public ReparacionesViewModel(@NonNull Application application) {
        super(application);
        ReparacionDao reparacionDao = AppDatabase.getInstance(application).reparacionDao();
        repository = new ReparacionRepository(reparacionDao, application);
    }

    public LiveData<List<Reparacion>> getListaReparaciones() { return listaReparaciones; }
    public LiveData<List<ProductoReparacion>> getRepuestosMaquinaActual() { return repuestosMaquinaActual; }
    public LiveData<File> getPdfGeneradoEvent() { return pdfGeneradoEvent; }
    public LiveData<Integer> getSessionCount() { return sessionCount; }
    public LiveData<String> getMensajeToast() { return mensajeToast; }

    public void cargarReparaciones() { repository.obtenerTodasLasReparaciones(listaReparaciones::postValue); }
    public void buscarReparaciones(String q) { repository.buscarReparacionesPorTermino(q, listaReparaciones::postValue); }

    // 1. Gestión de Repuestos para la máquina actual
    public void limpiarRepuestosActuales() { repuestosMaquinaActual.setValue(new ArrayList<>()); }

    public void agregarRepuestoActual(ProductoReparacion item) {
        List<ProductoReparacion> l = repuestosMaquinaActual.getValue();
        if(l!=null) { l.add(item); repuestosMaquinaActual.setValue(l); }
    }

    // 2. AGREGAR MÁQUINA AL LOTE (No guarda en DB todavía)
    public void agregarReparacionALote(int userId, int clienteId, String marca, String modelo, String serie, String desc, double manoObra, String estado) {
        List<ProductoReparacion> repuestos = new ArrayList<>(repuestosMaquinaActual.getValue());

        // Calcular costos AQUÍ para que el objeto en memoria tenga los datos correctos para el PDF
        double costoRepuestos = 0;
        StringBuilder nombresRep = new StringBuilder();
        for(ProductoReparacion pr : repuestos) {
            costoRepuestos += (pr.getCantidad() * pr.getPrecioUnitarioCobrado());
            nombresRep.append(pr.getCantidad()).append("x ").append(pr.getNombreProductoSnapshot()).append(" ");
        }

        Reparacion reparacion = new Reparacion(userId, clienteId, marca, modelo, serie, desc, nombresRep.toString(), costoRepuestos, manoObra, estado);

        // Guardar en memoria
        sessionReparaciones.add(reparacion);
        sessionRepuestos.put(reparacion, repuestos);

        sessionCount.setValue(sessionReparaciones.size());
        mensajeToast.setValue("Máquina agregada al lote. Total: " + sessionReparaciones.size());

        // Limpiar para la siguiente
        limpiarRepuestosActuales();
    }

    // 3. FINALIZAR SESIÓN: Guardar todo en DB y Generar PDF
    public void finalizarSesionYGenerarPdf(Context context, int clienteId) {
        if (sessionReparaciones.isEmpty()) {
            mensajeToast.setValue("No hay reparaciones cargadas para finalizar.");
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            // A. Guardar todo en Base de Datos
            for (Reparacion rep : sessionReparaciones) {
                List<ProductoReparacion> repuestos = sessionRepuestos.get(rep);
                // Guardamos usando el repository
                repository.insertarReparacionConRepuestos(rep.getClienteId(), rep.getEquipoMarca(), rep.getEquipoModelo(), rep.getEquipoSerie(),
                        rep.getDescripcionProblema(), rep.getCostoManoDeObra(), rep.getEstado(), repuestos, res -> {});
            }

            // B. Generar PDF con los objetos en memoria (que ya tienen los precios correctos)
            ClienteDao clienteDao = AppDatabase.getInstance(context).clienteDao();
            Cliente cliente = clienteDao.getById(clienteId);

            if (cliente != null) {
                // Usamos el generador para la lista completa de esta sesión
                File pdf = PdfGenerator.generarPresupuestoPdf(context, cliente, new ArrayList<>(sessionReparaciones));
                if (pdf != null) pdfGeneradoEvent.postValue(pdf);
            }

            // C. Limpiar sesión
            sessionReparaciones.clear();
            sessionRepuestos.clear();
            sessionCount.postValue(0);
            cargarReparaciones(); // Refrescar lista fondo
        });
    }

    public void limpiarSesion() {
        sessionReparaciones.clear();
        sessionRepuestos.clear();
        sessionCount.setValue(0);
    }

    // --- MÉTODOS INDIVIDUALES (Edición / Eliminación / PDF Individual) ---

    public void eliminarReparacion(int id) {
        repository.eliminarReparacionPorId(id, res -> cargarReparaciones());
    }

    public void actualizarReparacionSimple(int id, int cId, String ma, String mo, String se, String de, String repu, String cRep, String cMan, String est) {
        repository.actualizarReparacionSimple(id, cId, ma, mo, se, de, repu, cRep, cMan, est, res -> cargarReparaciones());
    }

    public void generarPdfYCompartir(Context context, Reparacion reparacion) {
        Executors.newSingleThreadExecutor().execute(() -> {
            ClienteDao clienteDao = AppDatabase.getInstance(context).clienteDao();
            Cliente cliente = clienteDao.getById(reparacion.getClienteId());
            if (cliente != null) {
                File pdf = PdfGenerator.generarPresupuestoIndividual(context, cliente, reparacion);
                if (pdf != null) pdfGeneradoEvent.postValue(pdf);
            }
        });
    }
}