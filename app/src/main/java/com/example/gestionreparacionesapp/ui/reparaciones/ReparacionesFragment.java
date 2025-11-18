package com.example.gestionreparacionesapp.ui.reparaciones;

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
import com.example.gestionreparacionesapp.ui.ventas.ProductoVenta;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ReparacionesFragment extends Fragment implements ReparacionesAdapter.OnReparacionInteractionListener {

    private ReparacionesViewModel reparacionesViewModel;
    private ClientesViewModel clientesViewModel;
    private ProductoViewModel productoViewModel;

    private RecyclerView recyclerViewReparaciones;
    private ReparacionesAdapter adapter;
    private TextView tvSinReparaciones;
    private FloatingActionButton fabAgregarReparacion;
    private EditText etBuscadorReparaciones;

    private List<Cliente> listaClientesSpinner = new ArrayList<>();
    private List<Producto> listaProductosSpinner = new ArrayList<>();

    public ReparacionesFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new ReparacionesAdapter(new ArrayList<>(), new ArrayList<>(), this);
        reparacionesViewModel = new ViewModelProvider(this).get(ReparacionesViewModel.class);
        clientesViewModel = new ViewModelProvider(requireActivity()).get(ClientesViewModel.class);
        productoViewModel = new ViewModelProvider(requireActivity()).get(ProductoViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reparaciones, container, false);
        recyclerViewReparaciones = view.findViewById(R.id.recyclerViewReparaciones);
        tvSinReparaciones = view.findViewById(R.id.tvSinReparaciones);
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

    @Override
    public void onReparacionClick(Reparacion reparacion) {
        mostrarDialogoReparacion(reparacion);
    }

    @Override
    public void onReparacionLongClick(Reparacion reparacion) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar Reparación")
                .setMessage("¿Estás seguro de que deseas eliminar la reparación #" + reparacion.getId() + "? Esta acción no restaurará el stock de los productos utilizados.")
                .setPositiveButton("Eliminar", (dialog, which) -> reparacionesViewModel.eliminarReparacion(reparacion))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void setupObservers() {
        reparacionesViewModel.getListaReparaciones().observe(getViewLifecycleOwner(), reparaciones -> {
            boolean isEmpty = reparaciones == null || reparaciones.isEmpty();
            tvSinReparaciones.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            recyclerViewReparaciones.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            if (!isEmpty) adapter.setReparaciones(reparaciones);
        });

        clientesViewModel.getListaClientes().observe(getViewLifecycleOwner(), clientes -> {
            if (clientes != null) {
                adapter.setClientes(clientes);
                listaClientesSpinner = clientes;
            }
        });

        productoViewModel.getListaProductos().observe(getViewLifecycleOwner(), productos -> {
            if (productos != null) listaProductosSpinner = productos;
        });

        reparacionesViewModel.getOperationResult().observe(getViewLifecycleOwner(), result -> {
            if (result != null) Toast.makeText(getContext(), result.message, Toast.LENGTH_SHORT).show();
        });
    }

    private void mostrarDialogoReparacion(@Nullable Reparacion reparacion) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_nueva_reparacion, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        Spinner spinnerCliente = dialogView.findViewById(R.id.spinnerClienteReparacion);
        Button btnNuevoCliente = dialogView.findViewById(R.id.btnNuevoClienteDialog);
        EditText etDescripcion = dialogView.findViewById(R.id.etDescripcionReparacion);
        LinearLayout containerProductos = dialogView.findViewById(R.id.containerProductosReparacion);
        Button btnAnadirProducto = dialogView.findViewById(R.id.btnAnadirProductoReparacion);
        Button btnNuevoProducto = dialogView.findViewById(R.id.btnNuevoProductoReparacionDialog);
        EditText etCosteServicio = dialogView.findViewById(R.id.etCosteServicio);
        TextView tvSubtotal = dialogView.findViewById(R.id.tvSubtotalReparacion);
        TextView tvTotal = dialogView.findViewById(R.id.tvTotalReparacion);
        Button btnGuardar = dialogView.findViewById(R.id.btnGuardarReparacionDialog);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelarReparacionDialog);

        final List<ProductoVenta> productosEnReparacion = new ArrayList<>();
        final Cliente[] clienteSeleccionado = {null};

        // --- Configurar Spinner de Clientes ---
        List<String> nombresClientes = new ArrayList<>();
        nombresClientes.add("Selecciona un cliente");
        if (listaClientesSpinner != null) {
            nombresClientes.addAll(listaClientesSpinner.stream().map(c -> c.getNombre() + " (DNI: " + c.getDni() + ")").collect(Collectors.toList()));
        }
        ArrayAdapter<String> clienteAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, nombresClientes);
        clienteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCliente.setAdapter(clienteAdapter);

        spinnerCliente.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                clienteSeleccionado[0] = (position > 0) ? listaClientesSpinner.get(position - 1) : null;
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // --- Lógica de cálculo de total ---
        TextWatcher totalCalculatorWatcher = (new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                calcularTotalReparacion(containerProductos, productosEnReparacion, etCosteServicio, tvSubtotal, tvTotal);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        etCosteServicio.addTextChangedListener(totalCalculatorWatcher);

        // --- Lógica de botones ---
        btnAnadirProducto.setOnClickListener(v -> agregarViewProducto(containerProductos, productosEnReparacion, etCosteServicio, tvSubtotal, tvTotal, totalCalculatorWatcher));
        btnNuevoCliente.setOnClickListener(v -> mostrarDialogoNuevoCliente());
        btnNuevoProducto.setOnClickListener(v -> mostrarDialogoNuevoProducto());

        if (reparacion != null) {
            dialog.setTitle("Editar Reparación");
            // Rellenar datos existentes
            if (listaClientesSpinner != null) {
                for (int i = 0; i < listaClientesSpinner.size(); i++) {
                    if (listaClientesSpinner.get(i).getId() == reparacion.getClienteId()) {
                        spinnerCliente.setSelection(i + 1);
                        break;
                    }
                }
            }
            etDescripcion.setText(reparacion.getDescripcion());
            etCosteServicio.setText(String.format(Locale.US, "%.2f", reparacion.getCosteServicio()));
            // TODO: Cargar productos desde JSON
        } else {
            dialog.setTitle("Nueva Reparación");
            agregarViewProducto(containerProductos, productosEnReparacion, etCosteServicio, tvSubtotal, tvTotal, totalCalculatorWatcher); // Añadir una fila por defecto
        }

        btnGuardar.setOnClickListener(v -> {
            actualizarListaProductos(containerProductos, productosEnReparacion);
            double costeServicio = 0.0;
            try {
                costeServicio = Double.parseDouble(etCosteServicio.getText().toString());
            } catch (NumberFormatException ignored) {}

            if (reparacion == null) {
                reparacionesViewModel.guardarReparacion(clienteSeleccionado[0], etDescripcion.getText().toString(), productosEnReparacion, costeServicio);
            } else {
                reparacionesViewModel.actualizarReparacion(reparacion.getId(), clienteSeleccionado[0], etDescripcion.getText().toString(), productosEnReparacion, costeServicio);
            }
            dialog.dismiss();
        });

        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void agregarViewProducto(LinearLayout container, List<ProductoVenta> productosLista, EditText etCosteServicio, TextView tvSubtotal, TextView tvTotal, TextWatcher totalCalculatorWatcher) {
        if (listaProductosSpinner == null || listaProductosSpinner.isEmpty()) {
            Toast.makeText(getContext(), "No hay productos disponibles", Toast.LENGTH_SHORT).show();
            return;
        }

        View productoView = getLayoutInflater().inflate(R.layout.item_producto_venta, container, false);
        Spinner spinnerProducto = productoView.findViewById(R.id.spinnerProducto);
        EditText etCantidad = productoView.findViewById(R.id.etCantidad);
        ImageButton btnEliminar = productoView.findViewById(R.id.btnEliminarProductoVenta);

        List<String> nombresProductos = new ArrayList<>();
        nombresProductos.add("Selecciona un producto");
        nombresProductos.addAll(listaProductosSpinner.stream()
                .map(p -> String.format(Locale.getDefault(), "%s - $%.2f (Stock: %d)", p.getNombre(), p.getPrecio(), p.getCantidad()))
                .collect(Collectors.toList()));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, nombresProductos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProducto.setAdapter(adapter);

        etCantidad.addTextChangedListener(totalCalculatorWatcher);
        spinnerProducto.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                calcularTotalReparacion(container, productosLista, etCosteServicio, tvSubtotal, tvTotal);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnEliminar.setOnClickListener(v -> {
            container.removeView(productoView);
            calcularTotalReparacion(container, productosLista, etCosteServicio, tvSubtotal, tvTotal);
        });

        container.addView(productoView);
    }

    private void actualizarListaProductos(LinearLayout container, List<ProductoVenta> productosLista) {
        productosLista.clear();
        for (int i = 0; i < container.getChildCount(); i++) {
            View itemView = container.getChildAt(i);
            Spinner spinnerProducto = itemView.findViewById(R.id.spinnerProducto);
            EditText etCantidad = itemView.findViewById(R.id.etCantidad);
            int pos = spinnerProducto.getSelectedItemPosition();
            if (pos > 0) {
                Producto p = listaProductosSpinner.get(pos - 1);
                int cant = 0;
                try {
                    cant = Integer.parseInt(etCantidad.getText().toString());
                } catch (NumberFormatException ignored) {}
                if (cant > 0) productosLista.add(new ProductoVenta(p, cant));
            }
        }
    }

    private void calcularTotalReparacion(LinearLayout container, List<ProductoVenta> productosLista, EditText etCosteServicio, TextView tvSubtotal, TextView tvTotal) {
        actualizarListaProductos(container, productosLista);
        double subtotalProductos = 0;
        for (ProductoVenta pv : productosLista) {
            subtotalProductos += pv.getSubtotal();
        }

        double costeServicio = 0.0;
        try {
            costeServicio = Double.parseDouble(etCosteServicio.getText().toString());
        } catch (NumberFormatException ignored) {}

        double totalFinal = subtotalProductos + costeServicio;

        tvSubtotal.setText(String.format(Locale.getDefault(), "Subtotal Productos: $%.2f", subtotalProductos));
        tvTotal.setText(String.format(Locale.getDefault(), "TOTAL: $%.2f", totalFinal));
    }

    // --- MÉTODOS REUTILIZABLES ---
    private void mostrarDialogoNuevoCliente() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_nuevo_cliente, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Nuevo Cliente Rápido")
                .setView(dialogView)
                .create();

        EditText etDni = dialogView.findViewById(R.id.etDniDialog);
        EditText etNombre = dialogView.findViewById(R.id.etNombreClienteDialog);
        EditText etDireccion = dialogView.findViewById(R.id.etDireccionDialog);
        EditText etLocalidad = dialogView.findViewById(R.id.etLocalidadDialog);
        EditText etCodigoPostal = dialogView.findViewById(R.id.etCodigoPostalDialog);

        Button btnGuardarCliente = dialogView.findViewById(R.id.btnGuardarClienteDialog);
        Button btnCancelarCliente = dialogView.findViewById(R.id.btnCancelarClienteDialog);

        btnGuardarCliente.setOnClickListener(v -> {
            String dni = etDni.getText().toString().trim();
            String nombre = etNombre.getText().toString().trim();
            String direccion = etDireccion.getText().toString().trim();
            String localidad = etLocalidad.getText().toString().trim();
            String codigoPostal = etCodigoPostal.getText().toString().trim();

            clientesViewModel.guardarCliente(dni, nombre, direccion, localidad, codigoPostal);
            dialog.dismiss();
            Toast.makeText(getContext(), "Cliente guardado. Cierra y vuelve a abrir el diálogo para seleccionarlo.", Toast.LENGTH_LONG).show();
        });

        btnCancelarCliente.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void mostrarDialogoNuevoProducto() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_nuevo_producto, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Nuevo Producto Rápido")
                .setView(dialogView)
                .create();

        // Usamos los IDs correctos que existen en tu layout 'dialog_nuevo_producto.xml'
        EditText etNombre = dialogView.findViewById(R.id.etNombreProductoDialog);
        EditText etPrecio = dialogView.findViewById(R.id.etPrecioDialog);
        EditText etCantidad = dialogView.findViewById(R.id.etCantidadDialog);

        Button btnGuardar = dialogView.findViewById(R.id.btnGuardarProductoDialog);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelarProductoDialog);

        btnGuardar.setOnClickListener(v -> {
            // --- INICIO DE LA CORRECCIÓN ---
            // Pasamos un String vacío para el SKU, que no se pide en este diálogo rápido.
            productoViewModel.guardarProducto(
                    "", // SKU
                    etNombre.getText().toString(),
                    etPrecio.getText().toString(),
                    etCantidad.getText().toString()
            );
            // --- FIN DE LA CORRECCIÓN ---
            dialog.dismiss();
            Toast.makeText(getContext(), "Producto guardado. Cierra y vuelve a abrir el diálogo para seleccionarlo.", Toast.LENGTH_LONG).show();
        });
        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}
