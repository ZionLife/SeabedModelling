package com.zionstudio.seabedmodelling;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import static com.zionstudio.seabedmodelling.Constants.BYTES_PER_FLOAT;

/**
 * Created by QiuXi'an on 2017/12/12 0012.
 * Email Zionlife1025@163.com
 */

public class Seabed {
    private static final int POSITION_COMPONENT_COUNT = 3;
    private static final int TEXTURE_COORDINATES_COMPONENT_COUNT = 2;
    private static final int STRIDE = (POSITION_COMPONENT_COUNT + TEXTURE_COORDINATES_COMPONENT_COUNT) * BYTES_PER_FLOAT;

    public Seabed(SeabedSurfaceView view) {
        initVertexData();
        initShader(view);
    }

    private void initShader(SeabedSurfaceView view) {

    }

    private void initVertexData() {
        //从灰度图片中加载陆地上每个顶点的高度
        Bitmap bmp = BitmapFactory.decodeResource(MyApplication.mContext.getResources(), R.mipmap.land);
        int colsPlusOne = bmp.getWidth(); //列数
        int rowsPlusOne = bmp.getHeight(); //行数
        float[][] result = new float[rowsPlusOne][colsPlusOne];

        final float LAND_HEIGHT_ADJUST = -2f; //陆地的高度调整
        final float LAND_HIGHEST = 20f; //陆地最大高差
        //遍历灰度图像
        for (int i = 0; i < rowsPlusOne; i++) {
            for (int j = 0; j < colsPlusOne; j++) {
                int color = bmp.getPixel(j, i);
                int r = Color.red(color);
                int g = Color.green(color);
                int b = Color.blue(color);

                int h = (r + g + b) / 3;
                //像素点的海拔高度 = 最大高差X像素值 / 255.0 + 最低海拔
                result[i][j] = h * LAND_HIGHEST / 255 + LAND_HEIGHT_ADJUST;
            }
        }

    }
}
