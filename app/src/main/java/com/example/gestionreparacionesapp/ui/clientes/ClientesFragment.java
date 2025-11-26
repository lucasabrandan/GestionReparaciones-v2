package com.example.gestionreparacionesapp.ui.clientes;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;

public class ClientesFragment extends Fragment implements ClientesAdapter.OnClienteInteractionListener {

    private ClientesViewModel viewModel;
    private RecyclerView recyclerViewClientes;
    private ClientesAdapter adapter;
    private TextView tvSinClientes;
    private FloatingActionButton fabAgregarCliente;
    private EditText etBuscadorClientes;

    public ClientesFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new ClientesAdapter(new ArrayList<>(), this);
        viewModel = new ViewModelProvider(this).get(ClientesViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_clientes, container, false);
        recyclerViewClientes = view.findViewById(R.id.recyclerViewClientes);
        tvSinClientes = view.findViewById(R.id.tvSinClientes);
        fabAgregarCliente = view.findViewById(R.id.fabAgregarCliente);
        etBuscadorClientes = view.findViewById(R.id.etBuscadorClientes);
        setupRecyclerView();
        setupListeners();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupObservers();
        viewModel.cargarClientes(ClientesViewModel.ClienteFilterType.TODOS);
    }

    private void setupRecyclerView() {
        recyclerViewClientes.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewClientes.setAdapter(adapter);
    }

    private void setupListeners() {
        fabAgregarCliente.setOnClickListener(v -> mostrarDialogoCliente(null));
        etBuscadorClientes.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.buscarClientes(s.toString());
            }
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    public void onClienteClick(Cliente cliente) {
        mostrarDialogoCliente(cliente);
    }

    @Override
    public void onClienteLongClick(Cliente cliente) {
        String[] options = {"Generar y Compartir PDF", "Eliminar Cliente"};

        new AlertDialog.Builder(requireContext())
                .setTitle("Acciones: " + cliente.getNombre())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        viewModel.generarPdfPresupuesto(requireContext(), cliente);
                    } else if (which == 1) {
                        new AlertDialog.Builder(requireContext())
                                .setMessage("¿Borrar definitivamente a " + cliente.getNombre() + "?")
                                .setPositiveButton("Sí", (d, w) -> viewModel.eliminarCliente(cliente))
                                .setNegativeButton("No", null)
                                .show();
                    }
                })
                .show();
    }

    private void setupObservers() {
        viewModel.getListaClientes().observe(getViewLifecycleOwner(), clientes -> {
            if (clientes == null || clientes.isEmpty()) {
                tvSinClientes.setVisibility(View.VISIBLE);
                recyclerViewClientes.setVisibility(View.GONE);
            } else {
                tvSinClientes.setVisibility(View.GONE);
                recyclerViewClientes.setVisibility(View.VISIBLE);
                adapter.setClientes(clientes);
            }
        });
        viewModel.getOperationResult().observe(getViewLifecycleOwner(), result -> {
            if (result != null) Toast.makeText(getContext(), result.message, Toast.LENGTH_SHORT).show();
        });

        // --- OBSERVAR EVENTO PDF PARA COMPARTIR ---
        viewModel.getPdfGeneradoEvent().observe(getViewLifecycleOwner(), this::compartirPdf);
    }

    private void compartirPdf(File pdfFile) {
        if (pdfFile == null) return;

        Uri pdfUri = FileProvider.getUriForFile(
                requireContext(),
                requireContext().getPackageName() + ".provider",
                pdfFile
        );

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/pdf");
        shareIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Estado de Cuenta");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Adjunto estado de cuenta / resumen de servicios.");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(shareIntent, "Compartir PDF vía..."));
    }

    private void mostrarDialogoCliente(@Nullable Cliente cliente) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_nuevo_cliente, null);
        builder.setView(dialogView);

        EditText etDni = dialogView.findViewById(R.id.etDniClienteDialog);
        EditText etNombre = dialogView.findViewById(R.id.etNombreClienteDialog);
        EditText etDireccion = dialogView.findViewById(R.id.etDireccionClienteDialog);
        EditText etLocalidad = dialogView.findViewById(R.id.etLocalidadClienteDialog);
        EditText etCodigoPostal = dialogView.findViewById(R.id.etCpClienteDialog);
        Button btnGuardar = dialogView.findViewById(R.id.btnGuardarClienteDialog);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelarClienteDialog);

        if (cliente != null) {
            builder.setTitle("Editar Cliente");
            etDni.setText(cliente.getDni());
            etNombre.setText(cliente.getNombre());
            etDireccion.setText(cliente.getDireccion());
            etLocalidad.setText(cliente.getLocalidad());
            etCodigoPostal.setText(cliente.getCodigoPostal());
            etDni.setEnabled(false);
        } else {
            builder.setTitle("Nuevo Cliente");
            etDni.setEnabled(true);
        }

        AlertDialog dialog = builder.create();
        btnGuardar.setOnClickListener(v -> {
            String dni = etDni.getText().toString().trim();
            String nombre = etNombre.getText().toString().trim();
            String dir = etDireccion.getText().toString().trim();
            String loc = etLocalidad.getText().toString().trim();
            String cp = etCodigoPostal.getText().toString().trim();

            if (cliente == null) viewModel.guardarCliente(dni, nombre, dir, loc, cp);
            else viewModel.actualizarCliente(cliente.getId(), dni, nombre, dir, loc, cp);
            dialog.dismiss();
        });
        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}