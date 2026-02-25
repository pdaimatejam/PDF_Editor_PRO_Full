package com.pdf.editorpro;

public class TextItem {

    public String text;
    public float x;
    public float y;
    public float textSize;
    public int color;
    public int pageIndex;

    public TextItem(String text,
                    float x,
                    float y,
                    float textSize,
                    int color,
                    int pageIndex) {

        this.text = text;
        this.x = x;
        this.y = y;
        this.textSize = textSize;
        this.color = color;
        this.pageIndex = pageIndex;
    }

    public TextItem(String text, float x, float y, float textSize, int color) {
    }
}