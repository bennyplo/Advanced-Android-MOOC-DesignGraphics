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

public class ArbitaryShape {
    private final String vertexShaderCode =
            "attribute vec3 aVertexPosition;" +//vertex of an object
                    "attribute vec4 aVertexColor;" +//the colour  of the object
                    "uniform mat4 uMVPMatrix;" +//model view  projection matrix
                    "varying vec4 vColor;" +//variable to be accessed by the fragment shader
                    "void main() {" +
                    "gl_Position=uMVPMatrix*vec4(aVertexPosition,1.0);"+//calculate the position of the vertex
                    "vColor=aVertexColor;}";//get the colour from the application program
    private final String fragmentShaderCode =
            "precision mediump float;" + //define the precision of float
                    "varying vec4 vColor;" + //variable from the vertex shader
                    "void main() {" +
                    "   gl_FragColor = vColor; "+
                    "}";

    private final FloatBuffer vertexBuffer, colorBuffer;
    private final IntBuffer indexBuffer;
    private final FloatBuffer vertex2Buffer, color2Buffer;
    private final IntBuffer index2Buffer;
    private final FloatBuffer ringVertexBuffer, ringColorBuffer;
    private final IntBuffer ringIndexBuffer;
    private final int mProgram;
    private int mPositionHandle, mColorHandle;
    private int mMVPMatrixHandle;
    //---------
    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static final int COLOR_PER_VERTEX = 4;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    private final int colorStride = COLOR_PER_VERTEX * 4;//4 bytes per vertex
    static float SphereVertex[];
    static int SphereIndex[];
    static float SphereColor[];
    //2nd sphere
    static float Sphere2Vertex[];
    static int Sphere2Index[];
    static float Sphere2Color[];
    //ring
    static float ringVertex[];
    static int ringIndex[];
    static float ringColor[];

