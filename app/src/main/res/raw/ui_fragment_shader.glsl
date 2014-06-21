precision mediump float;			// Set the default precision to medium. We don't need as high of a precision in the fragment shader.
uniform vec3 u_LightPos;			// The position of the light in eye space.
uniform sampler2D u_Texture;		// The input texture.

varying vec3 v_Position;			// Interpolated position for this fragment.
varying vec2 v_TextureCoordinate;	// Interpolated texture coordinate per fragment.

void main(){															// The entry point for our fragment shader.
	float distance		= length(u_LightPos - v_Position);				// Will be used for attenuation.
	vec3 lightVector	= normalize(u_LightPos - v_Position);			// Get a lighting direction vector from the light to the vertex.
	gl_FragColor		= texture2D(u_Texture, v_TextureCoordinate);	// Multiply the color by the diffuse illumination level to get final output color.
	gl_FragColor.rgb	*= 0.8;											// Multiply the RGB by the diffuse light.
}