package com.bennyplo.designgraphicswithopengl;

import android.opengl.GLES32;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

public class CharacterS {
    private final String vertexShaderCode =
            "attribute vec3 aVertexPosition;"+//vertex of an object
                    " attribute vec4 aVertexColor;"+//the colour  of the object
                    "     uniform mat4 uMVPMatrix;"+//model view  projection matrix
                    "    varying vec4 vColor;"+//variable to be accessed by the fragment shader
                    "void main() {" +
                    "        gl_Position = uMVPMatrix* vec4(aVertexPosition, 1.0);"+//calculate the position of the vertex
                    "        vColor=aVertexColor;}";//get the colour from the application program

    private final String fragmentShaderCode =
            //"precision mediump float;"+ //define the precision of float
            "precision lowp float;"+ //need to set to low in order to show the depth map
                    "varying vec4 vColor;"+ //variable from the vertex shader
                    "void main() {"+
                    "gl_FragColor = vColor;"+
                    "}";
    private final FloatBuffer vertexBuffer,colorBuffer;
    private final IntBuffer indexBuffer;
    private final int mProgram;
    private int mPositionHandle,mColorHandle;
    private int mMVPMatrixHandle;
    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static final int COLOR_PER_VERTEX = 4;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    private final int colorStride=COLOR_PER_VERTEX*4;//4 bytes per vertex
    static float CharSVertex[];
    static int CharSIndex[];
    static float CharSColor[];

