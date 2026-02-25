package com.pdf.editorpro;

import android.app.Application;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        PDFBoxResourceLoader.init(getApplicationContext());
    }
}