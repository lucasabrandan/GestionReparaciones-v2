package com.example.gestionreparacionesapp.ui.productos;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
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
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestionreparacionesapp.R;
import com.example.gestionreparacionesapp.data.db.entity.Producto;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductosFragment extends Fragment implements ProductosAdapter.OnProductoInteractionListener {

    private static final String KEY_URI = "saved_uri";
    private static final String KEY_DIALOG_VISIBLE = "dialog_visible";
    private static final String KEY_EDITING_ID = "editing_id";

    private ProductoViewModel viewModel;
    private RecyclerView recyclerViewProductos;
    private ProductosAdapter adapter;
    private TextView tvSinProductos;
    private FloatingActionButton fabAgregarProducto;
    private EditText etBuscadorProductos;

    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;

    // Variables de Estado para sobrevivir a la Cámara
    private Uri tempImageUri = null;
    private ImageView dialogImageView; // Referencia al ImageView del diálogo actual
    private boolean isDialogVisible = false;
    private int editingProductId = -1; // -1 si es nuevo, ID si es edición

    public ProductosFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new ProductosAdapter(new ArrayList<>(), this);
        viewModel = new ViewModelProvider(this).get(ProductoViewModel.class);

        // Restaurar estado si venimos de la cámara o rotación
        if (savedInstanceState != null) {
            String uriString = savedInstanceState.getString(KEY_URI);
            if (uriString != null) tempImageUri = Uri.parse(uriString);
            isDialogVisible = savedInstanceState.getBoolean(KEY_DIALOG_VISIBLE, false);
            editingProductId = savedInstanceState.getInt(KEY_EDITING_ID, -1);
        }

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) launchCamera();
                    else Toast.makeText(getContext(), "Permiso denegado", Toast.LENGTH_SHORT).show();
                }
        );

        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                isSuccess -> {
                    if (isSuccess) {
                        // Al volver, si el diálogo se restauró correctamente, actualizamos la imagen
                        if (dialogImageView != null && tempImageUri != null) {
                            dialogImageView.setImageURI(tempImageUri);
                        }
                    } else {
                        tempImageUri = null; // Cancelado
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

        // Si el diálogo estaba abierto antes de ir a la cámara, lo reabrimos
        if (isDialogVisible) {
            // Buscamos el producto si estábamos editando
            Producto p = null;
            if (editingProductId != -1 && viewModel.getListaProductos().getValue() != null) {
                for (Producto prod : viewModel.getListaProductos().getValue()) {
                    if (prod.getId() == editingProductId) {
                        p = prod;
                        break;
                    }
                }
            }
            mostrarDialogoProducto(p);

            // Si ya teníamos una foto tomada (al volver de la cámara), la mostramos
            if (dialogImageView != null && tempImageUri != null) {
                dialogImageView.setImageURI(tempImageUri);
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (tempImageUri != null) outState.putString(KEY_URI, tempImageUri.toString());
        outState.putBoolean(KEY_DIALOG_VISIBLE, isDialogVisible);
        outState.putInt(KEY_EDITING_ID, editingProductId);
    }

    private void setupRecyclerView() {
        recyclerViewProductos.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewProductos.setAdapter(adapter);
    }

    private void setupListeners() {
        fabAgregarProducto.setOnClickListener(v -> mostrarDialogoProducto(null));

        etBuscadorProductos.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.buscarProductos(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    public void onProductoClick(Producto producto) {
        mostrarDialogoProducto(producto);
    }

    @Override
    public void onProductoLongClick(Producto producto) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar Producto")
                .setMessage("¿Estás seguro de que deseas eliminar '" + producto.getNombre() + "'?")
                .setPositiveButton("Eliminar", (dialog, which) -> viewModel.eliminarProducto(producto))
                .setNegativeButton("Cancelar", null)
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
            if (result != null) Toast.makeText(getContext(), result.message, Toast.LENGTH_SHORT).show();
        });
    }

    private void mostrarDialogoProducto(@Nullable Producto producto) {
        isDialogVisible = true;
        editingProductId = (producto != null) ? producto.getId() : -1;

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
        Button btnAnadirFoto = dialogView.findViewById(R.id.btnAnadirFotoProducto);
        dialogImageView = dialogView.findViewById(R.id.ivProductoPreview); // Guardamos la referencia

        // Si no venimos de la cámara (tempUri es null) pero el producto tiene foto, la cargamos
        if (tempImageUri == null && producto != null && producto.getImageUri() != null) {
            dialogImageView.setImageURI(Uri.parse(producto.getImageUri()));
        }

        if (producto != null) {
            builder.setTitle("Editar Producto");
            etSku.setText(producto.getSku());
            etNombre.setText(producto.getNombre());
            etPrecio.setText(String.format(Locale.US, "%.2f", producto.getPrecio()));
            etCantidad.setText(String.valueOf(producto.getCantidad()));
            etSku.setEnabled(false);
        } else {
            builder.setTitle(R.string.nuevo_producto);
            etSku.setEnabled(true);
        }

        AlertDialog dialog = builder.create();

        // Importante: Detectar cuando se cierra para actualizar el estado
        dialog.setOnDismissListener(d -> {
            isDialogVisible = false;
            editingProductId = -1;
            // No limpiamos tempImageUri aquí para que persista si rotamos,
            // pero idealmente deberíamos limpiarlo si se guardó o canceló explícitamente.
        });

        btnAnadirFoto.setOnClickListener(v -> checkPermissionAndLaunchCamera());

        btnGuardarProducto.setOnClickListener(v -> {
            String sku = etSku.getText().toString().trim();
            String nombre = etNombre.getText().toString().trim();
            String precio = etPrecio.getText().toString().trim();
            String cantidad = etCantidad.getText().toString().trim();
            // Usamos la URI temporal si existe, sino mantenemos la del producto original (si editamos)
            String uriString = (tempImageUri != null) ? tempImageUri.toString() :
                    (producto != null ? producto.getImageUri() : null);

            if (producto == null) {
                viewModel.insertarProducto(sku, nombre, precio, cantidad, uriString);
            } else {
                viewModel.actualizarProducto(producto.getId(), sku, nombre, precio, cantidad, uriString);
            }
            tempImageUri = null; // Limpiar tras guardar
            dialog.dismiss();
        });

        btnCancelarProducto.setOnClickListener(v -> {
            tempImageUri = null; // Limpiar al cancelar
            dialog.dismiss();
        });

        dialog.show();
    }

    private void checkPermissionAndLaunchCamera() {
        String permission = Manifest.permission.CAMERA;
        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            requestPermissionLauncher.launch(permission);
        }
    }

    private void launchCamera() {
        try {
            File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File imageFile = File.createTempFile(
                    "JPEG_" + System.currentTimeMillis() + "_",
                    ".jpg",
                    storageDir
            );
            tempImageUri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".provider",
                    imageFile
            );
            takePictureLauncher.launch(tempImageUri);
        } catch (IOException e) {
            Toast.makeText(getContext(), "Error al crear archivo", Toast.LENGTH_SHORT).show();
        }
    }
}