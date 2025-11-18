package com.example.gestionreparacionesapp.ui.reparaciones;

// --- 1. IMPORTACIONES NECESARIAS ---
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
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

// ¡CAMBIO CLAVE! Importamos las clases necesarias para manejar el JSON
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    // Lanzador para permisos de cámara (sin cambios)
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) abrirCamara();
                else Toast.makeText(getContext(), "Permiso de cámara denegado.", Toast.LENGTH_SHORT).show();
            });

    public ReparacionesFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new ReparacionesAdapter(new ArrayList<>(), new ArrayList<>(), this);
        reparacionesViewModel = new ViewModelProvider(this).get(ReparacionesViewModel.class);
        // Usamos requireActivity() para que el ViewModel sea compartido entre fragments
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
                .setMessage("¿Estás seguro de que deseas eliminar la reparación #" + reparacion.getId() + "? Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> reparacionesViewModel.eliminarReparacionPorId(reparacion.getId()))
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

        // --- INICIO DE LA IMPLEMENTACIÓN (Paso 3) ---

        // OBSERVADOR B: Reacciona a CUALQUIER cambio en la lista de clientes del ViewModel.
        // Se activa al inicio y cada vez que la lista se actualiza (p.ej. después de crear uno nuevo).
        clientesViewModel.getListaClientes().observe(getViewLifecycleOwner(), clientes -> {
            if (clientes != null) {
                // Actualiza la lista para el RecyclerView principal
                adapter.setClientes(clientes);

                // ¡LÍNEA CLAVE! Actualiza la lista local que usan los diálogos para construir el Spinner.
                listaClientesSpinner = clientes;
            }
        });

        // OBSERVADOR A: Reacciona al evento "cliente creado con éxito".
        // Este se activa una sola vez cuando el ViewModel lo notifica desde el diálogo de creación.
        clientesViewModel.getClienteCreadoConExito().observe(getViewLifecycleOwner(), exito -> {
            if (exito != null && exito) {
                // El ViewModel ha notificado que se creó un cliente.
                // 1. Mostramos un mensaje de feedback al usuario.
                Toast.makeText(getContext(), "Lista de clientes actualizada.", Toast.LENGTH_SHORT).show();

                // 2. La actualización de la lista 'listaClientesSpinner' ya habrá ocurrido
                // gracias al OBSERVADOR B, que reacciona a los cambios de 'getListaClientes()'.
            }
        });

        // --- FIN DE LA IMPLEMENTACIÓN ---

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

        // --- Componentes del diálogo ---
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

        // --- Configuración del Spinner de Clientes ---
        ArrayAdapter<String> clienteAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, new ArrayList<>());
        clienteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCliente.setAdapter(clienteAdapter);

        // Refrescamos el spinner con los datos actuales. La selección se hará más abajo.
        refrescarSpinnerDeClientes(clienteAdapter, -1);

        spinnerCliente.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0 && (position - 1) < listaClientesSpinner.size()) {
                    clienteSeleccionado[0] = listaClientesSpinner.get(position - 1);
                } else {
                    clienteSeleccionado[0] = null;
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        TextWatcher totalCalculatorWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                calcularTotalReparacion(containerProductos, etCosteServicio, tvSubtotal, tvTotal);
            }
            @Override public void afterTextChanged(Editable s) {}
        };
        etCosteServicio.addTextChangedListener(totalCalculatorWatcher);

        btnAnadirProducto.setOnClickListener(v -> agregarViewProducto(containerProductos, null, totalCalculatorWatcher, etCosteServicio, tvSubtotal, tvTotal));
        btnNuevoCliente.setOnClickListener(v -> mostrarDialogoNuevoCliente());
        btnNuevoProducto.setOnClickListener(v -> mostrarDialogoNuevoProducto());

        if (reparacion != null) {
            // MODO EDICIÓN
            dialog.setTitle("Editar Reparación #" + reparacion.getId());
            btnGuardar.setText("Actualizar");

            etDescripcion.setText(reparacion.getDescripcion());
            etCosteServicio.setText(String.format(Locale.US, "%.2f", reparacion.getCosteServicio()));

            spinnerCliente.post(() -> {
                int clienteId = reparacion.getClienteId();
                for (int i = 0; i < listaClientesSpinner.size(); i++) {
                    if (listaClientesSpinner.get(i).getId() == clienteId) {
                        spinnerCliente.setSelection(i + 1); // +1 por el "Selecciona un cliente"
                        break;
                    }
                }
            });

            List<ProductoVenta> productosGuardados = parsearProductosDesdeJson(reparacion.getProductosJson());
            if (productosGuardados.isEmpty()) {
                agregarViewProducto(containerProductos, null, totalCalculatorWatcher, etCosteServicio, tvSubtotal, tvTotal);
            } else {
                for (ProductoVenta pv : productosGuardados) {
                    agregarViewProducto(containerProductos, pv, totalCalculatorWatcher, etCosteServicio, tvSubtotal, tvTotal);
                }
            }

        } else {
            // MODO CREAR
            dialog.setTitle("Nueva Reparación");
            btnGuardar.setText("Guardar");
            agregarViewProducto(containerProductos, null, totalCalculatorWatcher, etCosteServicio, tvSubtotal, tvTotal);
        }

        btnGuardar.setOnClickListener(v -> {
            List<ProductoVenta> productosFinales = actualizarListaProductos(containerProductos);
            double costeServicio = 0.0;
            try {
                costeServicio = Double.parseDouble(etCosteServicio.getText().toString());
            } catch (NumberFormatException ignored) {}

            if (clienteSeleccionado[0] == null){
                Toast.makeText(getContext(), "Debes seleccionar un cliente.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (reparacion == null) {
                reparacionesViewModel.guardarReparacion(clienteSeleccionado[0], etDescripcion.getText().toString(), productosFinales, costeServicio);
            } else {
                reparacionesViewModel.actualizarReparacion(reparacion.getId(), clienteSeleccionado[0], etDescripcion.getText().toString(), productosFinales, costeServicio);
            }
            dialog.dismiss();
        });

        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
        calcularTotalReparacion(containerProductos, etCosteServicio, tvSubtotal, tvTotal);
    }

    private void agregarViewProducto(LinearLayout container, @Nullable ProductoVenta productoExistente, TextWatcher totalCalculatorWatcher, EditText etCosteServicio, TextView tvSubtotal, TextView tvTotal) {
        if (listaProductosSpinner == null || listaProductosSpinner.isEmpty()) {
            Toast.makeText(getContext(), "No hay productos disponibles", Toast.LENGTH_SHORT).show();
            return;
        }

        View productoView = getLayoutInflater().inflate(R.layout.item_producto_venta, container, false);
        Spinner spinnerProducto = productoView.findViewById(R.id.spinnerProducto);
        EditText etCantidad = productoView.findViewById(R.id.etCantidad);
        ImageButton btnEliminar = productoView.findViewById(R.id.btnEliminarProductoVenta);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, getNombresDeProductos());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProducto.setAdapter(adapter);

        if (productoExistente != null) {
            etCantidad.setText(String.valueOf(productoExistente.getCantidad()));
            spinnerProducto.post(() -> {
                for (int i = 0; i < listaProductosSpinner.size(); i++) {
                    // La comprobación ahora debe hacerse contra el Producto interno de ProductoVenta
                    if (listaProductosSpinner.get(i).getId() == productoExistente.getProducto().getId()) {
                        spinnerProducto.setSelection(i + 1); // +1 por "Selecciona..."
                        break;
                    }
                }
            });
        }

        etCantidad.addTextChangedListener(totalCalculatorWatcher);
        spinnerProducto.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                calcularTotalReparacion(container, etCosteServicio, tvSubtotal, tvTotal);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnEliminar.setOnClickListener(v -> {
            container.removeView(productoView);
            calcularTotalReparacion(container, etCosteServicio, tvSubtotal, tvTotal);
        });

        container.addView(productoView);
    }

    private List<ProductoVenta> actualizarListaProductos(LinearLayout container) {
        List<ProductoVenta> productosLista = new ArrayList<>();
        for (int i = 0; i < container.getChildCount(); i++) {
            View itemView = container.getChildAt(i);
            Spinner spinnerProducto = itemView.findViewById(R.id.spinnerProducto);
            EditText etCantidad = itemView.findViewById(R.id.etCantidad);
            int pos = spinnerProducto.getSelectedItemPosition();
            if (pos > 0) {
                Producto p = listaProductosSpinner.get(pos - 1);
                int cant = 0;
                try { cant = Integer.parseInt(etCantidad.getText().toString()); } catch (NumberFormatException ignored) {}
                if (cant > 0) productosLista.add(new ProductoVenta(p, cant));
            }
        }
        return productosLista;
    }

    private void calcularTotalReparacion(LinearLayout container, EditText etCosteServicio, TextView tvSubtotal, TextView tvTotal) {
        List<ProductoVenta> productosActuales = actualizarListaProductos(container);
        double subtotalProductos = 0;
        for (ProductoVenta pv : productosActuales) subtotalProductos += pv.getSubtotal();

        double costeServicio = 0.0;
        try { costeServicio = Double.parseDouble(etCosteServicio.getText().toString()); } catch (NumberFormatException ignored) {}

        double totalFinal = subtotalProductos + costeServicio;

        tvSubtotal.setText(String.format(Locale.getDefault(), "Subtotal Productos: $%.2f", subtotalProductos));
        tvTotal.setText(String.format(Locale.getDefault(), "TOTAL: $%.2f", totalFinal));
    }

    private List<ProductoVenta> parsearProductosDesdeJson(String json) {
        List<ProductoVenta> lista = new ArrayList<>();
        if (json == null || json.isEmpty()) return lista;

        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                int productoId = obj.getInt("producto_id");
                int cantidad = obj.getInt("cantidad");

                Producto productoOriginal = null;
                for (Producto p : listaProductosSpinner) {
                    if (p.getId() == productoId) {
                        productoOriginal = p;
                        break;
                    }
                }
                if (productoOriginal != null) lista.add(new ProductoVenta(productoOriginal, cantidad));
            }
        } catch (JSONException e) {
            Log.e("ReparacionesFragment", "Error al parsear JSON de productos", e);
        }
        return lista;
    }

    private void refrescarSpinnerDeClientes(ArrayAdapter<String> adapter, int clienteId) {
        List<String> nombresClientes = new ArrayList<>();
        nombresClientes.add("Selecciona un cliente");
        if (listaClientesSpinner != null) {
            nombresClientes.addAll(listaClientesSpinner.stream().map(c -> c.getNombre() + " (DNI: " + c.getDni() + ")").collect(Collectors.toList()));
        }
        adapter.clear();
        adapter.addAll(nombresClientes);
        adapter.notifyDataSetChanged();
    }

    private List<String> getNombresDeProductos() {
        List<String> nombres = new ArrayList<>();
        nombres.add("Selecciona un producto");
        if (listaProductosSpinner != null) {
            nombres.addAll(listaProductosSpinner.stream()
                    .map(p -> String.format(Locale.getDefault(), "%s - $%.2f (Stock: %d)", p.getNombre(), p.getPrecio(), p.getCantidad()))
                    .collect(Collectors.toList()));
        }
        return nombres;
    }

    private void mostrarDialogoNuevoCliente() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_nuevo_cliente, null);
        final AlertDialog dialog = new AlertDialog.Builder(requireContext()).setTitle("Nuevo Cliente Rápido").setView(dialogView).create();

        EditText etDni = dialogView.findViewById(R.id.etDniClienteDialog);
        EditText etNombre = dialogView.findViewById(R.id.etNombreClienteDialog);
        EditText etDireccion = dialogView.findViewById(R.id.etDireccionClienteDialog);
        EditText etLocalidad = dialogView.findViewById(R.id.etLocalidadClienteDialog);
        EditText etCP = dialogView.findViewById(R.id.etCpClienteDialog);
        Button btnGuardar = dialogView.findViewById(R.id.btnGuardarClienteDialog);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelarClienteDialog);

        btnGuardar.setOnClickListener(v -> {
            String dni = etDni.getText().toString().trim();
            String nombre = etNombre.getText().toString().trim();
            if (dni.isEmpty() || nombre.isEmpty()) {
                Toast.makeText(getContext(), "DNI y Nombre son obligatorios.", Toast.LENGTH_SHORT).show();
                return;
            }
            clientesViewModel.guardarCliente(
                    dni,
                    nombre,
                    etDireccion.getText().toString().trim(),
                    etLocalidad.getText().toString().trim(),
                    etCP.getText().toString().trim()
            );
            dialog.dismiss();
        });
        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void mostrarDialogoNuevoProducto() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_nuevo_producto, null);
        final AlertDialog dialog = new AlertDialog.Builder(requireContext()).setTitle("Nuevo Producto Rápido").setView(dialogView).create();
        // ... Lógica similar a la de nuevo cliente para guardar un producto rápidamente
        // Esto también se beneficiaría de un SingleLiveEvent en el ProductoViewModel.
    }

    // Métodos de cámara (Dummy)
    private void abrirCamara() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivity(cameraIntent);
    }
}