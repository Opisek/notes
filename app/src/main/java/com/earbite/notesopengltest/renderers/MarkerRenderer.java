package com.earbite.notesopengltest.renderers;

import com.earbite.notesopengltest.drawables.Stroke;
import com.earbite.notesopengltest.drawables.Vertex;

public class MarkerRenderer extends DrawableRenderer {
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

    public MarkerRenderer(long ID, Stroke stroke) {
        super(ID, stroke.getX(), stroke.getY());

        vertexCount = (stroke.getVertexCount()-1)*4;
        triangleCount = ((stroke.getVertexCount()-1)+(Math.max(2,stroke.getVertexCount())-2))*6;

        float vertices[] = new float[vertexCount*COORDS_PER_VERTEX];
        short drawOrder[] = new short[triangleCount];

        int j = 0;
        short k = 0;
        Vertex lastVertex = stroke.getVertices()[0];
        for (int i = 1; i < stroke.getVertexCount(); i++) {
            Vertex currentVertex = stroke.getVertices()[i];

            double strokeAngle = Math.atan2(currentVertex.getY() - lastVertex.getY(), currentVertex.getX() - lastVertex.getX());
            double tangentAngle = Math.PI / 2d - strokeAngle;

            float thicknessX = (float)Math.cos(tangentAngle) * lastVertex.getThickness() / 50f / stroke.getZoom();
            float thicknessY = (float)Math.sin(tangentAngle) * lastVertex.getThickness() / 20f / stroke.getZoom();

            vertices[j++] = lastVertex.getX() - thicknessX;
            vertices[j++] = lastVertex.getY() + thicknessY;
            vertices[j++] = 0f;

            vertices[j++] = lastVertex.getX() + thicknessX;
            vertices[j++] = lastVertex.getY() - thicknessY;
            vertices[j++] = 0f;

            thicknessX = (float)Math.cos(tangentAngle) * currentVertex.getThickness() / 50f / stroke.getZoom();
            thicknessY = (float)Math.sin(tangentAngle) * currentVertex.getThickness() / 20 / stroke.getZoom();

            vertices[j++] = currentVertex.getX() - thicknessX;
            vertices[j++] = currentVertex.getY() + thicknessY;
            vertices[j++] = 0f;

            vertices[j++] = currentVertex.getX() + thicknessX;
            vertices[j++] = currentVertex.getY() - thicknessY;
            vertices[j++] = 0f;

            int l = (i-1)*4;
            drawOrder[k++] = (short)(l);
            drawOrder[k++] = (short)(l+1);
            drawOrder[k++] = (short)(l+2);
            drawOrder[k++] = (short)(l+1);
            drawOrder[k++] = (short)(l+2);
            drawOrder[k++] = (short)(l+3);

            if (i > 1) {
                drawOrder[k++] = (short)(l);
                drawOrder[k++] = (short)(l-1);
                drawOrder[k++] = (short)(l-2);
                drawOrder[k++] = (short)(l+1);
                drawOrder[k++] = (short)(l-1);
                drawOrder[k++] = (short)(l-2);
            }

            lastVertex = currentVertex;
        }

        init(vertices, drawOrder, vertexShaderCode, fragmentShaderCode, new float[]{ 1f, 0.85f, 0f, .5f});
    }
}
