package com.earbite.notesopengltest.viewmodels;

import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.earbite.notesopengltest.drawables.Drawable;
import com.earbite.notesopengltest.drawables.Marker;
import com.earbite.notesopengltest.drawables.Pen;
import com.earbite.notesopengltest.drawables.Vertex;
import com.earbite.notesopengltest.models.Chunk;
import com.earbite.notesopengltest.models.Coordinate;
import com.earbite.notesopengltest.repositories.DrawablesRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NotesViewModel extends ViewModel {

    private DrawablesRepository drawablesRepository;

    public NotesViewModel() {
        toolType = new MutableLiveData<>();
        setToolType(ToolType.PAN, false, true);
        currentTool = ToolType.PAN;
        lastFingerTool = ToolType.PAN;
        lastStylusTool = ToolType.PEN;

        anyDown = false;
        stylusHover = false;
        stylusHoverDelayed = false;
        stylusDown = false;
        previousPointerCount = 0;
        lastButton = false;

        drawablesRepository = DrawablesRepository.getInstance();

        drawablesRepository.SetOnDrawableChangesListener(new DrawablesRepository.OnDrawableChangeListener() {
            @Override
            public void onNewDrawable(Drawable drawable) {
                openGLInterface.TEMP_newDrawable(drawable);
                openGLInterface.onRenderRequest();
            }
            @Override
            public void onRevokeDrawable(long ID) {
                openGLInterface.onDrawableRevoke(ID);
            }
            @Override
            public void onRevokeDone() {
                openGLInterface.onRenderRequest();
            }
        });
    }

    // // // // // // // // // // // // // // // // // // // // // // // // // // //
    //
    //  TOOL BUTTONS
    //
    // // // // // // // // // // // // // // // // // // // // // // // // // // //

    public enum ToolType {
        PEN,
        ERASER,
        PAN,
        MARKER
    }
    private final Set<ToolType> stylusTools = new HashSet<>(Arrays.asList(
            ToolType.PEN,
            ToolType.ERASER,
            ToolType.MARKER
    ));
    private MutableLiveData<ToolType> toolType;
    private ToolType currentTool;
    private ToolType lastFingerTool;
    private ToolType lastStylusTool;

    private void setToolType(ToolType newTool, boolean isStylus, boolean isAutomatic) {
        if (!isAutomatic) {
            if (stylusTools.contains(newTool)) lastStylusTool = newTool;
            if (!isStylus) lastFingerTool = newTool;
        }
        if (newTool == currentTool) return;
        currentTool = newTool;
        toolType.setValue(newTool);
    }
    private void postToolType(ToolType newTool) {
        if (newTool == currentTool) return;
        currentTool = newTool;
        toolType.postValue(newTool);
    }
    public MutableLiveData<ToolType> getToolType() {return toolType;}
    public void onRadioClick(ToolType toolType, boolean isStylus) {
        if (
                toolType != this.toolType.getValue() &&
                !stylusHover &&
                !anyDown
        ) setToolType(toolType, isStylus, false);
    }

    // // // // // // // // // // // // // // // // // // // // // // // // // // //
    //
    //  ACTION BUTTONS
    //
    // // // // // // // // // // // // // // // // // // // // // // // // // // //

    public void onUndoClick() {
        if (!stylusHover && !anyDown) DrawablesRepository.getInstance().undoAction();
    }
    public void onRedoClick() {
        if (!stylusHover && !anyDown) DrawablesRepository.getInstance().redoAction();
    }

    // // // // // // // // // // // // // // // // // // // // // // // // // // //
    //
    //  OPEN GL INTERFACE
    //
    // // // // // // // // // // // // // // // // // // // // // // // // // // //

    private OpenGLInterface openGLInterface;
    public interface OpenGLInterface { // maybe this should be mutable live data?
        void onZoomChange(float z);
        void onPanChange(float x, float y);
        void onVelocityChange(float x, float y);
        void onRenderModeChange(int mode);
        void onRenderRequest();

        float getViewX(float x);
        float getViewY(float y);
        float getWorldX(float x);
        float getWorldY(float y);

        float getZoom();

        void TEMP_newDrawable(Drawable drawable);
        void onDrawableRevoke(long ID);
        void onTemporaryDrawable(Drawable drawable);
    }
    public void setOpenGLInterface(OpenGLInterface listener) {
        openGLInterface = listener;
        // ALL OF THIS IS BASICALLY TEMPORARY LOL
        HashMap<Long, Drawable> existingDrawables = new HashMap<>();

        for (Iterator<Map.Entry<Coordinate, Chunk>> iterator = drawablesRepository.getChunks().entrySet().iterator(); iterator.hasNext();)
            existingDrawables.putAll(iterator.next().getValue().drawables);

        for (Iterator<Map.Entry<Long, Drawable>> iterator = existingDrawables.entrySet().iterator(); iterator.hasNext();)
            openGLInterface.TEMP_newDrawable(iterator.next().getValue());

        openGLInterface.onRenderRequest();
    }

    // // // // // // // // // // // // // // // // // // // // // // // // // // //
    //
    // EVENT
    //
    // // // // // // // // // // // // // // // // // // // // // // // // // // //

    private float previousX;
    private float previousY;
    private float previousPointerCount;
    private boolean lastButton;

    public void onOpenGLMoveEvent(MotionEvent e) {
        if (openGLInterface == null) return;

        float x = e.getX();
        float y = e.getY();

        if (e.getPointerCount() != previousPointerCount) {
            previousPointerCount = e.getPointerCount();
            previousX = x;
            previousY = y;
        }

        boolean isStylus = e.getToolType(0) == e.TOOL_TYPE_STYLUS;
        boolean isButton =  e.getButtonState() == MotionEvent.BUTTON_STYLUS_PRIMARY;
        if (isButton != lastButton && !anyDown) { // && !anyDown prevents the button to take effect on a stroke that's already being drawn
            lastButton = isButton;
            if (isStylus) onButtonStateChange(isButton, x, y, e);
        }

        switch (e.getAction()) {
            case MotionEvent.ACTION_HOVER_ENTER:
                if (isStylus) onHoverDown();  break;

            case MotionEvent.ACTION_HOVER_MOVE:
                if (isStylus) onHoverMove();  break;

            case MotionEvent.ACTION_HOVER_EXIT:
                if (isStylus) onHoverUp();    break;

            case MotionEvent.ACTION_DOWN:
            case 211:
                onAnyDown(x, y, isStylus, currentTool, e);
                break;

            case MotionEvent.ACTION_MOVE:
            case 213:
                onAnyMove(currentTool, x, y, e);
                break;

            case MotionEvent.ACTION_UP:
            case 212:
                onAnyUp(isStylus, currentTool);
                break;
        }

        previousX = x;
        previousY = y;
    }

    public void onOpenGLGestureEvent(ScaleGestureDetector detector) {
        if (openGLInterface == null) return;
        if (currentTool != ToolType.PAN) return;
        float oldX = openGLInterface.getWorldX(detector.getFocusX());
        float oldY = openGLInterface.getWorldY(detector.getFocusY());
        openGLInterface.onZoomChange(detector.getScaleFactor());
        openGLInterface.onPanChange(openGLInterface.getWorldX(detector.getFocusX())-oldX, openGLInterface.getWorldY(detector.getFocusY())-oldY);
        openGLInterface.onRenderRequest();
    }

    private Queue<Float> previousDeltaX;
    private Queue<Float> previousDeltaY;

    private boolean anyDown;
    private boolean stylusDown;

    private boolean stylusHover;
    private boolean stylusHoverDelayed;

    private boolean buttonDown;

    private float firstX;
    private float firstY;
    private List<Vertex> vertices;
    private void addNewVertex(float x, float y, float thickness) {
        vertices.add(new Vertex(openGLInterface.getWorldX(x) - firstX, openGLInterface.getWorldY(y) - firstY, thickness));
    }

    // // // // // // // // // // // // // // // // // // // // // // // // // // //
    //
    // DOWN
    //
    // // // // // // // // // // // // // // // // // // // // // // // // // // //

    private void onAnyDown(float x, float y, boolean isStylus, ToolType currentTool, MotionEvent e) {
        if (isStylus) onStylusDown();
        switch (currentTool) {
            case MARKER:
            case PEN:       onStrokeDown(x, y, e.getPressure());   break;
            case ERASER:    onEraserDown(x, y);                    break;
            case PAN:       onPanDown();                           break;
        }

        anyDown = true;
        previousX = x;
        previousY = y;
        previousDeltaX = new LinkedList<>();
        previousDeltaY = new LinkedList<>();
        openGLInterface.onVelocityChange(0f, 0f);
        openGLInterface.onRenderModeChange(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }
    private void onStylusDown() {
        stylusDown = true;
        if (!stylusHoverDelayed) {
            stylusHoverDelayed = true;
            setAppropriateStylusTool();
        }
    }
    private void onHoverDown() {
        stylusHover = true;
        stylusHoverDelayed = true;
        setAppropriateStylusTool();
    }
    private void onStrokeDown(float x, float y, float thickness) {
        vertices = new ArrayList<>();
        firstX = openGLInterface.getWorldX(x);
        firstY = openGLInterface.getWorldY(y);
        addNewVertex(x, y, thickness);
    }
    private void onEraserDown(float x, float y) {
        drawablesRepository.beginErasing();
        drawablesRepository.eraseDrawables(openGLInterface.getWorldX(x), openGLInterface.getWorldY(y));
        openGLInterface.onRenderRequest();
    }
    private void onPanDown() {

    }

    // // // // // // // // // // // // // // // // // // // // // // // // // // //
    //
    // MOVE
    //
    // // // // // // // // // // // // // // // // // // // // // // // // // // //

    private void onAnyMove(ToolType currentTool, float x, float y, MotionEvent e) {
        switch (currentTool) {
            case MARKER:
            case PEN:       onStrokeMove(x, y, e);  break;
            case ERASER:    onEraserMove(x, y);     break;
            case PAN:       onPanMove(x, y);        break;
        }
    }
    private void onStrokeMove(float x, float y, MotionEvent e) {
        for (int i = 0; i < e.getHistorySize(); i++) addNewVertex(e.getHistoricalX(i), e.getHistoricalY(i), e.getHistoricalPressure(i));
        addNewVertex(x, y, e.getPressure());
        switch(currentTool) {
            case PEN:
                openGLInterface.onTemporaryDrawable(new Pen(firstX, firstY, openGLInterface.getZoom(), vertices.toArray(new Vertex[vertices.size()])));
                break;
            case MARKER:
                openGLInterface.onTemporaryDrawable(new Marker(firstX, firstY, openGLInterface.getZoom(), vertices.toArray(new Vertex[vertices.size()])));
                break;
        }
        openGLInterface.onRenderRequest();
    }
    private void onEraserMove(float x, float y) {
        drawablesRepository.eraseDrawables(openGLInterface.getWorldX(x), openGLInterface.getWorldY(y));
        openGLInterface.onRenderRequest();
    }
    private void onPanMove(float x, float y) {
        float deltaX = openGLInterface.getViewX(x) - openGLInterface.getViewX(previousX);
        previousDeltaX.add(deltaX);
        if (previousDeltaX.size() > 5) previousDeltaX.remove();

        float deltaY = openGLInterface.getViewY(y) - openGLInterface.getViewY(previousY);
        previousDeltaY.add(deltaY);
        if (previousDeltaY.size() > 5) previousDeltaY.remove();

        openGLInterface.onPanChange(deltaX, deltaY);
        openGLInterface.onRenderRequest();
    }
    private void onHoverMove() {
    }

    // // // // // // // // // // // // // // // // // // // // // // // // // // //
    //
    // UP
    //
    // // // // // // // // // // // // // // // // // // // // // // // // // // //

    private void onAnyUp(boolean isStylus, ToolType currentTool) {
        if (isStylus) onStylusUp();
        switch (currentTool) {
            case MARKER:
            case PEN:       onStrokeUp();       break;
            case ERASER:    onEraserUp();       break;
            case PAN:       onPanUp();          break;
        }

        anyDown = false;
        //openGLInterface.onTemporaryDrawable(null);
    }
    private void onStylusUp() {
        stylusDown = false;
    }
    private void onStrokeUp() {
        switch(currentTool) {
            case PEN:
                drawablesRepository.addNewDrawable(new Pen(firstX, firstY, openGLInterface.getZoom(), vertices.toArray(new Vertex[vertices.size()])), true);
                break;
            case MARKER:
                drawablesRepository.addNewDrawable(new Marker(firstX, firstY, openGLInterface.getZoom(), vertices.toArray(new Vertex[vertices.size()])), true);
                break;
        }
    }
    private void onHoverUp() {
        stylusHover = false;
        ScheduledExecutorService delayedHoverUp = Executors.newSingleThreadScheduledExecutor();
        delayedHoverUp.schedule(new Runnable() {
            public void run() {
                if (!stylusHover && stylusHoverDelayed && !stylusDown) {
                    stylusHoverDelayed = false;
                    postToolType(lastFingerTool);
                }
            }
        }, 10, TimeUnit.MILLISECONDS);
        delayedHoverUp.shutdown();
    }
    private void onEraserUp() {
        drawablesRepository.stopErasing();
    }
    private void onPanUp() {
        int deltaXCount = previousDeltaX.size();
        float deltaXTotal = 0f;
        while(!previousDeltaX.isEmpty()) deltaXTotal += previousDeltaX.remove();
        if (deltaXCount > 1) deltaXTotal /= deltaXCount;

        int deltaYCount = previousDeltaY.size();
        float deltaYTotal = 0f;
        while(!previousDeltaY.isEmpty()) deltaYTotal += previousDeltaY.remove();
        if (deltaYCount > 1) deltaYTotal /= deltaYCount;

        openGLInterface.onRenderModeChange(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        openGLInterface.onVelocityChange(deltaXTotal, deltaYTotal);
        openGLInterface.onRenderRequest();
    }

    // // // // // // // // // // // // // // // // // // // // // // // // // // //
    //
    // MISC
    //
    // // // // // // // // // // // // // // // // // // // // // // // // // // //

    private void setAppropriateStylusTool() {
        setToolType(buttonDown ? ToolType.ERASER : lastStylusTool, true, true);
    }

    private void onButtonStateChange(boolean isButton, float x, float y, MotionEvent e) {
        buttonDown = isButton;
        //boolean alreadyDown = stylusDown;
        //if (alreadyDown) onAnyUp(true, currentTool);
        setAppropriateStylusTool();
        //if (alreadyDown) onAnyDown(x, y, true, currentTool, e);
    }
}