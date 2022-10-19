package com.earbite.notesopengltest.drawables;

public class Marker extends Stroke {
    public drawableType getDrawableType() {
        return drawableType.MARKER;
    }
    public Marker(float x, float y, float zoom, Vertex[] vertices) {
        super(x, y, zoom, vertices);
    }
}
