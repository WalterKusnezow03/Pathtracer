package raytracer;

import utils.algebra.Vec2;

/// allows a screen coordinate to be converted to
/// -1,1 space or in 0,1 for a given width and height
public class ScreenToUV {

    //converts a pixel coordinate to uv space in [-1,1]^2
    public static void MakeScreenToUV(Vec2 pos, float viewPortWidth, float viewPortHeight){

        //center pixel / sizeViewPort
        //scalar = distTarget / distAll
        //*2  -1 = to center

        float u = ((pos.x + 0.5f) / viewPortWidth) * 2.0f - 1.0f;
        float v = ((pos.y + 0.5f) / viewPortHeight) * 2.0f - 1.0f;
        pos.x = u;
        pos.y = v;
    }

    //converts a pixel coordinate to uv space in [0,w][0,h]
    public static void MakeScreenToUV01(Vec2 pos, float viewPortWidth, float viewPortHeight){

        //center pixel / sizeViewPort
        float u = (pos.x + 0.5f) / viewPortWidth;
        float v = (pos.y + 0.5f) / viewPortHeight;
        pos.x = u;
        pos.y = v;
    }

};