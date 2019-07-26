package com.bennyplo.designgraphicswithopengl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES32;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

public class Sphere {
    private final String vertexShaderCode =
            "attribute vec3 aVertexPosition;"+"uniform mat4 uMVPMatrix;varying vec4 vColor;" +
                    "attribute vec4 aVertexColor;"+
                    "void main() {"+

                    "gl_Position = uMVPMatrix *vec4(aVertexPosition,1.0);" +
                    "vColor=aVertexColor;"+
                    "}";
    private final String fragmentShaderCode = "precision lowp float;varying vec4 vColor; "+
            "void main() {" +
             "gl_FragColor = vColor;" +
            "}";
    private final FloatBuffer vertexBuffer,colorBuffer;
    private final IntBuffer indexBuffer;
    private final int mProgram;
    private int mPositionHandle,mNormalHandle,mColorHandle;
    private int mMVPMatrixHandle;
    //--------
    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3,COLOR_PER_VERTEX=4;
    //---------
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    private final int colorStride=COLOR_PER_VERTEX*4;
    static float SphereVertex[];
    static float SphereColor[];
    static int SphereIndex[];

    private  void createShpere(float radius,int nolatitude,int nolongitude) {
        float vertices[]=new float[65535];
        int pindex[]=new int[65535];
        float pcolor[]=new float[65535];
        int vertexindex=0;
        int colorindex=0;
        int indx=0;
        float dist=0f;
        for (int row=0;row<=nolatitude;row++){
            double theta=row*Math.PI/nolatitude;
            double sinTheta=Math.sin(theta);
            double cosTheta=Math.cos(theta);
            float tcolor=-0.5f;
            float tcolorinc=1f/(float)(nolongitude+1);
            for (int col=0;col<=nolongitude;col++)
            {
                double phi=col*2*Math.PI/nolongitude;
                double sinPhi=Math.sin(phi);
                double cosPhi=Math.cos(phi);
                double x=cosPhi*sinTheta;
                double y=cosTheta;
                double z=sinPhi*sinTheta;
                vertices[vertexindex++]=(float)(radius*x);
                vertices[vertexindex++]=(float)(radius*y)+dist;
                vertices[vertexindex++]=(float)(radius*z);
                pcolor[colorindex++] = 1f;
                pcolor[colorindex++] = Math.abs(tcolor);
                pcolor[colorindex++] = 0f;
                pcolor[colorindex++] = 1f;
                //--------
                tcolor+=tcolorinc;
            }
        }
        for (int row=0;row<nolatitude;row++)
        {
            for (int col=0;col<nolongitude;col++)
            {
                int first=(row*(nolongitude+1))+col;
                int second=first+nolongitude+1;
                pindex[indx++]=first;
                pindex[indx++]=second;
                pindex[indx++]=first+1;
                pindex[indx++]=second;
                pindex[indx++]=second+1;
                pindex[indx++]=first+1;
            }
        }

        SphereVertex= Arrays.copyOf(vertices,vertexindex);
        SphereIndex=Arrays.copyOf(pindex,indx);
        SphereColor=Arrays.copyOf(pcolor,colorindex);
    }

    public Sphere(){

        createShpere(2,30,30);
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(SphereVertex.length * 4);// (# of coordinate values * 4 bytes per float)
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(SphereVertex);
        vertexBuffer.position(0);
        IntBuffer ib=IntBuffer.allocate(SphereIndex.length);
        indexBuffer=ib;
        indexBuffer.put(SphereIndex);
        indexBuffer.position(0);
        ByteBuffer cb=ByteBuffer.allocateDirect(SphereColor.length*4);
        cb.order(ByteOrder.nativeOrder());
        colorBuffer=cb.asFloatBuffer();
        colorBuffer.put(SphereColor);
        colorBuffer.position(0);
        //////////////////////
        ///============
        //set filtering
        //GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D,GLES32.GL_TEXTURE_MIN_FILTER,GLES32.GL_NEAREST);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D,GLES32.GL_TEXTURE_MIN_FILTER,GLES32.GL_LINEAR);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D,GLES32.GL_TEXTURE_MAG_FILTER,GLES32.GL_NEAREST);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D,GLES32.GL_TEXTURE_WRAP_S,GLES32.GL_CLAMP_TO_EDGE);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D,GLES32.GL_TEXTURE_WRAP_T,GLES32.GL_CLAMP_TO_EDGE);
        //////////////////////
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
        MyRenderer.checkGlError("glVertexAttribPointer");
        mColorHandle=GLES32.glGetAttribLocation(mProgram,"aVertexColor");
        GLES32.glEnableVertexAttribArray(mColorHandle);
        GLES32.glVertexAttribPointer(mColorHandle, COLOR_PER_VERTEX, GLES32.GL_FLOAT, false, colorStride, colorBuffer);
        mMVPMatrixHandle = GLES32.glGetUniformLocation(mProgram, "uMVPMatrix");
    }

    public void draw(float[] mvpMatrix) {
        GLES32.glUseProgram(mProgram);// Add program to OpenGL environment
        // Apply the projection and view transformation
        GLES32.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
               //set the attribute of the vertex to point to the vertex buffer
        GLES32.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES32.GL_FLOAT, false, vertexStride, vertexBuffer);
        GLES32.glVertexAttribPointer(mColorHandle, COLOR_PER_VERTEX,
                GLES32.GL_FLOAT, false, colorStride, colorBuffer);
        // Draw the sphere
        GLES32.glDrawElements(GLES32.GL_TRIANGLES,SphereIndex.length,GLES32.GL_UNSIGNED_INT,indexBuffer);

    }

}
