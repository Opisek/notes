package com.earbite.notesopengltest.actions;

import com.earbite.notesopengltest.drawables.Drawable;
import com.earbite.notesopengltest.repositories.DrawablesRepository;

public class DrawableCreatedAction extends Action {
    private Drawable drawable;

    public DrawableCreatedAction(Drawable drawable) {
        this.drawable = drawable;
    }

    @Override
    public void undo() {
        DrawablesRepository.getInstance().revokeDrawable(drawable, false);
    }

    @Override
    public void redo() {
        DrawablesRepository.getInstance().addNewDrawable(drawable, false);
    }
}
