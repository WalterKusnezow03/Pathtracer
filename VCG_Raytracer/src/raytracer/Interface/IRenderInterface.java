

package raytracer.Interface;

import scene.Scene;
import ui.Window;
import utils.*;
import utils.io.Log;

import java.awt.image.BufferedImage;
import utils.algebra.Vec2;
import utils.algebra.Vec3;
import raytracer.RenderMode;
import raytracer.Interface.FRenderPixelTask;


/**
 * This is the basic render interface to
 * set the rendermode (used of supported)
 * and allows to perform a Render Pixel task 
 * (description inside the class)
 */
public interface IRenderInterface {

    void SetRenderMode(RenderMode renderModeIn);
    void renderPixel(FRenderPixelTask task);

    //void renderPixel(int x, int y, Window mRenderWindowIn, BufferedImage image);
    //void renderPixel(Vec2 pos, Window mRenderWindowIn, BufferedImage image);
}
