package com.earbite.notesopengltest.models;

import com.earbite.notesopengltest.actions.Action;
import com.earbite.notesopengltest.drawables.Drawable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class Section {
    public HashMap<Coordinate, Chunk> chunks;

    public Stack<Action[]> pastActions;
    public Stack<Action[]> undoneActions;

    //public LinkedHashMap<Long, Drawable> drawables; // TEMP

    public Section() {
        chunks = new HashMap<>();
        pastActions = new Stack<>();
        undoneActions = new Stack<>();

        //drawables = new LinkedHashMap<>(); // TEMP
    }

    private long ID;
    public void setID(long ID) {this.ID = ID;}
    public long getID() {return ID;}
}
