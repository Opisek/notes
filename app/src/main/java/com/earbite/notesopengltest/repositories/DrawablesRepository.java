package com.earbite.notesopengltest.repositories;

import android.util.Log;

import com.earbite.notesopengltest.actions.Action;
import com.earbite.notesopengltest.actions.DrawableCreatedAction;
import com.earbite.notesopengltest.actions.DrawableRemovedAction;
import com.earbite.notesopengltest.drawables.Drawable;
import com.earbite.notesopengltest.models.Chunk;
import com.earbite.notesopengltest.models.Coordinate;
import com.earbite.notesopengltest.models.Section;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class DrawablesRepository {
    private static DrawablesRepository instance;
    public static DrawablesRepository getInstance() {
        if (instance == null) instance = new DrawablesRepository();
        return instance;
    }

    private HashMap<Long, Section> sections;
    private Section activeSection;

    private DrawablesRepository() {
        activeSection = new Section();

        /*onNewDrawable(new Pen(0f, 0f, .5f, new Vertex[]{
                new Vertex(0f, 0f, .1f),
                new Vertex(0f, .5f, .1f),
                new Vertex(.5f, .5f, .1f),
                new Vertex(.5f, 1.5f, .1f),
                new Vertex(-.5f, 2f, .1f)
        }));*/
    }

    //
    //
    // Drawables
    //
    //

    // Listener

    private OnDrawableChangeListener onDrawableChangesListener;
    public interface OnDrawableChangeListener {
        void onNewDrawable(Drawable drawable);
        void onRevokeDrawable(long ID);
        void onRevokeDone();
    }
    public void SetOnDrawableChangesListener(OnDrawableChangeListener onDrawableChangesListener) {
        this.onDrawableChangesListener = onDrawableChangesListener;
    }

    // Core Methods

    private void onNewDrawable(Drawable drawable, boolean canUndo) {
        if (canUndo) addAction(new DrawableCreatedAction(drawable));

        if (drawable.getID() == 0L) drawable.setID(System.currentTimeMillis());

        Coordinate[] coordinates = drawable.getChunks();
        for (Coordinate coordinate : coordinates) {
            Chunk chunk = activeSection.chunks.get(coordinate);
            if (chunk == null) {
                chunk = new Chunk(coordinate);
                activeSection.chunks.put(coordinate, chunk);
            }
            chunk.drawables.put(drawable.getID(), drawable);
        }

        if (onDrawableChangesListener != null) onDrawableChangesListener.onNewDrawable(drawable);
    }

    private boolean onRevokeDrawable(Drawable drawable, boolean canUndo) {
        Drawable revokedDrawable = null;

        for (int i = 0; i < drawable.getChunks().length; i++) {
            Chunk chunk = activeSection.chunks.get(drawable.getChunks()[i]);
            Drawable foundDrawable = chunk.drawables.get(drawable.getID());
            if (foundDrawable == null) continue;
            if (revokedDrawable == null) revokedDrawable = foundDrawable;
            chunk.drawables.remove(foundDrawable);
        }

        if (revokedDrawable == null) return false;
        if (canUndo) revokeBuffer.add(new DrawableRemovedAction(revokedDrawable));
        if (onDrawableChangesListener != null) onDrawableChangesListener.onRevokeDrawable(drawable.getID());

        return true;
    }
    private boolean onRevokeDrawable(long ID, boolean canUndo) { // DEPRECATED
        Drawable revokedDrawable = null;

        Set<Map.Entry<Coordinate, Chunk>> entrySet = activeSection.chunks.entrySet();
        for (Iterator<Map.Entry<Coordinate, Chunk>> iterator = entrySet.iterator(); iterator.hasNext();) {
            Chunk chunk = iterator.next().getValue();
            Drawable foundDrawable = chunk.drawables.get(ID);
            if (foundDrawable == null) continue;
            if (revokedDrawable == null) revokedDrawable = foundDrawable;
            chunk.drawables.remove(foundDrawable);
        }

        if (revokedDrawable == null) return false;
        if (canUndo) revokeBuffer.add(new DrawableRemovedAction(revokedDrawable));
        if (onDrawableChangesListener != null) onDrawableChangesListener.onRevokeDrawable(ID);

        return true;
    }

    // Interface Methods

    public HashMap<Long, Drawable> getDrawables(Coordinate coordinate) {
        Chunk chunk = activeSection.chunks.get(coordinate);
        if (chunk == null) return null;
        else return chunk.drawables;
    }
    public HashMap<Coordinate, Chunk> getChunks() {
        return activeSection.chunks;
    }

    public void addNewDrawable(Drawable drawable, boolean undoable) {
        onNewDrawable(drawable, undoable);
    }
    public void revokeDrawable(Drawable drawable, boolean undoable) {
        if (onRevokeDrawable(drawable, undoable) && onDrawableChangesListener != null) onDrawableChangesListener.onRevokeDone();
    }
    /*public void revokeDrawable(long ID, boolean undoable) { // DEPRECATED
        if (onRevokeDrawable(ID, undoable) && onDrawableChangesListener != null) onDrawableChangesListener.onRevokeDone();
    }*/

    private LinkedList<Action> revokeBuffer;
    public void beginErasing() {
        revokeBuffer = new LinkedList<>();
    }
    public void eraseDrawables(float x, float y) {
        Chunk chunk = activeSection.chunks.get(Chunk.getChunkCoordinate(x, y));
        if (chunk == null) return;

        Queue<Drawable> revokes = new LinkedList<>();
        Set<Map.Entry<Long, Drawable>> iterableDrawables = chunk.drawables.entrySet();
        for (Iterator<Map.Entry<Long, Drawable>> it = iterableDrawables.iterator(); it.hasNext();) {
            Map.Entry<Long, Drawable> entry = it.next();
            if (entry.getValue().containsPoint(x, y)) {
                revokes.add(entry.getValue());
            }
        }
        if (revokes.isEmpty()) return;

        do {
            onRevokeDrawable(revokes.remove(), true);
        } while (!revokes.isEmpty());
        if (onDrawableChangesListener != null) onDrawableChangesListener.onRevokeDone();
    }
    public void stopErasing() {
        if (revokeBuffer.size() > 0) addAction(revokeBuffer.toArray(new Action[revokeBuffer.size()]));
    }

    //
    //
    // Actions
    //
    //

    private void addAction(Action action) {
        addAction(new Action[]{action});
    }

    private void addAction(Action[] actions) {
        activeSection.pastActions.push(actions);
        activeSection.undoneActions.clear();
    }

    public void undoAction() {
        if (activeSection.pastActions.empty()) return;
        Action[] actions = activeSection.pastActions.pop();
        activeSection.undoneActions.push(actions);
        for (int i = 0; i < actions.length; i++) actions[i].undo();
    }

    public void redoAction() {
        if (activeSection.undoneActions.empty()) return;
        Action[] actions = activeSection.undoneActions.pop();
        activeSection.pastActions.push(actions);
        for (int i = 0; i < actions.length; i++) actions[i].redo();
    }
}
