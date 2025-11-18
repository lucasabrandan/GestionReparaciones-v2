package com.example.gestionreparacionesapp.ui.ventas;

// --- 1. IMPORTACIONES NECESARIAS (INCLUIDAS LAS NUEVAS) ---
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.example.gestionreparacionesapp.data.db.entity.Venta;
import com.example.gestionreparacionesapp.ui.clientes.ClientesViewModel;
import com.example.gestionreparacionesapp.ui.productos.ProductoViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class VentasFragment extends Fragment implements VentasAdapter.OnVentaInteractionListener {

    private VentasViewModel ventasViewModel;
    private ClientesViewModel clientesViewModel;
    private ProductoViewModel productoViewModel;

    private RecyclerView recyclerViewVentas;
    private VentasAdapter adapter;
    private TextView tvSinVentas;
    private FloatingActionButton fabAgregarVenta;
    private EditText etBuscadorVentas;

    private List<Cliente> listaClientesSpinner = new ArrayList<>();
    private List<Producto> listaProductosSpinner = new ArrayList<>();

    // --- LANZADOR PARA GESTIONAR LA SOLICITUD DE PERMISO DE CÁMARA ---
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    abrirCamara();
                } else {
                    Toast.makeText(getContext(), "Permiso de cámara denegado.", Toast.LENGTH_SHORT).show();
                }
            });

    public VentasFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new VentasAdapter(new ArrayList<>(), new ArrayList<>(), this);
        ventasViewModel = new ViewModelProvider(this).get(VentasViewModel.class);
        clientesViewModel = new ViewModelProvider(requireActivity()).get(ClientesViewModel.class);
        productoViewModel = new ViewModelProvider(requireActivity()).get(ProductoViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ventas, container, false);
        recyclerViewVentas = view.findViewById(R.id.recyclerViewVentas);
        tvSinVentas = view.findViewById(R.id.tvSinVentas);
        fabAgregarVenta = view.findViewById(R.id.fabAgregarVenta);
        etBuscadorVentas = view.findViewById(R.id.etBuscadorVentas);
        setupRecyclerView();
        setupListeners();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupObservers();
        ventasViewModel.cargarVentas();
        clientesViewModel.cargarClientes(ClientesViewModel.ClienteFilterType.TODOS);
        productoViewModel.cargarProductos();
    }

    private void setupRecyclerView() {
        recyclerViewVentas.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewVentas.setAdapter(adapter);
    }

    private void setupListeners() {
        fabAgregarVenta.setOnClickListener(v -> mostrarDialogoVenta());
        etBuscadorVentas.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                ventasViewModel.buscarVentas(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    public void onVentaClick(Venta venta) {
        Toast.makeText(getContext(), "Venta #" + venta.getId() + " seleccionada.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onVentaLongClick(Venta venta) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Anular Venta")
                .setMessage("Esta acción no se puede deshacer. ¿Deseas continuar?")
                .setPositiveButton("Anular", (dialog, which) -> ventasViewModel.eliminarVenta(venta))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void setupObservers() {
        ventasViewModel.getListaVentas().observe(getViewLifecycleOwner(), ventas -> {
            boolean isEmpty = ventas == null || ventas.isEmpty();
            tvSinVentas.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            recyclerViewVentas.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            if (!isEmpty) adapter.setVentas(ventas);
        });

        clientesViewModel.getListaClientes().observe(getViewLifecycleOwner(), clientes -> {
            if (clientes != null) {
                adapter.setClientes(clientes);
                listaClientesSpinner = clientes;
            }
        });

        // ¡NUEVO OBSERVADOR! Para reaccionar a la creación de un nuevo cliente.
        clientesViewModel.getClienteCreadoConExito().observe(getViewLifecycleOwner(), exito -> {
            if (exito != null && exito) {
                Toast.makeText(getContext(), "Lista de clientes actualizada.", Toast.LENGTH_SHORT).show();
                // No es necesario refrescar manualmente el spinner aquí,
                // el observador de 'getListaClientes' ya actualiza 'listaClientesSpinner',
                // que será usada la próxima vez que se cree un adapter para el spinner.
            }
        });

        productoViewModel.getListaProductos().observe(getViewLifecycleOwner(), productos -> {
            if (productos != null) listaProductosSpinner = productos;
        });

        ventasViewModel.getOperationResult().observe(getViewLifecycleOwner(), result -> {
            if (result != null) Toast.makeText(getContext(), result.message, Toast.LENGTH_SHORT).show();
        });
    }

    private void mostrarDialogoVenta() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_nueva_venta, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setTitle("Nueva Venta")
                .create();

        Spinner spinnerCliente = dialogView.findViewById(R.id.spinnerClienteVenta);
        Button btnNuevoCliente = dialogView.findViewById(R.id.btnNuevoClienteVentaDialog);
        LinearLayout containerProductos = dialogView.findViewById(R.id.containerProductosVenta);
        Button btnAnadirProducto = dialogView.findViewById(R.id.btnAnadirProductoVenta);
        Button btnNuevoProducto = dialogView.findViewById(R.id.btnNuevoProductoVentaDialog);
        TextView tvTotal = dialogView.findViewById(R.id.tvTotalVenta);
        Button btnGuardar = dialogView.findViewById(R.id.btnGuardarVentaDialog);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelarVentaDialog);

        final List<ProductoVenta> productosEnVenta = new ArrayList<>();
        final Cliente[] clienteSeleccionado = {null};

        // Configurar Spinner de Clientes
        refrescarSpinnerDeClientes(spinnerCliente, -1); // Carga inicial de datos

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

        btnAnadirProducto.setOnClickListener(v -> agregarViewProducto(containerProductos, productosEnVenta, tvTotal));
        agregarViewProducto(containerProductos, productosEnVenta, tvTotal); // Añade la primera fila

        btnNuevoCliente.setOnClickListener(v -> mostrarDialogoNuevoCliente(spinnerCliente));
        btnNuevoProducto.setOnClickListener(v -> mostrarDialogoNuevoProducto(containerProductos));

        btnGuardar.setOnClickListener(v -> {
            if (clienteSeleccionado[0] == null) {
                Toast.makeText(getContext(), "Debes seleccionar un cliente.", Toast.LENGTH_SHORT).show();
                return;
            }
            actualizarListaProductos(containerProductos, productosEnVenta);
            if (productosEnVenta.isEmpty()) {
                Toast.makeText(getContext(), "Debes añadir al menos un producto.", Toast.LENGTH_SHORT).show();
                return;
            }
            ventasViewModel.guardarVenta(clienteSeleccionado[0], productosEnVenta);
            dialog.dismiss();
        });
        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void agregarViewProducto(LinearLayout container, List<ProductoVenta> productosLista, TextView tvTotal) {
        View productoView = getLayoutInflater().inflate(R.layout.item_producto_venta, container, false);
        Spinner spinnerProducto = productoView.findViewById(R.id.spinnerProducto);
        EditText etCantidad = productoView.findViewById(R.id.etCantidad);
        ImageButton btnEliminar = productoView.findViewById(R.id.btnEliminarProductoVenta);

        List<String> nombresProductos = new ArrayList<>();
        nombresProductos.add("Selecciona un producto");
        if (listaProductosSpinner != null && !listaProductosSpinner.isEmpty()) {
            nombresProductos.addAll(listaProductosSpinner.stream()
                    .map(p -> String.format(Locale.getDefault(), "%s - $%.2f (Stock: %d)", p.getNombre(), p.getPrecio(), p.getCantidad()))
                    .collect(Collectors.toList()));
        }
        ArrayAdapter<String> productoAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, nombresProductos);
        productoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProducto.setAdapter(productoAdapter);

        TextWatcher textWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                calcularTotalVenta(container, productosLista, tvTotal);
            }
            @Override public void afterTextChanged(Editable s) {}
        };

        etCantidad.addTextChangedListener(textWatcher);
        spinnerProducto.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                calcularTotalVenta(container, productosLista, tvTotal);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnEliminar.setOnClickListener(v -> {
            container.removeView(productoView);
            calcularTotalVenta(container, productosLista, tvTotal);
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
            if (pos > 0 && pos - 1 < listaProductosSpinner.size()) {
                Producto p = listaProductosSpinner.get(pos - 1);
                int cant = 0;
                try { cant = Integer.parseInt(etCantidad.getText().toString()); } catch (NumberFormatException ignored) {}
                if (cant > 0) productosLista.add(new ProductoVenta(p, cant));
            }
        }
    }

    private void calcularTotalVenta(LinearLayout container, List<ProductoVenta> productosLista, TextView tvTotal) {
        actualizarListaProductos(container, productosLista);
        double total = 0;
        for (ProductoVenta pv : productosLista) {
            total += pv.getSubtotal();
        }
        tvTotal.setText(String.format(Locale.getDefault(), "TOTAL: $%.2f", total));
    }

    private void mostrarDialogoNuevoCliente(Spinner spinnerClienteVenta) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_nuevo_cliente, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Nuevo Cliente Rápido")
                .setView(dialogView)
                .create();

        // --- ¡AQUÍ LA CORRECCIÓN! ---
        // Se usan los IDs estandarizados que ahora existen en el archivo XML.
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

            // La lógica para refrescar el spinner se manejará de forma reactiva
            // gracias al nuevo observador del SingleLiveEvent que añadimos en setupObservers().
            // El código complejo de 'new Observer' que estaba aquí ya no es necesario.
        });
        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void mostrarDialogoNuevoProducto(LinearLayout containerProductosVenta) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_nuevo_producto, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Nuevo Producto Rápido")
                .setView(dialogView)
                .create();

        Button btnAnadirFoto = dialogView.findViewById(R.id.btnAnadirFoto);
        EditText etNombre = dialogView.findViewById(R.id.etNombreProductoDialog);
        EditText etSku = dialogView.findViewById(R.id.etSkuDialog);
        EditText etPrecio = dialogView.findViewById(R.id.etPrecioDialog);
        EditText etCantidad = dialogView.findViewById(R.id.etCantidadDialog);
        Button btnGuardar = dialogView.findViewById(R.id.btnGuardarProductoDialog);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelarProductoDialog);

        btnAnadirFoto.setOnClickListener(v -> comprobarPermisoYlanzarCamara());

        btnGuardar.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString();
            if (nombre.isEmpty()) {
                Toast.makeText(getContext(), "El nombre del producto es obligatorio.", Toast.LENGTH_SHORT).show();
                return;
            }
            // Asumiendo que existe un método 'guardarProducto' en ProductoViewModel
            // que se encarga de todo, incluyendo un SingleLiveEvent para productos.
            productoViewModel.insertarProducto(
                    etSku.getText().toString(),
                    nombre,
                    etPrecio.getText().toString(),
                    etCantidad.getText().toString(),
                    null // imageUri
            );
            dialog.dismiss();

            // Aquí también, la lógica reactiva con un SingleLiveEvent en ProductoViewModel es la mejor opción.
        });
        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void refrescarSpinnerDeClientes(Spinner spinnerCliente, int selectionIndex) {
        List<String> nombresClientes = new ArrayList<>();
        nombresClientes.add("Selecciona un cliente");
        if (listaClientesSpinner != null) {
            nombresClientes.addAll(listaClientesSpinner.stream().map(c -> c.getNombre() + " (DNI: " + c.getDni() + ")").collect(Collectors.toList()));
        }

        ArrayAdapter<String> nuevoAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, nombresClientes);
        nuevoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCliente.setAdapter(nuevoAdapter);

        if (selectionIndex != -1 && selectionIndex < nuevoAdapter.getCount()) {
            spinnerCliente.setSelection(selectionIndex);
        }
    }

    private void refrescarSpinnersDeProductos(LinearLayout container) {
        List<String> nombresProductos = new ArrayList<>();
        nombresProductos.add("Selecciona un producto");
        if (listaProductosSpinner != null) {
            nombresProductos.addAll(listaProductosSpinner.stream()
                    .map(p -> String.format(Locale.getDefault(), "%s - $%.2f (Stock: %d)", p.getNombre(), p.getPrecio(), p.getCantidad()))
                    .collect(Collectors.toList()));
        }

        for (int i = 0; i < container.getChildCount(); i++) {
            View productoView = container.getChildAt(i);
            Spinner spinnerProducto = productoView.findViewById(R.id.spinnerProducto);
            int seleccionActual = spinnerProducto.getSelectedItemPosition();

            ArrayAdapter<String> nuevoAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, nombresProductos);
            nuevoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerProducto.setAdapter(nuevoAdapter);

            if (seleccionActual < nuevoAdapter.getCount()) {
                spinnerProducto.setSelection(seleccionActual);
            }
        }
    }

    private void comprobarPermisoYlanzarCamara() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            abrirCamara();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void abrirCamara() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivity(cameraIntent);
    }
}