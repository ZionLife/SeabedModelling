package com.zionstudio.seabedmodelling;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.util.AttributeSet;

import com.zionstudio.seabedmodelling.utils.Logger;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DECR;
import static android.opengl.GLES20.GL_DEPTH_BITS;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_NEAREST;
import static android.opengl.GLES20.GL_RED_BITS;
import static android.opengl.GLES20.GL_REPEAT;
import static android.opengl.GLES20.GL_TEXTURE2;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_S;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_T;
import static android.opengl.GLES20.GL_VIEWPORT;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glGenerateMipmap;
import static android.opengl.GLES20.glTexParameterf;
import static android.opengl.GLES20.glViewport;

/**
 * Created by QiuXi'an on 2017/12/12 0012.
 * Email Zionlife1025@163.com
 */

public class SeabedSurfaceView extends GLSurfaceView {
    private final String TAG = getClass().getSimpleName();
    static float cx = 0; //摄像机x坐标
    static float cz = 12; //摄像机z坐标

    static float tx = 0; //观察目标点的x坐标
    static float tz = 0; //观察目标点的z坐标

    private MyRenderer mRenderer;

    public SeabedSurfaceView(Context context) {
        super(context);
        this.setEGLContextClientVersion(2);
        mRenderer = new MyRenderer();
        setRenderer(mRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY); //设置渲染模式为主动渲染
    }

    private int initTexture() {
        //生成纹理ID
        int[] textures = new int[1];
        glGenTextures(
                1,
                textures,
                0
        );
        int textureId = textures[0];
        glBindTexture(GL_TEXTURE_2D, textureId);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        //ST方向纹理拉伸方式
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

        @SuppressLint("ResourceType") InputStream is = this.getResources().openRawResource(R.mipmap.grass);
        Bitmap bitmap;
        try {
            bitmap = BitmapFactory.decodeStream(is);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //实际加载纹理
        GLUtils.texImage2D(
                GL_TEXTURE_2D,
                0,
                bitmap,
                0
        );
        //自动生成Mipmap纹理
        glGenerateMipmap(GL_TEXTURE_2D);
        //释放纹理图
        bitmap.recycle();
        return textureId;
    }

    private class MyRenderer implements Renderer {
        Seabed mSeabed;
        int mSeabedId;

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            Logger.i(TAG, "onSurfaceCreated");
            glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

            //开启深度检测
            glEnable(GL_DEPTH_TEST);
            MatrixState.setInitStack();

            mSeabed = new Seabed(SeabedSurfaceView.this);
            mSeabedId = initTexture();
        }


        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            Logger.i(TAG, "onSurfaceChanged");
            //设置视窗的大小
            glViewport(0, 0, width, height);
            //计算GLSurfaceView的宽高比
            float ratio = (float) width / height;
            //生成透视投影矩阵
//            MatrixState.setProjectFrustum(-ratio, ratio, -1, 1, 1, 100);
            MatrixState.setProjectFrustum(-1.777f, 1.777f, -1, 1, 1, 100);
            //产生摄像机9参数位置矩阵
            MatrixState.setCamera(cx, 3, cz, tx, 1, tz, 0, 1, 0);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            Logger.i(TAG, "onDrawFrame");
            //清除深度缓冲与颜色缓冲
            glClear(GL_DEPTH_BITS | GL_COLOR_BUFFER_BIT);

            MatrixState.pushMatrix();
            mSeabed.draw(mSeabedId);
            MatrixState.popMatrix();
        }
    }
}
