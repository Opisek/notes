package com.earbite.notesopengltest.actions;

import com.earbite.notesopengltest.drawables.Drawable;
import com.earbite.notesopengltest.repositories.DrawablesRepository;

public class DrawableRemovedAction extends Action {
    private Drawable drawable;

    public DrawableRemovedAction(Drawable drawable) {
        this.drawable = drawable;
    }

    @Override
    public void undo() {
        DrawablesRepository.getInstance().addNewDrawable(drawable, false);
    }

    @Override
    public void redo() {
        DrawablesRepository.getInstance().revokeDrawable(drawable, false);
    }
}
