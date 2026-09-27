package scene.customComponents;

import java.util.ArrayList;
import java.util.List;

import raytracer.HitTests.HitResult;
import raytracer.HitTests.Ray;
import scene.SceneObject;
import scene.light.Light;
import scene.light.Materials.MaterialEnum;
import scene.light.derived.AreaLight;
import scene.primitives.Plane;
import scene.primitives.Sphere;
import utils.RgbColor;
import utils.algebra.Vec3;

public class CornellBox extends SceneObject{

    private Plane back;
    private Plane right;
    private Plane left;
    private Plane bottom;
    private Plane top;

    private Sphere sphereDebug1 = null;;

    float widthCopy;
    float depthCopy;
    Vec3 locationBottom;
    Vec3 locationBack;
    Vec3 locationTop;
    Vec3 locationRight;
    Vec3 locationLeft;

    private Plane topLightPlane;

    private Light light;

    boolean useAreaLight = true;


    public Light GetLight() {
        return light;
    }

    @Override
    public List<SceneObject> GetChilds() {
        List<SceneObject> list = new ArrayList<>();

        list.add(bottom);
        list.add(back);
        list.add(top);
        list.add(right);
        list.add(left);
        list.add(topLightPlane);
        
        if (sphereDebug1 != null) {
            list.add(sphereDebug1);
        }
        
        return list;
    }

    /// sets up a cornell box with default planes
    /// transform is ignored / not needed, is at 0,0,0.
    public CornellBox() {
        
        float width = 15.0f;
        float lightSize = width * 0.2f;
        setupCornellBox(width, lightSize, 3);
    }

    public CornellBox(float width, float sizeLight, int AreaLightSamples2, boolean useAreaLightIn) {
        useAreaLight = useAreaLightIn;
        setupCornellBox(width, sizeLight, AreaLightSamples2);
    }
    
    private void setupCornellBox(float width, float sizeLight, int AreaLightSamples2) {
        float depth = (width / 4.0f) * 3.0f;
        System.out.print("Cornell Setup w" + width + " d" + depth);

        widthCopy = width;
        depthCopy = depth;

        locationBottom = new Vec3(15, 0, -3);
        locationBack = locationBottom.add(new Vec3(depth * 0.5f, 0.0f, depth * 0.5f));
        locationTop = locationBottom.add(new Vec3(0.0f, 0.0f, depth));
        locationRight = locationBottom.add(new Vec3(0.0f, width * 0.5f, depth * 0.5f));
        locationLeft = locationBottom.add(new Vec3(0.0f, -1.0f * width * 0.5f, depth * 0.5f));

        bottom = MakePlane(new RgbColor(1.0f, 1.0f, 1.0f), depth, width, "cornellBottom", locationBottom);

        back = MakePlane(new RgbColor(1.0f, 1.0f, 1.0f), depth, width, "cornellBack", locationBack);
        back.transform.SetRotation(0, -Math.PI / 2, 0.0f);

        top = MakePlane(new RgbColor(1.0f, 1.0f, 1.0f), depth, width, "cornellTop", locationTop);
        top.transform.SetRotation(0, -Math.PI, 0.0f);
        

        //left and right must be square
        float reflectAdd = 0.0f;
        right = MakePlane(new RgbColor(reflectAdd, reflectAdd, 1.0f), depth, depth, "cornellRight", locationRight);
        right.transform.SetRotation(Math.PI / 2, 0.0f, Math.PI * 0.5f);

        left = MakePlane(new RgbColor(1.0f, reflectAdd, reflectAdd), depth, depth, "cornellLeft", locationLeft);
        left.transform.SetRotation(Math.PI / 2, 0.0f, -Math.PI *0.5f);
        //left.transform.SetRotation(0.0f, 0.0f, -Math.PI *0.5f);


        if (useAreaLight) {
            light = new AreaLight(sizeLight, AreaLightSamples2);
            light.SetColor(new RgbColor(1,1,1));
            light.transform.SetTranslation(locationTop.add(new Vec3(0.0f, 0.0f, -0.5f)));
            light.transform.SetRotation(0.0f, Math.PI, 0.0f);
            light.name = "CornellLight_AreaLight";
            
            //AreaLight casted = (AreaLight) light;
            //topLightPlane = casted.getPlaneCopy();

        } else {
            light = new Light();
            light.SetColor(new RgbColor(1, 1, 1));
            light.transform.SetTranslation(locationBottom.add(new Vec3(0.0f, 0.0f, depth - 2.0f)));
            light.name = "CornellLight";

        }
        
        //top white plane emissive
        Vec3 locationTopLight = locationTop.add(new Vec3(0.0f,0.0f,-1.0f).multScalar(0.01f));
        topLightPlane = MakePlane(
            new RgbColor(1,1,1), sizeLight, sizeLight, "lightTopPlane", locationTopLight
        );
        topLightPlane.transform.SetRotation(0, Math.PI, 0); //180 deg flip 
        topLightPlane.material.OverrideMaterialType(MaterialEnum.Emissive);

    

        


        





       
    }
    
    public void AddDebugBalls() {
        //add debug ball at top left / right
        Vec3 pos = locationTop.add(new Vec3(-depthCopy / 2.0f, -widthCopy / 2.0f, 0.0f));
        sphereDebug1 = new Sphere(1.0f);
        sphereDebug1.material.SetColor(new RgbColor(0, 1, 0));
        sphereDebug1.SetLocation(pos);
        sphereDebug1.material.SetReflectiveNess(0.0f);
        sphereDebug1.material.OverrideMaterialType(MaterialEnum.Emissive);
        sphereDebug1.name = "debug Top left Green Ball";
    }




    ///creates a plane with the given local extents and the location.
    private Plane MakePlane(
        RgbColor color,
        float scaleX,
        float scaleY,
        String name,
        Vec3 location
    ){
        //aspect 4:3
        //x = 4
        //y = 3
        
        Plane plane = new Plane(scaleX, scaleY);
        plane.SetLocation(location); //at red ball ground
        plane.material.SetColor(color);
        plane.name = name;
        plane.material.SetReflectiveNess(0.0f);
        plane.material.SetSpecularStrenght(0.0f);
        plane.material.OverrideMaterialType(MaterialEnum.Lambert);
    
        return plane;
    }


    @Override
    public boolean DoesIntersectWithBetterResult(Ray ray, HitResult resultToUpdate) {
        if (!IsEnabled) {
            return false;
        }
        //return bottom.DoesIntersectWithBetterResult(ray, resultToUpdate);

        //goes through all children: planes
        return DoesIntersectWithBetterResultRecursiveChilds(ray, resultToUpdate);
    }

    


    
}
