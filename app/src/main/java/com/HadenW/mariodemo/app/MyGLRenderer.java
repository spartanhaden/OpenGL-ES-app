package com.HadenW.mariodemo.app;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by Haden Wasserbaech on 2/11/14.
 */
class MyGLRenderer implements GLSurfaceView.Renderer {
	public static final String TAG = "HadenOpenGL";    // Used for debug logs
	private static final int BYTES_PER_FLOAT = 4;
	private static final int POSITION_DATA_SIZE = 3;        // Size of position data in elements
	private static final int NORMAL_DATA_SIZE = 3;    // Size of normal data in elements
	private static final int TEXTURE_COORDINATE_DATA_SIZE = 2;    // Size of the texture coordinate data in elements.
	private static final int STRIDE = (POSITION_DATA_SIZE + NORMAL_DATA_SIZE + TEXTURE_COORDINATE_DATA_SIZE) * BYTES_PER_FLOAT;
	private FloatBuffer mCubePackedBuffer;// Store our model data in a float buffer.
	private FloatBuffer mSquarePackedBuffer;
	private final Context mActivityContext;
	private final float[] mLightPosInModelSpace = new float[]{0f, 0f, 0f, 1f};    // Holds light centered on origin in model space. need 4th coordinate so we can get translations to work when we multiply by transformation matrices
	private final float[] mLightPosInWorldSpace = new float[4];    // Holds current position of light in world space (after transformation via model matrix)
	private final float[] mLightPosInEyeSpace = new float[4];      // Holds transformed position of light in eye space (after transformation via modelview matrix)
	private final float[] mModelMatrix = new float[16];         // Store the model matrix. This matrix is used to move models from object space (where each model can be thought of being located at the center of the universe) to world space.
	private final float[] mViewMatrix = new float[16];          // Store the view matrix. This can be thought of as our camera. This matrix transforms world space to eye space; it positions things relative to our eye.
	private final float[] mProjectionMatrix = new float[16];    // Store the projection matrix. This is used to project the scene onto a 2D viewport
	private final float[] mOrthographicMatrix = new float[16];    // Store the orthographic matrix. This is used to project the ui onto the 2D viewport
	private final float[] mMVPMatrix = new float[16];           // Allocate storage for the final combined matrix. This will be passed into the shader program
	private final float[] mLightModelMatrix = new float[16];    // Stores a copy of the model matrix specifically for the light position
	private int mMVPMatrixHandle;    // Pass in the transformation matrix
	private int mMVMatrixHandle;     // Pass in the modelview matrix
	private int mLightPosHandle;     // Pass in the light position
	private int mPositionHandle;     // Pass in model position information
	private int mNormalHandle;       // Pass in model normal information
	private int mTextureUniformHandle;      // This will be used to pass in the texture.
	private int mTextureCoordinateHandle;   // This will be used to pass in model texture coordinate information.
	private int mTextureDataHandle;         // This is a handle to our texture data
	private int squareDataHandle;
	private int atomDataHandle;
	private int pointMVPMatrixHandle;
	private int pointPositionHandle;

	private int mObjectShaderHandle;    // Handle to per-vertex cube shading program
	private int mLightShaderHandle;        // Handle to light point program
	private int mUIShaderHandle;        // Handle to UI shader program

	private float dX = 0;
	private float dY = 0;
	private float angleX = 0;
	private float angleY = 0;
	private float shiftX = 0;
	private float shiftY = -8;

	private int[][] level;

	private int totalCubes = 1;

	private Cubes cubes;

	private float pX = 2f;
	private float pY = 5f;

	public MyGLRenderer(final Context context) {
		mActivityContext = context;
	}

