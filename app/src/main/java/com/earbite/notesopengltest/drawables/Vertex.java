package com.earbite.notesopengltest.drawables;

import android.util.Log;

import com.earbite.notesopengltest.models.Chunk;
import com.earbite.notesopengltest.models.Coordinate;

public class Vertex {
    private float x;
    private float y;
    private float thickness;

    public Vertex(float x, float y, float thickness) {
        this.x = x;
        this.y = y;
        this.thickness = thickness;
    }

    public float getX() {return x;}
    public float getY() {return y;}
    public float getThickness() {return thickness;}
}
