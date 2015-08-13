#version 150

in float input;

out float output;

void main(){
	output = input + 0.1f;
	gl_Position = vec4(input, 0, 0, 1);
}