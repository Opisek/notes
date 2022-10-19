package com.earbite.notesopengltest.drawables;

import com.earbite.notesopengltest.models.Chunk;
import com.earbite.notesopengltest.models.Coordinate;

public class Triangle extends Drawable {
    public Drawable.drawableType getDrawableType() {
        return Drawable.drawableType.TRIANGLE;
    }

    @Override
    public boolean containsPoint(float x, float y) {
        return false;
    }

    private float x;
    private float y;
    private float zoom;

    public Triangle(float x, float y, float zoom) {
        super();

        this.x = x;
        this.y = y;
        this.zoom = zoom;
    }

    public float getX() {
        return x;
    }
    public float getY() {
        return y;
    }
    public float getZoom() {return zoom;}

    @Override
    public Coordinate[] getChunks() {
        return new Coordinate[]{Chunk.getChunkCoordinate(x, y)};
    }
}
