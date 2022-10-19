package com.earbite.notesopengltest.renderers;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import com.earbite.notesopengltest.drawables.Drawable;
import com.earbite.notesopengltest.drawables.Stroke;
import com.earbite.notesopengltest.drawables.Triangle;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class OpenGLRenderer implements GLSurfaceView.Renderer {

    private final float[] MVPMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] viewMatrix = new float[16];

    private LinkedList<Long> revokedIDs = new LinkedList<>();

    private HashMap<Long, DrawableRenderer> pens;
    private Queue<Drawable> newPens = new LinkedList<>();
    private Drawable temporaryPen;

    private HashMap<Long, DrawableRenderer> markers;
    private Queue<Drawable> newMarkers = new LinkedList<>();
    private Drawable temporaryMarker;

    private long lastTime;
    private long velocityStartTime;

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES20.glClearColor(1f, 1f, 1f, 1f);
        pens = new HashMap<>();
        temporaryPen = null;
        markers = new HashMap<>();
        temporaryMarker = null;

        scale = .5f;
        x = 0f;
        y = 0f;
        velocityX = 0f;
        velocityY = 0f;

        lastTime = SystemClock.elapsedRealtimeNanos();
    }

    private int width;
    private int height;
    private float ratio;
    private float scale;

    private float x;
    private float y;
    private float velocityX;
    private float initialVelocityX;
    private float velocityY;
    private float initialVelocityY;

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        this.width = width;
        this.height = height;

        GLES20.glViewport(0, 0, width, height);
        ratio = (float) width / height;
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

    public float getViewX(float viewPortX) { return (viewPortX - width/2) / width * 2 * ratio / scale; }
    public float getWorldX(float viewPortX) {
        return getViewX(viewPortX) - x;
    }
    public float getViewY(float viewPortY) {
        return (viewPortY - height/2) / height * -2 / scale;
    }
    public float getWorldY(float viewPortY) {
        return getViewY(viewPortY) - y;
    }

    public float getZoom() { return scale; }

    public static int loadShader(int type, String shaderCode){
        int shader = GLES20.glCreateShader(type);

        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    //
    // Render
    //


    private int renderCalls;

    @Override
    public void onDrawFrame(GL10 gl10) {
        // Maths
        long currentTime = SystemClock.elapsedRealtimeNanos();
        float deltaTime = (currentTime - lastTime) / 1000000000f;
        float elapsedSinceVelocityStart = (currentTime - velocityStartTime) / 1000000000f;
        lastTime = currentTime;

        if (velocityX != 0f) {
            x += velocityX * deltaTime;
            velocityX = calculateCurrentVelocity(initialVelocityX, elapsedSinceVelocityStart);
            if (Math.abs(velocityX) <= 0.0001f) velocityX = 0f;
        }

        if (velocityY != 0f) {
            y += velocityY * deltaTime;
            velocityY = calculateCurrentVelocity(initialVelocityY, elapsedSinceVelocityStart);
            if (Math.abs(velocityY) <= 0.0001f) velocityY = 0f;
        }

        // Matrices
        Matrix.setLookAtM(viewMatrix, 0, 0, 0, 3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        Matrix.multiplyMM(MVPMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        Matrix.scaleM(MVPMatrix, 0, scale, scale, 0f);
        Matrix.translateM(MVPMatrix, 0, x, y, 0);

        // Drawing
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        renderCalls = 0;
        renderLayer(markers, newMarkers, temporaryMarker);
        renderLayer(pens, newPens, temporaryPen);
        //Log.d("OpenGLRenderer","render calls this frame: " + renderCalls);
    }

    private void renderLayer(HashMap<Long, DrawableRenderer> drawables, Queue<Drawable> newDrawables, Drawable temporaryDrawable) {
        while (!newDrawables.isEmpty()) {
            try {
                Drawable newDrawable = newDrawables.remove();
                drawables.put(newDrawable.getID(), compileNewDrawable(newDrawable));
            } catch (UnknownDrawableTypeException e) {
                Log.e("OpenGLRenderer", e.getMessage());
            }
        }

        for (ListIterator<Long> rev = revokedIDs.listIterator(); rev.hasNext();) {
            long ID = rev.next();
            if (drawables.get(ID) != null) {
                rev.remove();
                drawables.remove(ID);
            }
        }

        Set<Map.Entry<Long, DrawableRenderer>> iterableRenderers = drawables.entrySet();
        for (Iterator<Map.Entry<Long, DrawableRenderer>> it = iterableRenderers.iterator(); it.hasNext();) {
            renderCalls++;
            DrawableRenderer drawable = it.next().getValue();

            float[] scratch = new float[16];
            Matrix.multiplyMM(scratch, 0, MVPMatrix, 0, drawable.getModelMatrix(), 0);

            drawable.draw(scratch);
        }

        if (temporaryDrawable != null) {
            try {
                DrawableRenderer drawable = compileNewDrawable(temporaryDrawable);
                float[] scratch = new float[16];
                Matrix.multiplyMM(scratch, 0, MVPMatrix, 0, drawable.getModelMatrix(), 0);
                drawable.draw(scratch);
                drawable.delete();
            } catch (UnknownDrawableTypeException e) {
                Log.e("OpenGLRenderer", e.getMessage());
            }
        }
    }

    private DrawableRenderer compileNewDrawable(Drawable model) throws UnknownDrawableTypeException {
        switch(model.getDrawableType()) {
            case TRIANGLE:
                Triangle t = (Triangle)model;
                return new TriangleRenderer(model.getID(), t.getX(), t.getY());
            case PEN:
                return new PenRenderer(model.getID(), (Stroke)model);
            case MARKER:
                return new MarkerRenderer(model.getID(), (Stroke)model);
        }
        throw new UnknownDrawableTypeException(model.getDrawableType());
    }

    public class UnknownDrawableTypeException extends Exception {
        public UnknownDrawableTypeException(Drawable.drawableType type) {
            super("Drawable type " + type + " is not recognized by the renderer.");
        }
    }

    private float calculateCurrentVelocity(float initialVelocity, float secondsElapsed) {
        float currentVelocity = (-(float)Math.log(secondsElapsed * 3f + 1f) + 1f) * initialVelocity;
        if ((initialVelocity > 0f) == (currentVelocity > 0f)) return currentVelocity;
        if (onPanStopListener != null) onPanStopListener.onPanStop();
        return 0f;
    }

    public interface OnPanStopListener { void onPanStop(); }
    private OnPanStopListener onPanStopListener;
    public void setOnPanStopListener(OnPanStopListener onPanStopListener) { this.onPanStopListener = onPanStopListener; }

    //
    // External Updates
    //

    public void onZoomChange(float z) {
        scale = Math.max(.05f, Math.min(10f, scale * z));
    }

    public void onPanChange(float x, float y) {
        this.x += x;
        this.y += y;
    }

    public void onVelocityChange(float x, float y) {
        initialVelocityX = x * 50f;
        initialVelocityY = y * 50f;

        if (
            (Math.abs(initialVelocityX) * scale >= 0.3f || initialVelocityX == 0f)
            &&
            (Math.abs(initialVelocityY) * scale >= 0.3f || initialVelocityY == 0f)
        ) {
            velocityX = initialVelocityX;
            velocityY = initialVelocityY;
            velocityStartTime = SystemClock.elapsedRealtimeNanos();
        }
        else {
            onPanStopListener.onPanStop();
        }
    }

    public void addDrawable(Drawable drawable) {
        switch(drawable.getDrawableType()) {
            case PEN:
                newPens.add(drawable);
                temporaryPen = null;
                break;
            case MARKER:
                newMarkers.add(drawable);
                temporaryMarker = null;
                break;
        }
    }

    public void revokeDrawable(long ID) {
        revokedIDs.add(ID);
    }

    public void setTemporaryDrawable(Drawable drawable) {
        switch(drawable.getDrawableType()) {
            case PEN:
                temporaryPen = drawable;
                break;
            case MARKER:
                temporaryMarker = drawable;
                break;
        }
    }
}
