// ************************************************************ //
//                      Hochschule Duesseldorf                  //
//                                                              //
//                     Vertiefung Computergrafik                //
// ************************************************************ //


/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    1. Documentation:    Did you comment your code shortly but clearly?
    2. Structure:        Did you clean up your code and put everything into the right bucket?
    3. Performance:      Are all loops and everything inside really necessary?
    4. Theory:           Are you going the right way?

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

 <<< YOUR TEAM NAME >>>

     Master of Documentation:
     Master of Structure:
     Master of Performance:
     Master of Theory:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/

import raytracer.Raytracer;
import raytracer.RenderMode;
import ui.Window;
import scene.Scene;
import scene.customComponents.CornellBox;
import scene.light.Light;
import scene.light.Materials.IOR;
import scene.light.Materials.MaterialEnum;
import scene.primitives.Plane;
import scene.primitives.Sphere;
import utils.RgbColor;
import utils.algebra.Vec3;
import raytracer.camera.*;

/*
    - THE RAYTRACER -

    TEAM:

    1.
    2.
    3.
    4.
 */

// Main application class. This is the routine called by the JVM to run the program.
public class Main {

    /** RESOLUTION **/

    //public static final int IMAGE_WIDTH = 800;
    //public static final int IMAGE_HEIGHT = 600;
    public static final int IMAGE_WIDTH = 800;
    public static final int IMAGE_HEIGHT = 600;
   
    /** CORNELL_BOX_DIMENSION **/

    static final float BOX_DIMENSION = 15f;

    /** RAYTRACER **/

    static final int RECURSIONS = 3;
    static final int ANTI_ALIASING = 10;
    static final boolean USE_SOFT_SHADOWS = true;
    public static int AreaLightSamples2 = 32;

    /** LIGHT **/
    //static final short LIGHT_DENSITY = 20;
    //static final short LIGHT_SAMPLES = 40;

    static final RgbColor BACKGROUND_COLOR = RgbColor.BLACK;

    static final Vec3 LIGHT_POSITION = null;
    static final short AREA_LIGHT_SIZE = 2;

    /** GI **/
    static final boolean USE_GI = true;
    static final int GI_LEVEL = 1; //1
    static final int GI_SAMPLES = 5;//5;
    static final float GI_Increase = 1.5f;

    static final RgbColor LIGHT_COLOR = null;
    static final RgbColor AMBIENT_LIGHT = new RgbColor(.1f, .1f, .1f);
    static final float ambientCoefficent = 0.2f;//1.5f; //1.5f, pdf off

    static final boolean USE_AO = true;
    static final int NUMBER_OF_AO_SAMPLES = 25;
    static final float AO_MAX_DISTANCE = 1f;

    

    /** CAMERA **/

    static Vec3 CAM_POS = new Vec3(0, 0, 1.5f);
    static Vec3 LOOK_AT = new Vec3(100, 0, 1.5f);
    //static final Vec3 UP_VECTOR = null;

    static float VIEW_ANGLE = 70.0f;

    /** DEBUG **/

    static final boolean SHOW_PARAM_LABEL = true;


    /** Initial method. This is where the show begins. **/
    public static void main(String[] args) {
        Window renderWindow = new Window(IMAGE_WIDTH, IMAGE_HEIGHT);

        System.out.printf("Hello World! Again!");
        draw(renderWindow);
    }

    

    
    



    /**  Draw the scene using our Raytracer **/
    private static void draw(Window renderWindow){
        Scene renderScene = new Scene();

        setupScene(renderScene); //setted up scene is passed to the raytracer

        raytraceScene(renderWindow, renderScene);
    }

    /** Setup all components that we want to see in our scene **/
    private static void setupScene(Scene renderScene){
        setupCameras(renderScene);

        setupCornellBox(renderScene);

        setupObjects(renderScene);

        setupLights(renderScene);

        setupExtraParams(renderScene);
    }

    private static void setupCameras(Scene renderScene) {
        float aspect = (float) IMAGE_WIDTH / (float) IMAGE_HEIGHT;

        PerspectiveCamera camera = new PerspectiveCamera(CAM_POS, LOOK_AT, VIEW_ANGLE, aspect);
        renderScene.SetCamera(camera);
    }
    
    private static void setupExtraParams(Scene scene) {
       
        scene.SetupGI(USE_GI, GI_SAMPLES, GI_LEVEL, GI_Increase);
        scene.SetupAO(USE_AO, NUMBER_OF_AO_SAMPLES, AO_MAX_DISTANCE);
    }


