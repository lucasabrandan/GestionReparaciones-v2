package com.example.gestionreparacionesapp.ui.ventas;

// --- CÁMARA: Importaciones necesarias (algunas ya estaban) ---
import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.ImageView; // Importante
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
import androidx.core.content.FileProvider; // Importante
import androidx.fragment.app.Fragment;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File; // Importante
import java.io.IOException; // Importante
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

    // --- CÁMARA: PASO 1 - Declarar las variables modernas ---
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private Uri tempImageUri = null;
    private ImageView dialogImageView = null;
    // --- FIN CÁMARA PASO 1 ---

    public VentasFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new VentasAdapter(new ArrayList<>(), new ArrayList<>(), this);
        ventasViewModel = new ViewModelProvider(this).get(VentasViewModel.class);
        clientesViewModel = new ViewModelProvider(requireActivity()).get(ClientesViewModel.class);
        productoViewModel = new ViewModelProvider(requireActivity()).get(ProductoViewModel.class);

        // --- CÁMARA: PASO 2 - Inicializar los ActivityResultLaunchers ---
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        launchCamera(); // Si nos dan permiso, lanzamos la cámara
                    } else {
                        Toast.makeText(getContext(), "Permiso de cámara es necesario.", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                isSuccess -> {
                    if (isSuccess && dialogImageView != null && tempImageUri != null) {
                        // Si se tomó la foto, la mostramos
                        dialogImageView.setImageURI(tempImageUri);
                    }
                }
        );
        // --- FIN CÁMARA PASO 2 ---
    }

    // --- CÁMARA: PASO 3 - Modificar el diálogo de nuevo producto ---
    private void mostrarDialogoNuevoProducto(LinearLayout containerProductosVenta) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_nuevo_producto, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Nuevo Producto Rápido")
                .setView(dialogView)
                .create();

        EditText etNombre = dialogView.findViewById(R.id.etNombreProductoDialog);
        EditText etSku = dialogView.findViewById(R.id.etSkuDialog);
        EditText etPrecio = dialogView.findViewById(R.id.etPrecioDialog);
        EditText etCantidad = dialogView.findViewById(R.id.etCantidadDialog);
        Button btnGuardar = dialogView.findViewById(R.id.btnGuardarProductoDialog);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelarProductoDialog);

        // --- Conectar los botones de la cámara ---
        Button btnAnadirFoto = dialogView.findViewById(R.id.btnAnadirFotoProducto); // ID CORREGIDO
        dialogImageView = dialogView.findViewById(R.id.ivProductoPreview);
        btnAnadirFoto.setOnClickListener(v -> checkPermissionAndLaunchCamera());
        // -----------------------------------------

        btnGuardar.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString();
            if (nombre.isEmpty()) {
                Toast.makeText(getContext(), "El nombre del producto es obligatorio.", Toast.LENGTH_SHORT).show();
                return;
            }
            // Pasamos la Uri de la imagen al guardar
            String imageUriString = (tempImageUri != null) ? tempImageUri.toString() : null;

            productoViewModel.insertarProducto(
                    etSku.getText().toString(),
                    nombre,
                    etPrecio.getText().toString(),
                    etCantidad.getText().toString(),
                    imageUriString
            );
            dialog.dismiss();

            // Lógica para refrescar spinners
            productoViewModel.getListaProductos().observe(getViewLifecycleOwner(), new androidx.lifecycle.Observer<List<Producto>>() {
                @Override
                public void onChanged(List<Producto> productos) {
                    if (productos.stream().anyMatch(p -> p.getNombre().equals(nombre))) {
                        productoViewModel.getListaProductos().removeObserver(this);
                        Toast.makeText(getContext(), "Producto guardado. Actualizando listas...", Toast.LENGTH_SHORT).show();
                        refrescarSpinnersDeProductos(containerProductosVenta);
                    }
                }
            });
        });

        btnCancelar.setOnClickListener(v -> {
            tempImageUri = null; // Limpiar URI si se cancela
            dialogImageView = null;
            dialog.dismiss();
        });

        dialog.show();
    }

    // --- CÁMARA: PASO 4 - Añadir los métodos de ayuda ---
    private void checkPermissionAndLaunchCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void launchCamera() {
        try {
            File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File imageFile = File.createTempFile("JPEG_" + System.currentTimeMillis() + "_", ".jpg", storageDir);

            tempImageUri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".provider",
                    imageFile
            );
            takePictureLauncher.launch(tempImageUri);
        } catch (IOException e) {
            Toast.makeText(getContext(), "Error al preparar la cámara.", Toast.LENGTH_SHORT).show();
            tempImageUri = null;
        }
    }
    // --- FIN CÁMARA PASO 4 ---

    // ==============================================================
    // A PARTIR DE AQUÍ, ES TU CÓDIGO ORIGINAL SIN LA LÓGICA DE CÁMARA ANTIGUA
    // ==============================================================

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
        fabAgregarVenta.setOnClickListener(v -> mostrarDialogoVenta(null));
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
        mostrarDialogoVenta(venta);
    }

    @Override
    public void onVentaLongClick(Venta venta) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Anular Venta")
                .setMessage("Esta acción no se puede deshacer. ¿Deseas anular la venta #" + venta.getId() + "?")
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

        clientesViewModel.getClienteCreadoConExito().observe(getViewLifecycleOwner(), exito -> {
            if (exito != null && exito) {
                Toast.makeText(getContext(), "Lista de clientes actualizada.", Toast.LENGTH_SHORT).show();
            }
        });

        productoViewModel.getListaProductos().observe(getViewLifecycleOwner(), productos -> {
            if (productos != null) listaProductosSpinner = productos;
        });

        ventasViewModel.getOperationResult().observe(getViewLifecycleOwner(), result -> {
            if (result != null) Toast.makeText(getContext(), result.message, Toast.LENGTH_SHORT).show();
        });
    }

    private void mostrarDialogoVenta(@Nullable Venta venta) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_nueva_venta, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext()).setView(dialogView);
        AlertDialog dialog = builder.create();

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

        refrescarSpinnerDeClientes(spinnerCliente, -1);
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

        btnAnadirProducto.setOnClickListener(v -> agregarViewProducto(containerProductos, productosEnVenta, tvTotal, null));
        btnNuevoCliente.setOnClickListener(v -> mostrarDialogoNuevoCliente(spinnerCliente));
        btnNuevoProducto.setOnClickListener(v -> mostrarDialogoNuevoProducto(containerProductos));

        if (venta != null) {
            builder.setTitle("Editar Venta #" + venta.getId());
            btnGuardar.setText("Actualizar");

            spinnerCliente.post(() -> {
                for (int i = 0; i < listaClientesSpinner.size(); i++) {
                    if (listaClientesSpinner.get(i).getId() == venta.getClienteId()) {
                        spinnerCliente.setSelection(i + 1);
                        break;
                    }
                }
            });

            List<ProductoVenta> productosGuardados = parsearProductosDesdeJson(venta.getProductosJson());
            if (productosGuardados.isEmpty()) {
                agregarViewProducto(containerProductos, productosEnVenta, tvTotal, null);
            } else {
                for (ProductoVenta pv : productosGuardados) {
                    agregarViewProducto(containerProductos, productosEnVenta, tvTotal, pv);
                }
            }
        } else {
            builder.setTitle("Nueva Venta");
            btnGuardar.setText("Guardar");
            agregarViewProducto(containerProductos, productosEnVenta, tvTotal, null);
        }

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

            if (venta == null) {
                ventasViewModel.guardarVenta(clienteSeleccionado[0], productosEnVenta);
            } else {
                ventasViewModel.actualizarVenta(venta.getId(), clienteSeleccionado[0], productosEnVenta);
            }
            dialog.dismiss();
        });

        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void agregarViewProducto(LinearLayout container, List<ProductoVenta> productosLista, TextView tvTotal, @Nullable ProductoVenta productoExistente) {
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

        if (productoExistente != null) {
            etCantidad.setText(String.valueOf(productoExistente.getCantidad()));
            spinnerProducto.post(() -> {
                for (int i = 0; i < listaProductosSpinner.size(); i++) {
                    if (listaProductosSpinner.get(i).getId() == productoExistente.getProducto().getId()) {
                        spinnerProducto.setSelection(i + 1);
                        break;
                    }
                }
            });
        }

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
                if (productoOriginal != null) {
                    lista.add(new ProductoVenta(productoOriginal, cantidad));
                }
            }
        } catch (JSONException e) {
            Log.e("VentasFragment", "Error al parsear JSON de productos", e);
        }
        return lista;
    }

    private void mostrarDialogoNuevoCliente(Spinner spinnerClienteVenta) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_nuevo_cliente, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Nuevo Cliente Rápido")
                .setView(dialogView)
                .create();

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

            clientesViewModel.getListaClientes().observe(getViewLifecycleOwner(), new androidx.lifecycle.Observer<List<Cliente>>() {
                @Override
                public void onChanged(List<Cliente> clientes) {
                    if (clientes.stream().anyMatch(c -> c.getDni().equals(dni))) {
                        clientesViewModel.getListaClientes().removeObserver(this);
                        int newClientIndex = -1;
                        for (int i = 0; i < clientes.size(); i++) {
                            if (clientes.get(i).getDni().equals(dni)) {
                                newClientIndex = i + 1;
                                break;
                            }
                        }
                        refrescarSpinnerDeClientes(spinnerClienteVenta, newClientIndex);
                    }
                }
            });
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
}