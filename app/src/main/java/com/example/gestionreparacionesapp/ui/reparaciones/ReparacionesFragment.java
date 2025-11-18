package com.example.gestionreparacionesapp.ui.reparaciones;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestionreparacionesapp.R;
import com.example.gestionreparacionesapp.data.db.entity.Cliente;
import com.example.gestionreparacionesapp.data.db.entity.Producto;
import com.example.gestionreparacionesapp.data.db.entity.Reparacion;
import com.example.gestionreparacionesapp.ui.clientes.ClientesViewModel;
import com.example.gestionreparacionesapp.ui.productos.ProductoViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReparacionesFragment extends Fragment implements ReparacionesAdapter.OnReparacionInteractionListener {

    private ReparacionesViewModel reparacionesViewModel;
    private ClientesViewModel clientesViewModel;
    private ProductoViewModel productoViewModel;

    private RecyclerView recyclerViewReparaciones;
    private ReparacionesAdapter adapter;
    private FloatingActionButton fabAgregarReparacion;
    private EditText etBuscadorReparaciones;

    private List<Cliente> listaClientesSpinner = new ArrayList<>();
    private List<Producto> listaProductosSpinner = new ArrayList<>();

    public ReparacionesFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        reparacionesViewModel = new ViewModelProvider(this).get(ReparacionesViewModel.class);
        clientesViewModel = new ViewModelProvider(requireActivity()).get(ClientesViewModel.class);
        productoViewModel = new ViewModelProvider(requireActivity()).get(ProductoViewModel.class);
        adapter = new ReparacionesAdapter(new ArrayList<>(), new ArrayList<>(), this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reparaciones, container, false);
        recyclerViewReparaciones = view.findViewById(R.id.recyclerViewReparaciones);
        fabAgregarReparacion = view.findViewById(R.id.fabAgregarReparacion);
        etBuscadorReparaciones = view.findViewById(R.id.etBuscadorReparaciones);
        setupRecyclerView();
        setupListeners();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupObservers();
        reparacionesViewModel.cargarReparaciones();
        clientesViewModel.cargarClientes(ClientesViewModel.ClienteFilterType.TODOS);
        productoViewModel.cargarProductos();
    }

    private void setupRecyclerView() {
        recyclerViewReparaciones.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewReparaciones.setAdapter(adapter);
    }

    private void setupListeners() {
        fabAgregarReparacion.setOnClickListener(v -> mostrarDialogoReparacion(null));
        etBuscadorReparaciones.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                reparacionesViewModel.buscarReparaciones(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupObservers() {
        reparacionesViewModel.getListaReparaciones().observe(getViewLifecycleOwner(), reparaciones -> {
            if (reparaciones != null) {
                adapter.setReparaciones(reparaciones);
            }
        });
        clientesViewModel.getListaClientes().observe(getViewLifecycleOwner(), clientes -> {
            if (clientes != null) {
                this.listaClientesSpinner = clientes;
                adapter.setClientes(clientes);
            }
        });
        productoViewModel.getListaProductos().observe(getViewLifecycleOwner(), productos -> {
            if (productos != null) {
                this.listaProductosSpinner = productos;
            }
        });
        reparacionesViewModel.getResultadoOperacion().observe(getViewLifecycleOwner(), resultado -> {
            if (resultado != null && getContext() != null) {
                Toast.makeText(getContext(), resultado.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onReparacionClick(Reparacion reparacion) {
        mostrarDialogoReparacion(reparacion);
    }

    @Override
    public void onReparacionLongClick(Reparacion reparacion) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar Reparación")
                .setMessage("¿Estás seguro de que quieres eliminar la reparación del producto '" + reparacion.getProductoNombre() + "'?")
                .setPositiveButton("Eliminar", (dialog, which) -> reparacionesViewModel.eliminarReparacion(reparacion.getId()))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogoReparacion(@Nullable Reparacion reparacion) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        // Este layout DEBE existir y ser el del diálogo simple.
        View dialogView = inflater.inflate(R.layout.dialog_nueva_reparacion, null);
        builder.setView(dialogView);

        Spinner spinnerCliente = dialogView.findViewById(R.id.spinnerClienteReparacion);
        Spinner spinnerProducto = dialogView.findViewById(R.id.spinnerProductoReparacion);
        Button btnNuevoCliente = dialogView.findViewById(R.id.btnNuevoClienteReparacionDialog);
        Button btnNuevoProducto = dialogView.findViewById(R.id.btnNuevoProductoReparacionDialog);
        EditText etDescripcion = dialogView.findViewById(R.id.etDescripcionReparacion);
        EditText etPresupuesto = dialogView.findViewById(R.id.etPresupuestoReparacion);
        Spinner spinnerEstado = dialogView.findViewById(R.id.spinnerEstadoReparacion);
        Button btnGuardar = dialogView.findViewById(R.id.btnGuardarReparacionDialog);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelarReparacionDialog);

        refrescarSpinnerClientes(spinnerCliente, -1);
        refrescarSpinnerProductos(spinnerProducto, -1);

        // Asegúrate de tener este array en res/values/arrays.xml
        ArrayAdapter<CharSequence> estadoAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.estados_reparacion, android.R.layout.simple_spinner_item);
        estadoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstado.setAdapter(estadoAdapter);

        btnNuevoCliente.setOnClickListener(v -> mostrarDialogoNuevoCliente(spinnerCliente));
        btnNuevoProducto.setOnClickListener(v -> mostrarDialogoNuevoProducto(spinnerProducto));

        if (reparacion != null) { // MODO EDICIÓN
            builder.setTitle("Editar Reparación");
            btnGuardar.setText("Actualizar");

            etDescripcion.setText(reparacion.getDescripcionProblema());
            etPresupuesto.setText(String.valueOf(reparacion.getPresupuesto()));

            spinnerCliente.post(() -> {
                for (int i = 0; i < listaClientesSpinner.size(); i++) {
                    if (listaClientesSpinner.get(i).getId() == reparacion.getClienteId()) {
                        spinnerCliente.setSelection(i + 1); // +1 por "Seleccionar..."
                        break;
                    }
                }
            });

            spinnerProducto.post(() -> {
                for (int i = 0; i < listaProductosSpinner.size(); i++) {
                    if (listaProductosSpinner.get(i).getNombre().equals(reparacion.getProductoNombre())) {
                        spinnerProducto.setSelection(i + 1); // +1 por "Seleccionar..."
                        break;
                    }
                }
            });

            if (reparacion.getEstado() != null) {
                int estadoPosition = estadoAdapter.getPosition(reparacion.getEstado());
                if (estadoPosition >= 0) {
                    spinnerEstado.setSelection(estadoPosition);
                }
            }
        } else { // MODO CREAR
            builder.setTitle("Nueva Reparación");
            btnGuardar.setText("Guardar");
        }

        AlertDialog dialog = builder.create();

        btnGuardar.setOnClickListener(v -> {
            int clientePosition = spinnerCliente.getSelectedItemPosition();
            int productoPosition = spinnerProducto.getSelectedItemPosition();

            if (clientePosition <= 0 || productoPosition <= 0) {
                Toast.makeText(getContext(), "Debes seleccionar un cliente y un producto.", Toast.LENGTH_SHORT).show();
                return;
            }

            Cliente clienteSeleccionado = listaClientesSpinner.get(clientePosition - 1);
            Producto productoSeleccionado = listaProductosSpinner.get(productoPosition - 1);
            String descripcion = etDescripcion.getText().toString();
            String presupuestoStr = etPresupuesto.getText().toString();
            String estado = spinnerEstado.getSelectedItem().toString();

            if (reparacion == null) {
                reparacionesViewModel.insertarReparacion(clienteSeleccionado.getId(), productoSeleccionado.getNombre(), descripcion, presupuestoStr, estado);
            } else {
                reparacionesViewModel.actualizarReparacion(reparacion.getId(), clienteSeleccionado.getId(), productoSeleccionado.getNombre(), descripcion, presupuestoStr, estado);
            }
            dialog.dismiss();
        });

        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // El resto de métodos de ayuda no necesitan cambios.
    private void mostrarDialogoNuevoCliente(Spinner spinnerAActualizar) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_nuevo_cliente, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext()).setTitle("Nuevo Cliente Rápido").setView(dialogView).create();
        EditText etDni = dialogView.findViewById(R.id.etDniClienteDialog);
        EditText etNombre = dialogView.findViewById(R.id.etNombreClienteDialog);
        Button btnGuardar = dialogView.findViewById(R.id.btnGuardarClienteDialog);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelarClienteDialog);

        btnGuardar.setOnClickListener(v -> {
            String dni = etDni.getText().toString().trim();
            String nombre = etNombre.getText().toString().trim();
            if (nombre.isEmpty() || dni.isEmpty()) {
                Toast.makeText(getContext(), "El DNI y el Nombre son obligatorios.", Toast.LENGTH_SHORT).show();
                return;
            }
            clientesViewModel.guardarCliente(dni, nombre, "", "", "");
            dialog.dismiss();
        });
        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void mostrarDialogoNuevoProducto(Spinner spinnerAActualizar) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_nuevo_producto, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext()).setTitle("Nuevo Producto Rápido").setView(dialogView).create();
        EditText etNombre = dialogView.findViewById(R.id.etNombreProductoDialog);
        EditText etSku = dialogView.findViewById(R.id.etSkuDialog);
        EditText etPrecio = dialogView.findViewById(R.id.etPrecioDialog);
        EditText etCantidad = dialogView.findViewById(R.id.etCantidadDialog);
        Button btnGuardar = dialogView.findViewById(R.id.btnGuardarProductoDialog);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelarProductoDialog);

        btnGuardar.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            if (nombre.isEmpty()) {
                Toast.makeText(getContext(), "El nombre del producto es obligatorio.", Toast.LENGTH_SHORT).show();
                return;
            }
            productoViewModel.insertarProducto(etSku.getText().toString(), nombre, etPrecio.getText().toString(), etCantidad.getText().toString(), null);
            dialog.dismiss();
        });
        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void refrescarSpinnerClientes(Spinner spinner, int seleccion) {
        List<String> nombres = new ArrayList<>();
        nombres.add("Seleccionar cliente...");
        if (listaClientesSpinner != null) {
            nombres.addAll(listaClientesSpinner.stream().map(Cliente::getNombre).collect(Collectors.toList()));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, nombres);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        if (seleccion > 0 && seleccion < adapter.getCount()) {
            spinner.setSelection(seleccion);
        }
    }

    private void refrescarSpinnerProductos(Spinner spinner, int seleccion) {
        List<String> nombres = new ArrayList<>();
        nombres.add("Seleccionar producto...");
        if (listaProductosSpinner != null) {
            nombres.addAll(listaProductosSpinner.stream().map(Producto::getNombre).collect(Collectors.toList()));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, nombres);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        if (seleccion > 0 && seleccion < adapter.getCount()) {
            spinner.setSelection(seleccion);
        }
    }
}