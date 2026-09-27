package scene.light.Materials;

import raytracer.HitTests.HitResult;
import scene.Scene;
import scene.light.Light;
import utils.RgbColor;
import utils.algebra.Vec3;


//achtung: Die Material Klasse includiert Phong und Lambert Material alleine.
//wenn 
public class Material {
    
    
    //overrides the material type: pick from emissive, lambert and phong
    public void OverrideMaterialType(MaterialEnum typeIn) {
        materialEnumType = typeIn;
    }

    public MaterialEnum GetMaterialType() {
        return materialEnumType;
    }

    private MaterialEnum materialEnumType = MaterialEnum.Phong; //default

    //reflectiveness of the material for ray bouncing and mixing indirect light
    private float reflectiveCoeff = 0.5f;

    //index of refraction
    private float IOR = 1.0f; //air ?

    //material base color
    public RgbColor baseColor = new RgbColor(0, 0, 0);

    

    

    //base specular color: ks
    private RgbColor baseSpecular = new RgbColor(1,1,1);
    private float specularStrength = 0.5f; //ksmultiplier - visual editing only
    
    //specular exponent for ks(vr)^n
    private float specularExponent = 64.0f;


    public RgbColor Emissive() {
        if (materialEnumType == MaterialEnum.Emissive) {
            return new RgbColor(baseColor);
        }
        return new RgbColor(0, 0, 0);
    }

    public RgbColor BRDF() {
       return baseColor.multScalar(1.0f / Math.PI);
    }



    public Material() {

    }

    public float GetIOR() {
        return IOR;
    }

    public float GetReflectiveNess() {
        return reflectiveCoeff;
    }

    public void SetReflectiveNess(float valueIn) {
        valueIn = Math.max(0.0f, valueIn);
        valueIn = Math.min(1.0f, valueIn);
        reflectiveCoeff = valueIn;
    }

    private boolean bHasRefraction = false;
    public boolean HasRefraction() {
        return bHasRefraction;
    }

    public void SetIOR(float iorIn, boolean enableRefraction) {
        bHasRefraction = true;
        IOR = iorIn;
    }


    
    public void SetColor(RgbColor color) {
        baseColor = color;
    }

    public void SetSpecularColor(RgbColor color) {
        baseSpecular = color;
    }

    public void SetSpecularStrenght(float specilarStrengthIn) {
        float s = Math.max(specilarStrengthIn, 0.0f);
        s = Math.min(1.0f, s);
        
        specularStrength = s;
    }

    public void shade(Light light, HitResult result, RgbColor additiveColor) {
        shade(light, light.transform.GetTranslation(), result, additiveColor);
    }

    public void shade(Light light, Vec3 posOverride, HitResult result, RgbColor additiveColor) {
        if (light == null) {
            return;
        }
        if (result == null) {
            return;
        }
        //gi
        MaterialEnum shadedType = materialEnumType;
        if (result.HasShadingSettingOverride()) {
            shadedType = result.ShadingMaterialTypeOverride();
        }
        //gi end

        if (shadedType == MaterialEnum.Lambert) {
            LambertShader(light, posOverride, result, additiveColor);
            return;
        }
        if (shadedType == MaterialEnum.Phong) {
            PhongShader(light, posOverride, result, additiveColor);
            return;
        }
        if (shadedType == MaterialEnum.Emissive) {
            EmissiveShader(light, result, additiveColor);
            return;
        }
    }
    
    private void EmissiveShader(Light light, HitResult result, RgbColor additiveColor) {
        //emissve light only, 
        //Kein diffuser anteil, kein skalarprodukt 
        //kein einfallendes licht
        additiveColor.AddColor(baseColor);
    }


    ///computes the lambert shader for a given point
    private void LambertShader(Light light, Vec3 lightposOverride, HitResult result, RgbColor additiveColor){
        //setup vars for readability
        Vec3 hit = result.GetHitpointRaw();
        Vec3 N = result.GetHitNormal().normalize();
        RgbColor lightColor = light.color;
        
        Vec3 lightPosition = lightposOverride;//light.transform.GetTranslation();

        //create the Light vector from hit to light
        Vec3 L = (lightPosition.sub(hit)).normalize(); //AB = B - A, hit to light
        

        //compute the resulting lambert color and add it to the out Color
        RgbColor kdNLlambert = LambertShader(
            N,
            L,
            lightColor
        );
        //System.out.println("lambert " + kdNLlambert);
        additiveColor.AddColor(kdNLlambert);
    }





