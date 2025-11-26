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

    // --- MÉTODO 1: Presupuesto INDIVIDUAL (para Reparaciones) ---
    public static File generarPresupuestoIndividual(Context context, Cliente cliente, Reparacion reparacion) {
        int pageHeight = 1120;
        int pageWidth = 792;
        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        dibujarEncabezado(context, canvas, "ORDEN DE SERVICIO");
        dibujarDatosCliente(cliente, canvas);

        int startY = 300;
        dibujarTablaHeaderReparacion(context, canvas, startY);

        int y = startY + 60;
        dibujarFilaReparacion(canvas, reparacion, y);
        y += 100;

        dibujarTotales(context, canvas, y, reparacion.getPresupuestoTotal());

        pdfDocument.finishPage(page);

        String fileName = "Orden_" + cliente.getNombre().replace(" ", "") + "_" + reparacion.getId() + ".pdf";
        return guardarPdf(context, pdfDocument, fileName);
    }

    // --- MÉTODO 2: Presupuesto GRUPAL (para Reparaciones) ---
    public static File generarPresupuestoPdf(Context context, Cliente cliente, List<Reparacion> reparaciones) {
        int pageHeight = 1120;
        int pageWidth = 792;
        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        dibujarEncabezado(context, canvas, "ESTADO DE CUENTA / RESUMEN");
        dibujarDatosCliente(cliente, canvas);

        int startY = 280;
        dibujarTablaHeaderReparacion(context, canvas, startY);

        int y = startY + 60;
        double totalGeneral = 0;

        for (Reparacion rep : reparaciones) {
            dibujarFilaReparacion(canvas, rep, y);
            totalGeneral += rep.getPresupuestoTotal();
            y += 50;
        }

        dibujarTotales(context, canvas, y + 20, totalGeneral);

        pdfDocument.finishPage(page);

        String fileName = "EstadoCuenta_" + cliente.getNombre().replace(" ", "") + "_" + System.currentTimeMillis() + ".pdf";
        return guardarPdf(context, pdfDocument, fileName);
    }

    // --- MÉTODO 3: Comprobante de VENTA (AÑADIDO) ---
    public static File generarComprobanteVenta(Context context, Cliente cliente, Venta venta, List<ProductoVenta> productos) {
        int pageHeight = 1120;
        int pageWidth = 792;
        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        dibujarEncabezado(context, canvas, "COMPROBANTE DE VENTA");
        dibujarDatosCliente(cliente, canvas);

        int startY = 280;
        dibujarTablaHeaderVenta(context, canvas, startY);

        int y = startY + 60;
        for (ProductoVenta p : productos) {
            dibujarFilaVenta(canvas, p, y);
            y += 40;
        }

        dibujarTotales(context, canvas, y + 20, venta.getTotal());

        pdfDocument.finishPage(page);

        String fileName = "Venta_" + cliente.getNombre().replace(" ", "") + "_" + venta.getId() + ".pdf";
        return guardarPdf(context, pdfDocument, fileName);
    }

    // --- HELPERS GENERALES ---

    private static void dibujarEncabezado(Context context, Canvas canvas, String tituloDocumento) {
        Paint paint = new Paint();
        Paint titlePaint = new Paint();

        int colorDark = Color.DKGRAY;
        try { colorDark = context.getColor(R.color.app_dark_brand); } catch(Exception ignored){}

        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.app_logo);
        if (bitmap != null) {
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, false);
            canvas.drawBitmap(scaledBitmap, 40, 40, paint);
        }

        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        titlePaint.setTextSize(24);
        titlePaint.setColor(colorDark);
        canvas.drawText(tituloDocumento, 160, 70, titlePaint);

        paint.setTextSize(14);
        paint.setColor(Color.GRAY);
        canvas.drawText("Gestión de Reparaciones", 160, 95, paint);
        String fechaHoy = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        canvas.drawText("Fecha de emisión: " + fechaHoy, 160, 115, paint);
    }

    private static void dibujarDatosCliente(Cliente cliente, Canvas canvas) {
        Paint paint = new Paint();
        Paint boxPaint = new Paint();
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(1);
        boxPaint.setColor(Color.LTGRAY);
        canvas.drawRect(40, 150, 752, 260, boxPaint);

        paint.setTextSize(16);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("CLIENTE", 50, 175, paint);

        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setTextSize(14);
        canvas.drawText("Nombre: " + cliente.getNombre(), 50, 200, paint);
        canvas.drawText("DNI: " + cliente.getDni(), 50, 220, paint);
        canvas.drawText("Dirección: " + cliente.getDireccion() + " (" + cliente.getLocalidad() + ")", 50, 240, paint);
    }

    // --- HELPERS PARA REPARACIONES ---

    private static void dibujarTablaHeaderReparacion(Context context, Canvas canvas, int startY) {
        Paint paint = new Paint();
        Paint headerPaint = new Paint();
        int colorPrimary = Color.BLACK;
        try { colorPrimary = context.getColor(R.color.app_primary_yellow); } catch(Exception ignored){}

        headerPaint.setColor(colorPrimary);
        canvas.drawRect(40, startY, 752, startY + 30, headerPaint);
        paint.setColor(isColorLight(colorPrimary) ? Color.BLACK : Color.WHITE);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("EQUIPO / DETALLE", 50, startY + 20, paint);
        canvas.drawText("SUBTOTAL", 650, startY + 20, paint);
    }

    private static void dibujarFilaReparacion(Canvas canvas, Reparacion rep, int y) {
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        String equipo = rep.getEquipoMarca() + " " + rep.getEquipoModelo();
        canvas.drawText(equipo, 50, y, paint);

        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        String detalle = rep.getDescripcionProblema();
        if (rep.getRepuestosUtilizados() != null && !rep.getRepuestosUtilizados().isEmpty()) {
            detalle += " | Rep: " + rep.getRepuestosUtilizados();
        }
        if (detalle.length() > 85) detalle = detalle.substring(0, 85) + "...";
        canvas.drawText(detalle, 50, y + 20, paint);

        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText(String.format(Locale.getDefault(), "$ %.2f", rep.getPresupuestoTotal()), 650, y + 10, paint);

        Paint linePaint = new Paint();
        linePaint.setColor(Color.LTGRAY);
        canvas.drawLine(40, y + 30, 752, y + 30, linePaint);
    }

    // --- HELPERS PARA VENTAS (NUEVOS) ---

    private static void dibujarTablaHeaderVenta(Context context, Canvas canvas, int startY) {
        Paint paint = new Paint();
        Paint headerPaint = new Paint();
        int colorPrimary = Color.BLACK;
        try { colorPrimary = context.getColor(R.color.app_primary_yellow); } catch(Exception ignored){}

        headerPaint.setColor(colorPrimary);
        canvas.drawRect(40, startY, 752, startY + 30, headerPaint);
        paint.setColor(isColorLight(colorPrimary) ? Color.BLACK : Color.WHITE);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("PRODUCTO", 50, startY + 20, paint);
        canvas.drawText("CANT.", 450, startY + 20, paint);
        canvas.drawText("P. UNIT.", 550, startY + 20, paint);
        canvas.drawText("SUBTOTAL", 650, startY + 20, paint);
    }

    private static void dibujarFilaVenta(Canvas canvas, ProductoVenta producto, int y) {
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));

        // Dibuja el nombre del producto
        canvas.drawText(producto.getNombreProductoSnapshot(), 50, y, paint);

        // Dibuja la cantidad
        canvas.drawText(String.valueOf(producto.getCantidad()), 460, y, paint);

        // Dibuja el precio unitario
        canvas.drawText(String.format(Locale.getDefault(), "$ %.2f", producto.getPrecioUnitarioSnapshot()), 550, y, paint);

        // Dibuja el subtotal
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText(String.format(Locale.getDefault(), "$ %.2f", producto.getSubtotal()), 650, y, paint);

        Paint linePaint = new Paint();
        linePaint.setColor(Color.LTGRAY);
        canvas.drawLine(40, y + 15, 752, y + 15, linePaint);
    }

    // --- HELPERS COMUNES ---

    private static void dibujarTotales(Context context, Canvas canvas, int y, double total) {
        Paint paint = new Paint();
        Paint totalBgPaint = new Paint();
        int colorDark = Color.DKGRAY;
        try { colorDark = context.getColor(R.color.app_dark_brand); } catch(Exception ignored){}

        totalBgPaint.setColor(colorDark);
        canvas.drawRect(630, y - 25, 760, y + 10, totalBgPaint);

        paint.setTextSize(20);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setColor(Color.BLACK);
        canvas.drawText("TOTAL:", 500, y, paint);

        paint.setColor(Color.WHITE);
        canvas.drawText(String.format(Locale.getDefault(), "$ %.2f", total), 640, y, paint);
    }

    private static File guardarPdf(Context context, PdfDocument pdfDocument, String fileName) {
        File docsFolder = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (docsFolder != null && !docsFolder.exists()) {
            docsFolder.mkdirs();
        }
        File file = new File(docsFolder, fileName);

        try {
            pdfDocument.writeTo(new FileOutputStream(file));
            new Handler(Looper.getMainLooper()).post(() ->
                    Toast.makeText(context, "PDF guardado en Documentos", Toast.LENGTH_LONG).show()
            );
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            new Handler(Looper.getMainLooper()).post(() ->
                    Toast.makeText(context, "Error al guardar PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
            return null;
        } finally {
            pdfDocument.close();
        }
    }

    private static boolean isColorLight(int color) {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return darkness < 0.5;
    }
}