    private static void setupLights(Scene renderScene) {
        Light light = new Light();
        light.SetColor(new RgbColor(1, 1, 1));
        light.SetLocation(new Vec3(8, 0, 0));
        renderScene.AddLight(light);
        light.name = "White";
        light.IsEnabled = false;

        light = new Light();
        light.SetColor(new RgbColor(0.2f, 0, 1));
        light.SetLocation(new Vec3(10, -3, 3));
        renderScene.AddLight(light);
        light.name = "Blue";
        light.IsEnabled = false;

        light = new Light();
        light.SetColor(new RgbColor(.2f, 1, 0));
        light.SetLocation(new Vec3(14, 3, 4));
        renderScene.AddLight(light);
        light.name = "Green";
        light.IsEnabled = false;

    }

    private static void setupObjects(Scene renderScene) {
        Sphere sphere = new Sphere(3.0f);
        sphere.SetLocation(new Vec3(15, 0, -1.0f));
        sphere.SetLocation(new Vec3(15, 0, 0)); //debug
        sphere.material.SetColor(new RgbColor(new Vec3(1.0f, 0.0f, 0.0f)));
        renderScene.AddObject(sphere);
        sphere.name = "red sphere";

        boolean refractiveSphere = true;

        sphere = new Sphere(2.0f);//new Sphere(2.0f, 1,2);
        sphere.SetLocation(new Vec3(11, -4.5f, -1.0f));
        sphere.material.SetColor(new RgbColor(new Vec3(0.5f, 1.0f, 0.0f)));
        //sphere.material.SetReflectiveNess(0.1f);
        sphere.material.SetReflectiveNess(0.1f);
        sphere.material.OverrideMaterialType(MaterialEnum.Phong);
        renderScene.AddObject(sphere);
        sphere.name = "green sphere";
        sphere.IsEnabled = !refractiveSphere;
        sphere.material.SetSpecularColor(new RgbColor(new Vec3(0.0f, 1.0f, 0.0f)));


        sphere = new Sphere(1.5f);
        sphere.SetLocation(new Vec3(12, -4.5f, -1.5f));
        sphere.material.SetColor(new RgbColor(new Vec3(1,1,1)));
        renderScene.AddObject(sphere);
        sphere.name = "transparent refract sphere";
        sphere.IsEnabled = refractiveSphere;
        sphere.material.SetIOR(IOR.IOR_water(), true);
        //sphere.material.SetReflectiveNess(0.05f);
        sphere.material.SetReflectiveNess(0.01f);



        sphere = new Sphere(2f);
        sphere.SetLocation(new Vec3(13, 4.5f, -1.5f));
        sphere.material.SetColor(new RgbColor(new Vec3(1,1,1)));
        renderScene.AddObject(sphere);
        sphere.name = "white";
        sphere.IsEnabled = true;
        sphere.material.OverrideMaterialType(MaterialEnum.Lambert);
        //sphere.material.SetReflectiveNess(0.05f);
        sphere.material.SetReflectiveNess(0.0f);





        //Plane plane = new Plane(15);
        Plane plane = new Plane(10, 15);
        plane.SetLocation(new Vec3(15, 0, -3)); //at red ball ground
        plane.material.SetColor(new RgbColor(new Vec3(0.0f, 0.0f, 1.0f)));
        renderScene.AddObject(plane);
        System.out.println(plane);
        plane.name = "blue plane";
        plane.material.SetReflectiveNess(1.0f);
        plane.IsEnabled = false;
        
        
        
        
        
        
        
        
    }

    private static void setupCornellBox(Scene renderScene) {
        CornellBox box = new CornellBox(BOX_DIMENSION, AREA_LIGHT_SIZE, AreaLightSamples2, USE_SOFT_SHADOWS);
        box.name = "Cornell";
        //box.AddDebugBalls();

        renderScene.AddObject(box);
        renderScene.AddLight(box.GetLight());
    }

    /** Create our personal renderer and give it all of our items and prefs to calculate our scene **/
    private static void raytraceScene(Window renderWindow, Scene renderScene){
        Raytracer raytracer = new Raytracer(
                renderScene,
                renderWindow,
                RECURSIONS,
                BACKGROUND_COLOR,
                AMBIENT_LIGHT,
                ANTI_ALIASING,
                SHOW_PARAM_LABEL,
                ambientCoefficent
            );
       


        
        raytracer.UpdateRenderModeAndReload(RenderMode.RenderModePraikum3_6_PhongBounceNRefract);
        
        
    }
}