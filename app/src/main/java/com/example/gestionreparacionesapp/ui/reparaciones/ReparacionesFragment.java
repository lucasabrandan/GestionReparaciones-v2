package com.example.gestionreparacionesapp.ui.reparaciones;

// --- 1. IMPORTACIONES NECESARIAS (INCLUIDAS LAS NUEVAS PARA LA CÁMARA) ---
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

    // --- 2. LANZADOR PARA GESTIONAR LA SOLICITUD DE PERMISO DE CÁMARA ---
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // El usuario dio permiso, ahora sí abrimos la cámara.
                    abrirCamara();
                } else {
                    // El usuario negó el permiso. Le informamos.
                    Toast.makeText(getContext(), "Permiso de cámara denegado.", Toast.LENGTH_SHORT).show();
                }
            });


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
            // Lógica para rellenar datos existentes...
        } else {
            dialog.setTitle("Nueva Reparación");
            agregarViewProducto(containerProductos, productosEnReparacion, etCosteServicio, tvSubtotal, tvTotal, totalCalculatorWatcher);
        }

        btnGuardar.setOnClickListener(v -> {
            // Lógica para guardar la reparación...
            dialog.dismiss();
        });
        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void agregarViewProducto(LinearLayout container, List<ProductoVenta> productosLista, EditText etCosteServicio, TextView tvSubtotal, TextView tvTotal, TextWatcher totalCalculatorWatcher) {
        // Tu código original para agregar un producto a la reparación
    }

    private void actualizarListaProductos(LinearLayout container, List<ProductoVenta> productosLista) {
        // Tu código original para actualizar la lista de productos
    }

    private void calcularTotalReparacion(LinearLayout container, List<ProductoVenta> productosLista, EditText etCosteServicio, TextView tvSubtotal, TextView tvTotal) {
        // Tu código original para calcular el total
    }

    private void mostrarDialogoNuevoCliente() {
        // Tu código original para el diálogo de nuevo cliente
    }

    // --- 3. MÉTODO MODIFICADO PARA EL DIÁLOGO DE "NUEVO PRODUCTO RÁPIDO" ---
    private void mostrarDialogoNuevoProducto() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_nuevo_producto, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Nuevo Producto Rápido")
                .setView(dialogView)
                .create();

        // Encontrar los componentes del layout del diálogo
        ImageView ivPreview = dialogView.findViewById(R.id.ivProductoPreview);
        Button btnAnadirFoto = dialogView.findViewById(R.id.btnAnadirFoto); // El botón real
        EditText etSku = dialogView.findViewById(R.id.etSkuDialog);
        EditText etNombre = dialogView.findViewById(R.id.etNombreProductoDialog);
        EditText etPrecio = dialogView.findViewById(R.id.etPrecioDialog);
        EditText etCantidad = dialogView.findViewById(R.id.etCantidadDialog);
        Button btnGuardar = dialogView.findViewById(R.id.btnGuardarProductoDialog);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelarProductoDialog);

        // --- ASIGNAR LA LÓGICA DE LA CÁMARA AL BOTÓN "AÑADIR FOTO" ---
        btnAnadirFoto.setOnClickListener(v -> {
            comprobarPermisoYlanzarCamara();
        });

        btnGuardar.setOnClickListener(v -> {
            productoViewModel.guardarProducto(
                    etSku.getText().toString(),
                    etNombre.getText().toString(),
                    etPrecio.getText().toString(),
                    etCantidad.getText().toString()
                    // Aquí necesitarás también la URI de la imagen si la capturas
            );
            dialog.dismiss();
            Toast.makeText(getContext(), "Producto guardado. Cierra y vuelve a abrir el diálogo para seleccionarlo.", Toast.LENGTH_LONG).show();
        });

        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // --- 4. MÉTODOS AUXILIARES PARA LA LÓGICA DE LA CÁMARA ---

    /**
     * Comprueba si el permiso de la cámara está concedido.
     * Si lo está, abre la cámara. Si no, solicita el permiso.
     */
    private void comprobarPermisoYlanzarCamara() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // Permiso ya concedido, abre la cámara directamente.
            abrirCamara();
        } else {
            // Permiso no concedido, lanza el diálogo de solicitud.
            // El resultado lo gestionará el 'requestPermissionLauncher' que declaramos arriba.
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    /**
     * Crea y lanza un Intent para abrir la aplicación de la cámara.
     * Este método solo debe llamarse DESPUÉS de confirmar que el permiso está concedido.
     */
    private void abrirCamara() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // El siguiente paso es usar otro ActivityResultLauncher para recibir la foto y ponerla en el ImageView.
        // Por ahora, con esto la cámara ya se abrirá.
        startActivity(cameraIntent);
    }
}