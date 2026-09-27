


package scene.light;

import java.util.List;

import raytracer.HitTests.HitResult;
import raytracer.HitTests.HitResultRefractVisible;
import raytracer.HitTests.Ray;
import raytracer.Interface.FRenderPixelTask;
import scene.Scene;
import scene.SceneObject;

import scene.primitives.Sphere;
import utils.RgbColor;
import utils.algebra.Vec3;

public class Light extends SceneObject{
    
    

    public RgbColor color = new RgbColor(1, 1, 1);
    public float intensity = 1.0f; //zum spielen.
    private Sphere sphere = new Sphere(0.2f);

    public Light() {

    }

    public void SetColor(RgbColor colorIn) {
        color = colorIn;
    }

    //debug
    public boolean DoesIntersect(Ray ray) {
        if (IsEnabled) {
            sphere.transform.SetTranslation(this.transform.GetTranslation());
            return sphere.DoesIntersect(ray);
        }
        return false;
    }



    //abstract illuminate function
    //wird immer dann illuminated wenn
    //das licht auch in der scene vom hit result: point
    //aus sichtbar ist.
    //das belichtete material kommt aus dem hit result
    public boolean Illuminate(
        RgbColor outColorAdditive,
        Scene scene,
        HitResult result
    ) {
        if (!IsEnabled) {
            return false;
        }
        if (!ValidParams(outColorAdditive, scene, result)) {
            System.out.println("Light invalid params");
            return false;
        }

        //was man hier braucht ist ein 
        //light is visible, aber auch hit: refract ja oder nein.

        boolean newCheck = true;
        boolean isVisible = false;
        HitResultRefractVisible refractVisble = new HitResultRefractVisible();
        if (newCheck) {
            System.out.println("NEW TEST!");
            isVisible = scene.LightIsVisibleIncludingRefraction(this, result.GetHitpoint(), result.HitObjectName(), refractVisble);
        } else {
            isVisible = scene.LightIsVisible(this, result.GetHitpoint(), result.HitObjectName());
        }
        if (isVisible) {
            float scale = refractVisble.IsVisbleTroughRefract() ? 0.6f : 1.0f;
            RgbColor tempColor = new RgbColor(0, 0, 0);
            result.HitMaterial().shade(this, result, tempColor);
            outColorAdditive.AddColor(tempColor.multScalar(scale));
            
            
            //shade material default
            //result.HitMaterial().shade(this, result, outColorAdditive);   
            
            return true;
        }




        /* 
        //immer wenn das licht sichtbar ist, wird das material auch belichtet, sonst nicht.
        if (scene.LightIsVisible(this, result.GetHitpoint(), result.HitObjectName())) {
            //shade material
            result.HitMaterial().shade(this, result, outColorAdditive);
            return true;


        } else {
            //System.out.println("Light not visible");
        }*/
        return false;
    }

    protected boolean ValidParams(
        RgbColor outColorAdditive,
        Scene scene,
        HitResult result
    ) {
        if (outColorAdditive == null) {
            return false;
        }
        if (scene == null) {
            return false;
        }
        if (result == null) {
            return false;
        }
        return true;
    }






}

