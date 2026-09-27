package scene.light.derived;

import java.util.List;

import raytracer.HitTests.HitResult;
import raytracer.HitTests.HitResultRefractVisible;
import raytracer.HitTests.Ray;
import scene.Scene;

import scene.light.Light;
import scene.light.Materials.MaterialEnum;
import scene.primitives.Plane;
import utils.RgbColor;
import utils.algebra.Vec3;

/// das area light ist auch ein licht aber hat die möglichkeit 
/// über mehrere punkte von i bis n, wobei n immer eine sample seite ist,
/// zu samplen
public class AreaLight extends Light {
    
    private Plane plane;
    private int samplesSide = 1;
    private float samplesSide2 = 1.0f;

    public AreaLight(float size, int samples2) {
        plane = new Plane(size);
        


        float asFloat = samples2;
        samplesSide = (int) Math.ceil(Math.sqrt(Math.abs(asFloat)));
        samplesSide2 = asFloat;
    }



    /// samples a point (i,j), normalized to a sample size on x and y
    /// returns the world Location of the sample
    private Vec3 SamplePoint(int i, int j) {
        Vec3 point = plane.GetUVWorld(Normalized(i), Normalized(j));
        /// return transform.TransformPositition(point);
        return transform.GetTranslation().add(point);
    }

    ///normalizes a index to the sample count 
    private float Normalized(int i) {
        return (float) i / (float) samplesSide;
    }

    ///lits a point over all samples and adds it to the addive color out
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

        //all sample points
        //result / n
        boolean anyHit = false;

        //over all samples: compute color,
        // weight it against total sampleCount mean
        for (int i = 0; i < samplesSide; i++) {
            for (int j = 0; j < samplesSide; j++) {
                Vec3 samplePoint = SamplePoint(i, j);

                boolean newCheck = true;
                boolean isVisible = false;
                HitResultRefractVisible refractVisble = new HitResultRefractVisible();
                if (newCheck) {
                    
                    isVisible = scene.PointIsVisibleIncludingRefraction(
                            samplePoint, result.GetHitpoint(), result.HitObjectName(), refractVisble
                    );
                } else {
                    isVisible = scene.LightIsVisible(this, result.GetHitpoint(), result.HitObjectName());
                }
                


                //if (scene.PointIsVisible(samplePoint, result.GetHitpoint(), result.HitObjectName())) {
                if(isVisible){
                    anyHit = true;
                    float scale = refractVisble.IsVisbleTroughRefract() ? 0.6f : 1.0f;
                    

                    RgbColor current = new RgbColor(0, 0, 0);
                    result.HitMaterial().shade(this, samplePoint, result, current);
                    current = current.multScalar(1.0f / samplesSide2);
                    current = current.multScalar(scale);
                    outColorAdditive.AddColor(current);
                }
            }
        }
        if (anyHit && samplesSide2 > 0.0f) {
            //outColorAdditive.add(outColorAdditive)
            //outColorAdditive.AddColor(additiveTmp.multScalar(1.0f / samplesSide2));
            return true;
        }
        return false;

    }
    

    



};