    private void createSphere(float radius,int nolatitude,int nolongitude)
    {
        float vertices[]=new float[65535];
        int index[]=new int[65535];
        float color[]=new float[65535];
        int vertexindex=0;
        int colorindex=0;
        int indx=0;
        float vertices2[]=new float[65535];
        int index2[]=new int[65535];
        float color2[]=new float[65525];
        int vertex2index=0;
        int color2index=0;
        int indx2=0;
        float ring_vertices[]=new float[65535];
        int ring_index[]=new int[65535];
        float ring_color[]=new float[65525];
        int rvindx=0;
        int rcindex=0;
        int rindx=0;
        float dist=3;
        int plen=(nolongitude+1)*3*3;
        int pcolorlen=(nolongitude+1)*4*3;
        for (int row=0;row<nolatitude+1;row++)
        {
            double theta=row*Math.PI/nolatitude;
            double sinTheta=Math.sin(theta);
            double cosTheta=Math.cos(theta);
            float tcolor=-0.5f;
            float tcolorinc=1/(float)(nolongitude+1);
            for (int col=0;col<nolongitude+1;col++)
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

                vertices2[vertex2index++]=(float)(radius*x);
                vertices2[vertex2index++]=(float)(radius*y)-dist;
                vertices2[vertex2index++]=(float)(radius*z);

                color[colorindex++]=1;
                color[colorindex++]=Math.abs(tcolor);
                color[colorindex++]=0;
                color[colorindex++]=1;

                color2[color2index++]=0;
                color2[color2index++]=1;
                color2[color2index++]=Math.abs(tcolor);
                color2[color2index++]=1;

                if (row==20)
                {
                    ring_vertices[rvindx++]=(float)(radius*x);
                    ring_vertices[rvindx++]=(float)(radius*y)+dist;
                    ring_vertices[rvindx++]=(float)(radius*z);
                    ring_color[rcindex++]=1;
                    ring_color[rcindex++]=Math.abs(tcolor);
                    ring_color[rcindex++]=0;
                    ring_color[rcindex++]=1;
                }
                if (row==15)
                {
                    ring_vertices[rvindx++]=(float)(radius*x)/2;
                    ring_vertices[rvindx++]=(float)(radius*y)/2+0.2f*dist;
                    ring_vertices[rvindx++]=(float)(radius*z)/2;
                    ring_color[rcindex++]=1;
                    ring_color[rcindex++]=Math.abs(tcolor);
                    ring_color[rcindex++]=0;
                    ring_color[rcindex++]=1;
                }
                if (row==10)
                {
                    ring_vertices[rvindx++]=(float)(radius*x)/2;
                    ring_vertices[rvindx++]=(float)(radius*y)/2-0.1f*dist;
                    ring_vertices[rvindx++]=(float)(radius*z)/2;
                    ring_color[rcindex++]=0;
                    ring_color[rcindex++]=1;
                    ring_color[rcindex++]=Math.abs(tcolor);
                    ring_color[rcindex++]=1;
                }
                if (row==20)
                {
                    ring_vertices[plen++]=(float)(radius*x);
                    ring_vertices[plen++]=(float)(-radius*y)-dist;
                    ring_vertices[plen++]=(float)(radius*z);
                    ring_color[pcolorlen++]=0;
                    ring_color[pcolorlen++]=1;
                    ring_color[pcolorlen++]=Math.abs(tcolor);
                    ring_color[pcolorlen++]=1;
                }
                tcolor+=tcolorinc;
            }
        }
        //index buffer
        for (int row=0;row<nolatitude;row++)
        {
            for (int col=0;col<nolongitude;col++)
            {
                int P0=(row*(nolongitude+1))+col;
                int P1=P0+nolongitude+1;
                index[indx++]=P1;
                index[indx++]=P0;
                index[indx++]=P0+1;
                index[indx++]=P1+1;
                index[indx++]=P1;
                index[indx++]=P0+1;

                index2[indx2++]=P1;
                index2[indx2++]=P0;
                index2[indx2++]=P0+1;
                index2[indx2++]=P1+1;
                index2[indx2++]=P1;
                index2[indx2++]=P0+1;

            }
        }
        rvindx=(nolongitude+1)*3*4;
        rcindex=(nolongitude+1)*4*4;
        plen=nolongitude+1;
        for (int j=0;j<plen-1;j++)
        {
            ring_index[rindx++]=j;
            ring_index[rindx++]=j+plen;
            ring_index[rindx++]=j+1;
            ring_index[rindx++]=j+plen+1;
            ring_index[rindx++]=j+1;
            ring_index[rindx++]=j+plen;

            ring_index[rindx++]=j+plen;
            ring_index[rindx++]=j+plen*2;
            ring_index[rindx++]=j+plen+1;
            ring_index[rindx++]=j+plen*2+1;
            ring_index[rindx++]=j+plen+1;
            ring_index[rindx++]=j+plen*2;

            ring_index[rindx++]=j+plen*3;
            ring_index[rindx++]=j;
            ring_index[rindx++]=j+1;
            ring_index[rindx++]=j+1;
            ring_index[rindx++]=j+plen*3+1;
            ring_index[rindx++]=j+plen*3;
        }


