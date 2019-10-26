#version 140

in vec3 position;		// vertex position in local space
in vec3 normal;			// vertex normal in local space

out vec3 frag_normal;	// fragment normal in world space
out vec2 frag_texcoord;	// fragment normal in world space

uniform mat4 mvp_matrix; // model-view-projection matrix

const float MAP_SIZE = 10;

void main()
{
    // Typicaly normal is transformed by the model matrix
    // Since the model matrix is identity in our case, we do not modify normals
    frag_normal = normal;

    frag_texcoord = vec2((position.x + MAP_SIZE/2)/MAP_SIZE, (position.z + MAP_SIZE/2)/MAP_SIZE);

    // The position is projected to the screen coordinates using mvp_matrix
	gl_Position = mvp_matrix * vec4(position, 1.0);
}