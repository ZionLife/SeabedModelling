package com.zionstudio.seabedmodelling;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;

import com.zionstudio.seabedmodelling.utils.ShaderHelper;
import com.zionstudio.seabedmodelling.utils.TextResourceReader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUniform4fv;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glVertexAttribPointer;
import static com.zionstudio.seabedmodelling.Constants.BYTES_PER_FLOAT;
import static android.opengl.GLES20.glUseProgram;

/**
 * Created by QiuXi'an on 2017/12/12 0012.
 * Email Zionlife1025@163.com
 */

public class Seabed {
    private static final String TAG = "Seabed";
    private static final int POSITION_COMPONENT_COUNT = 3;
    private static final int TEXTURE_COORDINATES_COMPONENT_COUNT = 2;
    private static final int STRIDE = (POSITION_COMPONENT_COUNT + TEXTURE_COORDINATES_COMPONENT_COUNT) * BYTES_PER_FLOAT;
    private int mVerCounts; //顶点数量
    private int bmpWidth; //高度图的宽度
    private int bmpHeight; //高度图的高度
    private float[] mVertexData;//顶点数据
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTexCoorBuffer;
    private short[] mIndexData; //顶点索引数据
    private float[][] mHeightmapData; //高度数据


    private int mProgram;//Program id
    private int maPositionHandle; //顶点位置属性的引用id
    private int maTexCoorHandle; //顶点纹理坐标属性的引用id
    private int muMVPMatrixHandle; //总变换矩阵的引用id
    private int sTextureGrassHandle; //草地纹理

    private static final float LAND_HIGHEST = 20f; //陆地最大高度差
    private static final float LAND_HIGH_ADJUST = -2f; //陆地的高度调整值

    private final float UNIT_SIZE = 1.0f;

    public Seabed(SeabedSurfaceView view) {
        initVertexData();
        initShader(view);
    }

    private void initShader(SeabedSurfaceView view) {
        mProgram = ShaderHelper.buildProgram(TextResourceReader.readTextFileFromResource(MyApplication.mContext, R.raw.heightmap_vertex_shader),
                TextResourceReader.readTextFileFromResource(MyApplication.mContext, R.raw.heightmap_fragment_shader));
        maPositionHandle = glGetAttribLocation(mProgram, "aPosition");
        maTexCoorHandle = glGetAttribLocation(mProgram, "aTexCoor");
        muMVPMatrixHandle = glGetUniformLocation(mProgram, "uMVPMatrix");
        sTextureGrassHandle = glGetUniformLocation(mProgram, "vTextureCoord");
    }


    private void initVertexData() {
        //从灰度图片中加载陆地上每个顶点的高度
        Bitmap bmp = BitmapFactory.decodeResource(MyApplication.mContext.getResources(), R.mipmap.land);
        bmpWidth = bmp.getWidth();
        bmpHeight = bmp.getHeight();
        mVerCounts = (bmpWidth - 1) * (bmpHeight - 1) * 2 * 3;
        loadBitmapData(bmp);
    }

