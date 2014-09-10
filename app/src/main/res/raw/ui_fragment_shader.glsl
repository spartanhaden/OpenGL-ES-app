precision mediump float;	// Set the default precision to medium. We don't need as high of a precision in the fragment shader.

varying vec4 v_Color;		// Interpolated texture coordinate per fragment.

void main(){				// The entry point for the fragment shader.
	gl_FragColor = v_Color;	// Set the output color to the input color.
}