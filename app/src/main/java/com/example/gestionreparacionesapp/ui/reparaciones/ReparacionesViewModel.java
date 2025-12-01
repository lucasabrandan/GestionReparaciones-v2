package com.example.gestionreparacionesapp.ui.reparaciones;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.gestionreparacionesapp.data.db.AppDatabase;
import com.example.gestionreparacionesapp.data.db.dao.ClienteDao;
import com.example.gestionreparacionesapp.data.db.dao.ProductoReparacionDao;
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

    private final List<Reparacion> sessionReparaciones = new ArrayList<>();
    private final Map<Reparacion, List<ProductoReparacion>> sessionRepuestos = new HashMap<>();

    private final MutableLiveData<Integer> sessionCount = new MutableLiveData<>(0);
    private final SingleLiveEvent<File> pdfGeneradoEvent = new SingleLiveEvent<>();
    private final SingleLiveEvent<String> mensajeToast = new SingleLiveEvent<>();

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

    public void limpiarRepuestosActuales() { repuestosMaquinaActual.setValue(new ArrayList<>()); }

    public void agregarRepuestoActual(ProductoReparacion item) {
        List<ProductoReparacion> l = repuestosMaquinaActual.getValue();
        if(l == null) l = new ArrayList<>();
        l.add(item);
        repuestosMaquinaActual.setValue(l);
    }

    // Método para actualizar repuesto si se cambia en la lista (opcional, por ahora solo agrega)
    public void setRepuestosActuales(List<ProductoReparacion> lista) {
        repuestosMaquinaActual.setValue(lista);
    }

    public void agregarReparacionALote(int userId, int clienteId, String marca, String modelo, String serie, String desc, double manoObra, String estado) {
        List<ProductoReparacion> repuestos = new ArrayList<>();
        if (repuestosMaquinaActual.getValue() != null) {
            repuestos.addAll(repuestosMaquinaActual.getValue());
        }

        double costoRepuestos = 0;
        StringBuilder nombresRep = new StringBuilder();
        for(ProductoReparacion pr : repuestos) {
            costoRepuestos += (pr.getCantidad() * pr.getPrecioUnitarioCobrado());
            nombresRep.append(pr.getCantidad()).append("x ").append(pr.getNombreProductoSnapshot()).append(" ");
        }

        Reparacion reparacion = new Reparacion(userId, clienteId, marca, modelo, serie, desc, nombresRep.toString(), costoRepuestos, manoObra, estado);

        sessionReparaciones.add(reparacion);
        sessionRepuestos.put(reparacion, repuestos);
        sessionCount.setValue(sessionReparaciones.size());
        mensajeToast.setValue("Máquina agregada. Total en lote: " + sessionReparaciones.size());
        limpiarRepuestosActuales();
    }

    public void finalizarSesionYGenerarPdf(Context context, int clienteId) {
        if (sessionReparaciones.isEmpty()) {
            mensajeToast.setValue("No hay reparaciones para finalizar.");
            return;
        }
        Executors.newSingleThreadExecutor().execute(() -> {
            for (Reparacion rep : sessionReparaciones) {
                List<ProductoReparacion> repuestos = sessionRepuestos.get(rep);
                repository.insertarReparacionConRepuestos(rep.getClienteId(), rep.getEquipoMarca(), rep.getEquipoModelo(), rep.getEquipoSerie(),
                        rep.getDescripcionProblema(), rep.getCostoManoDeObra(), rep.getEstado(), repuestos, res -> {});
            }
            ClienteDao clienteDao = AppDatabase.getInstance(context).clienteDao();
            Cliente cliente = clienteDao.getById(clienteId);
            if (cliente != null) {
                File pdf = PdfGenerator.generarPresupuestoPdf(context, cliente, new ArrayList<>(sessionReparaciones));
                if (pdf != null) pdfGeneradoEvent.postValue(pdf);
            }
            sessionReparaciones.clear();
            sessionRepuestos.clear();
            sessionCount.postValue(0);
            cargarReparaciones();
        });
    }

    public void limpiarSesion() {
        sessionReparaciones.clear();
        sessionRepuestos.clear();
        sessionCount.setValue(0);
    }

    public void eliminarReparacion(int id) {
        repository.eliminarReparacionPorId(id, res -> cargarReparaciones());
    }

    // ACTUALIZAR USANDO LA NUEVA LÓGICA COMPLETA
    public void actualizarReparacionCompleta(int id, int cId, String ma, String mo, String se, String de, double manoObra, String est) {
        List<ProductoReparacion> repuestos = repuestosMaquinaActual.getValue();
        if(repuestos == null) repuestos = new ArrayList<>();

        repository.actualizarReparacionCompleta(id, cId, ma, mo, se, de, manoObra, est, repuestos, res -> {
            mensajeToast.postValue(res.message);
            if(res.isSuccess) cargarReparaciones();
        });
    }

    public void generarPdfYCompartir(Context context, Reparacion reparacion) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            Cliente cliente = db.clienteDao().getById(reparacion.getClienteId());
            List<ProductoReparacion> repuestos = db.productoReparacionDao().getItemsPorReparacion(reparacion.getId());

            if (cliente != null) {
                // Actualizamos los costos en memoria antes de imprimir si es necesario
                // (Aunque si se guardó bien, ya deberían estar bien)
                File pdf = PdfGenerator.generarPresupuestoReparacion(context, cliente, reparacion, repuestos);
                if (pdf != null) {
                    reparacion.setPdfPath(pdf.getAbsolutePath());
                    db.reparacionDao().update(reparacion);
                    pdfGeneradoEvent.postValue(pdf);
                }
            }
        });
    }

    public void cargarRepuestosDeReparacion(int reparacionId) {
        AppDatabase db = AppDatabase.getInstance(getApplication());
        Executors.newSingleThreadExecutor().execute(() -> {
            List<ProductoReparacion> items = db.productoReparacionDao().getItemsPorReparacion(reparacionId);
            repuestosMaquinaActual.postValue(items);
        });
    }
}