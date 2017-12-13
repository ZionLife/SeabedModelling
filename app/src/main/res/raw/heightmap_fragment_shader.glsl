#version 120
precision mediump float;
varying vec2 vTextureCorrd; //接收从顶点着色器过来的参数
uniform sampler2D sTexture; //纹理内容数据
void main(){
    //给此片元从纹理中采样出颜色值
    gl_FragColor = texture2D(sTexture, vTextureCorrd);
}
//precision mediump float;
//varying vec3 v_Color;
//
//void main() {
//    gl_FragColor = vec4(v_Color, 1.0);
//}