	public void initData() {
		level = FileIO.arrayFromRaw_TSVFile(mActivityContext, R.raw.level);
		final float cubePositionData[] = {
				// Front face
				-1f, 1f, 1f,
				-1f, -1f, 1f,
				1f, 1f, 1f,
				-1f, -1f, 1f,
				1f, -1f, 1f,
				1f, 1f, 1f,

				// Right face
				1f, 1f, 1f,
				1f, -1f, 1f,
				1f, 1f, -1f,
				1f, -1f, 1f,
				1f, -1f, -1f,
				1f, 1f, -1f,

				// Back face
				1f, 1f, -1f,
				1f, -1f, -1f,
				-1f, 1f, -1f,
				1f, -1f, -1f,
				-1f, -1f, -1f,
				-1f, 1f, -1f,

				// Left face
				-1f, 1f, -1f,
				-1f, -1f, -1f,
				-1f, 1f, 1f,
				-1f, -1f, -1f,
				-1f, -1f, 1f,
				-1f, 1f, 1f,

				// Top face
				-1f, 1f, -1f,
				-1f, 1f, 1f,
				1f, 1f, -1f,
				-1f, 1f, 1f,
				1f, 1f, 1f,
				1f, 1f, -1f,

				// Bottom face
				1f, -1f, -1f,
				1f, -1f, 1f,
				-1f, -1f, -1f,
				1f, -1f, 1f,
				-1f, -1f, 1f,
				-1f, -1f, -1f
		};
		// The normal is used in light calculations and is a vector which points orthogonal to the plane of the surface. For a cube model, the normals should be orthogonal to the points of each face.
		final float[] cubeNormalData = {
				// Front face
				0f, 0f, 1f,
				0f, 0f, 1f,
				0f, 0f, 1f,
				0f, 0f, 1f,
				0f, 0f, 1f,
				0f, 0f, 1f,

				// Right face
				1f, 0f, 0f,
				1f, 0f, 0f,
				1f, 0f, 0f,
				1f, 0f, 0f,
				1f, 0f, 0f,
				1f, 0f, 0f,

				// Back face
				0f, 0f, -1f,
				0f, 0f, -1f,
				0f, 0f, -1f,
				0f, 0f, -1f,
				0f, 0f, -1f,
				0f, 0f, -1f,

				// Left face
				-1f, 0f, 0f,
				-1f, 0f, 0f,
				-1f, 0f, 0f,
				-1f, 0f, 0f,
				-1f, 0f, 0f,
				-1f, 0f, 0f,

				// Top face
				0f, 1f, 0f,
				0f, 1f, 0f,
				0f, 1f, 0f,
				0f, 1f, 0f,
				0f, 1f, 0f,
				0f, 1f, 0f,

				// Bottom face
				0f, -1f, 0f,
				0f, -1f, 0f,
				0f, -1f, 0f,
				0f, -1f, 0f,
				0f, -1f, 0f,
				0f, -1f, 0f
		};
		final float[] cubeTextureCoordinateData = {
				// Front face
				0f, 0f,
				0f, 1f,
				1f, 0f,
				0f, 1f,
				1f, 1f,
				1f, 0f,

				// Right face
				0f, 0f,
				0f, 1f,
				1f, 0f,
				0f, 1f,
				1f, 1f,
				1f, 0f,

				// Back face
				0f, 0f,
				0f, 1f,
				1f, 0f,
				0f, 1f,
				1f, 1f,
				1f, 0f,

				// Left face
				0f, 0f,
				0f, 1f,
				1f, 0f,
				0f, 1f,
				1f, 1f,
				1f, 0f,

				// Top face
				0f, 0f,
				0f, 1f,
				1f, 0f,
				0f, 1f,
				1f, 1f,
				1f, 0f,

				// Bottom face
				0f, 0f,
				0f, 1f,
				1f, 0f,
				0f, 1f,
				1f, 1f,
				1f, 0f
		};
		final float[] squarePositionData = {
				-1f, 2f, 1f,
				-1f, -2f, 1f,
				1f, 2f, 1f,
				-1f, -2f, 1f,
				1f, -2f, 1f,
				1f, 2f, 1f
		};
		final float[] squareNormalData = {
				0f, 0f, 1f,
				0f, 0f, 1f,
				0f, 0f, 1f,
				0f, 0f, 1f,
				0f, 0f, 1f,
				0f, 0f, 1f
		};
		final float[] squareTextureCoordinateData = {
				0f, 0f,
				0f, 1f,
				1f, 0f,
				0f, 1f,
				1f, 1f,
				1f, 0f
		};
		mCubePackedBuffer = getPackedBuffer(cubePositionData, cubeNormalData, cubeTextureCoordinateData);
		mSquarePackedBuffer = getPackedBuffer(squarePositionData, squareNormalData, squareTextureCoordinateData);
		cubes = new Cubes(getPackedBuffer(cubePositionData, cubeNormalData, cubeTextureCoordinateData));
		//squareVBO = setupVBO(getPackedBuffer(squarePositionData, squareNormalData, squareTextureCoordinateData));

		GLES20.glClearColor(.5f, .75f, 1f, 0f);

		GLES20.glEnable(GLES20.GL_CULL_FACE);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);

