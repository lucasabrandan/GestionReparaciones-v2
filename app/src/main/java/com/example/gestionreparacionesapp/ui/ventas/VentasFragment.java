package com.example.gestionreparacionesapp.ui.ventas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.gestionreparacionesapp.R;

public class VentasFragment extends Fragment {

    public VentasFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Por ahora, solo muestra un texto para confirmar que la navegación funciona
        TextView textView = new TextView(getContext());
        textView.setText("Contenido de Ventas");
        textView.setGravity(View.TEXT_ALIGNMENT_CENTER);
        return textView;
    }
}