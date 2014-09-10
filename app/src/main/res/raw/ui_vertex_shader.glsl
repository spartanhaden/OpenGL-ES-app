uniform mat4 u_MVPMatrix;					// A constant representing the combined model/view matrix.

attribute vec4 a_Position;					// Per-vertex position information to be passed in.
attribute vec4 a_Color;						// Per-vertex color information to be passed in.

varying vec4 v_Color;						// Passed to fragment shader

void main(){								// The entry point for our vertex shader.
	v_Color = a_Color;
	gl_Position	= u_MVPMatrix * a_Position;	// gl_Position is a special variable used to store the final position. Multiply the vertex by the matrix to get the final point in normalized screen coordinates.
}