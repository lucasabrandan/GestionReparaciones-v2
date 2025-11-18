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
import com.example.gestionreparacionesapp.ui.ventas.ProductoVenta;
// Importamos los ViewModels de las otras entidades
import com.example.gestionreparacionesapp.ui.clientes.ClientesViewModel;
import com.example.gestionreparacionesapp.ui.productos.ProductoViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ReparacionesFragment extends Fragment implements ReparacionesAdapter.OnReparacionInteractionListener {

    private ReparacionesViewModel reparacionesViewModel;
    // ViewModels de las otras entidades para poblar los Spinners
    private ClientesViewModel clientesViewModel;
    private ProductoViewModel productoViewModel;

    private RecyclerView recyclerViewReparaciones;
    private ReparacionesAdapter adapter;
    private TextView tvSinReparaciones;
    private FloatingActionButton fabAgregarReparacion;
    private EditText etBuscadorReparaciones;

    // Listas para los spinners
    private List<Cliente> listaClientesSpinner = new ArrayList<>();
    private List<Producto> listaProductosSpinner = new ArrayList<>();

    public ReparacionesFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new ReparacionesAdapter(new ArrayList<>(), new ArrayList<>(), this);

        // Inicializamos los 3 ViewModels
        reparacionesViewModel = new ViewModelProvider(this).get(ReparacionesViewModel.class);
        // Usamos requireActivity() para que el ViewModel se comparta entre los fragments de la HomeActivity
        clientesViewModel = new ViewModelProvider(requireActivity()).get(ClientesViewModel.class);
        productoViewModel = new ViewModelProvider(requireActivity()).get(ProductoViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Asegúrate de que este layout (fragment_reparaciones.xml) exista
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

        // Carga inicial de datos
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
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                reparacionesViewModel.buscarReparaciones(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // Clic normal (para Editar)
    @Override
    public void onReparacionClick(Reparacion reparacion) {
        mostrarDialogoReparacion(reparacion);
    }

    // Clic Largo (para Borrar)
    @Override
    public void onReparacionLongClick(Reparacion reparacion) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar Reparación")
                .setMessage("¿Estás seguro de que deseas eliminar la reparación #" + reparacion.getId() + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    reparacionesViewModel.eliminarReparacion(reparacion);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void setupObservers() {
        // Observador de Reparaciones
        reparacionesViewModel.getListaReparaciones().observe(getViewLifecycleOwner(), reparaciones -> {
            if (reparaciones == null || reparaciones.isEmpty()) {
                tvSinReparaciones.setVisibility(View.VISIBLE);
                recyclerViewReparaciones.setVisibility(View.GONE);
            } else {
                tvSinReparaciones.setVisibility(View.GONE);
                recyclerViewReparaciones.setVisibility(View.VISIBLE);
                adapter.setReparaciones(reparaciones);
            }
        });

        // Observador de Clientes (para el Adapter y el Spinner)
        clientesViewModel.getListaClientes().observe(getViewLifecycleOwner(), clientes -> {
            if (clientes != null) {
                adapter.setClientes(clientes); // Actualiza el adapter
                listaClientesSpinner = clientes; // Actualiza la lista del spinner
            }
        });

        // Observador de Productos (para el Spinner)
        productoViewModel.getListaProductos().observe(getViewLifecycleOwner(), productos -> {
            if (productos != null) {
                listaProductosSpinner = productos; // Actualiza la lista del spinner
            }
        });

        // Observador de Operaciones (Crear/Editar/Borrar)
        reparacionesViewModel.getOperationResult().observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;
            Toast.makeText(getContext(), result.message, Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Muestra el diálogo para CREAR o EDITAR una Reparación.
     */
    private void mostrarDialogoReparacion(@Nullable Reparacion reparacion) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_nueva_reparacion, null);
        builder.setView(dialogView);

        Spinner spinnerCliente = dialogView.findViewById(R.id.spinnerClienteReparacion);
        Button btnNuevoCliente = dialogView.findViewById(R.id.btnNuevoClienteDialog);
        EditText etDescripcion = dialogView.findViewById(R.id.etDescripcionReparacion);
        LinearLayout containerProductos = dialogView.findViewById(R.id.containerProductosReparacion);
        Button btnAnadirProducto = dialogView.findViewById(R.id.btnAnadirProductoReparacion);
        TextView tvTotal = dialogView.findViewById(R.id.tvTotalReparacion);
        Button btnGuardar = dialogView.findViewById(R.id.btnGuardarReparacionDialog);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelarReparacionDialog);

        final List<ProductoVenta> productosEnReparacion = new ArrayList<>();
        final Cliente[] clienteSeleccionado = {null};

        // --- Configurar Spinner de Clientes ---
        List<String> nombresClientes = new ArrayList<>();
        nombresClientes.add("Selecciona un cliente");
        if (listaClientesSpinner != null) {
            for (Cliente c : listaClientesSpinner) {
                nombresClientes.add(c.getNombre() + " (DNI: " + c.getDni() + ")");
            }
        }
        ArrayAdapter<String> clienteAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, nombresClientes);
        clienteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCliente.setAdapter(clienteAdapter);

        spinnerCliente.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    clienteSeleccionado[0] = listaClientesSpinner.get(position - 1);
                } else {
                    clienteSeleccionado[0] = null;
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // --- Lógica de Añadir Producto (Dinámico) ---
        btnAnadirProducto.setOnClickListener(v -> {
            agregarViewProducto(containerProductos, productosEnReparacion, tvTotal);
        });

        // --- Modo Editar ---
        if (reparacion != null) {
            builder.setTitle("Editar Reparación");
            // Seleccionar cliente en el spinner
            if (listaClientesSpinner != null) {
                for (int i = 0; i < listaClientesSpinner.size(); i++) {
                    if (listaClientesSpinner.get(i).getId() == reparacion.getClienteId()) {
                        spinnerCliente.setSelection(i + 1);
                        clienteSeleccionado[0] = listaClientesSpinner.get(i);
                        break;
                    }
                }
            }
            etDescripcion.setText(reparacion.getDescripcion());
            // TODO: Cargar productos desde JSON si es modo editar
        } else {
            builder.setTitle("Nueva Reparación");
            agregarViewProducto(containerProductos, productosEnReparacion, tvTotal); // Añadir uno por defecto
        }

        AlertDialog dialog = builder.create();

        btnGuardar.setOnClickListener(v -> {
            String descripcion = etDescripcion.getText().toString().trim();
            // Recalcular productos (por si el usuario cambió algo)
            actualizarListaProductos(containerProductos, productosEnReparacion);

            if (reparacion == null) {
                reparacionesViewModel.guardarReparacion(clienteSeleccionado[0], descripcion, productosEnReparacion);
            } else {
                reparacionesViewModel.actualizarReparacion(reparacion.getId(), clienteSeleccionado[0], descripcion, productosEnReparacion);
            }
            dialog.dismiss();
        });

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        // Lógica del botón "Nuevo Cliente" (¡Tu idea!)
        btnNuevoCliente.setOnClickListener(v -> {
            // Reutilizamos la lógica del ClientesFragment (¡Genial!)
            mostrarDialogoNuevoCliente();
            // NOTA: El spinner de clientes no se actualizará automáticamente
            // hasta que el diálogo de Reparación se cierre y se vuelva a abrir.
        });

        dialog.show();
    }

    /**
     * Reutiliza la lógica de ClientesFragment para añadir un nuevo cliente "al vuelo".
     */
    private void mostrarDialogoNuevoCliente() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_nuevo_cliente, null);
        builder.setView(dialogView);

        EditText etDni = dialogView.findViewById(R.id.etDniDialog);
        EditText etNombre = dialogView.findViewById(R.id.etNombreClienteDialog);
        EditText etDireccion = dialogView.findViewById(R.id.etDireccionDialog);
        EditText etLocalidad = dialogView.findViewById(R.id.etLocalidadDialog);
        EditText etCodigoPostal = dialogView.findViewById(R.id.etCodigoPostalDialog);

        Button btnGuardarCliente = dialogView.findViewById(R.id.btnGuardarClienteDialog);
        Button btnCancelarCliente = dialogView.findViewById(R.id.btnCancelarClienteDialog);

        builder.setTitle("Nuevo Cliente Rápido");

        AlertDialog dialog = builder.create();

        btnGuardarCliente.setOnClickListener(v -> {
            String dni = etDni.getText().toString().trim();
            String nombre = etNombre.getText().toString().trim();
            String direccion = etDireccion.getText().toString().trim();
            String localidad = etLocalidad.getText().toString().trim();
            String codigoPostal = etCodigoPostal.getText().toString().trim();

            // Llamamos al ClientesViewModel para guardarlo
            clientesViewModel.guardarCliente(dni, nombre, direccion, localidad, codigoPostal);
            // El observador del ClientesViewModel se activará y recargará la lista de clientes

            dialog.dismiss();
            Toast.makeText(getContext(), "Cliente nuevo guardado. Selecciónalo en la lista.", Toast.LENGTH_LONG).show();
        });

        btnCancelarCliente.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }


    /**
     * Añade una fila de Producto/Cantidad al diálogo de Reparación.
     */
    private void agregarViewProducto(LinearLayout container, List<ProductoVenta> productosLista, TextView tvTotal) {
        if (listaProductosSpinner == null || listaProductosSpinner.isEmpty()) {
            Toast.makeText(getContext(), "No hay productos cargados", Toast.LENGTH_SHORT).show();
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(getContext());
        // Asegúrate de que este layout (item_producto_venta.xml) exista
        View productoView = inflater.inflate(R.layout.item_producto_venta, container, false);

        Spinner spinnerProducto = productoView.findViewById(R.id.spinnerProducto);
        EditText etCantidad = productoView.findViewById(R.id.etCantidad);
        Button btnEliminar = productoView.findViewById(R.id.btnEliminar);

        // Configurar spinner de productos
        List<String> nombresProductos = listaProductosSpinner.stream()
                .map(p -> String.format("%s - $%.2f (Stock: %d)", p.getNombre(), p.getPrecio(), p.getCantidad()))
                .collect(Collectors.toList());
        nombresProductos.add(0, "Selecciona un producto");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, nombresProductos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProducto.setAdapter(adapter);

        // Listener para calcular total
        TextWatcher textWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                calcularTotalReparacion(container, productosLista, tvTotal);
            }
            @Override public void afterTextChanged(Editable s) {}
        };
        etCantidad.addTextChangedListener(textWatcher);
        spinnerProducto.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                calcularTotalReparacion(container, productosLista, tvTotal);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnEliminar.setOnClickListener(v -> {
            container.removeView(productoView);
            calcularTotalReparacion(container, productosLista, tvTotal);
        });

        container.addView(productoView);
    }

    /**
     * Recalcula la lista de productos y el total.
     */
    private void actualizarListaProductos(LinearLayout container, List<ProductoVenta> productosLista) {
        productosLista.clear();
        for (int i = 0; i < container.getChildCount(); i++) {
            View itemView = container.getChildAt(i);
            Spinner spinnerProducto = itemView.findViewById(R.id.spinnerProducto);
            EditText etCantidad = itemView.findViewById(R.id.etCantidad);

            int pos = spinnerProducto.getSelectedItemPosition();
            if (pos > 0) { // Si no es "Selecciona..."
                Producto p = listaProductosSpinner.get(pos - 1);
                int cant = 0;
                try {
                    cant = Integer.parseInt(etCantidad.getText().toString());
                } catch (NumberFormatException e) {
                    cant = 0;
                }

                if (cant > 0) {
                    productosLista.add(new ProductoVenta(p, cant));
                }
            }
        }
    }

    private void calcularTotalReparacion(LinearLayout container, List<ProductoVenta> productosLista, TextView tvTotal) {
        actualizarListaProductos(container, productosLista);
        double total = 0;
        for (ProductoVenta pv : productosLista) {
            total += pv.getSubtotal();
        }
        tvTotal.setText(String.format(Locale.getDefault(), "$%.2f", total));
    }
}