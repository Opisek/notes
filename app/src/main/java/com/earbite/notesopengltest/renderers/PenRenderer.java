package com.earbite.notesopengltest.renderers;

import com.earbite.notesopengltest.drawables.Stroke;
import com.earbite.notesopengltest.drawables.Vertex;

public class PenRenderer extends DrawableRenderer {
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

    public PenRenderer(long ID, Stroke stroke) {
        super(ID, stroke.getX(), stroke.getY());

        vertexCount = (stroke.getVertexCount()-1)*4;
        triangleCount = ((stroke.getVertexCount()-1)+(Math.max(2,stroke.getVertexCount())-2))*6;

        float vertices[] = new float[vertexCount*COORDS_PER_VERTEX];
        short drawOrder[] = new short[triangleCount];

        int j = 0;
        short k = 0;
        Vertex lastVertex = stroke.getVertices()[0];
        float clearanceLength = 0f;
        for (int i = 1; i < stroke.getVertexCount(); i++) {
            Vertex currentVertex = stroke.getVertices()[i];

            double strokeAngle = Math.atan2(currentVertex.getY() - lastVertex.getY(), currentVertex.getX() - lastVertex.getX());
            double tangentAngle = Math.PI / 2d - strokeAngle;

            float thicknessX = (float)Math.cos(tangentAngle) * lastVertex.getThickness() / 100f / stroke.getZoom();
            float thicknessY = (float)Math.sin(tangentAngle) * lastVertex.getThickness() / 100f / stroke.getZoom();

            float clearanceX = 0f;
            float clearanceY = 0f;
            /*if (i > 1) {
                clearanceX = (float)Math.cos(strokeAngle) * clearanceLength;
                clearanceY = (float)Math.sin(strokeAngle) * clearanceLength;
            }*/

            vertices[j++] = lastVertex.getX() + clearanceX - thicknessX;
            vertices[j++] = lastVertex.getY() + clearanceY + thicknessY;
            vertices[j++] = 0f;

            vertices[j++] = lastVertex.getX() + clearanceX + thicknessX;
            vertices[j++] = lastVertex.getY() + clearanceY - thicknessY;
            vertices[j++] = 0f;

            thicknessX = (float)Math.cos(tangentAngle) * currentVertex.getThickness() / 100f / stroke.getZoom();
            thicknessY = (float)Math.sin(tangentAngle) * currentVertex.getThickness() / 100f / stroke.getZoom();

            clearanceX = 0f;
            clearanceY = 0f;
            /*if (i < stroke.getVertexCount()-1) {
                Vertex nextVertex = stroke.getVertices()[i+1];
                double nextStrokeAngle = Math.atan2(nextVertex.getY() - currentVertex.getY(), nextVertex.getX() - currentVertex.getX());

                double angleDifference = strokeAngle - nextStrokeAngle;
                //if (angleDifference > Math.PI) angleDifference = 2d * Math.PI - angleDifference;
                //else if (angleDifference < Math.PI) angleDifference = -2d * Math.PI + angleDifference;
                angleDifference /= 2;
                if (strokeAngle < nextStrokeAngle) angleDifference *= -1;
                //clearanceLength = (float)(Math.sin(Math.PI/2d-angleDifference) * currentVertex.getThickness()/2d/stroke.getZoom() / angleDifference);
                clearanceX = (float)Math.cos(strokeAngle) * clearanceLength;
                clearanceY = (float)Math.sin(strokeAngle) * clearanceLength;
            }*/

            vertices[j++] = currentVertex.getX() - clearanceX - thicknessX;
            vertices[j++] = currentVertex.getY() - clearanceY + thicknessY;
            vertices[j++] = 0f;

            vertices[j++] = currentVertex.getX() - clearanceX + thicknessX;
            vertices[j++] = currentVertex.getY() - clearanceY - thicknessY;
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

        init(vertices, drawOrder, vertexShaderCode, fragmentShaderCode, new float[]{ 0f, 0f, 0f, 1.0f});
    }
}
