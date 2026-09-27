package scene.RayGeneration.AmbientOcclusion;

import raytracer.HitTests.HitResult;
import raytracer.HitTests.MonteCarloRay;
import raytracer.HitTests.Ray;
import scene.Scene;
import scene.RayGeneration.SampleRayGenerator;
import utils.RgbColor;
import utils.algebra.Vec3;

public class AmbientOcclusionCollector {
    
    private SampleRayGenerator generator;
    private float maxDistance = 1.0f;

    public AmbientOcclusionCollector(SampleRayGenerator generator, float maxDistanceIn) {
        this.generator = generator;
        OverrideMaxDistance(maxDistanceIn);
    }

    public void OverrideMaxDistance(float maxDistanceIn) {
        maxDistance = Math.abs(maxDistanceIn);
    }

    /*
    Aus eigener mitschrift:
    
    \section{Sampling - Ambient Occlusion}
    Ambient Occlusion ist der Effekt der dafür sorgt dass es schatten \textbf{ohne} echte Lichtquellen gibt.
    Es wird davon ausgegangen dass eine Fläche sich \(draussen\) befindet.
    Es entsteht dort schatten wo generell "\(wenig\)" Licht hinkommt.
    
    Gemessen wird das durch samples über eine hemisphere.
    Dabei gilt folgende Formel:
    
    $$
    A_p = 1 - (\frac{1}{\pi} \cdot \int V(p_{localtion}, \omega)\cdot (n \cdot \Vec{p_{location}\omega} ))
    $$
    mit der normierung von \(\frac{1}{\pi}\), auf der hemisphere von \(cos(\theta) \in [0, \pi]\)
    
    
    bzw:
    $$
    A_p = 1 - (\frac{1}{k} \cdot \sum_i^k V(p_{localtion}, \omega_i)\cdot (n \cdot \Vec{p_{location}\omega_i} ))
    $$
    Mit \(1-x\), um den effekt korrekt zu invertieren.
    
    A_p ist das resultierende Weight für die farbe.
    */
   

    public void ApplyAmbientOcclusion(RgbColor outColor, Scene scene, HitResult result, int count) {
        float weight = 1.0f - ComputeAOSamples(scene, result, count);
        //float weight = ComputeAOSamples(scene, result, count);
        weight = Math.clamp(weight, 0.0f, 1.0f);
        outColor = outColor.multScalarInPlace(weight);

        //debug
        /*if (weight < 0.5f) {
            outColor.SetColor(new RgbColor(0,1,0));
        }*/
    }


    private float ComputeAOSamples(Scene scene, HitResult result, int count) {
        float weight = 0.0f;
        MonteCarloRay ray = new MonteCarloRay(new Vec3(), new Vec3());
        boolean useMonteCarlo = false;
        
        for (int i = 0; i < count; i++) {
            generator.MakeSampleRay(ray, result);
           

            if (useMonteCarlo) {
                weight += ComputeAOSample(scene, result, ray) * ray.GetWeight();
            } else {
                weight += ComputeAOSample(scene, result, ray);// * ray.GetWeight();
            }
        }

        if (count > 0) {
            float asFloat = count;
            weight *= (1.0f / asFloat);
        }

        return weight;
    }   



    ///compute a AO sample, ray includes Starting Point of AO sample!
    private float ComputeAOSample(Scene scene, HitResult result, Ray ray) {

        //A_p = 1 - (\frac{1}{k} \cdot \sum_i^k V(p_{localtion}, \omega_i)\cdot (n \cdot \Vec{p_{location}\omega_i} ))

        //V(p_{localtion}, \omega) in [0,1]  //visiblity term, umschliesst max distance hier auch!
        //n \cdot \Vec{p_{location}\omega_i} //lambert term (weight by normal dot ähnlichkeit)

        float visibility = SampleIsOccluded(scene, ray);
       

        float dot = Vec3.Dot(ray.Direction().normalize(), result.GetHitNormal().normalize());
        return visibility * dot;
    }
    
    private float SampleIsOccluded(Scene scene, Ray ray){
        return 1.0f - SampleIsVisible(scene, ray);
    }
    
    private float SampleIsVisible(Scene scene, Ray ray) {
        if(scene != null){
            Vec3 end = ray.Evaluate(maxDistance);
            if(scene.PointIsVisible(end, ray.Origin())){
                return 1.0f;
            }
        }
        return 0.0f;
    }



}
