package com.HadenW.mariodemo.app;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Haden on 4/18/14.
 */
class GLShaderHelper {
	public static int createShaderProgramHandle(Context context, final int vertex, final int fragment, final String[] strings) {
		final int vertexShaderHandle = compileShader(GLES20.GL_VERTEX_SHADER, readShaderToString(context, vertex));
		final int fragmentShaderHandle = compileShader(GLES20.GL_FRAGMENT_SHADER, readShaderToString(context, fragment));
		return createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle, strings);
	}

	private static int createAndLinkProgram(final int vertexShaderHandle, final int fragmentShaderHandle, final String[] attributes) {
		int programHandle = GLES20.glCreateProgram();
		if (programHandle != 0) {
			GLES20.glAttachShader(programHandle, vertexShaderHandle);
			GLES20.glAttachShader(programHandle, fragmentShaderHandle);

			final int size = attributes.length;
			for (int i = 0; i < size; i++)
				GLES20.glBindAttribLocation(programHandle, i, attributes[i]);

			GLES20.glLinkProgram(programHandle);

			final int[] linkStatus = new int[1];
			GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

			if (linkStatus[0] == 0) {
				Log.e(MyGLRenderer.TAG, "Error compiling program: " + GLES20.glGetProgramInfoLog(programHandle));
				GLES20.glDeleteProgram(programHandle);
				programHandle = 0;
			}
		}
		if (programHandle == 0)
			throw new RuntimeException("Error creating program.");
		return programHandle;
	}

	private static int compileShader(final int shaderType, final String shader) {
		int handle = GLES20.glCreateShader(shaderType);
		if (handle != 0) {
			GLES20.glShaderSource(handle, shader);
			GLES20.glCompileShader(handle);

			final int[] compileStatus = new int[1];
			GLES20.glGetShaderiv(handle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

			if (compileStatus[0] == 0) {
				GLES20.glDeleteShader(handle);
				handle = 0;
			}
		}
		if (handle == 0)
			throw new RuntimeException("Error creating the " + shaderType + " shader");
		return handle;
	}

	private static String readShaderToString(final Context context, final int resourceId) {
		final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(resourceId)));
		final StringBuilder body = new StringBuilder();
		String nextLine;

		try {
			while ((nextLine = bufferedReader.readLine()) != null) {
				body.append(nextLine);
				body.append('\n');
			}
		} catch (IOException e) {
			return null;
		}
		return body.toString();
	}
}