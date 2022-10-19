package com.earbite.notesopengltest.views;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import androidx.annotation.Nullable;

import com.earbite.notesopengltest.drawables.Drawable;
import com.earbite.notesopengltest.renderers.OpenGLRenderer;
import com.earbite.notesopengltest.viewmodels.NotesViewModel;

public class NotesView extends GLSurfaceView implements NotesViewModel.OpenGLInterface {

    private OpenGLRenderer renderer;

    public NotesView(Context context, @Nullable AttributeSet attributeSet) {
        super(context, attributeSet);

        setEGLContextClientVersion(2);
        setPreserveEGLContextOnPause(true);

        renderer = new OpenGLRenderer();
        setRenderer(renderer);

        onRenderModeChange(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        renderer.setOnPanStopListener(new OpenGLRenderer.OnPanStopListener() {
            @Override
            public void onPanStop() {
                onRenderModeChange(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
            }
        });

        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    private ScaleGestureDetector scaleGestureDetector;
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            listener.onZoomEvent(detector);
            invalidate();
            return true;
        }
    }

    private OnMoveListener listener;
    public interface OnMoveListener {
        void onMoveEvent(MotionEvent e);
        void onZoomEvent(ScaleGestureDetector e);
    }
    public void setOnMoveListener(OnMoveListener listener) { this.listener = listener; }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        scaleGestureDetector.onTouchEvent(e);
        listener.onMoveEvent(e);
        return true;
    }

    @Override
    public boolean onHoverEvent(MotionEvent e) {
        listener.onMoveEvent(e);
        return true;
    }

    @Override
    public void onZoomChange(float z) {
        renderer.onZoomChange(z);
    }

    @Override
    public void onPanChange(float x, float y) {
        renderer.onPanChange(x, y);
    }

    @Override
    public void onVelocityChange(float x, float y) {
        renderer.onVelocityChange(x, y);
    }

    @Override
    public void onRenderModeChange(int mode) {
        setRenderMode(mode);
        Log.v("NotesView", "new render mode: " + mode);
    }

    @Override
    public void onRenderRequest() {
        requestRender();
    }

    @Override
    public float getZoom() { return renderer.getZoom(); }

    @Override
    public float getViewX(float x) {
        return renderer.getViewX(x);
    }

    @Override
    public float getViewY(float y) {
        return renderer.getViewY(y);
    }

    @Override
    public float getWorldX(float x) {
        return renderer.getWorldX(x);
    }

    @Override
    public float getWorldY(float y) {
        return renderer.getWorldY(y);
    }

    @Override
    public void TEMP_newDrawable(Drawable drawable) {
        renderer.addDrawable(drawable);
    }

    @Override
    public void onDrawableRevoke(long ID) {
        renderer.revokeDrawable(ID);
    }

    @Override
    public void onTemporaryDrawable(Drawable drawable) {
        renderer.setTemporaryDrawable(drawable);
    }
}
