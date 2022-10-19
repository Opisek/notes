package com.earbite.notesopengltest.drawables;

public class Pen extends Stroke {
    public Drawable.drawableType getDrawableType() {
        return Drawable.drawableType.PEN;
    }
    public Pen(float x, float y, float zoom, Vertex[] vertices) {
        super(x, y, zoom, vertices);
    }
}
