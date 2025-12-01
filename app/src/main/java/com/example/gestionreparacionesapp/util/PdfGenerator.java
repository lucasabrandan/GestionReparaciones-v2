package com.example.gestionreparacionesapp.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.example.gestionreparacionesapp.R;
import com.example.gestionreparacionesapp.data.db.entity.Cliente;
import com.example.gestionreparacionesapp.data.db.entity.ProductoReparacion;
import com.example.gestionreparacionesapp.data.db.entity.ProductoVenta;
import com.example.gestionreparacionesapp.data.db.entity.Reparacion;
import com.example.gestionreparacionesapp.data.db.entity.Venta;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PdfGenerator {

    private static final int PAGE_WIDTH = 595; // A4 Standard width in points
    private static final int PAGE_HEIGHT = 842; // A4 Standard height in points
    private static final int MARGIN = 40;

    // Colores corporativos
    private static final int COLOR_PRIMARY = Color.rgb(255, 193, 7); // Tu amarillo
    private static final int COLOR_BLACK = Color.BLACK;
    private static final int COLOR_GRAY = Color.DKGRAY;
    private static final int COLOR_LIGHT_GRAY = Color.LTGRAY;

    // --- REPARACIÓN INDIVIDUAL (Con lista de repuestos detallada) ---
    public static File generarPresupuestoReparacion(Context context, Cliente cliente, Reparacion reparacion, List<ProductoReparacion> repuestos) {
        PdfDocument pdf = new PdfDocument();
        PdfDocument.Page page = pdf.startPage(new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create());
        Canvas canvas = page.getCanvas();

        int y = MARGIN;

        // 1. Encabezado Estilo "LATIENDA"
        y = dibujarEncabezado(context, canvas, y, "PRESUPUESTO DE REPARACIÓN");

        // 2. Datos Cliente y Presupuesto (2 Columnas)
        y = dibujarDatosDosColumnas(canvas, y, cliente, "REP-" + reparacion.getId(), "REPARACION");

        // 3. Detalle Equipo (Solo en reparación)
        y = dibujarDetalleEquipo(canvas, y, reparacion);

        // 4. Tabla de Repuestos
        y = dibujarTablaHeader(canvas, y, "Repuesto");

        double totalAcumulado = 0;

        // Filas de Repuestos
        if (repuestos != null) {
            for (ProductoReparacion item : repuestos) {
                double subtotalItem = item.getCantidad() * item.getPrecioUnitarioCobrado();
                totalAcumulado += subtotalItem;

                String nombre = item.getNombreProductoSnapshot();
                // Si el nombre es muy largo, lo cortamos visualmente
                if (nombre.length() > 35) nombre = nombre.substring(0, 35) + "...";

                y = dibujarFilaTabla(canvas, y,
                        "SKU-" + item.getProductoId(), // Si tienes SKU real úsalo aquí
                        nombre,
                        String.valueOf(item.getCantidad()),
                        formatMoney(item.getPrecioUnitarioCobrado()),
                        formatMoney(subtotalItem));
            }
        }

        // 5. Mano de Obra (Como un item más o separado)
        if (reparacion.getCostoManoDeObra() > 0) {
            y = dibujarFilaTabla(canvas, y, "MO", "Mano de Obra / Servicio Técnico", "1",
                    formatMoney(reparacion.getCostoManoDeObra()),
                    formatMoney(reparacion.getCostoManoDeObra()));
            totalAcumulado += reparacion.getCostoManoDeObra();
        }

        // Si no hay nada, mostrar fila vacía
        if (totalAcumulado == 0 && (repuestos == null || repuestos.isEmpty())) {
            y = dibujarFilaTabla(canvas, y, "-", "Sin cargos registrados", "-", "-", "$ 0.00");
        }

        dibujarLineaFinal(canvas, y);
        y += 20;

        // 6. Totales
        dibujarTotales(canvas, y, totalAcumulado);

        // 7. Footer
        dibujarFooter(canvas, PAGE_HEIGHT - 50);

        pdf.finishPage(page);
        return guardarPdf(context, pdf, "REP-" + reparacion.getId() + "_" + cliente.getNombre());
    }

    // --- VENTA (Comprobante) ---
    public static File generarComprobanteVenta(Context context, Cliente cliente, Venta venta, List<ProductoVenta> productos) {
        PdfDocument pdf = new PdfDocument();
        PdfDocument.Page page = pdf.startPage(new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create());
        Canvas canvas = page.getCanvas();

        int y = MARGIN;

        // 1. Encabezado
        y = dibujarEncabezado(context, canvas, y, "PRESUPUESTO DE VENTA");

        // 2. Bloque Datos
        y = dibujarDatosDosColumnas(canvas, y, cliente, "VEN-" + venta.getId(), "VENTA");

        y += 20;
        y = dibujarTablaHeader(canvas, y, "Producto");

        double totalAcumulado = 0;

        if (productos != null) {
            for (ProductoVenta pv : productos) {
                double subtotal = pv.getCantidad() * pv.getPrecioUnitarioSnapshot();
                totalAcumulado += subtotal;

                String nombre = pv.getNombreProductoSnapshot();
                if (nombre.length() > 35) nombre = nombre.substring(0, 35) + "...";

                y = dibujarFilaTabla(canvas, y,
                        "SKU-" + pv.getProductoId(),
                        nombre,
                        String.valueOf(pv.getCantidad()),
                        formatMoney(pv.getPrecioUnitarioSnapshot()),
                        formatMoney(subtotal));
            }
        }

        dibujarLineaFinal(canvas, y);
        y += 20;
        dibujarTotales(canvas, y, totalAcumulado);
        dibujarFooter(canvas, PAGE_HEIGHT - 50);

        pdf.finishPage(page);
        return guardarPdf(context, pdf, "VEN-" + venta.getId() + "_" + cliente.getNombre());
    }

    // --- ESTADO DE CUENTA (Global) ---
    public static File generarPresupuestoPdf(Context context, Cliente cliente, List<Reparacion> reparaciones) {
        PdfDocument pdf = new PdfDocument();
        PdfDocument.Page page = pdf.startPage(new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create());
        Canvas canvas = page.getCanvas();

        int y = MARGIN;
        y = dibujarEncabezado(context, canvas, y, "ESTADO DE CUENTA");
        y = dibujarDatosDosColumnas(canvas, y, cliente, "RESUMEN", "CTA. CTE.");

        y += 20;
        y = dibujarTablaHeader(canvas, y, "Servicio");

        double totalAcumulado = 0;

        if (reparaciones != null) {
            for (Reparacion rep : reparaciones) {
                String desc = rep.getEquipoMarca() + " " + rep.getEquipoModelo() + " - " + rep.getDescripcionProblema();
                if (desc.length() > 40) desc = desc.substring(0, 40) + "...";

                y = dibujarFilaTabla(canvas, y,
                        "REP-" + rep.getId(),
                        desc,
                        "1",
                        formatMoney(rep.getPresupuestoTotal()),
                        formatMoney(rep.getPresupuestoTotal()));

                totalAcumulado += rep.getPresupuestoTotal();
            }
        }

        dibujarLineaFinal(canvas, y);
        y += 20;
        dibujarTotales(canvas, y, totalAcumulado);
        dibujarFooter(canvas, PAGE_HEIGHT - 50);

        pdf.finishPage(page);
        return guardarPdf(context, pdf, "EstadoCuenta_" + cliente.getNombre());
    }


    // ================= HELPERS DE DIBUJO =================

    private static int dibujarEncabezado(Context context, Canvas canvas, int y, String tituloDoc) {
        Paint paint = new Paint();

        // Logo (Izquierda)
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.app_logo);
        if (bitmap != null) {
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 50, 50, false);
            canvas.drawBitmap(scaled, MARGIN, y, paint);
        }

        // Título Empresa
        paint.setColor(COLOR_BLACK);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextSize(14);
        canvas.drawText("DISPENSER", MARGIN + 60, y + 20, paint);
        canvas.drawText("LATIENDA", MARGIN + 60, y + 35, paint);

        // Título Documento (Derecha)
        paint.setTextSize(18);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(tituloDoc, PAGE_WIDTH - MARGIN, y + 30, paint);
        paint.setTextAlign(Paint.Align.LEFT); // Reset

        return y + 80;
    }

    private static int dibujarDatosDosColumnas(Canvas canvas, int y, Cliente cliente, String nro, String tipo) {
        Paint paint = new Paint();
        paint.setTextSize(10);
        int col2X = PAGE_WIDTH / 2 + 20;

        // Títulos de Columnas
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("Datos del Cliente", MARGIN, y, paint);
        canvas.drawText("Datos del Presupuesto", col2X, y, paint);
        y += 15;

        // Datos
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        int y1 = y;
        canvas.drawText("Nombre: " + cliente.getNombre(), MARGIN, y1, paint); y1 += 12;
        canvas.drawText("Contacto: " + (cliente.getDni().isEmpty() ? "-" : cliente.getDni()), MARGIN, y1, paint); y1 += 12;
        canvas.drawText("Email: -", MARGIN, y1, paint); y1 += 12;
        canvas.drawText("Dirección: " + cliente.getDireccion(), MARGIN, y1, paint); y1 += 12;
        canvas.drawText("Localidad: " + cliente.getLocalidad(), MARGIN, y1, paint); y1 += 12;

        int y2 = y;
        canvas.drawText("N°: " + nro, col2X, y2, paint); y2 += 12;
        String fecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        canvas.drawText("Fecha: " + fecha, col2X, y2, paint); y2 += 12;
        if(tipo != null) {
            canvas.drawText("Tipo: " + tipo, col2X, y2, paint); y2 += 12;
        }

        return Math.max(y1, y2) + 10;
    }

    private static int dibujarDetalleEquipo(Canvas canvas, int y, Reparacion rep) {
        Paint paint = new Paint();
        paint.setTextSize(10);

        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("Equipo", MARGIN, y, paint);
        y += 15;

        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        String linea = "Marca: " + rep.getEquipoMarca() + "   |   Modelo: " + rep.getEquipoModelo() + "   |   Serie: " + rep.getEquipoSerie();
        canvas.drawText(linea, MARGIN, y, paint);
        y += 12;

        String falla = "Falla: " + rep.getDescripcionProblema();
        if(falla.length() > 90) falla = falla.substring(0, 90) + "...";
        canvas.drawText(falla, MARGIN, y, paint);

        return y + 25;
    }

    private static int dibujarTablaHeader(Canvas canvas, int y, String labelItem) {
        Paint bgPaint = new Paint();
        bgPaint.setColor(COLOR_LIGHT_GRAY);
        bgPaint.setAlpha(80);
        canvas.drawRect(MARGIN, y - 12, PAGE_WIDTH - MARGIN, y + 6, bgPaint);

        Paint textPaint = new Paint();
        textPaint.setTextSize(10);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        textPaint.setColor(COLOR_BLACK);

        canvas.drawText("SKU", MARGIN + 5, y, textPaint);
        canvas.drawText(labelItem, MARGIN + 70, y, textPaint);
        canvas.drawText("Cant.", PAGE_WIDTH - MARGIN - 180, y, textPaint);
        canvas.drawText("P. Unit.", PAGE_WIDTH - MARGIN - 120, y, textPaint);
        canvas.drawText("Total", PAGE_WIDTH - MARGIN - 60, y, textPaint);

        return y + 20;
    }

    private static int dibujarFilaTabla(Canvas canvas, int y, String sku, String desc, String cant, String pUnit, String total) {
        Paint paint = new Paint();
        paint.setTextSize(10);
        paint.setColor(COLOR_BLACK);

        canvas.drawText(sku, MARGIN + 5, y, paint);

        if (desc.length() > 40) desc = desc.substring(0, 40) + "...";
        canvas.drawText(desc, MARGIN + 70, y, paint);

        canvas.drawText(cant, PAGE_WIDTH - MARGIN - 180, y, paint);
        canvas.drawText(pUnit, PAGE_WIDTH - MARGIN - 120, y, paint);
        canvas.drawText(total, PAGE_WIDTH - MARGIN - 50, y, paint); // Alineado un poco más a la derecha

        return y + 15;
    }

    private static void dibujarLineaFinal(Canvas canvas, int y) {
        Paint linePaint = new Paint();
        linePaint.setColor(COLOR_LIGHT_GRAY);
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint);
    }

    private static void dibujarTotales(Canvas canvas, int y, double total) {
        Paint paint = new Paint();
        paint.setTextSize(12);

        // Subtotal
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        String subTxt = "Subtotal:";
        float subW = paint.measureText(subTxt);
        canvas.drawText(subTxt, PAGE_WIDTH - MARGIN - 150, y, paint);
        canvas.drawText(formatMoney(total), PAGE_WIDTH - MARGIN - 60, y, paint);

        y += 20;

        // Total Final
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("TOTAL FINAL:", PAGE_WIDTH - MARGIN - 150, y, paint);
        canvas.drawText(formatMoney(total), PAGE_WIDTH - MARGIN - 60, y, paint);
    }

    private static void dibujarFooter(Canvas canvas, int y) {
        Paint paint = new Paint();
        paint.setTextSize(9);
        paint.setColor(COLOR_GRAY);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Este presupuesto posee una validez de 7 días a partir de su emisión.", PAGE_WIDTH / 2, y, paint);
    }

    private static String formatMoney(double amount) {
        return "$ " + String.format("%.2f", amount);
    }

    private static File guardarPdf(Context context, PdfDocument pdf, String fileNameBase) {
        File folder = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (!folder.exists()) folder.mkdirs();

        String fileName = fileNameBase.replace("/", "-") + ".pdf";
        File file = new File(folder, fileName.replace(" ", "_") + ".pdf");

        try {
            pdf.writeTo(new FileOutputStream(file));
            new Handler(Looper.getMainLooper()).post(() ->
                    Toast.makeText(context, "PDF guardado: " + fileName, Toast.LENGTH_SHORT).show()
            );
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            pdf.close();
        }
    }
}