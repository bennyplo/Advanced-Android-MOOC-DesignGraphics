package com.bennyplo.designgraphicsassessment;

import android.opengl.GLES32;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class CharacterA {
    private final String vertexShaderCode =
            "attribute vec3 aVertexPosition;"+//vertex of an object
                    "attribute vec4 aVertexColor;"+//the colour  of the object
                    "attribute vec3 aVertexNormal;"+
                    "uniform mat4 uMVPMatrix;"+//model view  projection matrix
                    "varying vec4 vColor;"+//variable to be accessed by the fragment shader
                    "uniform vec3 uPointLightingLocation;"+
                    "uniform vec3 uAmbientColor;"+
                    "varying vec3 vLightWeighting;"+
                    "uniform vec3 uDiffuseLightLocation;"+
                    "uniform vec4 uDiffuseColor;" +//color of the diffuse light
                    "varying vec4 vDiffuseColor;" +
                    "varying float vDiffuseLightWeighting;" +
                    "uniform vec3 uAttenuation;"+//light attenuation
                    "uniform vec4 uSpecularColor;"+
                    "varying vec4 vSpecularColor;" +
                    "varying float vSpecularLightWeighting; "+
                    "uniform vec3 uSpecularLightLocation;"+
                    "uniform float uMaterialShininess;"+
                    "void main() {" +
                    "        gl_Position = uMVPMatrix* vec4(aVertexPosition, 1.0);"+//calculate the position of the vertex
                    "vLightWeighting=vec3(1.0,1.0,1.0);     "+
                    "vec4 mvPosition=uMVPMatrix*vec4(aVertexPosition,1.0);"+
                    "vec3 lightDirection=normalize(uPointLightingLocation-mvPosition.xyz);" +
                    "vec3 diffuseLightDirection=normalize(uDiffuseLightLocation-mvPosition.xyz);"+
                    "vec3 transformedNormal = normalize((uMVPMatrix * vec4(aVertexNormal, 0.0)).xyz);"+
                    "vLightWeighting=uAmbientColor;"+
                    "vDiffuseColor=uDiffuseColor;" +
                    " vSpecularColor=uSpecularColor; "+
                    "float specularLightWeighting=0.0;" +
                    "vec3 eyeDirection=normalize(-mvPosition.xyz);" +
                    "vec3 specularlightDirection=normalize(uSpecularLightLocation-mvPosition.xyz);"+
                    "vec3 inverseLightDirection = normalize(uPointLightingLocation);"+
                    "vec3 reflectionDirection=reflect(-lightDirection,transformedNormal);" +
                    "vec3 vertexToLightSource = mvPosition.xyz-uPointLightingLocation;"+
                    "float diff_light_dist = length(vertexToLightSource);"+
                    "float attenuation = 1.0 / (uAttenuation.x"+
                    "                           + uAttenuation.y * diff_light_dist" +
                    "                           + uAttenuation.z * diff_light_dist * diff_light_dist);"+
                    "float diffuseLightWeighting=0.0;"+
                    "diffuseLightWeighting =attenuation*max(dot(transformedNormal,lightDirection),0.0);"+
                    "vDiffuseLightWeighting=diffuseLightWeighting;"+
                    "specularLightWeighting=attenuation*pow(max(dot(reflectionDirection,eyeDirection), 0.0), uMaterialShininess);" +
                    "vSpecularLightWeighting=specularLightWeighting;"+
                    "vColor=aVertexColor;}";//get the colour from the application program

    private final String fragmentShaderCode =
            //"precision mediump float;"+ //define the precision of float
            "precision lowp float;"+ //need to set to low in order to show the depth map
                    "varying vec4 vColor;"+ //variable from the vertex shader
                    "varying vec3 vLightWeighting;"+
                    "varying vec4 vDiffuseColor;" +
                    "varying float vDiffuseLightWeighting;" +
                    "varying vec4 vSpecularColor;" +
                    "varying float vSpecularLightWeighting; "+
                    "void main() {"+
                    "vec4 diffuseColor=vDiffuseLightWeighting*vDiffuseColor;" +
                    "vec4 specularColor=vSpecularLightWeighting*vSpecularColor;"+
                    "gl_FragColor=vec4(vColor.xyz*vLightWeighting,1)+diffuseColor+specularColor;"+
                    "}";
    private final FloatBuffer vertexBuffer,colorBuffer,normalBuffer;
    private final IntBuffer indexBuffer;
    private final int mProgram;
    private int mPositionHandle,mColorHandle,mNormalHandle;
    private int mMVPMatrixHandle;
    private int diffuseColorHandle;
    private int pointLightingLocationHandle,uAmbientColorHandle;
    private int diffuseLightLocationHandle;
    private int specularColorHandle,specularLightLocationHandle;
    private int materialShininessHandle;
    private int attenuateHandle;

    static float lightlocation[]=new float[3];
    static float diffuselightlocation[]=new float[3];
    static float attenuation[]=new float[3];//light attenuation
    static float diffusecolor[]=new float[4];//diffuse light colour
    static float specularcolor[]=new float[4];//specular highlight colour
    static float MaterialShininess=10f;//material shiness
    static float specularlightlocation[]=new float[3];//specular light location
    //--------


    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static final int COLOR_PER_VERTEX = 4;
    private int vertexCount;// number of vertices
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    private final int colorStride=COLOR_PER_VERTEX*4;//4 bytes per vertex
    //static float shape_color[]={0.184f,0.3f,0.525f,1.0f};
    static float shape_color[]={1f,1f,1f,1.0f};
    static float CharAVertex[] ={
            //front face
            -5,-5,1,
            -1.5f,-3,1,
            -3,-5,1,
            -1,-1,1,//3
            0,2,1,//4
            -1.5f,5,1,//5
            1.5f,5,1,//6
            1,-1,1,//7
            5,-5,1,//8
            1.5f,-3,1,//9
            3,-5,1,//10
            //back face
            -5,-5,-1,
            -1.5f,-3,-1,
            -3,-5,-1,
            -1,-1,-1,//3
            0,2,-1,//4
            -1.5f,5,-1,//5
            1.5f,5,-1,//6
            1,-1,-1,//7
            5,-5,-1,//8
            1.5f,-3,-1,//9
            3,-5,-1,//10
            //top face
            -1.5f,5,1,
            1.5f,5,1,
            1.5f,5,-1,
            -1.5f,5,-1,
            -1,-1,1,
            1,-1,1,
            1,-1,-1,
            -1,-1,-1,
            //bottom face
            -1.5f,-3,1,
            1.5f,-3,1,
            1.5f,-3,-1,
            -1.5f,-3,-1,
            -5,-5,1,
            -3,-5,1,
            -3,-5,-1,
            -5,-5,-1,
            3,-5,1,
            5,-5,1,
            5,-5,-1,
            3,-5,-1,
            //right face
            0,2,1,
            -1,-1,1,
            -1,-1,-1,
            0,2,-1,
            -1.5f,-3,1,
            -3,-5,1,
            -3,-5,-1,
            -1.5f,-3,-1,
            1.5f,5,1,
            5,-5,1,
            5,-5,-1,
            1.5f,5,-1,
            //left face
            -1.5f,5,1,
            -5,-5,1,
            -5,-5,-1,
            -1.5f,5,-1,

            0,2,1,
            1,-1,1,
            1,-1,-1,
            0,2,-1,

            1.5f,-3,1,
            3,-5,1,
            3,-5,-1,
            1.5f,-3,-1,

    };
    static int CharAIndex[] ={
            0, 1, 2, 0, 1, 3,//front face
            3,4,5,0,5,3,
            4,5,6,4,6,7,
            7,8,6,7,8,9,
            9,10,8,
            3,7,1,7,9,1,
            11, 12, 13, 11, 12, 14,//back face
            14,15,16,11,16,14,
            15,16,17,15,17,18,
            18,19,17,18,19,20,
            20,21,19,
            14,18,12,18,20,12,
            22,23,24,22,24,25,//top face
            26,27,28,26,28,29,
            30,31,32,30,32,33,//bottom face
            34,35,36,34,36,37,
            38,39,40,38,40,41,
            42,43,44,42,44,45,//right face
            46,47,48,46,48,49,
            50,51,52,50,52,53,
            54,55,56,54,56,57,//left face
            58,59,60,58,60,61,
            62,63,64,62,64,65,

    };
    static float CharANormal[]={
            //front face
            0f,0.0f,-1.0f,
            0f,0.0f,-1.0f,
            0f,0.0f,-1.0f,
            0f,0.0f,-1.0f,
            0f,0.0f,-1.0f,
            0f,0.0f,-1.0f,
            0f,0.0f,-1.0f,
            0f,0.0f,-1.0f,
            0f,0.0f,-1.0f,
            0f,0.0f,-1.0f,
            0f,0.0f,-1.0f,
            //back face
            0f,0f,1.0f,
            0f,0f,1.0f,
            0f,0f,1.0f,
            0f,0f,1.0f,
            0f,0f,1.0f,
            0f,0f,1.0f,
            0f,0f,1.0f,
            0f,0f,1.0f,
            0f,0f,1.0f,
            0f,0f,1.0f,
            0f,0f,1.0f,
            //top face
            0f,-1.0f,0f,
            0f,-1.0f,0f,
            0f,-1.0f,0f,
            0f,-1.0f,0f,
            0f,-1.0f,0f,
            0f,-1.0f,0f,
            0f,-1.0f,0f,
            0f,-1.0f,0f,
            //bottom face
            0f,1.0f,0f,
            0f,1.0f,0f,
            0f,1.0f,0f,
            0f,1.0f,0f,
            0f,1.0f,0f,
            0f,1.0f,0f,
            0f,1.0f,0f,
            0f,1.0f,0f,
            0f,1.0f,0f,
            0f,1.0f,0f,
            0f,1.0f,0f,
            0f,1.0f,0f,
            //right face
            1.0f,0f,0f,
            1.0f,0f,0f,
            1.0f,0f,0f,
            1.0f,0f,0f,
            1.0f,0f,0f,
            1.0f,0f,0f,
            1.0f,0f,0f,
            1.0f,0f,0f,
            1.0f,0f,0f,
            1.0f,0f,0f,
            1.0f,0f,0f,
            1.0f,0f,0f,
            //left face
            -1.0f,0f,0f,
            -1.0f,0f,0f,
            -1.0f,0f,0f,
            -1.0f,0f,0f,

            -1.0f,0f,0f,
            -1.0f,0f,0f,
            -1.0f,0f,0f,
            -1.0f,0f,0f,

            -1.0f,0f,0f,
            -1.0f,0f,0f,
            -1.0f,0f,0f,
            -1.0f,0f,0f,

    };

    static float CharAColor[]={
            //front
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            //back
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            //top
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            //bottom
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            //right
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            //left
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
            shape_color[0],shape_color[1],shape_color[2],shape_color[3],
    };


    public CharacterA(){
        ///============
        lightlocation[0]=10f;
        lightlocation[1]=10f;
        lightlocation[2]=10f;
        diffuselightlocation[0]=2f;
        diffuselightlocation[1]=0.2f;
        diffuselightlocation[2]=2;
        specularcolor[0]=1;
        specularcolor[1]=1;
        specularcolor[2]=1;
        specularcolor[3]=1;
        specularlightlocation[0]=-7;
        specularlightlocation[1]=-4;
        specularlightlocation[2]=2;
        //////////////////////

        //////////////////
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(CharAVertex.length * 4);// (# of coordinate values * 4 bytes per float)
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(CharAVertex);
        vertexBuffer.position(0);
        vertexCount=CharAVertex.length/COORDS_PER_VERTEX;
        ByteBuffer cb=ByteBuffer.allocateDirect(CharAColor.length * 4);// (# of coordinate values * 4 bytes per float)
        cb.order(ByteOrder.nativeOrder());
        colorBuffer = cb.asFloatBuffer();
        colorBuffer.put(CharAColor);
        colorBuffer.position(0);
        ByteBuffer nb = ByteBuffer.allocateDirect(CharANormal.length * 4);// (# of coordinate values * 4 bytes per float)
        nb.order(ByteOrder.nativeOrder());
        normalBuffer=nb.asFloatBuffer();
        normalBuffer.put(CharANormal);
        normalBuffer.position(0);
        IntBuffer ib=IntBuffer.allocate(CharAIndex.length);
        indexBuffer=ib;
        indexBuffer.put(CharAIndex);
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
        mNormalHandle=GLES32.glGetAttribLocation(mProgram,"aVertexNormal");
        GLES32.glEnableVertexAttribArray(mNormalHandle);
        GLES32.glVertexAttribPointer(mNormalHandle, COORDS_PER_VERTEX, GLES32.GL_FLOAT, false, vertexStride, normalBuffer);
        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES32.glGetUniformLocation(mProgram, "uMVPMatrix");
        //MyRenderer.checkGlError("glGetUniformLocation");
        pointLightingLocationHandle=GLES32.glGetUniformLocation(mProgram, "uPointLightingLocation");
        diffuseLightLocationHandle=GLES32.glGetUniformLocation(mProgram,"uDiffuseLightLocation");
        diffuseColorHandle=GLES32.glGetUniformLocation(mProgram,"uDiffuseColor");
        diffusecolor[0]=1;diffusecolor[1]=1;diffusecolor[2]=1;diffusecolor[3]=1;
        attenuateHandle=GLES32.glGetUniformLocation(mProgram,"uAttenuation");
        attenuation[0]=1;attenuation[1]=0.14f;attenuation[2]=0.07f;
        uAmbientColorHandle=GLES32.glGetUniformLocation(mProgram,"uAmbientColor");
        specularColorHandle=GLES32.glGetUniformLocation(mProgram,"uSpecularColor");
        specularLightLocationHandle=GLES32.glGetUniformLocation(mProgram,"uSpecularLightLocation");
        materialShininessHandle=GLES32.glGetUniformLocation(mProgram,"uMaterialShininess");
    }

    public void draw(float[] mvpMatrix) {
        GLES32.glUseProgram(mProgram);// Add program to OpenGL environment
        // Apply the projection and view transformation
        GLES32.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        // MyRenderer.checkGlError("glUniformMatrix4fv");
        GLES32.glUniform3fv(pointLightingLocationHandle,1,lightlocation,0);
        GLES32.glUniform3fv(diffuseLightLocationHandle,1,diffuselightlocation,0);
        GLES32.glUniform4fv(diffuseColorHandle,1,diffusecolor,0);
        GLES32.glUniform3fv(attenuateHandle,1,attenuation,0);
        GLES32.glUniform3f(uAmbientColorHandle,0.6f,0.6f,0.6f);
        GLES32.glUniform4fv(specularColorHandle,1,specularcolor,0);
        GLES32.glUniform1f(materialShininessHandle,MaterialShininess);
        GLES32.glUniform3fv(specularLightLocationHandle,1,specularlightlocation,0);

        //set the attribute of the vertex to point to the vertex buffer
        GLES32.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES32.GL_FLOAT, false, vertexStride, vertexBuffer);
        GLES32.glVertexAttribPointer(mColorHandle, COORDS_PER_VERTEX,
                GLES32.GL_FLOAT, false, colorStride, colorBuffer);
        GLES32.glVertexAttribPointer(mNormalHandle, COORDS_PER_VERTEX,
                GLES32.GL_FLOAT, false, vertexStride, normalBuffer);
        // Draw the pentagon prism
        //GLES32.glDrawArrays(GLES32.GL_TRIANGLES,0, vertexCount);
        GLES32.glDrawElements(GLES32.GL_TRIANGLES,CharAIndex.length,GLES32.GL_UNSIGNED_INT,indexBuffer);
    }
    public void setLightLocation(float px,float py,float pz)
    {
        lightlocation[0]=px;
        lightlocation[1]=py;
        lightlocation[2]=pz;
    }
}
