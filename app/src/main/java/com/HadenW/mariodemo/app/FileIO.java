package com.HadenW.mariodemo.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Haden on 4/19/14.
 */
class FileIO {
	public static int[][] arrayFromRaw_TSVFile(final Context context, final int resourceId) {
		final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(resourceId)));
		String line;
		String[] lineArray;
		int[][] values = new int[25][];
		int xValue = 0;
		try {
			while ((line = bufferedReader.readLine()) != null) {
				lineArray = line.split("\t");
				values[xValue] = new int[lineArray.length];
				for (int i = 0; i < lineArray.length; i++)
					if (lineArray[i].equals(""))
						values[xValue][i] = 0;
					else
						values[xValue][i] = Integer.valueOf(lineArray[i]);
				xValue++;
			}
			return values;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new int[0][0];
	}

	public static int loadTexture(final Context context, final int resourceId) {
		final int[] textureHandle = new int[1];
		GLES20.glGenTextures(1, textureHandle, 0);
		if (textureHandle[0] != 0) {
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inScaled = false;    // No pre-scaling
			final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);    // Set filtering.
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);    // Set filtering.
			GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);    // Load the bitmap into the bound texture.
			bitmap.recycle();    // Recycle the bitmap, since its data has already been loaded into OpenGL
		}
		if (textureHandle[0] == 0)
			throw new RuntimeException("Error loading the texture.");
		return textureHandle[0];
	}
}