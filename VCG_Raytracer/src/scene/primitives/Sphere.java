package scene.primitives;


import scene.*;
import utils.algebra.Vec3;
import raytracer.HitTests.*;

//scene object class to derive sphere and others from
public class Sphere extends SceneObject {

    private final float radius2 = 1.0f; //keep like this.

    public Sphere(float radius) {

        this.transform.SetScaleUniform(radius);

        //radius2 = radius * radius;
    }
    
    public Sphere(float rX, float rY, float rZ) {
        this.transform.SetScale(rX, rY, rZ);
    }


    @Override
    public boolean DoesIntersect(Ray ray) {
        HitResult none = new HitResult();//HitresultPerformancePool.Find();//new HitResult();
        boolean outResult = DoesIntersectWithBetterResult(ray, none);
        
        return outResult;
    }
    
    //is tested
    @Override
    public boolean DoesIntersectWithBetterResult(Ray ray, HitResult resultToUpdate) {
        
        if (!super.DoesIntersectWithBetterResult(ray, resultToUpdate)) {
            return false;
        }

        //// -------> ray in local system bringen, radius immer im einheits kreis!!

        //System.out.println("SPHERE HIT Test ");


        //formel manuell umstellen:
        
        //basis: x^2 + y^2 + z^2 = r^2
        //mit: 
        
        
        //(ax + tdx)^2 +
        //(ay + tdy)^2 +
        //(az + tdz)^2
        // <= r^2

        //(a+b)^2 = a^2 + 2ab + b^2
        
        //umstellen:
        //ax^2 + 2ax tdx + (tdx)^2 + 
        //ay^2 + 2ay tdy + (tdy)^2 +
        //az^2 + 2az tdz + (tdz)^2 
        // <= r^2

        //solve for t
        //ax^2 +px + q = 0

        // a^2 + 2(a*d)t + d*d t^2 - r^2 = 0

        //d*d t^2 + 2(a*d) t + (a^2-r^2)

        float worldRaySize = ray.Direction().length();
       
        Ray localRay = InverseTransformRay(ray);
        Vec3 rayDir = localRay.Direction();//.normalize(); //was not
        Vec3 Origin = localRay.Origin();
        
       
        

       




        //ax^2 + bx + c = 0
        //x1,2 = frac{-b +- sqrt(b^2-4ac)}{2a}

        float a = Vec3.Dot(rayDir, rayDir);
        float b = 2.0f * Vec3.Dot(Origin, rayDir);
        float c = Vec3.Dot(Origin, Origin) - radius2;


        float deskriminante = (b * b) - (4.0f * a * c);
        //System.out.println("Camera Dir " + ray.Direction() + " local dir " + rayDir);

        
        //System.out.println("SPHERE HIT Test desk " + deskriminante + " local origin " + Origin);
        if (deskriminante >= 0.0f) {
            //has real result
            double sqrt = Math.sqrt(deskriminante);

            double t0 = (-1.0f * b - sqrt) / (2.0f * a);
            double t1 = (-1.0f * b + sqrt) / (2.0f * a);

            //dont allow points behind the starting point
            boolean t0Valid = t0 >= 0.0f;
            boolean t1Valid = t1 >= 0.0f;

            double tResult = Float.MAX_VALUE;
            if (t0Valid && t1Valid) {
                tResult = Math.min(t0, t1); //closer value
            } else if (t0Valid && !t1Valid) {
                tResult = t0;
            } else if (!t0Valid && t1Valid) {
                tResult = t1;
            } else {
                //System.out.println("SPHERE T NONE VALID " + t0 + " " + t1);
                return false;
            }

            //compute local hit and move to world space
            Vec3 hitLocal = Origin.add(rayDir.multScalar(tResult));
            Vec3 hitWorld = this.transform.TransformPositition(hitLocal);

            //necessary for consistent world updates
            //necessary for consistent world updates for hit order
            tResult = tScalarToWorldSpace(ray, hitWorld);


            //System.out.println("SPHERE HIT T " + tResult);

            //Only update if new result is closer
            if (resultToUpdate.TIsCloser(tResult)) {

                if (ray.IsRefractRay()) {
                    //String s = ray.latestHitComponent.name != null ? ray.latestHitComponent.name : " none ";
                    //System.out.println("refract ray hit!" + ray.rayIOR + " from " + s + " to " + this.name);
                }

                Vec3 normalDirection = hitWorld.sub(transform.GetTranslation()).normalize(); //AB = B - A
                return resultToUpdate.IsCloser(
                    (float)tResult, this, hitWorld, normalDirection, ray
                );
            }

        }
        
        return false;
    }

}