    private  void createCurve(float controlpts_right[],float controlpts_left[]) {
        float vertices[] = new float[65535];
        float color[] = new float[65535];
        int pindex[] = new int[65535];
        int vertexindex = 0;
        int colorindex = 0;
        int indx = 0;
        int controlptindex=0;
        int nosegments=(controlpts_right.length/2)/3;
        double t,x,y,xl,yl,z;
        z=0.3;
        double centrex=0,centrey=0;
        for (int i=0;i<controlpts_right.length;i+=2)
        {
            centrex+=controlpts_right[i];
            centrey+=controlpts_right[i+1];
        }
        centrex/=(float)(controlpts_right.length/2.0);
        centrey/=(float)(controlpts_right.length/2.0);
        int v0,v1,v2,v3,v4,v5,v6,v7;
        for (int segment=0;segment<nosegments;segment++) {
            for (t = 0; t < 1.0; t += 0.1) {
                x = Math.pow(1.0 - t, 3) * controlpts_right[controlptindex] + controlpts_right[controlptindex + 2] * 3 * t * Math.pow(1 - t, 2) + controlpts_right[controlptindex + 4] * 3 * t * t * (1 - t) + controlpts_right[controlptindex + 6] * Math.pow(t, 3);
                y = Math.pow(1.0 - t, 3) * controlpts_right[controlptindex + 1] + controlpts_right[controlptindex + 3] * 3 * t * Math.pow(1 - t, 2) + controlpts_right[controlptindex + 5] * 3 * t * t * (1 - t) + controlpts_right[controlptindex + 7] * Math.pow(t, 3);
                xl = Math.pow(1.0 - t, 3) * controlpts_left[controlptindex] + controlpts_left[controlptindex + 2] * 3 * t * Math.pow(1 - t, 2) + controlpts_left[controlptindex + 4] * 3 * t * t * (1 - t) + controlpts_left[controlptindex + 6] * Math.pow(t, 3);
                yl = Math.pow(1.0 - t, 3) * controlpts_left[controlptindex + 1] + controlpts_left[controlptindex + 3] * 3 * t * Math.pow(1 - t, 2) + controlpts_left[controlptindex + 5] * 3 * t * t * (1 - t) + controlpts_left[controlptindex + 7] * Math.pow(t, 3);
                vertices[vertexindex++] = (float) (x-centrex);
                vertices[vertexindex++] = (float) (y-centrey);
                vertices[vertexindex++] = (float) (z);
                vertices[vertexindex++] = (float) (xl-centrex);
                vertices[vertexindex++] = (float) (yl-centrey);
                vertices[vertexindex++] = (float) (z);
                vertices[vertexindex++] = (float) (x-centrex);
                vertices[vertexindex++] = (float) (y-centrey);
                vertices[vertexindex++] = (float) (-z);
                vertices[vertexindex++] = (float) (xl-centrex);
                vertices[vertexindex++] = (float) (yl-centrey);
                vertices[vertexindex++] = (float) (-z);
                color[colorindex++] = 1; color[colorindex++] = 1; color[colorindex++] = 0;  color[colorindex++] = 1;
                color[colorindex++] = 1; color[colorindex++] = 1; color[colorindex++] = 0;  color[colorindex++] = 1;
                color[colorindex++] = 1f; color[colorindex++] = 0f; color[colorindex++] = 0;  color[colorindex++] = 1;
                color[colorindex++] = 1f; color[colorindex++] = 0f; color[colorindex++] = 0;  color[colorindex++] = 1;
            }
            controlptindex+=6;
        }
        for (v0=0,v1=1,v2=4,v3=5,v4=2,v5=3,v6=6,v7=7;v7<vertexindex/3;v0+=4,v1+=4,v2+=4,v3+=4,v4+=4,v5+=4,v6+=4,v7+=4)
        {   //the front
            pindex[indx++]=v0;
            pindex[indx++]=v1;
            pindex[indx++]=v2;
            pindex[indx++]=v2;
            pindex[indx++]=v1;
            pindex[indx++]=v3;
            //back
            pindex[indx++]=v4;
            pindex[indx++]=v5;
            pindex[indx++]=v6;
            pindex[indx++]=v6;
            pindex[indx++]=v5;
            pindex[indx++]=v7;
            //bottom
            pindex[indx++]=v4;
            pindex[indx++]=v0;
            pindex[indx++]=v2;
            pindex[indx++]=v2;
            pindex[indx++]=v6;
            pindex[indx++]=v4;
            //top
            pindex[indx++]=v5;
            pindex[indx++]=v1;
            pindex[indx++]=v3;
            pindex[indx++]=v3;
            pindex[indx++]=v7;
            pindex[indx++]=v5;
        }
        //cover bottom end
        pindex[indx++]=1;
        pindex[indx++]=0;
        pindex[indx++]=2;
        pindex[indx++]=2;
        pindex[indx++]=3;
        pindex[indx++]=1;
        //cover the top end
        pindex[indx++]=v1;
        pindex[indx++]=v0;
        pindex[indx++]=v4;
        pindex[indx++]=v4;
        pindex[indx++]=v5;
        pindex[indx++]=v1;

        CharSVertex= Arrays.copyOf(vertices,vertexindex);
        CharSIndex=Arrays.copyOf(pindex,indx);
        CharSColor=Arrays.copyOf(color,colorindex);
    }
    public CharacterS(){
        float controlpts_right[]=new float[14];
        float controlpts_left[]=new float[14];
        int ci=0;
        controlpts_right[ci++]=2;controlpts_right[ci++]=0f;
        controlpts_right[ci++]=3.2f;controlpts_right[ci++]=0f;
        controlpts_right[ci++]=4;controlpts_right[ci++]=0.8f;
        controlpts_right[ci++]=2.8f;controlpts_right[ci++]=1.3f;
        controlpts_right[ci++]=2f;controlpts_right[ci++]=1.5f;
        controlpts_right[ci++]=2f;controlpts_right[ci++]=2f;
        controlpts_right[ci++]=3.2f;controlpts_right[ci++]=2f;
        ci=0;
        controlpts_left[ci++]=2f;controlpts_left[ci++]=0.2f;
        controlpts_left[ci++]=2.2f;controlpts_left[ci++]=0.2f;
        controlpts_left[ci++]=3.6f;controlpts_left[ci++]=0.4f;
        controlpts_left[ci++]=2.8f;controlpts_left[ci++]=1.0f;
        controlpts_left[ci++]=1.4f;controlpts_left[ci++]=1.5f;
        controlpts_left[ci++]=1.6f;controlpts_left[ci++]=2.2f;
        controlpts_left[ci++]=3.2f;controlpts_left[ci++]=2.2f;
        createCurve(controlpts_right,controlpts_left);
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(CharSVertex.length * 4);// (# of coordinate values * 4 bytes per float)
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(CharSVertex);
        vertexBuffer.position(0);
        ByteBuffer cb=ByteBuffer.allocateDirect(CharSColor.length * 4);// (# of coordinate values * 4 bytes per float)
        cb.order(ByteOrder.nativeOrder());
        colorBuffer = cb.asFloatBuffer();
        colorBuffer.put(CharSColor);
        colorBuffer.position(0);
        IntBuffer ib=IntBuffer.allocate(CharSIndex.length);
        indexBuffer=ib;
        indexBuffer.put(CharSIndex);
        indexBuffer.position(0);
        // prepare shaders and OpenGL program
        int vertexShader = MyRenderer.loadShader(GLES32.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = MyRenderer.loadShader(GLES32.GL_FRAGMENT_SHADER, fragmentShaderCode);
        mProgram = GLES32.glCreateProgram();             // create empty OpenGL Program
        GLES32.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES32.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES32.glLinkProgram(mProgram);                  // link the  OpenGL program to create an executable
        GLES32.glUseProgram(mProgram);// Add program to OpenGL environment
        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES32.glGetAttribLocation(mProgram, "aVertexPosition");
        // Enable a handle to the triangle vertices
        GLES32.glEnableVertexAttribArray(mPositionHandle);
        // Prepare the triangle coordinate data
        GLES32.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES32.GL_FLOAT, false, vertexStride, vertexBuffer);
        //MyRenderer.checkGlError("glVertexAttribPointer");
        //get the handle to vertex shader's aVertexColor member
        mColorHandle = GLES32.glGetAttribLocation(mProgram, "aVertexColor");
        // Enable a handle to the  colour
        GLES32.glEnableVertexAttribArray(mColorHandle);
        // Prepare the colour coordinate data
        GLES32.glVertexAttribPointer(mColorHandle, COLOR_PER_VERTEX, GLES32.GL_FLOAT, false, colorStride, colorBuffer);
        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES32.glGetUniformLocation(mProgram, "uMVPMatrix");
    }

    public void draw(float[] mvpMatrix) {
        GLES32.glUseProgram(mProgram);// Add program to OpenGL environment
        // Apply the projection and view transformation
        GLES32.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        //set the attribute of the vertex to point to the vertex buffer
        GLES32.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES32.GL_FLOAT, false, vertexStride, vertexBuffer);
        GLES32.glVertexAttribPointer(mColorHandle, COORDS_PER_VERTEX,
                GLES32.GL_FLOAT, false, colorStride, colorBuffer);
        GLES32.glDrawElements(GLES32.GL_TRIANGLES,CharSIndex.length,GLES32.GL_UNSIGNED_INT,indexBuffer);

    }
}
