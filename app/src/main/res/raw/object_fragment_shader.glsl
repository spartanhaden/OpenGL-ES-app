precision mediump float;			// Set the default precision to medium. We don't need as high of a precision in the fragment shader.
uniform vec3 u_LightPos;			// The position of the light in eye space.
uniform sampler2D u_Texture;		// The input texture.

varying vec3 v_Position;			// Interpolated position for this fragment.
varying vec3 v_Normal;				// Interpolated normal for this fragment.
varying vec2 v_TextureCoordinate;	// Interpolated texture coordinate per fragment.

void main(){															// The entry point for our fragment shader.
	float distance		= length(u_LightPos - v_Position);				// Will be used for attenuation.
	vec3 lightVector	= normalize(u_LightPos - v_Position);			// Get a lighting direction vector from the light to the vertex.
	float diffuse		= max(dot(v_Normal, lightVector), 0.1);			// Calculate the dot product of the light vector and vertex normal. If the normal and light vector are pointing in the same direction then it will get max illumination.
	diffuse				= diffuse * (1.0 / (1.0 + (0.1 * distance)));	// Add attenuation.
	diffuse				+= 0.3;											// Add ambient lighting.
	gl_FragColor		= texture2D(u_Texture, v_TextureCoordinate);	// Multiply the color by the diffuse illumination level to get final output color.
	gl_FragColor.rgb	*= diffuse;										// Multiply the RGB by the diffuse light.
}