package com.pdf.editorpro;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.github.barteksc.pdfviewer.PDFView;
import com.tom_roush.pdfbox.pdmodel.*;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.pdmodel.font.PDType0Font;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private PDFView pdfView;
    private TouchOverlayView touchOverlay;
    private Uri pdfUri;
    private SeekBar pageSeekBar;

    private Button btnDraw, btnSave, btnShare, btnDelete;
    private Button btnRed, btnBlue, btnBlack;

    private int currentColor = 0xFF000000;
    private float currentTextSize = 24f;

    private int selectedPage = 0;
    private boolean userSeeking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pdfView = findViewById(R.id.pdfView);
        touchOverlay = findViewById(R.id.touchOverlay);
        pageSeekBar = findViewById(R.id.pageSeekBar);

        btnDraw = findViewById(R.id.btnDraw);
        btnSave = findViewById(R.id.btnSave);
        btnShare = findViewById(R.id.btnShare);
        btnDelete = findViewById(R.id.btnDelete);

        btnRed = findViewById(R.id.btnRed);
        btnBlue = findViewById(R.id.btnBlue);
        btnBlack = findViewById(R.id.btnBlack);

        // Silme butonu
        btnDelete.setOnClickListener(v -> touchOverlay.deleteSelected());

        pdfUri = getIntent().getData();
        if (pdfUri != null) loadPdf();

        // Renk seçimi
        btnRed.setOnClickListener(v -> currentColor = 0xFFFF0000);
        btnBlue.setOnClickListener(v -> currentColor = 0xFF0000FF);
        btnBlack.setOnClickListener(v -> currentColor = 0xFF000000);

        // Yazı ekleme
        btnDraw.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Yazı Gir");

            final EditText input = new EditText(this);
            input.setHint("Buraya yaz...");
            builder.setView(input);

            builder.setPositiveButton("Tamam", (dialog, which) -> {
                String userText = input.getText().toString();
                if (userText.trim().isEmpty()) return;

                // Ortada ekleme, crash çözümü, satırlı destekli
                touchOverlay.addTextCentered(userText, currentTextSize, currentColor);

                Toast.makeText(this, "Yazı eklendi", Toast.LENGTH_SHORT).show();
            });

            builder.setNegativeButton("İptal", null);
            builder.show();
        });

        btnSave.setOnClickListener(v -> savePdf(false));
        btnShare.setOnClickListener(v -> savePdf(true));

        pageSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onStartTrackingTouch(SeekBar seekBar) { userSeeking = true; }

            @Override public void onStopTrackingTouch(SeekBar seekBar) {
                pdfView.jumpTo(seekBar.getProgress(), true);
                selectedPage = seekBar.getProgress();
                userSeeking = false;
            }

            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
        });
    }

    private void loadPdf() {
        pdfView.fromUri(pdfUri)
                .enableSwipe(true)
                .enableDoubletap(true)
                .enableAnnotationRendering(true)
                .spacing(5)
                .onPageChange((page, pageCount) -> {
                    selectedPage = page;
                    if (!userSeeking) {
                        pageSeekBar.setMax(pageCount - 1);
                        pageSeekBar.setProgress(page);
                    }
                })
                .load();
    }

    private void savePdf(boolean shareAfterSave) {
        try {
            if (pdfUri == null) return;

            InputStream inputStream = getContentResolver().openInputStream(pdfUri);
            PDDocument document = PDDocument.load(inputStream);

            InputStream fontStream = getAssets().open("fonts/Roboto-Bold.ttf");
            PDType0Font font = PDType0Font.load(document, fontStream, true);
            fontStream.close();

            // TouchOverlayView'den yazıları al
            List<TextItem> textItems = touchOverlay.getTextItems();

            for (TextItem item : textItems) {
                PDPage page = document.getPage(item.pageIndex);
                PDRectangle mediaBox = page.getMediaBox();

                float pdfWidth = mediaBox.getWidth();
                float pdfHeight = mediaBox.getHeight();

                float renderedWidth = pdfView.getWidth();
                float renderedHeight = pdfView.getHeight();

                float pdfX = (item.x / renderedWidth) * pdfWidth;
                float pdfY = (item.y / renderedHeight) * pdfHeight;
                pdfY = pdfHeight - pdfY;

                PDPageContentStream contentStream =
                        new PDPageContentStream(document, page,
                                PDPageContentStream.AppendMode.APPEND,
                                true, true);

                contentStream.beginText();
                contentStream.setFont(font, item.textSize);

                int r = (item.color >> 16) & 0xFF;
                int g = (item.color >> 8) & 0xFF;
                int b = item.color & 0xFF;

                contentStream.setNonStrokingColor(r, g, b);

                // Satırlı yazı desteği
                String[] lines = item.text.split("\n");
                float lineHeight = item.textSize;
                for (int i = 0; i < lines.length; i++) {
                    float lineY = pdfY - i * lineHeight;
                    contentStream.newLineAtOffset(pdfX, lineY);
                    contentStream.showText(lines[i]);
                    contentStream.newLineAtOffset(0, -lineHeight); // bir sonraki satır için
                }

                contentStream.endText();
                contentStream.close();
            }

            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, "Edited_" + System.currentTimeMillis() + ".pdf");
            values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/PdfEditorPro");
            values.put(MediaStore.MediaColumns.IS_PENDING, 1);

            Uri uri = getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);
            OutputStream os = getContentResolver().openOutputStream(uri);
            document.save(os);
            os.close();
            document.close();

            values.clear();
            values.put(MediaStore.MediaColumns.IS_PENDING, 0);
            getContentResolver().update(uri, values, null, null);

            if (shareAfterSave) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("application/pdf");
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(shareIntent, "PDF Paylaş"));
            } else {
                Toast.makeText(this, "Kaydedilen sayfa: " + selectedPage, Toast.LENGTH_SHORT).show();
                Toast.makeText(this, "Belgeler klasörüne kaydedildi", Toast.LENGTH_LONG).show();
            }

            Toast.makeText(this, "Toplam yazı: " + textItems.size(), Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Hata: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}