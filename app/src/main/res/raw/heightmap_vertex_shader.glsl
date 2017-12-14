uniform mat4 uMVPMatrix; //总变换矩阵
attribute vec3 aPosition; //顶点位置
attribute vec2 aTexCoor; //顶点纹理坐标
varying vec2 vTextureCoord; //用于传递给片元着色器的变量

void main(){
    gl_Position = uMVPMatrix * vec4(aPosition, 1); //根据总变换矩阵计算此次绘制顶点的位置
    vTextureCoord = aTexCoor; //将接收的纹理坐标传递给片元着色器
}
//uniform mat4 u_Matrix;
//attribute vec3 a_Position;
//varying vec3 v_Color;
//
//void main() {
//    v_Color = mix(vec3(0.180, 0.467,0.153),
//                  vec3(0.660, 0.670, 0.680),
//                  a_Position.y
//                );
//
//    gl_Position = u_Matrix * vec4(a_Position, 1.0);
//}
