package com.earbite.notesopengltest.drawables;

import com.earbite.notesopengltest.models.Coordinate;

public abstract class Drawable {
    public enum drawableType {
        TRIANGLE,
        PEN,
        MARKER
    }

    public abstract drawableType getDrawableType();
    public abstract boolean containsPoint(float x, float y);

    public abstract Coordinate[] getChunks();

    public abstract float getX();
    public abstract float getY();
    public abstract float getZoom();

    private long ID;
    public void setID(long ID) {this.ID = ID;}
    public long getID() {return ID;}

    public Drawable() {
    }
}
