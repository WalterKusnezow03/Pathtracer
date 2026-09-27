package raytracer.HitTests;
import utils.algebra.Vec3;

import raytracer.HitTests.*;
import scene.SceneObject;
import scene.light.Materials.Material;
import scene.light.Materials.MaterialEnum;

public class HitResult{

    //closest hit object "distance".
    private float t_clostestScalar = Float.MAX_VALUE;
    private SceneObject hitObject = null;
    private Vec3 HitPoint = new Vec3();
    private Vec3 HitNormal = new Vec3();
    private Ray hitRay = null;

    private Vec3 tangentX = null;
    private Vec3 tangentY = null;

    public String nametest = "default";

    private Vec3 cameraPos = null;

    // --- global illumination use lambert only ---
    private boolean hasShadingSettingOverride = false;
    private MaterialEnum shadingMaterialTypeOverride = MaterialEnum.Phong;

    public void OverrideShadingSetting(MaterialEnum type) {
        shadingMaterialTypeOverride = type;
        hasShadingSettingOverride = true;
    }

    public void RemoveShadingSettingOverride() {
        hasShadingSettingOverride = false;
    }

    public MaterialEnum ShadingMaterialTypeOverride() {
        return shadingMaterialTypeOverride;
    }

    public boolean HasShadingSettingOverride() {
        return hasShadingSettingOverride;
    }
    // --- global illumination use lambert only ---


    

    public String HitObjectName() {
        return hitObject != null ? hitObject.name : "none";
    }

    //allows to set a t as custom maxima which has to be beaten
    public void setTExternal(float tIn) {
        t_clostestScalar = tIn;
    }


    public void SetCameraPos(Vec3 pos) {
        cameraPos = pos;
    }

    public float GetT() {
        return t_clostestScalar;
    }

    public boolean tValid(float t) {
        return t >= 0.0f;
    }

    public HitResult() {

    }

    public Material HitMaterial() {
        return hitObject.material;
    }

    public boolean HitMaterialHasReflectiveNess() {
        Material m = HitMaterial();
        if (m != null) {
            return m.GetReflectiveNess() > 0.0f;
        }
        return false;
    }

    public boolean HitMaterialHasRefraction() {
        Material m = HitMaterial();
        if (m != null) {
            return m.HasRefraction();
        }
        return false;
    }


    private void ComputeTangents() {
        Vec3 n = GetHitNormal();

        //sicheren orthogonalen vektor nehmen, sonst kommen kaputte tangenten bei raus
        Vec3 helper = (Math.abs(n.z) < 0.999f)
            ? new Vec3(0,0,1)
            : new Vec3(0,1,0);

        tangentX = Vec3.Cross(helper, n).normalize();
        tangentY = Vec3.Cross(tangentX, GetHitNormal()).normalize();
    }


    /// returns a offseted hitpoint
    private static final float EPSILON = 0.001f;
    //private static final float EPSILON = 0.001f; //was okayish

    public Vec3 GetHitpoint() {
        return GetHitpoint(HitNormal);
    }
    
    ///gets a hitpoint with a given offset
    public Vec3 GetHitpoint(Vec3 offsetDir) {
        return new Vec3(HitPoint).add(offsetDir.normalize().multScalar(EPSILON));
    }

    ///returns a copy of the current hitpoint
    public Vec3 GetHitpointRaw() {
        return new Vec3(HitPoint);
    }

    ///hitpoint normal
    public Vec3 GetHitNormal() {
        return new Vec3(HitNormal);
    }


    public Vec3 TangentA() {
        if (tangentX == null) {
            ComputeTangents();
        }
        return tangentX;
    }

    public Vec3 TangentB() {
        if (tangentY == null) {
            ComputeTangents();
        }
        return tangentY;
    }



    ///returns if a given t is closer than the current closest hit result
    public boolean TIsCloser(float tIn) {
        return tIn < t_clostestScalar;
    }

    ///returns if a given t is closer than the current closest hit result
    public boolean TIsCloser(double tIn) {
        return tIn < t_clostestScalar;
    }

    //variant for meshdata hit
    //replaces a hit result is is closer
    public boolean IsCloser(
        float tin,
        Vec3 hitPointIn,
        Vec3 hitNormalIn
    ) {

        if (tValid(tin) == false) {
            return false;
        }
        if (tin < t_clostestScalar) {
            if (t_clostestScalar != Float.MAX_VALUE) {
                //System.out.println("(small) Hit replace t old" + t_clostestScalar + "-> new  " + tin);
            }

            t_clostestScalar = tin;
            HitPoint = hitPointIn;
            HitNormal = hitNormalIn;
            return true;
        }
        return false;
    }

    //variant for object hit
    //replaces a hit result if the t is closer than the previous
    public boolean IsCloser(
        float tin,
        SceneObject updateObject,
        Vec3 hitPointIn,
        Vec3 hitNormalIn,
        Ray incomingRay
    ) {
        if (tValid(tin) == false) {
            return false;
        }
        //t scalar näher dran: update
        if (tin < t_clostestScalar) {
            if(hitObject != null){
                //System.out.println("Hit replace old" + hitObject.name + "-> new  " + updateObject.name + "(" + nametest + ")");
            } else {
                //System.out.println("Hit first "+  updateObject.name);
            }

            
            t_clostestScalar = tin;
            hitObject = updateObject;
            HitPoint = hitPointIn;
            HitNormal = hitNormalIn;
            hitRay = incomingRay.copy();
            return true;
        }
        return false;
    }

    /// generates a reflection ray at the hitpoint
    /// along the normal.
    /// The hitpoint is offseted
    public Ray ReflectionRay() {
        Vec3 inComing = hitRay.Direction();
        Vec3 reflect = Vec3.Reflect(inComing, HitNormal);
        //offseted hitpoint here!
        Ray ray = new Ray(GetHitpoint(), reflect.normalize());
        return ray;
    }

