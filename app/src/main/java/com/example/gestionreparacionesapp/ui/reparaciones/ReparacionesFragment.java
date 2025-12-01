package com.example.gestionreparacionesapp.ui.reparaciones;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReparacionesFragment extends Fragment implements ReparacionesAdapter.OnReparacionInteractionListener {

    private ReparacionesViewModel viewModel;
    private ClientesViewModel clientesViewModel;
    private ProductoViewModel productoViewModel;
    private ReparacionesAdapter adapter;

    private List<Cliente> listaClientesSpinner = new ArrayList<>();
    private List<Producto> listaProductosSpinner = new ArrayList<>();

    private ArrayAdapter<String> activeClientSpinnerAdapter;
    private Spinner activeClientSpinner;
    private ArrayAdapter<String> activeProductSpinnerAdapter;
    private Spinner activeProductSpinner;

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
        EditText etBus = view.findViewById(R.id.etBuscadorReparaciones);
        etBus.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) { viewModel.buscarReparaciones(s.toString()); }
            public void afterTextChanged(Editable s) {}
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel.cargarReparaciones();
        clientesViewModel.cargarClientes(ClientesViewModel.ClienteFilterType.TODOS);
        productoViewModel.cargarProductos();

        viewModel.getListaReparaciones().observe(getViewLifecycleOwner(), list -> adapter.setReparaciones(list));

        // Clientes
        clientesViewModel.getListaClientes().observe(getViewLifecycleOwner(), list -> {
            if(list != null) {
                listaClientesSpinner = list;
                adapter.setClientes(list);
                if (activeClientSpinnerAdapter != null && activeClientSpinner != null) {
                    actualizarDatosSpinnerClientes(activeClientSpinnerAdapter, list);
                    if(list.size() > 0 && activeClientSpinner.getSelectedItemPosition() == 0) {
                        activeClientSpinner.setSelection(list.size());
                    }
                }
            }
        });

        // Productos
        productoViewModel.getListaProductos().observe(getViewLifecycleOwner(), list -> {
            if(list != null) {
                listaProductosSpinner = list;
                if (activeProductSpinnerAdapter != null && activeProductSpinner != null) {
                    actualizarDatosSpinnerProductos(activeProductSpinnerAdapter, list);
                    if(list.size() > 0 && activeProductSpinner.getSelectedItemPosition() == 0) {
                        activeProductSpinner.setSelection(list.size());
                    }
                }
            }
        });

        viewModel.getPdfGeneradoEvent().observe(getViewLifecycleOwner(), file -> {
            if(file != null) compartirPdf(file);
        });
        viewModel.getMensajeToast().observe(getViewLifecycleOwner(), msg -> Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show());

        // Refrescos Automáticos
        clientesViewModel.getClienteCreadoConExito().observe(getViewLifecycleOwner(), success -> {
            if(success) clientesViewModel.cargarClientes(ClientesViewModel.ClienteFilterType.TODOS);
        });
        productoViewModel.getNuevoProductoCreadoEvent().observe(getViewLifecycleOwner(), prod -> {
            if(prod != null) productoViewModel.cargarProductos();
        });
    }

    private void iniciarNuevaSesion() {
        viewModel.limpiarSesion();
        currentClienteId = -1;
        mostrarDialogoCarga(null);
    }

    private void mostrarDialogoCarga(@Nullable Reparacion reparacionExistente) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_nueva_reparacion, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        // UI References
        Spinner spCliente = view.findViewById(R.id.spinnerClienteReparacion);
        this.activeClientSpinner = spCliente;
        Button btnNuevoCliente = view.findViewById(R.id.btnNuevoClienteReparacionDialog);

        EditText etMarca = view.findViewById(R.id.etEquipoMarca);
        EditText etModelo = view.findViewById(R.id.etEquipoModelo);
        EditText etSerie = view.findViewById(R.id.etEquipoSerie);
        EditText etDesc = view.findViewById(R.id.etDescripcionReparacion);
        EditText etManoObra = view.findViewById(R.id.etCostoManoDeObra);
        Spinner spEstado = view.findViewById(R.id.spinnerEstadoReparacion);

        Button btnHabilitarRepuestos = view.findViewById(R.id.btnHabilitarRepuestos);
        LinearLayout llFormularioRepuestos = view.findViewById(R.id.llFormularioRepuestos);
        Spinner spProducto = view.findViewById(R.id.spinnerProductoSelector);
        this.activeProductSpinner = spProducto;
        Button btnNuevoProducto = view.findViewById(R.id.btnNuevoProductoReparacion);

        EditText etCantRep = view.findViewById(R.id.etCantidadRepuesto);
        EditText etPrecioRep = view.findViewById(R.id.etPrecioRepuesto);
        Button btnAddRepuesto = view.findViewById(R.id.btnAgregarRepuesto);
        LinearLayout llContainer = view.findViewById(R.id.llRepuestosContainer);
        TextView tvTotal = view.findViewById(R.id.tvTotalPresupuesto);

        Button btnAddMaquina = view.findViewById(R.id.btnGuardarYOtraReparacion);
        Button btnFinalizar = view.findViewById(R.id.btnGuardarReparacionDialog);
        Button btnCancelar = view.findViewById(R.id.btnCancelarReparacionDialog);
        ImageButton btnPdf = view.findViewById(R.id.btnImprimirPdf);

        configurarSpinnerClientes(spCliente, (reparacionExistente != null) ? reparacionExistente.getClienteId() : currentClienteId);
        configurarSpinnerProductos(spProducto, etPrecioRep);
        configurarSpinnerEstados(spEstado, (reparacionExistente != null) ? reparacionExistente.getEstado() : null);

        if (currentClienteId != -1 && reparacionExistente == null) spCliente.setEnabled(false);

        List<ProductoReparacion> repuestosTemp = new ArrayList<>();

        // Listeners de "NUEVO"
        btnNuevoCliente.setOnClickListener(v -> mostrarDialogoNuevoCliente());
        btnNuevoProducto.setOnClickListener(v -> mostrarDialogoNuevoProducto());

        btnHabilitarRepuestos.setOnClickListener(v -> {
            if (llFormularioRepuestos.getVisibility() == View.VISIBLE) {
                llFormularioRepuestos.setVisibility(View.GONE);
                btnHabilitarRepuestos.setText("Añadir Repuesto");
            } else {
                llFormularioRepuestos.setVisibility(View.VISIBLE);
                btnHabilitarRepuestos.setText("Ocultar Repuestos");
            }
        });

        btnAddRepuesto.setOnClickListener(v -> {
            if (spProducto.getSelectedItemPosition() <= 0) {
                Toast.makeText(getContext(), "Selecciona un repuesto", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                Producto p = listaProductosSpinner.get(spProducto.getSelectedItemPosition() - 1);
                int cant = Integer.parseInt(etCantRep.getText().toString());
                double precio = Double.parseDouble(etPrecioRep.getText().toString());

                if (cant > p.getCantidad()) {
                    etCantRep.setError("Stock insuficiente (" + p.getCantidad() + ")");
                    return;
                }
                ProductoReparacion item = new ProductoReparacion(0, p.getId(), p.getNombre(), cant, precio);
                repuestosTemp.add(item);

                agregarVistaRepuesto(llContainer, item, repuestosTemp, tvTotal, etManoObra);
                actualizarTotalVisual(tvTotal, repuestosTemp, etManoObra);

                etCantRep.setText("1"); spProducto.setSelection(0); etPrecioRep.setText("");
            } catch(Exception e) { etCantRep.setError("Datos inválidos"); }
        });

        etManoObra.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                actualizarTotalVisual(tvTotal, repuestosTemp, etManoObra);
            }
            public void afterTextChanged(Editable s) {}
        });

        // Lógica de Edición
        if (reparacionExistente != null) {
            btnPdf.setVisibility(View.VISIBLE);
            btnAddMaquina.setVisibility(View.GONE);
            btnFinalizar.setText("Actualizar");
            btnPdf.setOnClickListener(v -> viewModel.generarPdfYCompartir(requireContext(), reparacionExistente));

            etMarca.setText(reparacionExistente.getEquipoMarca());
            etModelo.setText(reparacionExistente.getEquipoModelo());
            etSerie.setText(reparacionExistente.getEquipoSerie());
            etDesc.setText(reparacionExistente.getDescripcionProblema());
            etManoObra.setText(String.valueOf(reparacionExistente.getCostoManoDeObra()));

            viewModel.cargarRepuestosDeReparacion(reparacionExistente.getId());
        } else {
            btnPdf.setVisibility(View.GONE);
            viewModel.limpiarRepuestosActuales();
        }

        viewModel.getRepuestosMaquinaActual().observe(getViewLifecycleOwner(), lista -> {
            if (lista != null && !lista.isEmpty() && llContainer.getChildCount() == 0) {
                repuestosTemp.addAll(lista);
                for (ProductoReparacion item : lista) {
                    agregarVistaRepuesto(llContainer, item, repuestosTemp, tvTotal, etManoObra);
                }
                actualizarTotalVisual(tvTotal, repuestosTemp, etManoObra);
            }
        });

        View.OnClickListener saveAction = v -> {
            boolean mantenerAbierto = (v.getId() == R.id.btnGuardarYOtraReparacion);
            if (spCliente.getSelectedItemPosition() <= 0) {
                TextView err = (TextView) spCliente.getSelectedView();
                if(err!=null) err.setError("Requerido");
                return;
            }
            if (etMarca.getText().toString().trim().isEmpty()) { etMarca.setError("Requerido"); return; }

            Cliente c = listaClientesSpinner.get(spCliente.getSelectedItemPosition() - 1);
            currentClienteId = c.getId();

            viewModel.limpiarRepuestosActuales();
            for(ProductoReparacion pr : repuestosTemp) viewModel.agregarRepuestoActual(pr);

            try {
                double mo = etManoObra.getText().toString().isEmpty() ? 0 : Double.parseDouble(etManoObra.getText().toString());
                String est = spEstado.getSelectedItem().toString();

                if (reparacionExistente == null) {
                    viewModel.agregarReparacionALote(SessionManager.getUserId(getContext()), c.getId(),
                            etMarca.getText().toString(), etModelo.getText().toString(), etSerie.getText().toString(),
                            etDesc.getText().toString(), mo, est);

                    dialog.dismiss();
                    if (mantenerAbierto) mostrarDialogoCarga(null);
                    else viewModel.finalizarSesionYGenerarPdf(requireContext(), currentClienteId);
                } else {
                    // ACTUALIZAR COMPLETO
                    viewModel.actualizarReparacionCompleta(reparacionExistente.getId(), c.getId(),
                            etMarca.getText().toString(), etModelo.getText().toString(), etSerie.getText().toString(),
                            etDesc.getText().toString(), mo, est);
                    dialog.dismiss();
                }
            } catch (NumberFormatException e) { etManoObra.setError("Precio inválido"); }
        };

        btnAddMaquina.setOnClickListener(saveAction);
        btnFinalizar.setOnClickListener(saveAction);
        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void actualizarTotalVisual(TextView tv, List<ProductoReparacion> reps, EditText etMo) {
        double total = 0;
        for(ProductoReparacion r : reps) total += (r.getCantidad() * r.getPrecioUnitarioCobrado());
        try {
            String moStr = etMo.getText().toString();
            if (!moStr.isEmpty()) {
                total += Double.parseDouble(moStr);
            }
        } catch(Exception e){}
        tv.setText("Total Estimado: $" + String.format(Locale.US, "%.2f", total));
    }

    private void agregarVistaRepuesto(LinearLayout container, ProductoReparacion item, List<ProductoReparacion> lista, TextView tvTotal, EditText etMo) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        TextView tv = new TextView(getContext());
        tv.setText(item.getCantidad() + "x " + item.getNombreProductoSnapshot() + " ($" + item.getPrecioUnitarioCobrado() + ")");
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        tv.setLayoutParams(params);
        Button btnQuitar = new Button(getContext(), null, android.R.attr.borderlessButtonStyle);
        btnQuitar.setText("X");
        btnQuitar.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        btnQuitar.setOnClickListener(v -> {
            container.removeView(row);
            lista.remove(item);
            actualizarTotalVisual(tvTotal, lista, etMo);
        });
        row.addView(tv); row.addView(btnQuitar);
        container.addView(row);
    }

    private void mostrarDialogoNuevoCliente() {
        View view = getLayoutInflater().inflate(R.layout.dialog_nuevo_cliente, null);
        AlertDialog d = new AlertDialog.Builder(requireContext()).setView(view).create();
        EditText etDni = view.findViewById(R.id.etDniClienteDialog);
        EditText etNombre = view.findViewById(R.id.etNombreClienteDialog);
        EditText etDir = view.findViewById(R.id.etDireccionClienteDialog);
        EditText etLoc = view.findViewById(R.id.etLocalidadClienteDialog);
        EditText etCp = view.findViewById(R.id.etCpClienteDialog);

        view.findViewById(R.id.btnGuardarClienteDialog).setOnClickListener(v -> {
            if(etNombre.getText().toString().isEmpty()) { etNombre.setError("Requerido"); return; }
            String dni = etDni.getText().toString().isEmpty() ? "-" : etDni.getText().toString();
            clientesViewModel.guardarCliente(dni, etNombre.getText().toString(),
                    etDir.getText().toString(), etLoc.getText().toString(), etCp.getText().toString());
            d.dismiss();
        });
        view.findViewById(R.id.btnCancelarClienteDialog).setOnClickListener(v -> d.dismiss());
        d.show();
    }

    private void mostrarDialogoNuevoProducto() {
        View view = getLayoutInflater().inflate(R.layout.dialog_nuevo_producto, null);
        AlertDialog d = new AlertDialog.Builder(requireContext()).setView(view).create();
        EditText etNom = view.findViewById(R.id.etNombreProductoDialog);
        EditText etPre = view.findViewById(R.id.etPrecioDialog);
        EditText etCant = view.findViewById(R.id.etCantidadDialog);

        view.findViewById(R.id.btnGuardarProductoDialog).setOnClickListener(v -> {
            if(etNom.getText().toString().isEmpty()) { etNom.setError("Requerido"); return; }
            productoViewModel.guardarProducto("", etNom.getText().toString(), etPre.getText().toString(), etCant.getText().toString());
            d.dismiss();
        });
        view.findViewById(R.id.btnCancelarProductoDialog).setOnClickListener(v -> d.dismiss());
        d.show();
    }

    private void configurarSpinnerClientes(Spinner sp, int selId) {
        this.activeClientSpinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, new ArrayList<>());
        this.activeClientSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(this.activeClientSpinnerAdapter);
        actualizarDatosSpinnerClientes(this.activeClientSpinnerAdapter, listaClientesSpinner);
        if (selId != -1) {
            for(int i=0; i<listaClientesSpinner.size(); i++) if(listaClientesSpinner.get(i).getId() == selId) sp.setSelection(i+1);
        }
    }

    private void configurarSpinnerProductos(Spinner sp, EditText etPrecio) {
        this.activeProductSpinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, new ArrayList<>());
        this.activeProductSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(this.activeProductSpinnerAdapter);
        actualizarDatosSpinnerProductos(this.activeProductSpinnerAdapter, listaProductosSpinner);
        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position > 0) etPrecio.setText(String.valueOf(listaProductosSpinner.get(position - 1).getPrecio()));
                else etPrecio.setText("");
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void actualizarDatosSpinnerClientes(ArrayAdapter<String> adapter, List<Cliente> list) {
        adapter.clear(); adapter.add("Seleccionar Cliente...");
        for(Cliente c : list) adapter.add(c.getNombre());
        adapter.notifyDataSetChanged();
    }

    private void actualizarDatosSpinnerProductos(ArrayAdapter<String> adapter, List<Producto> list) {
        adapter.clear(); adapter.add("Seleccionar Repuesto...");
        for(Producto p : list) adapter.add(p.getNombre() + " (Stock: " + p.getCantidad() + ")");
        adapter.notifyDataSetChanged();
    }

    private void configurarSpinnerEstados(Spinner sp, String est) {
        ArrayAdapter<CharSequence> adp = ArrayAdapter.createFromResource(requireContext(), R.array.estados_reparacion, android.R.layout.simple_spinner_item);
        adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(adp);
        if(est != null) { int pos = adp.getPosition(est); if(pos >= 0) sp.setSelection(pos); }
    }

    private void compartirPdf(File pdfFile) {
        if(pdfFile == null) return;
        androidx.core.content.FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".provider", pdfFile);
        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(android.content.Intent.EXTRA_STREAM, androidx.core.content.FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".provider", pdfFile));
        intent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(android.content.Intent.createChooser(intent, "Compartir Presupuesto"));
    }

    @Override public void onReparacionClick(Reparacion r) { mostrarDialogoCarga(r); }
    @Override public void onReparacionLongClick(Reparacion r) {
        new AlertDialog.Builder(requireContext()).setTitle("Eliminar").setMessage("Borrar?").setPositiveButton("Sí", (d,w) -> viewModel.eliminarReparacion(r.getId())).setNegativeButton("No", null).show();
    }
}