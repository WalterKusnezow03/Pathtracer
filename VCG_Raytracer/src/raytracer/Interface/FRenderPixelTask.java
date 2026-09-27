package raytracer.Interface;

import ui.Window;

import java.awt.image.BufferedImage;

import utils.RgbColor;
import utils.algebra.Vec2;
import utils.algebra.Vec3;

/// Abstraction class which saves: (Imgae, Window, pixelPos) and allows to set a
/// Color immidiatly as result! removes task from the raytracer to set the color
/// inside the
/// raytracing method (this abstraction is needed!)
public class FRenderPixelTask {

    //renderWindow.setPixel(image, color, pixelPosCopy);

    public Window renderWindow;
    public BufferedImage image;
    private Vec2 pos = new Vec2();

    public FRenderPixelTask(Window windowIn, BufferedImage imageIn) {
        renderWindow = windowIn;
        image = imageIn;
    }

    public void SetPixelPosition(int x, int y) {
        pos.x = x;
        pos.y = y;
    }

    public int GetWindowWidth() {
        return renderWindow.GetWidth();
    }

    public int GetWindowHeight() {
        return renderWindow.GetHeight();
    }
    
    public Vec2 GetPixelPosition() {
        return new Vec2(pos);
    }


    /// sets the color of the setup pixel to the given window
    /// and buffered image
    public void SetColor(RgbColor colorResult) {
        Vec2 modified = new Vec2(pos);
        modified.y = (float) GetWindowHeight() - modified.y - 1.0f;
        renderWindow.setPixel(image, colorResult, modified);

        //System.out.println("FRenderPixelTask Set Color " + pos + " " + colorResult);
        //renderWindow.setPixel(image, colorResult, pos);
    }



}
