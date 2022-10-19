package com.earbite.notesopengltest.models;

import java.util.Objects;

public class Coordinate {
    private final int x;
    private final int y;
    private final int hashCode;

    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
        this.hashCode = Objects.hash(x, y);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Coordinate coordinate = (Coordinate) object;
        return coordinate.x == x && coordinate.y == y;
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }
}