    private void loadBitmapData(Bitmap bitmap) {
        final int[] pixels = new int[bmpWidth * bmpHeight];
        bitmap.getPixels(pixels, 0, bmpWidth, 0, 0, bmpWidth, bmpHeight);
        bitmap.recycle();

        mHeightmapData = new float[bmpWidth][bmpHeight];
        int offset = 0;
        for (int row = 0; row < bmpHeight; row++) {
            for (int col = 0; col < bmpWidth; col++) {
                int color = pixels[row * bmpWidth + col];
                int r = Color.red(color);
                int g = Color.green(color);
                int b = Color.blue(color);
                int h = (r + g + b) / 3;
                //像素顶点的海拔高度 = 最大高差 * 像素值 / 255.0 + 最低海拔
                mHeightmapData[row][col] = h * LAND_HIGHEST / 255 + LAND_HIGH_ADJUST;
            }
        }

        //构造顶点数据
        mVertexData = new float[mVerCounts * 3];
        offset = 0;
        for (int row = 0; row < bmpHeight - 1; row++) {
            for (int col = 0; col < bmpWidth - 1; col++) {
                float topLeftX = -UNIT_SIZE * (bmpWidth - 1) / 2 + col * UNIT_SIZE; //计算当前小格子左上侧点的X坐标
                float topLeftZ = -UNIT_SIZE * (bmpHeight - 1) / 2 + row * UNIT_SIZE; //计算当前小格子左上侧点的Z坐标

                //构建三角形
                //1.
                mVertexData[offset++] = topLeftX;
                mVertexData[offset++] = mHeightmapData[row][col];
                mVertexData[offset++] = topLeftZ;

                mVertexData[offset++] = topLeftX;
                mVertexData[offset++] = mHeightmapData[row + 1][col];
                mVertexData[offset++] = topLeftZ + UNIT_SIZE;

                mVertexData[offset++] = topLeftX + UNIT_SIZE;
                mVertexData[offset++] = mHeightmapData[row][col + 1];
                mVertexData[offset++] = topLeftZ;

                //2.
                mVertexData[offset++] = topLeftX + UNIT_SIZE;
                mVertexData[offset++] = mHeightmapData[row][col + 1];
                mVertexData[offset++] = topLeftZ;

                mVertexData[offset++] = topLeftX;
                mVertexData[offset++] = mHeightmapData[row + 1][col];
                mVertexData[offset++] = topLeftZ + UNIT_SIZE;

                mVertexData[offset++] = topLeftX + UNIT_SIZE;
                mVertexData[offset++] = mHeightmapData[row + 1][col + 1];
                mVertexData[offset++] = topLeftZ + UNIT_SIZE;
            }
        }

        //创建顶点坐标数据缓冲
        mVertexBuffer = ByteBuffer
                .allocateDirect(mVertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(mVertexData);
        mVertexBuffer.position(0);

        //初始化纹理坐标数据
        float[] texCoor = generateTexCoor(bmpWidth, bmpHeight);
        //创建顶点纹理坐标数据缓冲
        mTexCoorBuffer = ByteBuffer
                .allocateDirect(texCoor.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(texCoor);
        mTexCoorBuffer.position(0);

        //构造索引数据
//        createIndexData();
    }

    private float[] generateTexCoor(int bw, int bh) {
        float[] result = new float[bw * bh * 6 * 2];
        float sizew = 16.0f / bw; //行数
        float sizeh = 16.0f / bh; //列数
        int offset = 0;
        for (int i = 0; i < bh - 1; i++) {
            for (int j = 0; j < bw - 1; j++) {
                //每行列一个矩形，由两个三角形构成，共六个点，12个纹理坐标
                float s = j * sizew;
                float t = i * sizeh;

                //贴第一个三角形
                result[offset++] = s;
                result[offset++] = t;

                result[offset++] = s;
                result[offset++] = t + sizeh;

                result[offset++] = s + sizew;
                result[offset++] = t;

                //贴第二个三角形
                result[offset++] = s + sizew;
                result[offset++] = t;

                result[offset++] = s;
                result[offset++] = t + sizeh;

                result[offset++] = s + sizew;
                result[offset++] = t + sizeh;
            }
        }

        return result;
    }

    public void draw(int texId) {
        glUseProgram(mProgram);
        //传入变换矩阵
        glUniformMatrix4fv(muMVPMatrixHandle, 1, false, MatrixState.getFinalMatrix(), 0);
        StringBuilder sb = new StringBuilder();
        //传送顶点位置数据
        glVertexAttribPointer(
                maPositionHandle,
                3,
                GL_FLOAT,
                false,
                3 * 4,
                mVertexBuffer
        );

        //传送顶点纹理坐标数据
        glVertexAttribPointer(
                maTexCoorHandle,
                2,
                GL_FLOAT,
                false,
                2 * 4,
                mTexCoorBuffer
        );

        //Enable顶点位置数据
        glEnableVertexAttribArray(maPositionHandle);
        glEnableVertexAttribArray(maTexCoorHandle);

        //绑定纹理
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texId);
        glUniform1i(sTextureGrassHandle, 0);  //使用0号纹理

        //绘制纹理矩形
        glDrawArrays(GL_TRIANGLES, 0, mVerCounts);
    }
}

