		// Eye behind origin
		final float eyeX = 0f;
		final float eyeY = 0f;
		final float eyeZ = -.5f;

		// Look at
		final float lookX = 0f;
		final float lookY = 0f;
		final float lookZ = -5f;

		// Direction pointing head
		final float upX = 0f;
		final float upY = 1f;
		final float upZ = 0f;

		// Set view matrix
		Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

		mObjectShaderHandle = GLShaderHelper.createShaderProgramHandle(mActivityContext, R.raw.object_vertex_shader, R.raw.object_fragment_shader, new String[]{"a_Position", "a_Color", "a_Normal", "a_TextureCoordinate"});
		mLightShaderHandle = GLShaderHelper.createShaderProgramHandle(mActivityContext, R.raw.light_vertex_shader, R.raw.light_fragment_shader, new String[]{"a_Position"});
		mUIShaderHandle = GLShaderHelper.createShaderProgramHandle(mActivityContext, R.raw.ui_vertex_shader, R.raw.ui_fragment_shader, new String[]{"a_Position", "a_Color", "a_Normal", "a_TextureCoordinate"});

		mMVPMatrixHandle = GLES20.glGetUniformLocation(mObjectShaderHandle, "u_MVPMatrix");
		mMVMatrixHandle = GLES20.glGetUniformLocation(mObjectShaderHandle, "u_MVMatrix");
		mLightPosHandle = GLES20.glGetUniformLocation(mObjectShaderHandle, "u_LightPos");
		mTextureUniformHandle = GLES20.glGetUniformLocation(mObjectShaderHandle, "u_Texture");
		mPositionHandle = GLES20.glGetAttribLocation(mObjectShaderHandle, "a_Position");
		mNormalHandle = GLES20.glGetAttribLocation(mObjectShaderHandle, "a_Normal");
		mTextureCoordinateHandle = GLES20.glGetAttribLocation(mObjectShaderHandle, "a_TextureCoordinate");

		pointMVPMatrixHandle = GLES20.glGetUniformLocation(mLightShaderHandle, "u_MVPMatrix");
		pointPositionHandle = GLES20.glGetAttribLocation(mLightShaderHandle, "a_Position");

