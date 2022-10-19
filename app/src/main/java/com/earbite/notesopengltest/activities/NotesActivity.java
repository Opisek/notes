package com.earbite.notesopengltest.activities;

import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.earbite.notesopengltest.R;
import com.earbite.notesopengltest.viewmodels.NotesViewModel;
import com.earbite.notesopengltest.views.NotesView;

import java.util.HashMap;
import java.util.Map;

public class NotesActivity extends AppCompatActivity {

    private NotesViewModel viewModel;
    private NotesView notesView;

    private View penButton;
    private View eraserButton;
    private View panButton;
    private View markerButton;

    private Map<NotesViewModel.ToolType, View> buttons;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);
        viewModel = ViewModelProviders.of(this).get(NotesViewModel.class);

        notesView = findViewById(R.id.notes_renderer);
        notesView.setOnMoveListener(new NotesView.OnMoveListener() {
            @Override
            public void onMoveEvent(MotionEvent e) {
                viewModel.onOpenGLMoveEvent(e);
            }
            @Override
            public void onZoomEvent(ScaleGestureDetector d) {
                viewModel.onOpenGLGestureEvent(d);
            }
        });
        viewModel.setOpenGLInterface(notesView);

        buttons = new HashMap<>();

        penButton = findViewById(R.id.notes_button_pen);
        buttons.put(NotesViewModel.ToolType.PEN, penButton);
        penButton.setOnTouchListener(new View.OnTouchListener() {
            private boolean isClicked = false;
            @Override
            public boolean onTouch(View view, MotionEvent e) {
                isClicked = onButtonTouch(view, e, isClicked, new buttonTouchCallback() {
                    @Override
                    public void onClick(MotionEvent e) {
                        onRadioButtonClick(NotesViewModel.ToolType.PEN, e);
                    }
                });
                return true;
            }
        });

        eraserButton = findViewById(R.id.notes_button_eraser);
        buttons.put(NotesViewModel.ToolType.ERASER, eraserButton);
        eraserButton.setOnTouchListener(new View.OnTouchListener() {
            private boolean isClicked = false;
            @Override
            public boolean onTouch(View view, MotionEvent e) {
                isClicked = onButtonTouch(view, e, isClicked, new buttonTouchCallback() {
                    @Override
                    public void onClick(MotionEvent e) {
                        onRadioButtonClick(NotesViewModel.ToolType.ERASER, e);
                    }
                });
                return true;
            }
        });

        panButton = findViewById(R.id.notes_button_panning);
        buttons.put(NotesViewModel.ToolType.PAN, panButton);
        panButton.setOnTouchListener(new View.OnTouchListener() {
            private boolean isClicked = false;
            @Override
            public boolean onTouch(View view, MotionEvent e) {
                isClicked = onButtonTouch(view, e, isClicked, new buttonTouchCallback() {
                    @Override
                    public void onClick(MotionEvent e) {
                        onRadioButtonClick(NotesViewModel.ToolType.PAN, e);
                    }
                });
                return true;
            }
        });

        markerButton = findViewById(R.id.notes_button_marker);
        buttons.put(NotesViewModel.ToolType.MARKER, markerButton);
        markerButton.setOnTouchListener(new View.OnTouchListener() {
            private boolean isClicked = false;
            @Override
            public boolean onTouch(View view, MotionEvent e) {
                isClicked = onButtonTouch(view, e, isClicked, new buttonTouchCallback() {
                    @Override
                    public void onClick(MotionEvent e) {
                        onRadioButtonClick(NotesViewModel.ToolType.MARKER, e);
                    }
                });
                return true;
            }
        });

        findViewById(R.id.notes_button_undo).setOnTouchListener(new View.OnTouchListener() {
            private boolean isClicked = false;
            @Override
            public boolean onTouch(View view, MotionEvent e) {
                isClicked = onButtonTouch(view, e, isClicked, new buttonTouchCallback() {
                    @Override
                    public void onClick(MotionEvent e) {
                        viewModel.onUndoClick();
                    }
                });
                return true;
            }
        });

        findViewById(R.id.notes_button_redo).setOnTouchListener(new View.OnTouchListener() {
            private boolean isClicked = false;
            @Override
            public boolean onTouch(View view, MotionEvent e) {
                isClicked = onButtonTouch(view, e, isClicked, new buttonTouchCallback() {
                    @Override
                    public void onClick(MotionEvent e) {
                        viewModel.onRedoClick();
                    }
                });
                return true;
            }
        });

        viewModel.getToolType().observe(this, new Observer<NotesViewModel.ToolType>() {
            @Override
            public void onChanged(NotesViewModel.ToolType toolType) {
                toolChange(toolType);
            }
        });
    }

    // Buttons

    private interface buttonTouchCallback {
        void onClick(MotionEvent e);
    }

    private boolean onButtonTouch(View view, MotionEvent e, boolean isClicked, buttonTouchCallback callback) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_UP:
                Rect outRect = new Rect();
                int[] location = new int[2];
                view.getDrawingRect(outRect);
                view.getLocationOnScreen(location);
                outRect.offset(location[0], location[1]);
                if (outRect.contains((int)e.getRawX(), (int)e.getRawY())) callback.onClick(e);
                return true;
        }
        return isClicked;
    }

    private void onRadioButtonClick(NotesViewModel.ToolType toolType, MotionEvent e) {
        viewModel.onRadioClick(toolType, e.getToolType(0) == e.TOOL_TYPE_STYLUS);
    }

    // Misc

    private View activeButton;
    private void toolChange(NotesViewModel.ToolType toolType) {
        if (activeButton != null) activeButton.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        activeButton = buttons.get(toolType);
        if (activeButton != null) activeButton.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
    }

    @Override
    protected void onResume() {
        super.onResume();
        notesView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        notesView.onPause();
    }
}