    public void MakeReflectionRay(Ray ray) {
        Vec3 inComing = hitRay.Direction();
        Vec3 reflect = Vec3.Reflect(inComing, HitNormal);
        ray.override(GetHitpoint(), reflect);
    }



    /// returns the incoming ray direction
    public Vec3 hitRayDirection() {
        return hitRay.Direction();
    }

    /// returns the view direction to the camera from the raw hitpoint
    public Vec3 ViewRayToCamera() {
        return cameraPos.sub(HitPoint).normalize();//AB = B - A
    }


    





    // --- refraction ---
    private float hitRayIOR() {
        if (hitRay != null) {
            return hitRay.rayIOR;
        }
        return 1.0f;
    };

    public float hitMaterialIOR() {
        return HitMaterial().GetIOR();
    }

    //hit ray ior
    ///generates the refraction ray from a incoming ray, out Ray per reference
    public boolean MakeRefractRay(Ray inRay, Ray outRay) {
        //throw new RuntimeException("HERE");
        return MakeRefractRayFrom(inRay, inRay.rayIOR, outRay);
    }


    //hit ray ior
    /// creates a refraction ray from a incoming ray, and a given previous ior,
    /// entering or exiting a current object ior
    /// to the out ray
    private boolean MakeRefractRayFrom(Ray inRay, float prevIOR, Ray outRay) {
        if (hitRay == null) {
            //System.out.println("Hit Ray invalid refract!");
            return false;
        }


        /*
        //HERLEITUNG SIEHE _Herleitungen/ReflectionAndRefraction.pdf

        $$
        \Vec{T} = 
        \frac{\mu_1}{\mu_2} * 
        ((n\cdot i)n - \Vec{i}) - 
        (n \cdot (\sqrt{1- ( 1- (\Vec{n}\cdot \Vec{i})^2) \cdot (\frac{\mu_1}{\mu_2})^2}))
        $$
        */
        float mu1 = prevIOR;
        float mu2 = hitMaterialIOR(); //entering IOR

        Vec3 i_incident = inRay.Direction().normalize();//.multScalar(-1.0f);

        //exiting ior
        //wenn der strahl das aktuelle objekt verlässt, ist die exit ior
        //mu2 immer 1.0, luft
        Vec3 n_normal = HitNormal.normalize();
        
        boolean swapped = false;
        if (
            hitRay.latestHitComponent == hitObject || //selbes objekt getroffen: ray vorher eingetreten
            prevIOR != 1.0f ||
            Vec3.Dot(i_incident, n_normal) > 0.0f
        ) {
            //System.out.println("RAY EXIT " + hitRay.latestHitComponent.name);
            //exit object

            //DEBUG
            mu2 = 1.0f;//1.0f;

            //------ WICHTIG! ------
            //Normale MUSS umgedreht werden, weil wir die INNENSEITE treffen!!!!!
            //normale zeigt sonst immer nach aussen 
            n_normal = n_normal.multScalar(-1.0f);
            swapped = true;
        }
        
        if(Vec3.Dot(i_incident, n_normal) > 0.0f && !swapped)
        {
            //n_normal = n_normal.multScalar(-1.0f);
        }
            
            
        

        //System.out.println("ior from " + mu1 + " to " + mu2);
        
       
        // refractions ray nach snellius berechnen
        float fracMu1Mu2 = mu1 / mu2; //DEBUG FLIP, doesnt work makes smaller
        float fracMu1Mu2Squared = fracMu1Mu2 * fracMu1Mu2;

        
        
        

        /// ------ UMDREHEN WENN FALSCHE RICHTUNG! ------- ?????
        /*if (Vec3.Dot(i_incident, n_normal) > 0.0f){
            n_normal = n_normal.multScalar(-1.0f);
        }*/
        
        
        float nDoti = Vec3.Dot(n_normal, i_incident);
        float nDoti_2 = nDoti * nDoti;
        Vec3 frontVector = i_incident.multScalar(fracMu1Mu2);
        
        
        //eta*I - (eta*dot(N,I) + sqrt(k))*N;
        
        //(nDoti_n.sub(i_incident)).multScalar(fracMu1Mu2);

        double innerSqrt = 1.0f - ((1.0f - nDoti_2) * fracMu1Mu2Squared);
        //System.out.println("refract sqrt " + innerSqrt);
        if (innerSqrt >= 0.0f) {
            //System.out.println("refract sqrt");

            double sqrt = Math.sqrt(innerSqrt);
            //front teil der formel - sqrt
            Vec3 transmisisonRay = frontVector.sub(
                n_normal.multScalar(sqrt + fracMu1Mu2 * nDoti)
            );
            
            //paket * snellius
            //transmisisonRay = transmisisonRay.multScalar(fracMu1Mu2).normalize();

            //DEBUG
            //transmisisonRay = transmisisonRay.multScalar(-1.0f);

            


            outRay.rayIOR = mu2;//hitMaterialIOR(); //currentIOR(); //ior of next medium
            outRay.override(
                GetHitpoint(transmisisonRay),
                transmisisonRay
            );

            //der aktuelle hitcomponent muss in den
            //ray reinkopiert werden um zu prüfen ob ich
            //das objekt nochmal getroffen hab.
            //wenn ja: ist es ein refract ray.

            //copy hit component for refract detection out going
            outRay.latestHitComponent = hitObject;
            //default
            return true;

        }
        //System.out.println("Hit Ray invalid refract sqrt!");
        return false;
            
    }


    public SceneObject GetHitObject() {
        return hitObject;
    }










}