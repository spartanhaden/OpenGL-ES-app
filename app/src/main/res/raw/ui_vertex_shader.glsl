uniform mat4 u_MVPMatrix;			// A constant representing the combined model/view/projection matrix.
uniform mat4 u_MVMatrix;			// A constant representing the combined model/view matrix.

attribute vec4 a_Position;			// Per-vertex position information we will pass in.
attribute vec2 a_TextureCoordinate;	// Texture coordinate we will pass in

varying vec3 v_Position;			// Passed to fragment shader
varying vec2 v_TextureCoordinate;	// Passed to fragment shader

void main(){											// The entry point for our vertex shader.
	v_Position	= vec3(u_MVMatrix * a_Position);		// Transform the vertex into eye space.
	v_TextureCoordinate	= a_TextureCoordinate;			// Pass through the texture coordinate
	gl_Position	= u_MVPMatrix * a_Position;				// gl_Position is a special variable used to store the final position. Multiply the vertex by the matrix to get the final point in normalized screen coordinates.
}