		mTextureDataHandle = FileIO.loadTexture(mActivityContext, R.drawable.brick_block);
		squareDataHandle = FileIO.loadTexture(mActivityContext, R.drawable.robot_cropped);
		atomDataHandle = FileIO.loadTexture(mActivityContext, R.drawable.atom);
	}

	public void changeAngle(final float x, final float y) {
		dX = x;
		dY = y;
		angleX += x;
		angleY += y;
	}

	public void changeX(final float x) {
		shiftX += x;
	}

	public void changeY(final float y) {
		shiftY += y;
	}

	public void moveP(final float x, final float y) {
		pX += x;
		pY += y;
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		initData();
	}

	@Override
	public void onSurfaceChanged(GL10 gl, final int width, final int height) {
		final float ratio = (float) width / height;    //right = ratio left = -ratio
		final float bottom = -1f;
		final float top = 1f;
		final float near = 1f;
		final float far = 100f;

		final float orthoSize = 20f;

		GLES20.glViewport(0, 0, width, height);
		Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, bottom, top, near, far);
		Matrix.orthoM(mOrthographicMatrix, 0, -ratio * orthoSize, ratio * orthoSize, -50f, 50f, near, far);
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		float angleInDegrees = (360f / 10000f) * ((int) SystemClock.uptimeMillis() % 10000L);

		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		GLES20.glUseProgram(mObjectShaderHandle);

		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		GLES20.glEnable(GLES20.GL_BLEND);

		Matrix.setIdentityM(mLightModelMatrix, 0);
		Matrix.translateM(mLightModelMatrix, 0, 0f, 0f, -5f);
		Matrix.rotateM(mLightModelMatrix, 0, angleInDegrees, 0f, 1f, 0f);
		Matrix.translateM(mLightModelMatrix, 0, 0f, 0f, 2f);
		Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
		Matrix.multiplyMV(mLightPosInEyeSpace, 0, mViewMatrix, 0, mLightPosInWorldSpace, 0);

		Matrix.setIdentityM(mModelMatrix, 0);
		Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -3.5f);

		setView();

		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);// Set the active texture unit to texture unit 0
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);// Bind the texture to this unit
		GLES20.glUniform1i(mTextureUniformHandle, 0);

		for (int x = 0; x < level.length; x++) {
			for (int y = 0; y < level[x].length; y++) {
				if (level[x][y] == 1) {
					drawCube(x * 2, y * 2, -10f);
				}
			}
		}
		//cubes.render();

		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, squareDataHandle);
		drawSquare(pX, pY, -11f);

		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, atomDataHandle);
		drawHUD();

		GLES20.glUseProgram(mLightShaderHandle);
		drawLight();
	}

	private void setView() {
		Matrix.setIdentityM(mViewMatrix, 0);
		Matrix.rotateM(mViewMatrix, 0, angleX, 0f, 1f, 0f);
		Matrix.rotateM(mViewMatrix, 0, angleY, 1f, 0f, 0f);
		Matrix.translateM(mViewMatrix, 0, shiftX, shiftY, 0f);
		angleX += dX;
		angleY += dY;
		dX *= .9f;
		dY *= .9f;
	}

	private FloatBuffer getPackedBuffer(final float[] positions, final float[] normals, final float[] textureCoordinates) {
		final int totalObjects = positions.length / normals.length;
		final int dataLength = positions.length + totalObjects * (normals.length + textureCoordinates.length);
		final int vertices = positions.length / POSITION_DATA_SIZE;
		int positionOffset = 0;
		int normalOffset = 0;
		int textureOffset = 0;

		final FloatBuffer buffer = ByteBuffer.allocateDirect(dataLength * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();

		for (int l = 0; l < totalObjects; l++) {
			for (int i = 0; i < vertices; i++) {
				buffer.put(positions, positionOffset, POSITION_DATA_SIZE);
				positionOffset += POSITION_DATA_SIZE;
				buffer.put(normals, normalOffset, NORMAL_DATA_SIZE);
				normalOffset += NORMAL_DATA_SIZE;
				buffer.put(textureCoordinates, textureOffset, TEXTURE_COORDINATE_DATA_SIZE);
				textureOffset += TEXTURE_COORDINATE_DATA_SIZE;
			}
			normalOffset = 0;
			textureOffset = 0;
		}

		buffer.position(0);

		return buffer;
	}

	private void drawSquare(final float x, final float y, final float z) {
		Matrix.setIdentityM(mModelMatrix, 0);
		Matrix.translateM(mModelMatrix, 0, x, y, z);

		GLES20.glEnableVertexAttribArray(mPositionHandle);
		GLES20.glEnableVertexAttribArray(mNormalHandle);
		GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);

		mSquarePackedBuffer.position(0);
		GLES20.glVertexAttribPointer(mPositionHandle, POSITION_DATA_SIZE, GLES20.GL_FLOAT, false, STRIDE, mSquarePackedBuffer);
		mSquarePackedBuffer.position(POSITION_DATA_SIZE);
		GLES20.glVertexAttribPointer(mNormalHandle, NORMAL_DATA_SIZE, GLES20.GL_FLOAT, false, STRIDE, mSquarePackedBuffer);
		mSquarePackedBuffer.position(POSITION_DATA_SIZE + NORMAL_DATA_SIZE);
		GLES20.glVertexAttribPointer(mTextureCoordinateHandle, TEXTURE_COORDINATE_DATA_SIZE, GLES20.GL_FLOAT, false, STRIDE, mSquarePackedBuffer);

		Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);// Multiplies view matrix by model matrix, stores result in MVP matrix (which currently contains model * view).
		GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);// Pass in MVMatrix
		Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);// Multiplies modelview matrix by projection matrix, stores result in MVP matrix (which now contains model * view * projection)
		GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);// Pass in combined matrix
		GLES20.glUniform3f(mLightPosHandle, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);// Pass in light position in eye space

		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
	}

	private void drawHUD() {

	}

	private void drawCube(final float x, final float y, final float z) {
		Matrix.setIdentityM(mModelMatrix, 0);
		Matrix.translateM(mModelMatrix, 0, x, y, z);

		GLES20.glEnableVertexAttribArray(mPositionHandle);
		GLES20.glEnableVertexAttribArray(mNormalHandle);
		GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);

		mCubePackedBuffer.position(0);
		GLES20.glVertexAttribPointer(mPositionHandle, POSITION_DATA_SIZE, GLES20.GL_FLOAT, false, STRIDE, mCubePackedBuffer);
		mCubePackedBuffer.position(POSITION_DATA_SIZE);
		GLES20.glVertexAttribPointer(mNormalHandle, NORMAL_DATA_SIZE, GLES20.GL_FLOAT, false, STRIDE, mCubePackedBuffer);
		mCubePackedBuffer.position(POSITION_DATA_SIZE + NORMAL_DATA_SIZE);
		GLES20.glVertexAttribPointer(mTextureCoordinateHandle, TEXTURE_COORDINATE_DATA_SIZE, GLES20.GL_FLOAT, false, STRIDE, mCubePackedBuffer);

		Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);// Multiplies view matrix by model matrix, stores result in MVP matrix (which currently contains model * view).
		GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);// Pass in MVMatrix
		Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);// Multiplies modelview matrix by projection matrix, stores result in MVP matrix (which now contains model * view * projection)
		GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);// Pass in combined matrix
		GLES20.glUniform3f(mLightPosHandle, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);// Pass in light position in eye space

		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);
	}

	private void drawLight() {
		GLES20.glVertexAttrib3f(pointPositionHandle, mLightPosInModelSpace[0], mLightPosInModelSpace[1], mLightPosInModelSpace[2]);
		GLES20.glDisableVertexAttribArray(pointPositionHandle);

		Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mLightModelMatrix, 0);
		Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
		GLES20.glUniformMatrix4fv(pointMVPMatrixHandle, 1, false, mMVPMatrix, 0);

		GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1);
	}

	class Cubes {
		final int bufferObject;

		public Cubes(final FloatBuffer buffer) {
			final int buffers[] = new int[1];
			GLES20.glGenBuffers(1, buffers, 0);

			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[0]);
			GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, buffer.capacity() * BYTES_PER_FLOAT, buffer, GLES20.GL_STATIC_DRAW);

			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

			buffer.limit(0);
			bufferObject = buffers[0];
		}

		public void render() {
			Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);// Multiplies view matrix by model matrix, stores result in MVP matrix (which currently contains model * view).
			GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);// Pass in MVMatrix
			Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);// Multiplies modelview matrix by projection matrix, stores result in MVP matrix (which now contains model * view * projection)
			GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);// Pass in combined matrix
			GLES20.glUniform3f(mLightPosHandle, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);// Pass in light position in eye space

			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufferObject);
			GLES20.glEnableVertexAttribArray(mPositionHandle);
			GLES20.glVertexAttribPointer(mPositionHandle, POSITION_DATA_SIZE, GLES20.GL_FLOAT, false, STRIDE, 0);

			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufferObject);
			GLES20.glEnableVertexAttribArray(mNormalHandle);
			GLES20.glVertexAttribPointer(mNormalHandle, NORMAL_DATA_SIZE, GLES20.GL_FLOAT, false, STRIDE, POSITION_DATA_SIZE * BYTES_PER_FLOAT);

			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufferObject);
			GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);
			GLES20.glVertexAttribPointer(mTextureCoordinateHandle, TEXTURE_COORDINATE_DATA_SIZE, GLES20.GL_FLOAT, false, STRIDE, (POSITION_DATA_SIZE + NORMAL_DATA_SIZE) * BYTES_PER_FLOAT);

			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
			GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36 * totalCubes);
		}

		public void release() {
			GLES20.glDeleteBuffers(1, new int[]{bufferObject}, 0);
		}
	}
}