/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    1. Send primary ray
    2. intersection test with all shapes
    3. if hit:
    3a: send secondary ray to the light source
    3b: 2
        3b.i: if hit:
            - Shape is in the shade
            - Pixel color = ambient value
        3b.ii: in NO hit:
            - calculate local illumination
    4. if NO hit:
        - set background color

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/

package raytracer;

import scene.Scene;
import ui.Window;
import utils.*;
import utils.io.Log;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import utils.algebra.Vec2;
import utils.algebra.Vec3;

import raytracer.RenderMode;
import raytracer.Interface.FRenderPixelTask;
import raytracer.Interface.IKeyListener;
import raytracer.Interface.IRenderInterface;


public class Raytracer extends IKeyListener{

    private BufferedImage mBufferedImage;

    private Scene mScene;
    private Window mRenderWindow;

    private int mMaxRecursions;

    private RgbColor mBackgroundColor;
    private RgbColor mAmbientLight;

    private int mAntiAliasingSamples;

    private boolean mDebug;
    private long tStart;

    //shared render mode
    private RenderMode renderMode = //RenderMode.RenderModePraikum1_0_cameraColors;
            RenderMode.RenderModePraikum1_2_SpehereIntersect;
    
    private Praktikum0Renderer renderParktikum0;

    /**  Constructor **/
    public Raytracer(Scene scene, Window renderWindow, int recursions, RgbColor backColor, RgbColor ambientLight,
            int antiAliasingSamples, boolean debugOn, float ambientCoefficent) {
        super();
        Log.print(this, "Init");
        mMaxRecursions = recursions;
        scene.setBounces(recursions);

        mBufferedImage = renderWindow.getBufferedImage();

        mAntiAliasingSamples = antiAliasingSamples;

        mBackgroundColor = backColor;
        mAmbientLight = ambientLight;
        mScene = scene;
        mRenderWindow = renderWindow;
        mDebug = debugOn;
        tStart = System.currentTimeMillis();

        mScene.SetBackGroundColor(backColor);
        mScene.SetAmbientLight(ambientLight, ambientCoefficent);

        //set render mode here for different tasks!
        renderParktikum0 = new Praktikum0Renderer(renderMode); //shared render mode, not result if nothing happens
        mScene.SetRenderMode(renderMode);

        RegisterToWindow();

        //update render mode raw - no auto render.
        if (antiAliasingSamples == 1) {
            UpdateRenderMode(RenderMode.RenderModePraikum3_6_PhongBounceNRefract);
        }
        if (antiAliasingSamples > 1) {
            mScene.SetupAA(true, antiAliasingSamples);
        }
    }
    
    




    /**  Send the created window to the frame delivered by JAVA to display our result **/
    public void exportRendering(){
        mRenderWindow.exportRendering(String.valueOf(stopTime(tStart)), mScene.GetNumBounces(), mAntiAliasingSamples, mDebug);
    }

    /**  Stop time of rendering **/
    private static double stopTime(long tStart){
        long tEnd = System.currentTimeMillis();
        long tDelta = tEnd - tStart;
        return tDelta / 1000.0;
    }

    /**  This is where our scene is actually ray-traced **/
    public void renderScene() {
        tStart = System.currentTimeMillis();
        Log.print(this, "Prepare rendering at " + String.valueOf(stopTime(tStart)));

        //Only render if mode is found inside the implementation
        //of the object
        Render(renderParktikum0);
        Render(mScene);

        Log.print(this, "Finish rendering at " + String.valueOf(stopTime(tStart)));

        this.exportRendering();
    }

    /// for all pixels, perform the rendertask
    /// for the given pixel (render mode support inside
    /// the derived class which implements the interface)
    private void Render(IRenderInterface renderInterface) {
        if (true) {
            RenderAsnyc(renderInterface);
            return;
        }
        
    
        
        if (renderInterface != null) {
            FRenderPixelTask task = new FRenderPixelTask(mRenderWindow, mBufferedImage);
            int Width = mRenderWindow.GetWidth();
            int Height = mRenderWindow.GetHeight();
            for(int y = 0; y < Height; y++){
                for (int x = 0; x < Width; x++) {
                    
                    task.SetPixelPosition(x, y);
                    renderInterface.renderPixel(task);
                }
            }
        }
    }




    private ExecutorService executor = null;

    private void ResetExecutor(int blocks) {
        if (executor != null) {
            executor.shutdownNow();
            return;
        }
        executor = Executors.newFixedThreadPool(blocks);
    }


    private void RenderAsnyc(IRenderInterface renderInterface){
        int blockSize = 128; //16 //32
        int Width = mRenderWindow.GetWidth();
        int Height = mRenderWindow.GetHeight();
        int blocks = (Height / blockSize) * (Width / blockSize);

        ResetExecutor(blocks);

        ExecutorService executor = //Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
                Executors.newFixedThreadPool(blocks);
        for (int by = 0; by < Height; by += blockSize) {

            for (int bx = 0; bx < Width; bx += blockSize) {
                final int startX = bx;
                final int startY = by;

                executor.submit(() -> {
                    FRenderPixelTask task = new FRenderPixelTask(mRenderWindow, mBufferedImage);
                    for (int y = startY; y < Math.min(startY + blockSize, Height); y++) {
                        for (int x = startX; x < Math.min(startX + blockSize, Width); x++) {
                            task.SetPixelPosition(x, y);
                            renderInterface.renderPixel(task);

                           
                        }
                    }
                    
                });
                
            }

        }
        executor.shutdown();
        try{
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (Exception e) {
            
        }
    }





    /// registers the raytracer to the key listening of
    /// the window. Receives numbers to change the render mode
    private void RegisterToWindow() {
        if (mRenderWindow != null) {
            mRenderWindow.registerKeyListener(this);
            mRenderWindow.SetupSceneTreeUi(mScene);
        }
    }

    
    //to be overriden
    //this method will switch the render mode for the different
    //renders and reload the render
    @Override
    public void ReceiveKeyNumber(int key) {
        RenderMode mode = RenderModeStatic.FromKey(key);
        UpdateRenderMode(mode);
    }

    /// updates the render mode and reloads the rendered image.
    public void UpdateRenderMode(RenderMode mode) {
        mScene.SetRenderMode(mode);
        renderParktikum0.SetRenderMode(mode);
        //renderScene();

    }

    public void UpdateRenderModeAndReload(RenderMode mode) {
        UpdateRenderMode(mode);
        renderScene();
    }
    
    public void Reload() {
        renderScene();
    }






}
