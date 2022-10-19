package com.earbite.notesopengltest.drawables;


import com.earbite.notesopengltest.models.Chunk;
import com.earbite.notesopengltest.models.Coordinate;

import java.util.HashSet;

public abstract class Stroke extends Drawable {
    private float x;
    private float y;
    private float zoom;
    private Vertex[] vertices;
    private Coordinate[] chunks;

    public Stroke(float x, float y, float zoom, Vertex[] vertices) {
        super();

        this.x = x;
        this.y = y;
        this.zoom = zoom;
        this.vertices = vertices;

        HashSet<Coordinate> strokeChunks = new HashSet<>();
        for (Vertex vertex : vertices) strokeChunks.add(Chunk.getChunkCoordinate(vertex.getX() + x, vertex.getY() + y));
        chunks = strokeChunks.toArray(new Coordinate[strokeChunks.size()]);
    }

    public float getX() {return x;}
    public float getY() {return y;}

    public float getZoom() {return zoom;}

    public Vertex[] getVertices() {return vertices;}
    public int getVertexCount() {return vertices.length;}

    public Coordinate[] getChunks() {return chunks;}

    @Override
    public boolean containsPoint(float x, float y) {
        double maximumDistance = Math.pow(.05 / zoom, 2d);
        x -= this.x;
        y -= this.y;
        for (Vertex vertex : vertices) {
            double distanceSquared = Math.pow(vertex.getX() - x, 2d) + Math.pow(vertex.getY() - y, 2d);
            if (distanceSquared <= maximumDistance) return true;
        }
        return false;
    }
}
