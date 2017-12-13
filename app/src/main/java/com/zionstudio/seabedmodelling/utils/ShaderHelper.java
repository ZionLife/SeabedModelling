package com.zionstudio.seabedmodelling.utils;

import android.util.Log;

import com.zionstudio.seabedmodelling.AppConfig;

import static android.opengl.GLES20.GL_COMPILE_STATUS;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_LINK_STATUS;
import static android.opengl.GLES20.GL_VALIDATE_STATUS;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDeleteProgram;
import static android.opengl.GLES20.glDeleteShader;
import static android.opengl.GLES20.glGetProgramInfoLog;
import static android.opengl.GLES20.glGetProgramiv;
import static android.opengl.GLES20.glGetShaderInfoLog;
import static android.opengl.GLES20.glGetShaderiv;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLES20.glValidateProgram;

/**
 * Created by QiuXi'an on 2017/11/24 0024.
 * Email Zionlife1025@163.com
 */

public class ShaderHelper {
    private static final String TAG = "ShaderHelper";

    public static int compileVertextShader(String shaderCode) {
        return compileShader(GL_VERTEX_SHADER, shaderCode);
    }

    public static int compileFragmentShader(String shaderCode) {
        return compileShader(GL_FRAGMENT_SHADER, shaderCode);
    }

    /**
     * @return the id of the shader object or 0 if failed.
     * @desc compile the shader code
     * @params
     * @author QiuXi'an
     * created at 2017/11/24 0024 22:45
     */

    private static int compileShader(int type, String shaderCode) {
        final int shaderObjectId = glCreateShader(type);

        if (shaderObjectId == 0) {
            Logger.w(TAG, "Could not create new shader.");
            return 0;
        }
        glShaderSource(shaderObjectId, shaderCode);
        glCompileShader(shaderObjectId);
        final int[] compileStatus = new int[1];
        glGetShaderiv(shaderObjectId, GL_COMPILE_STATUS, compileStatus, 0);
        Logger.v(TAG, "Result of compiling source:"
                + "\n" + shaderCode + "\n" + glGetShaderInfoLog(shaderObjectId));

        if (compileStatus[0] == 0) {
            //编译失败
            glDeleteShader(shaderObjectId);
            Logger.w(TAG, "Compilation of shader failed.");
            return 0;
        }

        return shaderObjectId;
    }

    /**
     * @return
     * @desc link the vertex shader and fragment shader together
     * @params
     * @author QiuXi'an
     * created at 2017/11/24 0024 23:06
     */

    public static int linkProgram(int vertexShaderId, int fragmentShaderId) {
        final int programObjectId = glCreateProgram();
        if (programObjectId == 0) {
            Logger.w(TAG, "Could not create new Program");
            return 0;
        }
        glAttachShader(programObjectId, vertexShaderId);
        glAttachShader(programObjectId, fragmentShaderId);

        glLinkProgram(programObjectId);
        final int[] linkStatus = new int[1];
        glGetProgramiv(programObjectId, GL_LINK_STATUS, linkStatus, 0);
        Logger.v(TAG, "Results of linking Program:\n"
                + glGetProgramInfoLog(programObjectId));
        if (linkStatus[0] == 0) {
            //链接失败，删除program对象
            glDeleteProgram(programObjectId);
            Logger.w(TAG, "Linking of Program failed.");
            return 0;
        }
        return programObjectId;
    }

    /**
     * @return
     * @desc 验证program是否有效
     * @params
     * @author QiuXi'an
     * created at 2017/12/5 0005 9:40
     */

    public static boolean validateProgram(int programObjectId) {
        glValidateProgram(programObjectId);

        final int[] validateStatus = new int[1];
        glGetProgramiv(programObjectId, GL_VALIDATE_STATUS, validateStatus, 0);
        Log.v(TAG, "Results of validating Program: "
                + validateStatus[0] + "\nLog: " + glGetProgramInfoLog(programObjectId));

        return validateStatus[0] != 0;
    }

    public static int buildProgram(String vertexShaderSource, String fragmentShaderSource) {
        int program;

        //compile the shader
        int vertexShader = compileVertextShader(vertexShaderSource);
        int fragmentShader = compileFragmentShader(fragmentShaderSource);

        //link them into a shader Program
        program = linkProgram(vertexShader, fragmentShader);

        if (AppConfig.IS_DEBUG) {
            validateProgram(program);
        }
        return program;
    }
}
