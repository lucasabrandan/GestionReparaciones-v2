package com.example.gestionreparacionesapp.ui.reparaciones;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestionreparacionesapp.R;
import com.example.gestionreparacionesapp.data.db.entity.Cliente;
import com.example.gestionreparacionesapp.data.db.entity.Producto;
import com.example.gestionreparacionesapp.data.db.entity.ProductoReparacion;
import com.example.gestionreparacionesapp.data.db.entity.Reparacion;
import com.example.gestionreparacionesapp.ui.clientes.ClientesViewModel;
import com.example.gestionreparacionesapp.ui.productos.ProductoViewModel;
import com.example.gestionreparacionesapp.util.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReparacionesFragment extends Fragment implements ReparacionesAdapter.OnReparacionInteractionListener {

    private ReparacionesViewModel viewModel;
    private ClientesViewModel clientesViewModel;
    private ProductoViewModel productoViewModel;
    private ReparacionesAdapter adapter;

    // Variables globales para el diálogo
    private Spinner spClienteGlobal;
    private boolean isSessionActive = false;
    private int currentClienteId = -1;

    public ReparacionesFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ReparacionesViewModel.class);
        clientesViewModel = new ViewModelProvider(requireActivity()).get(ClientesViewModel.class);
        productoViewModel = new ViewModelProvider(this).get(ProductoViewModel.class);
        adapter = new ReparacionesAdapter(new ArrayList<>(), new ArrayList<>(), this);
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reparaciones, container, false);
        RecyclerView rv = view.findViewById(R.id.recyclerViewReparaciones);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);

        view.findViewById(R.id.fabAgregarReparacion).setOnClickListener(v -> iniciarNuevaSesion());
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel.cargarReparaciones();
        clientesViewModel.cargarClientes(ClientesViewModel.ClienteFilterType.TODOS);
        productoViewModel.cargarProductos();

        viewModel.getListaReparaciones().observe(getViewLifecycleOwner(), list -> adapter.setReparaciones(list));
        clientesViewModel.getListaClientes().observe(getViewLifecycleOwner(), list -> adapter.setClientes(list));

        // Escuchar eventos de PDF y Toast
        viewModel.getPdfGeneradoEvent().observe(getViewLifecycleOwner(), this::compartirPdf);
        viewModel.getMensajeToast().observe(getViewLifecycleOwner(), msg -> Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show());
    }

    // --- LÓGICA DE SESIÓN ---
    private void iniciarNuevaSesion() {
        viewModel.limpiarSesion();
        isSessionActive = true;
        currentClienteId = -1; // Reset
        mostrarDialogoCarga();
    }

    private void mostrarDialogoCarga() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_nueva_reparacion, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        // UI References
        Spinner spCliente = view.findViewById(R.id.spinnerClienteReparacion);
        EditText etMarca = view.findViewById(R.id.etEquipoMarca);
        EditText etModelo = view.findViewById(R.id.etEquipoModelo);
        EditText etSerie = view.findViewById(R.id.etEquipoSerie);
        EditText etDesc = view.findViewById(R.id.etDescripcionReparacion);
        EditText etManoObra = view.findViewById(R.id.etCostoManoDeObra);
        Spinner spProducto = view.findViewById(R.id.spinnerProductoSelector);
        EditText etCantRep = view.findViewById(R.id.etCantidadRepuesto);
        EditText etPrecioRep = view.findViewById(R.id.etPrecioRepuesto);
        Button btnAddRepuesto = view.findViewById(R.id.btnAgregarRepuesto);
        LinearLayout llContainer = view.findViewById(R.id.llRepuestosContainer);
        Button btnAddMaquina = view.findViewById(R.id.btnGuardarYOtraReparacion);
        Button btnFinalizar = view.findViewById(R.id.btnGuardarReparacionDialog);

        // Textos de botones actualizados para la lógica de sesión
        btnAddMaquina.setText("Añadir Máquina a la Orden");
        btnFinalizar.setText("Finalizar y Generar PDF");

        // Configurar Clientes
        configurarSpinnerClientes(spCliente, currentClienteId);
        // Si ya elegimos cliente en la máquina anterior, bloquear spinner
        if (currentClienteId != -1) spCliente.setEnabled(false);

        // Configurar Productos
        configurarSpinnerProductos(spProducto, etPrecioRep);

        List<ProductoReparacion> repuestosTemp = new ArrayList<>();

        // Agregar Repuesto a la lista visual
        btnAddRepuesto.setOnClickListener(v -> {
            if (spProducto.getSelectedItemPosition() <= 0) return;
            // ... Validaciones ...
            try {
                Producto p = (Producto) ((ObjectWrapper)spProducto.getSelectedItem()).getObject();
                int cant = Integer.parseInt(etCantRep.getText().toString());
                double precio = Double.parseDouble(etPrecioRep.getText().toString());

                ProductoReparacion item = new ProductoReparacion(0, p.getId(), p.getNombre(), cant, precio);
                repuestosTemp.add(item);

                // Agregar visualmente
                TextView tv = new TextView(getContext());
                tv.setText(cant + "x " + p.getNombre() + " ($" + precio + ")");
                llContainer.addView(tv);

                etCantRep.setText("1"); spProducto.setSelection(0); etPrecioRep.setText("");
            } catch(Exception e) {}
        });

        // 1. AÑADIR MÁQUINA A LA SESIÓN
        btnAddMaquina.setOnClickListener(v -> {
            if (spCliente.getSelectedItemPosition() <= 0) { Toast.makeText(getContext(), "Selecciona cliente", Toast.LENGTH_SHORT).show(); return; }

            // Bloquear cliente para las siguientes maquinas
            Cliente c = (Cliente) ((ObjectWrapper)spCliente.getSelectedItem()).getObject();
            currentClienteId = c.getId();

            // Pasar repuestos al ViewModel temporalmente
            viewModel.limpiarRepuestosActuales();
            for(ProductoReparacion pr : repuestosTemp) viewModel.agregarRepuestoActual(pr);

            try {
                double mo = Double.parseDouble(etManoObra.getText().toString());
                // Agregar al lote
                viewModel.agregarReparacionALote(SessionManager.getUserId(getContext()), c.getId(),
                        etMarca.getText().toString(), etModelo.getText().toString(), etSerie.getText().toString(),
                        etDesc.getText().toString(), mo, "Pendiente");

                dialog.dismiss();
                mostrarDialogoCarga(); // REABRIR para la siguiente máquina
            } catch (NumberFormatException e) { Toast.makeText(getContext(), "Precio inválido", Toast.LENGTH_SHORT).show(); }
        });

        // 2. FINALIZAR TODO
        btnFinalizar.setOnClickListener(v -> {
            // Si hay datos en pantalla, intentar guardarlos como última máquina
            if (!etMarca.getText().toString().isEmpty()) {
                btnAddMaquina.performClick(); // Truco: Forzar guardado de la actual
            }
            // Generar PDF con todo el lote
            viewModel.finalizarSesionYGenerarPdf(requireContext(), currentClienteId);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void compartirPdf(File pdfFile) {
        Uri uri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".provider", pdfFile);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "Compartir Presupuesto"));
    }

    // Helpers de Spinners Simplificados usando Wrappers
    private void configurarSpinnerClientes(Spinner sp, int selId) {
        List<ObjectWrapper> list = new ArrayList<>();
        list.add(new ObjectWrapper(null, "Seleccionar Cliente..."));
        if(clientesViewModel.getListaClientes().getValue() != null) {
            for(Cliente c : clientesViewModel.getListaClientes().getValue()) list.add(new ObjectWrapper(c, c.getNombre()));
        }
        ArrayAdapter<ObjectWrapper> adp = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, list);
        sp.setAdapter(adp);
        if(selId != -1) {
            for(int i=0; i<list.size(); i++) if(list.get(i).obj instanceof Cliente && ((Cliente)list.get(i).obj).getId() == selId) sp.setSelection(i);
        }
    }

    private void configurarSpinnerProductos(Spinner sp, EditText etPrecio) {
        List<ObjectWrapper> list = new ArrayList<>();
        list.add(new ObjectWrapper(null, "Seleccionar Repuesto..."));
        if(productoViewModel.getListaProductos().getValue() != null) {
            for(Producto p : productoViewModel.getListaProductos().getValue()) list.add(new ObjectWrapper(p, p.getNombre()));
        }
        ArrayAdapter<ObjectWrapper> adp = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, list);
        sp.setAdapter(adp);
        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position > 0) etPrecio.setText(String.valueOf(((Producto)((ObjectWrapper)parent.getSelectedItem()).obj).getPrecio()));
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // Clase auxiliar para Spinners
    private static class ObjectWrapper {
        Object obj; String label;
        ObjectWrapper(Object o, String l){this.obj=o; this.label=l;}
        @Override public String toString(){return label;}
        public Object getObject(){return obj;}
    }

    @Override public void onReparacionClick(Reparacion reparacion) { /* Mostrar detalle si quieres */ }
    @Override public void onReparacionLongClick(Reparacion reparacion) { viewModel.eliminarReparacion(reparacion.getId()); }
}