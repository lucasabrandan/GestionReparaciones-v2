package com.example.gestionreparacionesapp.ui.productos;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable; // Importar TextWatcher
import android.text.TextWatcher; // Importar TextWatcher
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.example.gestionreparacionesapp.data.db.entity.Producto;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Locale;

public class ProductosFragment extends Fragment implements ProductosAdapter.OnProductoInteractionListener {

    private ProductoViewModel viewModel;
    private RecyclerView recyclerViewProductos;
    private ProductosAdapter adapter;
    private TextView tvSinProductos;
    private FloatingActionButton fabAgregarProducto;

    // NUEVO: EditText para el buscador
    private EditText etBuscadorProductos;

    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private Uri tempImageUri = null;
    private ImageView dialogImageView;

    public ProductosFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new ProductosAdapter(new ArrayList<>(), this);
        viewModel = new ViewModelProvider(this).get(ProductoViewModel.class);

        // 1. Inicializar el lanzador para Pedir Permiso de Cámara
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        launchCamera();
                    } else {
                        Toast.makeText(getContext(), "Permiso de cámara necesario", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // 2. Inicializar el lanzador para Tomar Foto
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                isSuccess -> {
                    if (isSuccess) {
                        if (dialogImageView != null && tempImageUri != null) {
                            dialogImageView.setImageURI(tempImageUri);
                        }
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_productos, container, false);
        recyclerViewProductos = view.findViewById(R.id.recyclerViewProductos);
        tvSinProductos = view.findViewById(R.id.tvSinProductos);
        fabAgregarProducto = view.findViewById(R.id.fabAgregarProducto);

        // Vincular el nuevo EditText
        etBuscadorProductos = view.findViewById(R.id.etBuscadorProductos);

        setupRecyclerView();
        setupListeners();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupObservers();
        viewModel.cargarProductos();
    }

    private void setupRecyclerView() {
        recyclerViewProductos.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewProductos.setAdapter(adapter);
    }

    private void setupListeners() {
        fabAgregarProducto.setOnClickListener(v -> {
            mostrarDialogoProducto(null);
        });

        // NUEVO: Listener para el buscador (TextWatcher)
        etBuscadorProductos.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Llama al ViewModel para filtrar la lista CADA VEZ que el texto cambia
                viewModel.buscarProductos(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Clic normal (para Editar)
     */
    @Override
    public void onProductoClick(Producto producto) {
        mostrarDialogoProducto(producto); // Abre el diálogo para editar
    }

    /**
     * Clic Largo (para Borrar)
     */
    @Override
    public void onProductoLongClick(Producto producto) {
        // Mostrar diálogo de confirmación antes de borrar
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar Producto")
                .setMessage("¿Estás seguro de que deseas eliminar '" + producto.getNombre() + "'? Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    // Llamar al ViewModel para borrar
                    viewModel.eliminarProducto(producto);
                })
                .setNegativeButton("Cancelar", null) // No hace nada
                .show();
    }

    private void setupObservers() {
        viewModel.getListaProductos().observe(getViewLifecycleOwner(), productos -> {
            if (productos == null || productos.isEmpty()) {
                tvSinProductos.setVisibility(View.VISIBLE);
                recyclerViewProductos.setVisibility(View.GONE);
            } else {
                tvSinProductos.setVisibility(View.GONE);
                recyclerViewProductos.setVisibility(View.VISIBLE);
                adapter.setProductos(productos);
            }
        });
        viewModel.getOperationResult().observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;
            Toast.makeText(getContext(), result.message, Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Muestra el diálogo para CREAR o EDITAR.
     */
    private void mostrarDialogoProducto(@Nullable Producto producto) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_nuevo_producto, null);
        builder.setView(dialogView);

        EditText etSku = dialogView.findViewById(R.id.etSkuDialog);
        EditText etNombre = dialogView.findViewById(R.id.etNombreProductoDialog);
        EditText etPrecio = dialogView.findViewById(R.id.etPrecioDialog);
        EditText etCantidad = dialogView.findViewById(R.id.etCantidadDialog);
        Button btnGuardarProducto = dialogView.findViewById(R.id.btnGuardarProductoDialog);
        Button btnCancelarProducto = dialogView.findViewById(R.id.btnCancelarProductoDialog);
        Button btnAnadirFoto = dialogView.findViewById(R.id.btnAnadirFoto);
        dialogImageView = dialogView.findViewById(R.id.ivProductoPreview);
        tempImageUri = null;

        if (producto != null) {
            builder.setTitle("Editar Producto");
            etSku.setText(producto.getSku());
            etNombre.setText(producto.getNombre());
            etPrecio.setText(String.format(Locale.US, "%.2f", producto.getPrecio()));
            etCantidad.setText(String.valueOf(producto.getCantidad()));
            etSku.setEnabled(false);
            if (producto.getImageUri() != null) {
                tempImageUri = Uri.parse(producto.getImageUri());
                dialogImageView.setImageURI(tempImageUri);
            }
        } else {
            builder.setTitle(R.string.nuevo_producto);
            etSku.setEnabled(true);
        }

        AlertDialog dialog = builder.create();
        btnAnadirFoto.setOnClickListener(v -> checkPermissionAndLaunchCamera());

        btnGuardarProducto.setOnClickListener(v -> {
            String sku = etSku.getText().toString().trim();
            String nombre = etNombre.getText().toString().trim();
            String precio = etPrecio.getText().toString().trim();
            String cantidad = etCantidad.getText().toString().trim();
            String uriString = (tempImageUri != null) ? tempImageUri.toString() : null;

            if (producto == null) {
                viewModel.insertarProducto(sku, nombre, precio, cantidad, uriString);
            } else {
                viewModel.actualizarProducto(producto.getId(), sku, nombre, precio, cantidad, uriString);
            }
            dialog.dismiss();
        });

        btnCancelarProducto.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // --- Lógica de Cámara ---

    private void checkPermissionAndLaunchCamera() {
        String permission = Manifest.permission.CAMERA;
        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            requestPermissionLauncher.launch(permission);
        }
    }

    private void launchCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Foto Producto " + System.currentTimeMillis());
        values.put(MediaStore.Images.Media.DESCRIPTION, "Foto tomada por Gestión Reparaciones App");
        tempImageUri = requireContext().getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
        );

        if (tempImageUri != null) {
            takePictureLauncher.launch(tempImageUri);
        } else {
            Toast.makeText(getContext(), "Error al crear archivo de imagen", Toast.LENGTH_SHORT).show();
        }
    }
}