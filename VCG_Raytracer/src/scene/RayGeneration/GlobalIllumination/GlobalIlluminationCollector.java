package scene.RayGeneration.GlobalIllumination;

import java.util.List;

import raytracer.HitTests.HitResult;
import raytracer.HitTests.MonteCarloRay;
import raytracer.HitTests.Ray;
import scene.Scene;
import scene.RayGeneration.SampleRayGenerator;
import scene.light.Materials.MaterialEnum;
import utils.RgbColor;
import utils.RgbColorTraced;
import utils.algebra.Vec3;

/// class which moves the globa illumination task to this class helping to
/// collect samples for a given HitResult and a given Ray Generator
public class GlobalIlluminationCollector {

    private SampleRayGenerator generator;
    

    public GlobalIlluminationCollector(SampleRayGenerator generator) {
        this.generator = generator;
        if (generator == null) {

        }

    }
    public void ComputeGiSample(
        Scene scene,
        RgbColor outColor,
        HitResult result,
        int count
    ) {
        int recursionsLeft = 1;
        ComputeGiSample(scene, outColor, result, count, recursionsLeft);
    }

    public void ComputeGiSample(
        Scene scene,
        RgbColor outColor,
        HitResult result,
        int count,
        int recursionsLeft
    ) {
        float samplesUniform = 1.0f / (float) count;
        MonteCarloRay ray = new MonteCarloRay(result.GetHitpoint(), result.GetHitNormal());

        //one ray force in normal dir: (einfach so.)
        RgbColorTraced rawColor;
        


        for (int i = 0; i < count; i++) {
            generator.MakeSampleRay(ray, result);

            //für gi erstmal ein bounce
            //int recursionsLeft = 1;
            rawColor = scene.RecursiveBounceRayRefractionAndReflection(ray, recursionsLeft);
            rawColor.multScalarInPlace(samplesUniform);

            ray.ApplyPDFWeight(rawColor);

            //importance gewichten nach dot product mit normalen
            float nl = Vec3.Dot(ray.Direction(), result.GetHitNormal());
            rawColor.multScalarInPlace(nl);

            rawColor.clamp();
            outColor.AddColor(rawColor);
        }
    }


    private float ComputeFormFactorTerm(HitResult result, RgbColorTraced resultSecondary) {
        if (result != null && resultSecondary != null) {
            return ComputeFormFactorTerm(
                result,
                resultSecondary.getHitResult()
            );    
        }
        return 1.0f;
    }


    private float ComputeFormFactorTerm(HitResult result, HitResult resultSecondary) {
        if (result != null && resultSecondary != null) {
            return ComputeFormFactorTerm(
                result.GetHitpoint(),
                result.GetHitNormal(),
                resultSecondary.GetHitpoint(),
                resultSecondary.GetHitNormal());    
        }
        return 1.0f;
    }
    
    


    private float ComputeFormFactorTerm(Vec3 a, Vec3 n1, Vec3 b, Vec3 n2) {
        //weighted result by:
        //F(i,j) = cos_theta1 * cos_theta2 / r^2
        Vec3 lotAB = b.sub(a); //AB = B - A

        //r^2 = |a|^2 = sqrt(a*a)^2 = a^2 //self dot product for norman distance
        float size2 = Vec3.Dot(lotAB, lotAB);

        if (Math.abs(size2) > 0.0000000000000000001f) {

            //cos(theta) = a * b //if |a| = 1 \land |b| = 1
            lotAB = lotAB.normalize();
            Vec3 lotBA = lotAB.multScalar(-1.0f);
            float cos1 = Vec3.Dot(n1, lotAB);
            float cos2 = Vec3.Dot(n2, lotBA);

            cos1 = Math.max(cos1, 0.0f);
            cos2 = Math.max(cos2, 0.0f);

            

            float nominator = cos1 * cos2;
            float FormFactor = nominator / ((float) Math.sqrt(size2));

            FormFactor = Math.clamp(FormFactor, 0.0f, 1.0f);
            return FormFactor;

        }
        return 0.0f;
    }






}