    private void PhongShader(Light light, Vec3 posOverride, HitResult result, RgbColor additiveColor) {
        //setup vars for readability
        Vec3 hit = result.GetHitpointRaw();
        Vec3 N = result.GetHitNormal().normalize();
        RgbColor lightColor = light.color;
        
        Vec3 lightPosition = posOverride;//light.transform.GetTranslation();
        Vec3 L = (lightPosition.sub(hit)).normalize(); //AB = B - A, hit to light
        
        
        Vec3 V = result.ViewRayToCamera();//IncidentRay.multScalar(-1.0f).normalize();

        /*System.out.println("hit " + hit);
        System.out.println("light " + lightPosition);
        System.out.println("normal " + N);*/

        RgbColor kdNLlambert = LambertShader(
            N,
            L,
            lightColor
        );
        //System.out.println("lambert " + kdNLlambert);
        additiveColor.AddColor(kdNLlambert);
        
        if (Scene.LambertOnly()) {
            return;
        }

        //return;

        additiveColor.AddColor(SpecularShaderPart(V, N, L, lightColor));

    }

    //LambertColor = iL + kd * max(NL,0)
    private RgbColor LambertShader(
        Vec3 N, 
        Vec3 L,
        RgbColor lightColor
    ) {
        //LambertColor = iL + kd * max(NL,0)
        if (N.isNaN()) {
            System.out.println("N is NaN " + N);
        }
        if (L.isNaN()) {
            System.out.println("N is NaN " + L);
        }

        //diffusen licht anteil mit projection von normale auf lichtvektor
        //wie weit schaut meine ebene zum licht.
        //dieser anteil ist nur in 0 bis 1.
        //negatives licht gibt es nicht.
        float diffuse = Math.max(Vec3.Dot(N, L), 0.0f);
        
    
        //System.out.println("diffuse NL " + diffuse + " N " + N + ", L " + L);

        //material base color wird mit dem anteil multipliziert, 
        //und dann weniger stark belichtet basierend auf der gewichtung: licht normale und deren "ähnlichkeit"
        RgbColor kdNL = baseColor.multScalar(diffuse);


        // color = iA kA + sum iP (-->(kd(NL)<-- + ks(VR)^n))
        kdNL = kdNL.multRGBInPlace(lightColor);

        return kdNL;

    }
    
    ///computes the specular for phong shading
    RgbColor SpecularShaderPart(
        Vec3 V, 
        Vec3 N, 
        Vec3 L,
        RgbColor lightColor
    ) {
        if (Vec3.Dot(N, L) <= 0.0f) {
            return RgbColor.BLACK;
        }

        //für den specularen licht anteil gilt nicht der
        //licht vektor sondern der Incident Ray
        Vec3 I = L.multScalar(-1.0f);

        //abstrackte reflect funktion aus der vector3 klasse nutzen 
        //(wurde von mir dazu gebaut.)
        //incident ray wird reflektiert and der normalen
        Vec3 R = Vec3.Reflect(I, N).normalize();
        
        
        //wo sind wir in der belichtungs formel:
        // color = iA kA + sum iP (kd(NL) + ks(VR)^n)
        // color = iA kA + sum iP ((kd(NL) + -->ks(VR)^n<--))
        
        //um den specularen licht anteil zu erhalten müssen wir die ähnlichkeit
        //des Reflektions vektors richtung kamera vermessen
        //wenn er sehr ähnlich ist: Dot Product = 1 
        //dann wird der speculare anteil voll gewichtet
        float VR = Vec3.Dot(V.normalize(), R);
        VR = Math.max(VR, 0.0f);

        //durch einen hohen specularen exponenten
        //werden leichte abweichungen hoch bestraft.
        //wenn der spekulare exponent, niedrig ist: gegen 1
        //dann wird der specular fleck insgesamt breiter
        //weil mehr spielraum erlaubt ist
        double VRSpecPowN = Math.pow(VR, specularExponent); //specular Color none
        if (VRSpecPowN <= 0.0f) {
            return RgbColor.BLACK;
        }

        //ein zusätzlicher parameter //specularStrength ist rein fiktiv 
        //und lässt einen spekularen anteil zusätzlich dimmen
        RgbColor resultSpecularBase = baseSpecular.multScalar(VRSpecPowN * specularStrength);

        // die multiplikation von dem specular base color: K_s, gewichtet mit dem
        // view und reflektionsvektor in ähnlichkeit
        // und der licht farbe,
        // erzeugt die finale farbe
        RgbColor color = resultSpecularBase.multRGBInPlace(lightColor);

    
        return color;
    }

    

}
