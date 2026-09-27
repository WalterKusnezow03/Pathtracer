package raytracer;

import raytracer.RenderMode;


import scene.Scene;
import ui.Window;
import utils.*;
import utils.io.Log;

import java.awt.image.BufferedImage;
import utils.algebra.Vec2;
import utils.algebra.Vec3;
import raytracer.Interface.FRenderPixelTask;
import raytracer.Interface.IRenderInterface;
import raytracer.ScreenToUV;


/// this class will render, either the color gradient or color circle
public class Praktikum0Renderer implements IRenderInterface{

    private RenderMode renderMode;
    private Vec3 colorA = new Vec3(1, 0, 0);
    private Vec3 colorB = new Vec3(0, 0, 1);
    private RgbColor color = new RgbColor(0,0,0);
    
    @Override
    public void SetRenderMode(RenderMode renderModeIn) {
        renderMode = renderModeIn;
    }

    public Praktikum0Renderer(RenderMode renderModeIn) {
        SetRenderMode(renderModeIn);
    }
    

    public void renderPixel(FRenderPixelTask task) {
        renderPixel(
            task.GetPixelPosition(),
            task.renderWindow,
            task.image
        );      
    }



    // ---- to be refractured ----

    public void renderPixel(int x, int y, Window renderWindow, BufferedImage image){
        Vec2 pos = new Vec2(x,y);
        renderPixel(pos, renderWindow, image);
    }

    public void renderPixel(Vec2 pos, Window renderWindow, BufferedImage image){
        if(renderMode == RenderMode.RenderModePraikum0_0_colorGradient){
            renderPixelTask0(pos, renderWindow, image);
            return;
        }
        if(renderMode == RenderMode.RenderModePraikum0_1_colorCircle){
            renderPixelTask1(pos, renderWindow, image);
            //renderPixelTask1_new(pos, renderWindow, image, depth);
            return;
        }
    }

    private void renderPixelTask0(Vec2 pos, Window renderWindow, BufferedImage image){
        int Width = renderWindow.GetWidth();
        int Height = renderWindow.GetHeight();

        float scalarX = (float) pos.x / (float) Width;
        Vec3 colorAFrac = colorA.multScalar(scalarX);
        Vec3 colorBFrac = colorB.multScalar(1.0f - scalarX);
        Vec3 gX = colorAFrac.add(colorBFrac);
        color.SetColor(gX);
        renderWindow.setPixel(image, color, pos);
    }



     private void renderPixelTask1(Vec2 pos, Window renderWindow, BufferedImage image){
        Vec2 pixelPosCopy = new Vec2(pos);        

        //made with UVS
        //ScreenToUV.MakeScreenToUV(pos, renderWindow.GetWidth(), renderWindow.GetHeight()); //-1,1
        
        //uvs from 0 to 1
        ScreenToUV.MakeScreenToUV01(pos, renderWindow.GetWidth(), renderWindow.GetHeight());
        //System.out.println("UV "+ pos.x + " " + pos.y);

        Vec2 center = new Vec2(0.5f, 0.5f);
        pos = center.sub(pos);//-0.5 to 0.5
        pos = pos.normalize();//-1 to 1

        float zValue = 0.0f;
        if(pos.x < 0.0f){
            zValue += Math.abs(pos.x);
        }
        if(pos.y < 0.0f){
            zValue += Math.abs(pos.y);
        }
        if(zValue > 1.0f){
            zValue = 1.0f; //clamp
        }

        //-1, 1 to 0,1 for color 
        pos.x += 1.0f;
        pos.y += 1.0f;
        pos.x *= 0.5f;
        pos.y *= 0.5f;

        Vec3 asColor = new Vec3(pos.x, pos.y, zValue);
        color.SetColor(asColor);
        renderWindow.setPixel(image, color, pixelPosCopy);
        return;


    }



    






}