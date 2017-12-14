package com.zionstudio.seabedmodelling;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

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
    private float[] mHeightmapData; //高度数据


    private int mProgram;//Program id
    private int maPositionHandle; //顶点位置属性的引用id
    private int maTexCoorHandle; //顶点纹理坐标属性的引用id
    private int muMVPMatrixHandle; //总变换矩阵的引用id
    private int sTextureGrassHandle; //草地纹理

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

//    private void initVertexData() {
//        //从灰度图片中加载陆地上每个顶点的高度
//        Bitmap bmp = BitmapFactory.decodeResource(MyApplication.mContext.getResources(), R.mipmap.land);
//        int colsPlusOne = bmp.getWidth(); //列数
//        int rowsPlusOne = bmp.getHeight(); //行数
//        float[][] result = new float[rowsPlusOne][colsPlusOne];
//
//        final float LAND_HEIGHT_ADJUST = -2f; //陆地的高度调整
//        final float LAND_HIGHEST = 20f; //陆地最大高差
//        //遍历灰度图像
//        for (int i = 0; i < rowsPlusOne; i++) {
//            for (int j = 0; j < colsPlusOne; j++) {
//                int color = bmp.getPixel(j, i);
//                int r = Color.red(color);
//                int g = Color.green(color);
//                int b = Color.blue(color);
//
//                int h = (r + g + b) / 3;
//                //像素点的海拔高度 = 最大高差X像素值 / 255.0 + 最低海拔
//                result[i][j] = h * LAND_HIGHEST / 255 + LAND_HEIGHT_ADJUST;
//            }
//        }
//        Logger.i(TAG, "行数: " + result.length + "; 列数: " + result[0].length);
//
//        int cols = colsPlusOne - 1;
//        int rows = rowsPlusOne - 1;
//        mVerCounts = (colsPlusOne - 1) * (rowsPlusOne - 1) * 2 * 3; //每个各自2个三角形，每个三角形3个顶点
//        float vertices[] = new float[mVerCounts * 3]; //每个顶点有X、Y、Z三个坐标
//        int count = 0;
//        for (int j = 0; j < rows; j++) {
//            for (int i = 0; i < cols; i++) {
//                //计算当前格子左上侧点坐标，坐标中心点在图形中心点，故顶点坐标x、y、z的计算需要
//
//            }
//        }
//
//    }

    private void initVertexData() {
        //从灰度图片中加载陆地上每个顶点的高度
        Bitmap bmp = BitmapFactory.decodeResource(MyApplication.mContext.getResources(), R.mipmap.land);
        bmpWidth = bmp.getWidth();
        bmpHeight = bmp.getHeight();
        if (bmpWidth * bmpHeight > 65536) {
            throw new RuntimeException("Bitmap is too large for the index buffer.");
        }
        mVerCounts = (bmpWidth - 1) * (bmpHeight - 1) * 2 * 3;
        loadBitmapData(bmp);
//        bmp.recycle();
    }

    private void loadBitmapData(Bitmap bitmap) {
        final int[] pixels = new int[bmpWidth * bmpHeight];
        bitmap.getPixels(pixels, 0, bmpWidth, 0, 0, bmpWidth, bmpHeight);
        bitmap.recycle();

        mHeightmapData = new float[bmpWidth * bmpHeight * POSITION_COMPONENT_COUNT];
        int offset = 0;
        for (int row = 0; row < bmpHeight; row++) {
            for (int col = 0; col < bmpWidth; col++) {
                final float xPosition = ((float) col / (float) (bmpWidth - 1)) - 0.5f;
                final float yPosition = (float) Color.red(pixels[row * bmpHeight] + col) / (float) 255;
                final float zPosition = ((float) col / (float) (bmpHeight - 1)) - 0.5f;

                mHeightmapData[offset++] = xPosition;
                mHeightmapData[offset++] = yPosition;
                mHeightmapData[offset++] = zPosition;
            }
        }

        //构造顶点数据
        mVertexData = new float[mVerCounts];
        offset = 0;
        for (int row = 0; row < bmpHeight - 1; row++) {
            for (int col = 0; col < bmpWidth - 1; col++) {
                short topLeftIndexNum = (short) (row * bmpWidth + col);
                short topRightIndexNum = (short) (row * bmpWidth + col + 1);
                short bottomLeftIndexNum = (short) ((row + 1) * bmpWidth + col);
                short bottomRightIndexNum = (short) ((row + 1) * bmpWidth + col + 1);

                mVertexData[offset++] = mHeightmapData[topLeftIndexNum];
                mVertexData[offset++] = mHeightmapData[bottomLeftIndexNum];
                mVertexData[offset++] = mHeightmapData[topRightIndexNum];

                mVertexData[offset++] = mHeightmapData[topRightIndexNum];
                mVertexData[offset++] = mHeightmapData[bottomLeftIndexNum];
                mVertexData[offset++] = mHeightmapData[bottomRightIndexNum];
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

    private void createIndexData() {
        //createIndexData
        mIndexData = new short[mVerCounts];
        int offset = 0;
        for (int row = 0; row < bmpHeight - 1; row++) {
            for (int col = 0; col < bmpWidth - 1; col++) {
                short topLeftIndexNum = (short) (row * bmpWidth + col);
                short topRightIndexNum = (short) (row * bmpWidth + col + 1);
                short bottomLeftIndexNum = (short) ((row + 1) * bmpWidth + col);
                short bottomRightIndexNum = (short) ((row + 1) * bmpWidth + col + 1);

                //Write out two triangles.
                //1.
                mIndexData[offset++] = topLeftIndexNum;
                mIndexData[offset++] = bottomLeftIndexNum;
                mIndexData[offset++] = topRightIndexNum;

                //2.
                mIndexData[offset++] = topRightIndexNum;
                mIndexData[offset++] = bottomLeftIndexNum;
                mIndexData[offset++] = bottomRightIndexNum;
            }
        }
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

















