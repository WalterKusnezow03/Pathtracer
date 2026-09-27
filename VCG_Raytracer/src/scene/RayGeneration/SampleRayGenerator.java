package scene.RayGeneration;

import java.util.ArrayList;
import java.util.List;

import raytracer.HitTests.HitResult;
import raytracer.HitTests.MonteCarloRay;
import raytracer.HitTests.Ray;
import utils.algebra.Vec3;

public class SampleRayGenerator {

    protected float sizePlane = 0.5f;

    public SampleRayGenerator(float sizePlaneIn) {
        sizePlane = sizePlaneIn;
    }
    
    public void MakeSampleRay(MonteCarloRay ray, HitResult fromResult) {
        
    }

    protected float RandomInRangeMinusPlus1() {
        float u = (RandomInRangeZeroOne() * 2.0f - 1.0f) % 1.0f;
        return u;
    }

    protected float RandomInRangeZeroOne() {
        float u = (float) Math.random() % 1.0f;
        return u;
    }
    

    
}
