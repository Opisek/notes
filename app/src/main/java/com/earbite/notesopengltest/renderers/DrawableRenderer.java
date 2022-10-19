package com.earbite.notesopengltest.renderers;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.earbite.notesopengltest.renderers.OpenGLRenderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public abstract class DrawableRenderer {
    public final long ID;

    public final int COORDS_PER_VERTEX = 3;
    public final int vertexStride = COORDS_PER_VERTEX * 4;

    public float color[];

    public float[] modelMatrix;
    public float[] getModelMatrix() { return modelMatrix; }

    public int vertexCount;
    public int triangleCount;

    private int buffers[] = new int[2];
    public FloatBuffer vertexBuffer;
    public ShortBuffer drawListBuffer;
    public int mProgram;

    private int vertexShader;
    private int fragmentShader;

    public void init(float[] vertices, short[] drawOrder, String vertexShaderCode, String fragmentShaderCode, float[] color) {


        GLES20.glGenBuffers(buffers.length, buffers, 0);

        vertexBuffer = (FloatBuffer)ByteBuffer
                .allocateDirect(vertices.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertices)
                .position(0);

        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertices.length * 4, vertexBuffer, GLES20.GL_STATIC_DRAW);

        drawListBuffer = (ShortBuffer)ByteBuffer
            .allocateDirect(drawOrder.length * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
            .put(drawOrder)
            .position(0);

        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, drawOrder.length * 2, drawListBuffer, GLES20.GL_STATIC_DRAW);

        vertexShader = OpenGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        fragmentShader = OpenGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        this.color = color;

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);
    }

    public DrawableRenderer(long ID, float x, float y) {
        this.ID = ID;
        modelMatrix = new float[16];
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, x, y ,0);
    }

    public void draw(float[] mvpMatrix) {
        GLES20.glUseProgram(mProgram);

        int positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

        int colorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        GLES20.glUniform4fv(colorHandle, 1, color, 0);

        int vPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, mvpMatrix, 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, triangleCount, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
        GLES20.glDisableVertexAttribArray(positionHandle);
    }

    public void delete() {
        GLES20.glDeleteBuffers(2, buffers, 0);
        GLES20.glDeleteProgram(mProgram);
        GLES20.glDeleteShader(vertexShader);
        GLES20.glDeleteShader(fragmentShader);
        //Log.v("DrawableRenderer","deleted");
    }
}