        //set the buffers
        SphereVertex= Arrays.copyOf(vertices,vertexindex);
        SphereIndex=Arrays.copyOf(index,indx);
        SphereColor=Arrays.copyOf(color,colorindex);
        Sphere2Vertex= Arrays.copyOf(vertices2,vertex2index);
        Sphere2Index=Arrays.copyOf(index2,indx2);
        Sphere2Color=Arrays.copyOf(color2,color2index);
        ringVertex=Arrays.copyOf(ring_vertices,rvindx);
        ringColor=Arrays.copyOf(ring_color,rcindex);
        ringIndex=Arrays.copyOf(ring_index,rindx);
    }

    public ArbitaryShape(){
        createSphere(2,30,30);
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(SphereVertex.length * 4);// (# of coordinate values * 4 bytes per float)
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(SphereVertex);
        vertexBuffer.position(0);
        ByteBuffer cb=ByteBuffer.allocateDirect(SphereColor.length * 4);// (# of coordinate values * 4 bytes per float)
        cb.order(ByteOrder.nativeOrder());
        colorBuffer = cb.asFloatBuffer();
        colorBuffer.put(SphereColor);
        colorBuffer.position(0);
        IntBuffer ib=IntBuffer.allocate(SphereIndex.length);
        indexBuffer=ib;
        indexBuffer.put(SphereIndex);
        indexBuffer.position(0);
        //2nd sphere
        ByteBuffer bb2 = ByteBuffer.allocateDirect(Sphere2Vertex.length * 4);// (# of coordinate values * 4 bytes per float)
        bb2.order(ByteOrder.nativeOrder());
        vertex2Buffer = bb2.asFloatBuffer();
        vertex2Buffer.put(Sphere2Vertex);
        vertex2Buffer.position(0);
        ByteBuffer cb2=ByteBuffer.allocateDirect(Sphere2Color.length * 4);// (# of coordinate values * 4 bytes per float)
        cb2.order(ByteOrder.nativeOrder());
        color2Buffer = cb2.asFloatBuffer();
        color2Buffer.put(Sphere2Color);
        color2Buffer.position(0);
        IntBuffer ib2=IntBuffer.allocate(Sphere2Index.length);
        index2Buffer=ib2;
        index2Buffer.put(SphereIndex);
        index2Buffer.position(0);
        ByteBuffer rbb = ByteBuffer.allocateDirect(ringVertex.length * 4);// (# of coordinate values * 4 bytes per float)
        rbb.order(ByteOrder.nativeOrder());
        ringVertexBuffer = rbb.asFloatBuffer();
        ringVertexBuffer.put(ringVertex);
        ringVertexBuffer.position(0);
        ByteBuffer rcb=ByteBuffer.allocateDirect(ringColor.length * 4);// (# of coordinate values * 4 bytes per float)
        rcb.order(ByteOrder.nativeOrder());
        ringColorBuffer = rcb.asFloatBuffer();
        ringColorBuffer.put(ringColor);
        ringColorBuffer.position(0);
        IntBuffer rib=IntBuffer.allocate(ringIndex.length);
        ringIndexBuffer=rib;
        ringIndexBuffer.put(ringIndex);
        ringIndexBuffer.position(0);
        //----------
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
        mColorHandle = GLES32.glGetAttribLocation(mProgram, "aVertexColor");
        // Enable a handle to the  colour
        GLES32.glEnableVertexAttribArray(mColorHandle);
        // Prepare the colour coordinate data
        GLES32.glVertexAttribPointer(mColorHandle, COLOR_PER_VERTEX, GLES32.GL_FLOAT, false, colorStride, colorBuffer);
        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES32.glGetUniformLocation(mProgram, "uMVPMatrix");
        //MyRenderer.checkGlError("glGetUniformLocation");
    }

    public void draw(float[] mvpMatrix) {
        GLES32.glUseProgram(mProgram);// Add program to OpenGL environment
        // Apply the projection and view transformation
        GLES32.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        //---------
        //set the attribute of the vertex to point to the vertex buffer
        GLES32.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES32.GL_FLOAT, false, vertexStride, vertexBuffer);
        GLES32.glVertexAttribPointer(mColorHandle, COORDS_PER_VERTEX,
                GLES32.GL_FLOAT, false, colorStride, colorBuffer);
        // Draw the Sphere
        GLES32.glDrawElements(GLES32.GL_TRIANGLES,SphereIndex.length,GLES32.GL_UNSIGNED_INT,indexBuffer);
        //---------
        //2nd sphere
        GLES32.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES32.GL_FLOAT, false, vertexStride, vertex2Buffer);
        GLES32.glVertexAttribPointer(mColorHandle, COORDS_PER_VERTEX,
                GLES32.GL_FLOAT, false, colorStride, color2Buffer);
        // Draw the Sphere
        GLES32.glDrawElements(GLES32.GL_TRIANGLES,Sphere2Index.length,GLES32.GL_UNSIGNED_INT,index2Buffer);
        ///////////////////
        //Rings
        GLES32.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES32.GL_FLOAT, false, vertexStride, ringVertexBuffer);
        GLES32.glVertexAttribPointer(mColorHandle, COORDS_PER_VERTEX,
                GLES32.GL_FLOAT, false, colorStride, ringColorBuffer);
        GLES32.glDrawElements(GLES32.GL_TRIANGLES,ringIndex.length,GLES32.GL_UNSIGNED_INT,ringIndexBuffer);
    }
}
