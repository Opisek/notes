package com.earbite.notesopengltest.renderers;

import android.opengl.GLES20;

public class TriangleRenderer extends DrawableRenderer {
    private final float vertices[] = {
        0.0f,  (float)Math.sqrt(2d) * 0.5f * .5f, 0.0f,
        -0.5f, (float)Math.sqrt(2d) * 0.5f * -.5f, 0.0f,
        0.5f, (float)Math.sqrt(2d) * 0.5f * -.5f, 0.0f
    };

    private final String vertexShaderCode =
        "uniform mat4 uMVPMatrix;" +
        "attribute vec4 vPosition;" +
        "void main() {" +
        "  gl_Position = uMVPMatrix * vPosition;" +
        "}";

    private final String fragmentShaderCode =
        "precision mediump float;" +
        "uniform vec4 vColor;" +
        "void main() {" +
        "  gl_FragColor = vColor;" +
        "}";

    public TriangleRenderer(long ID, float x, float y) {
        super(ID, x, y);
        init(vertices, new short[]{0,1,2}, vertexShaderCode, fragmentShaderCode, new float[]{ 0.25f, 0.34f, 0.72f, 1.0f});
    }
}
