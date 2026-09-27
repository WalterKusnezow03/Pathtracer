package scene.light.Materials;

import raytracer.HitTests.HitResult;
import raytracer.HitTests.Ray;
import utils.algebra.Vec3;

public class Fresnel {

    public static float ComputeFresnel_RelfectionWeight(
        Ray incidentRay,
        HitResult result
    ) {
        return ComputeFresnel_RelfectionWeight(incidentRay.rayIOR, result.hitMaterialIOR(), result);
    }


    public static float ComputeFresnel_RelfectionWeight(
        float ior1,
        float ior2,
        HitResult result
    ) {
        Vec3 rayDir = result.hitRayDirection();
        return ComputeFresnel_RelfectionWeight(ior1, ior2, Vec3.Dot(rayDir.multScalar(-1.0f), result.GetHitNormal()));
    }

    
    public static float ComputeFresnel_RelfectionWeight(
        float ior1,
        float ior2,
        float cos
    ) {
        //der fresnel term / schlick approximation lautet

        //F(theta) = F0 + (1-F0)*(1-cos(theta)^5)

        //F0 = ((mu1 - mu2)/(mu1+mu2))^2
        float F0 = MakeF0(ior1, ior2);
        return F0 + (1.0f - F0) * (float) Math.pow((1.0f - cos), 5.0f);
    }

    private static float MakeF0(float ior1, float ior2) {
        float frac = (ior1 - ior2) / (ior1 + ior2);
        return frac * frac;
    }



}
