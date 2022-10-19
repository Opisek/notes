package com.earbite.notesopengltest.models;

import com.earbite.notesopengltest.drawables.Drawable;

import java.util.LinkedHashMap;

public class Chunk {
    private final static int chunkSize = 1;

    public static Coordinate getChunkCoordinate(float x, float y) {
        return new Coordinate((int)(x / chunkSize),(int)(y / chunkSize));
    }

    private Coordinate coordinate;

    public LinkedHashMap<Long, Drawable> drawables;

    public Chunk(Coordinate coordinate) {
        this.coordinate = coordinate;
        drawables = new LinkedHashMap<>();
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }
}
