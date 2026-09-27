package raytracer;

public class RenderModeStatic {
    
    static RenderMode FromKey(int key) {
        if (key == 0) {
            return RenderMode.RenderModePraikum0_0_colorGradient;
        }
        if (key == 1) {
            return RenderMode.RenderModePraikum0_1_colorCircle;
        }
        if (key == 2) {
            return RenderMode.RenderModePraikum1_0_cameraColors;
        }
        if (key == 3) {
            return RenderMode.RenderModePraikum1_2_SpehereIntersect;
        }
        if (key == 4) {
            return RenderMode.RenderModePraikum2_3_LambertBounceOne;
        }
        if (key == 5) {
            return RenderMode.RenderModePraikum2_4_PhongBounceOne;
        }
        return RenderMode.RenderModePraikum0_0_colorGradient;
    }

}
