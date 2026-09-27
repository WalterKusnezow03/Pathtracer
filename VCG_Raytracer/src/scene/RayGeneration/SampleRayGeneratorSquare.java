package scene.RayGeneration;

import java.util.ArrayList;
import java.util.List;

import raytracer.HitTests.HitResult;
import raytracer.HitTests.MonteCarloRay;
import raytracer.HitTests.Ray;
import utils.algebra.Vec3;

public class SampleRayGeneratorSquare extends SampleRayGenerator {

    public SampleRayGeneratorSquare(float sizePlane) {
        super(sizePlane);
    }

    @Override
    public void MakeSampleRay(MonteCarloRay ray, HitResult fromResult) {
        if (fromResult != null) {
            Vec3 n = fromResult.GetHitNormal();
            Vec3 tangent = fromResult.TangentA();
            Vec3 binormal = fromResult.TangentB();

            //gi samples über quad
            //in [0,1]
            float u = (((float) Math.random() % 1.0f) * 2.0f - 1.0f);
            float v = (((float) Math.random() % 1.0f) * 2.0f - 1.0f);

            u *= sizePlane * 0.5f;
            v *= sizePlane * 0.5f;

            Vec3 dir = n.add(tangent.multScalar(u).add(binormal.multScalar(v)));
            ray.override(fromResult.GetHitpoint(), dir);

            //area integriert ist die umkehr funktion der sample funktion
            float a = sizePlane * sizePlane;
            ray.SetWeight(a);
        }
    }



}
