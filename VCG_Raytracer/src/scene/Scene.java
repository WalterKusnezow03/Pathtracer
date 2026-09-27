package scene;

import java.util.ArrayList;
import java.util.List;


import raytracer.camera.PerspectiveCamera;
import raytracer.camera.PixelOffsetAA;
import scene.RayGeneration.SampleRayGenerator;
import scene.RayGeneration.SampleRayGeneratorHemiSphere;
import scene.RayGeneration.SampleRayGeneratorSquare;
import scene.RayGeneration.AmbientOcclusion.AmbientOcclusionCollector;

import scene.RayGeneration.GlobalIllumination.GlobalIlluminationCollector;
import scene.light.Light;
import scene.light.Materials.Fresnel;
import scene.light.Materials.Material;
import scene.light.Materials.MaterialEnum;
import scene.light.derived.AreaLight;
import ui.Window;
import raytracer.Interface.FRenderPixelTask;
import raytracer.Interface.IRenderInterface;
import raytracer.*;
import raytracer.HitTests.HitResult;
import raytracer.HitTests.HitResultRefractVisible;
import raytracer.HitTests.Ray;
import utils.algebra.Vec2;
import utils.io.Log;
import java.awt.image.BufferedImage;

import utils.RgbColor;
import utils.RgbColorTraced;
import utils.algebra.*;
import utils.algebra.Vec3;

//world for objects and lights
//performs all ray intersect, 
//shading tasks, and can
//compute FRenderPixel Tasks by a given render mode and 
//settings.

//die scene hält alle objekte die geschnitten werden können und auch alle lichter
//die scene ist dafür zuständig einen render pixel task durchzuführen
//der render mode kann dabei eingestellt werden
//jede teilaufgabe enthält dazu eine passende methode und variert von
//rekursivität / refraction / usw.
public class Scene implements IRenderInterface{

    public ArrayList<SceneObject> GetSceneObjects() {
        return sceneObjects;
    }

    public ArrayList<Light> GetLights() {
        return Lights;
    }

    private ArrayList<SceneObject> sceneObjects = new ArrayList<>();
    private ArrayList<Light> Lights = new ArrayList<>();
    private PerspectiveCamera camera = null;
    private static RenderMode renderMode;
    private boolean DrawLightsDebug = false;
    private RgbColor backgroundColor;
    private RgbColor ambientLight = new RgbColor(0, 0, 0);
    private float ambientCoefficent = 1.0f;
    private int bounces = 2;


    private int samplesGI = 50;
    private int levelsGI = 1;
    private boolean bGiEnabled = false;
    private float increaseGi = 1.0f;
    private GlobalIlluminationCollector GI_Collector = null;

    private int samplesAO = 50;
    private float aoMaxDistance = 1.0f;
    private boolean bAOEnabled = false;
    private AmbientOcclusionCollector AO_Collector = null;


    private int AASamples = 1;
    private boolean bAAEnabled = false;

    //shared ray sample generator
    private SampleRayGenerator sampleGeneratorHemiSphere = null;
    private float sizePlaneSampleGenerator = 1.0f;



    //set camera to render rays from.
    public void SetCamera(PerspectiveCamera cameraIn) {
        camera = cameraIn;
        System.out.println("Scene Set Camera!");
    }

    public static boolean LambertOnly() {
        return renderMode == RenderMode.RenderModePraikum2_3_LambertBounceOne;
    }

    //Only render if mode is found
    public void SetRenderMode(RenderMode renderModeIn) {
        renderMode = renderModeIn;

        //float ratio = Main.IMAGE_WIDTH / Main.IMAGE_HEIGHT;
        float aspect = 800.0f / 600.0f;
        if (renderMode == RenderMode.RenderModePraikum1_0_cameraColors) {
            camera.UpdateViewAngleAndAspectRatio(170, aspect);
        } else {
            camera.UpdateViewAngleAndAspectRatio(70, aspect);
        }
    }

    // --- extra settings ---
    public void SetupGI(boolean gi_enabled, int samples, int levels, float increaseGiIn) {
        samplesGI = Math.abs(samples);
        levelsGI = Math.max(Math.abs(levels), 1);
        bGiEnabled = gi_enabled;
        increaseGi = Math.abs(increaseGiIn);
    }

    public void SetupAO(boolean USE_AO, int samples, float AO_MAX_DISTANCE) {
        samplesAO = Math.abs(samples);
        aoMaxDistance = Math.abs(AO_MAX_DISTANCE);
        bAOEnabled = USE_AO;
        AO_Collector.OverrideMaxDistance(aoMaxDistance);
    }

    public void SetupAA(boolean enabled, int samples) {
        AASamples = Math.max(samples, 1);
        bAAEnabled = enabled;
        if (AASamples == 1) {
            bAAEnabled = false;
        }
    }




    // --- extra settings ---

    
    public void SetBackGroundColor(RgbColor color) {
        backgroundColor = color;
    }

    public void SetAmbientLight(RgbColor color, float coefficent) {
        ambientLight = color;
        ambientCoefficent = Math.clamp(coefficent, 0.0f, 1.0f);
    }

    public RgbColor AmbientLightColor() {
        if (bGiEnabled) {
            return RgbColor.BLACK;
        }

        if (ambientLight != null) {
            return ambientLight.multScalar(ambientCoefficent);
        }
        return new RgbColor(0, 0, 0);
    }


    private boolean debugShadow = false;
    public void EnableDebugShadows(boolean flag) {
        debugShadow = flag;
    }

    public void EnableDebugLights(boolean flag) {
        DrawLightsDebug = flag;
    }

    private boolean bRefractDebugDraw = false;
    public void EnableDebugRefract(boolean flag) {
        bRefractDebugDraw = flag;
    }


    public Scene() 
    {
        SetupGiCollector();
        Log.print(this, "Scene Init");
    }

    private void SetupGiCollector() {
        
        sampleGeneratorHemiSphere = new SampleRayGeneratorHemiSphere(sizePlaneSampleGenerator);
        GI_Collector = new GlobalIlluminationCollector(sampleGeneratorHemiSphere);
        AO_Collector = new AmbientOcclusionCollector(sampleGeneratorHemiSphere, aoMaxDistance);
    }

    public void setBounces(int bounces) {
        this.bounces = Math.max(Math.abs(bounces), 0);
    }

    public int GetNumBounces() {
        return bounces;
    }

    /// renders a renderpixel task with the selected render mode
    /// each render mode has a own render loop.
    public void renderPixel(FRenderPixelTask task){
        if (renderMode == RenderMode.RenderModePraikum1_0_cameraColors) {
            RenderCameraColors(task);
            return;
        }
        if (renderMode == RenderMode.RenderModePraikum1_2_SpehereIntersect) {
            RenderCameraSingleIntersect(task);
            RenderLights(task);
            return;
            //System.out.println("SCENE RENDER MODE 2");
        }
        if (
            renderMode == RenderMode.RenderModePraikum2_3_LambertBounceOne ||
            renderMode == RenderMode.RenderModePraikum2_4_PhongBounceOne
        ) {
            RenderCameraSingleIntersectLight(task);
            RenderLights(task);
            return;
        }
        
        if (renderMode == RenderMode.RenderModePraikum3_5_PhongBounceN) {
            RenderCameraNIntersectLight(task, bounces);
            RenderLights(task);
            return;
        }


        if(bAAEnabled){
            if(AASamples > 1){
                RenderAA(task, bounces, AASamples);
                RenderLights(task);
                return;
            }
        }
            
            


        if (renderMode == RenderMode.RenderModePraikum3_6_PhongBounceNRefract) {
            RenderCameraNIntersectLitAndRefracted(task, bounces);
            RenderLights(task);
            return;
        }
        

        

        

    }
    
    //is not properly tested but extecuted
    private void RenderCameraColors(FRenderPixelTask task) {
        if (task == null) {
            return;
        }
        if (camera == null) {
            return;
        }
        

        task.SetColor(
            //camera.MakeRayColorNormalizedSpace(
            camera.MakeRayColorRaw(
                task.GetPixelPosition(), task.GetWindowWidth(), task.GetWindowHeight()
            )
        );
    }
    








    /// boolean intersect color from ray
    private void RenderCameraSingleIntersect(FRenderPixelTask task) {
        RenderCameraSingleIntersect(task, true);
    }
    

    /// boolean intersect color from ray
    private void RenderCameraSingleIntersect(FRenderPixelTask task, boolean useAO) {
        HitResult result = new HitResult();////new HitResult();
        Ray ray = camera.MakeRay(task);

        //prints every pixel as expected
        //System.out.println("ray test " + task.GetPixelPosition());
        if (IntersecTest(ray, result)) {
            
            RgbColor colorBase = new RgbColor(result.HitMaterial().baseColor);
            if (useAO) {
                AO_Collector.ApplyAmbientOcclusion(colorBase, this, result, samplesAO);
            }
        
            task.SetColor(colorBase);
        } else {
            //draw none
            task.SetColor(new RgbColor(new Vec3(0, 0, 0)));
        }
    }







    
    //rendert das bild mit einem einfachen hittest
    //berücksichtigt dabei aber das hit material und lichter innder scene
    //es wird kein bounce durchgeführt
    private void RenderCameraSingleIntersectLight(FRenderPixelTask task) {

        //ray erzeugen und durch die scene schicken
        Ray ray = camera.MakeRay(task);
        HitResult result = new HitResult();//new HitResult();//HitresultPerformancePool.Find();//new HitResult();
       
        //wenn ein hitresult valide ist: wird es ausgewertet
        if (IntersecTest(ray, result)) {

            //der additive farbton beginnt bei garkeiner farbe
            RgbColor additiveColor = new RgbColor(0.0f, 0.0f, 0.0f);//new RgbColor(.1f, .1f, .1f);

            //für den schnittpunkt werden alle lichter durchgegangen 
            //und abgelichtet wenn sichtbar - visibility test wird vom licht aufgerufen / hier versteckt.
            boolean inShadow = IlluminateResultInShadow(result, additiveColor);
            

            //debug only
            if (inShadow) {
                //set debug color.
                if (debugShadow) {
                    task.SetColor(new RgbColor(0, 1, 0)); //DEBUG COLOR
                    
                    return;
                }
            }
            
            //farbe wird in pixel task und dann bild eingetragen
            additiveColor.AddColor(ambientLight);
            task.SetColor(additiveColor);

        } else {
            //if no hit was detected: show background color
            task.SetColor(backgroundColor);
        }
        
    }


    /// illuminates a hit result and returns if the point is lit or not.
    public boolean IlluminateResultInShadow(
        HitResult result,
        RgbColor additiveColor //color added and clamped
    ) {
        //if material is refractive it cant be lit
        if (result.HitMaterialHasRefraction()) {
            return false;
        }



        //check all lights
        boolean inShadow = true;
        //durch alle lichter wird das result, output Farbe und
        //diese scene durch gegeben
        //die szene wird mit gegeben um zu prüfen:
        //ist das licht sichtbar, ja oder nein: shadow ray.
        for (Light light : Lights) {
            if (light.Illuminate(additiveColor, this, result)) {
                inShadow = false;
                additiveColor.clamp();
            }
        }
        return inShadow;
    }

    private RgbColor GetDirectLightIncludingShadowTest(HitResult result){
        RgbColor directLight = new RgbColor(0, 0, 0);
        boolean inShadow = IlluminateResultInShadow(result, directLight);
        return directLight;
    }
    
            




    


    /// methode rendert einen pixel mit den angegebenen bounces die durchgeführt
    /// werden sollen.
    /// Mindestens einer ist notwendig.
    public void RenderCameraNIntersectLight(FRenderPixelTask task, int bouncesLeft){
        if (bouncesLeft <= 0) {
            return;
        }
        Ray ray = camera.MakeRay(task);
        RgbColor additiveColor = RecursiveBounceRay(ray, bouncesLeft);
        additiveColor.AddColor(AmbientLightColor());
        task.SetColor(additiveColor);
    } 

    //reine reflektions methode
    private RgbColorTraced RecursiveBounceRay(Ray ray, int recursionsLeft) {
        if (recursionsLeft < 0) {
            return new RgbColorTraced(false);
        }
        recursionsLeft--; //eine rekursion weniger für den nächsten bounce


        HitResult result = new HitResult();//HitresultPerformancePool.Find();//new HitResult();
        
        //wenn ein hit erkannt wurde kann das result belichtet werden
        if (IntersecTest(ray, result)) {
            //alle lichter durchgehen, standard ableuchten
            RgbColor directLight = GetDirectLightIncludingShadowTest(result);
            

            RgbColor indirectLight = RgbColor.BLACK;
            
            //wenn das material reflektierend ist, also einen reflektiven anteil
            //von 0 bis 1, wird der reflektion ray durch die szene geschickt 
            //und die farbe als solches gespeichert
            if (result.HitMaterialHasReflectiveNess()) { // || true 
                //Ray ray2 = result.ReflectionRay();
                result.MakeReflectionRay(ray); //inplace, ray not used anymore

                //um das indirekte licht zu erhalten schicken wir
                //den strahl weiter durch die scene
                //indirectLight = RecursiveBounceRay(ray2, recursionsLeft);
                indirectLight = RecursiveBounceRay(ray, recursionsLeft);
            }

            //macht result color aus direct und indirect light und
            //der reflektivität des materials
            RgbColor resultColor = AddIndirectLightToBasedOnHitResultMaterial(
                directLight, indirectLight, result
            );

            RgbColorTraced traced = new RgbColorTraced(resultColor);
            traced.wasHitColor = true;
            
            return traced;
        }
       
        return new RgbColorTraced(RgbColor.BLACK);
    }
    

    //inplace to direct light
    private RgbColor AddIndirectLightToBasedOnHitResultMaterial(
        RgbColor directLight,
        RgbColor indirectLight,
        HitResult hitResultWithMaterial
    ) {
        //aus dem indirekten licht: aus den bounces, wird es mit der eigenen
        //reflektivitäts gewichtung auf die direkte beleuchtung durch Lambert / Phong modell
        //drauf gerechnet
        //direct + reflectiveCoeff * indirect //bounce light gewichtet aufaddieren
        /*RgbColor resultColor = directLight.add(
                    indirectLight.multScalar(
                        hitResultWithMaterial.HitMaterial().GetReflectiveNess()));*/
        directLight.AddColor(indirectLight.multScalar(
                        hitResultWithMaterial.HitMaterial().GetReflectiveNess()));
        return directLight;
    }




    //methode um einen render task mit reflektion und refraction zu berechnen!
    public void RenderCameraNIntersectLitAndRefracted(FRenderPixelTask task, int bouncesLeft) {
        if (bouncesLeft <= 0) {
            return;
        }
        Ray ray = camera.MakeRay(task);
        //RgbColor additiveColor = RecursiveBounceRayIncludingRefraction(ray, bouncesLeft);

        RgbColorTraced additiveColor = RecursiveBounceRayRefractionAndReflection(ray, bouncesLeft);

        if (!additiveColor.wasHitColor) {
            additiveColor.AddColor(AmbientLightColor());
        }
        task.SetColor(additiveColor);
    }
    
    
    //methode erlaubt reflektierendes und refraktierendes licht
    //rekursiv
    public RgbColorTraced RecursiveBounceRayRefractionAndReflection(Ray ray, int recursionsLeft) {
        if (recursionsLeft < 0) {
            return new RgbColorTraced();
        }
        recursionsLeft--; //eine recursion weniger

        HitResult result = new HitResult();
        if (IntersecTest(ray, result)) {
            //weight fresnel vorher cachen
            float weightReflect = Fresnel.ComputeFresnel_RelfectionWeight(
                    ray, result);

            //direktes Licht holen
            RgbColor directLight = GetDirectLightIncludingShadowTest(result);

            //add AO
            if (bAOEnabled) {
                AO_Collector.ApplyAmbientOcclusion(directLight, this, result, samplesAO);
            }

            //add Global illumnation
            RgbColor ambientGi = new RgbColor(0, 0, 0);
            AddColorGi(result, ambientGi, recursionsLeft);


            //hit any object: true: no ambient here: personal design choice.
            RgbColorTraced resultColorRefraction = new RgbColorTraced(result);

            //das refraktierende licht wird einfach reingerechnet / addiert erstmal.
            AddColorBounceRayRefractiveIfMaterialIsRefractive(ray, result, resultColorRefraction, recursionsLeft);

            //indirektes licht aus der scene holen
            //solange ein material reflektiv ist, wird auch die reflektierte farbe 
            //drauf gerechnet.
            //only reflect if material is reflective
            RgbColor indirectLight = new RgbColor(0, 0, 0);
            if (result.HitMaterialHasReflectiveNess()) { // || true 
                //Ray ray2 = result.ReflectionRay();
                result.MakeReflectionRay(ray); //in place, ray not used anymore
                indirectLight = RecursiveBounceRayRefractionAndReflection(ray, recursionsLeft);
            }

            //make fresnel weight
            if (result.HitMaterialHasRefraction()) {
                float weightRefract = 1.0f - weightReflect;

                weightRefract = Math.clamp(weightRefract, 0.0f, 1.0f);
                weightReflect = Math.clamp(weightReflect, 0.0f, 1.0f);

                //float a = weightReflect;
                //float b = weightRefract;

                //refractive base right now
                resultColorRefraction.SetColor(resultColorRefraction.multScalar(weightRefract));
                resultColorRefraction.AddColor(indirectLight.multScalar(weightReflect));
                return resultColorRefraction; //reflection blended in
            }

            //auswerten vom direkten(beleuchtung + refraktion) und indirekten licht (reflektion)
            RgbColor _resultColor = AddIndirectLightToBasedOnHitResultMaterial(
                    directLight, indirectLight, result);
            
            //add ambient gi
            _resultColor.AddColor(ambientGi);

            return new RgbColorTraced(result, _resultColor);

        }

        return new RgbColorTraced(false);
    }
    
    //adds the global illumated color and prepares it as ambient color.
    private void AddColorGi(HitResult hitResult, RgbColor outColor, int recursionsLeft) {
        if (GIAllowed(recursionsLeft)) {
            //new
            if (recursionsLeft <= 0) {
                return;
            }
            //recursionsLeft = 1;
            if (hitResult.HitMaterialHasRefraction() == false || true) {
                GI_Collector.ComputeGiSample(this, outColor, hitResult, samplesGI, recursionsLeft);
                outColor.multScalarInPlace(ambientCoefficent);
                
                //debug
                outColor.multScalarInPlace(increaseGi);
            }
        }
    }




    /// rechnet den refkraktiven farb anteil auf die outColor
    /// drauf, wenn das Material refraktierend ist.
    private void AddColorBounceRayRefractiveIfMaterialIsRefractive(
        Ray ray, //hit ray, do not modify
        HitResult result, //hit result
        RgbColor outColorAdditive, //color to be modified
        int recursionsLeft //recursions left
    ) {
        boolean isRefractive = result.HitMaterialHasRefraction();
        if (isRefractive) {
            Ray refractRay = new Ray(new Vec3(), new Vec3());
            if (result.MakeRefractRay(ray, refractRay)) { //cant modify ray, is used later
                int compensateRefractAfterHit = recursionsLeft;
                
                
                //wenn der akteulle ray in das material eintritt,
                //müssen wir den austritt erlauben um objekte dahinter auszuwerten
                //die rekursions zahl wird einmalig erhöht.
                //ob der ray aktuell eintritt oder austritt wird durch die generierung
                //des refract rays im hit result eingestellt.
                if (ray.IsRefractRay() == false) {
                    compensateRefractAfterHit += 1;
                }
                RgbColor refractColor = RecursiveBounceRayRefractionAndReflection(refractRay,
                        compensateRefractAfterHit);

                //System.out.println("refract color " + refractColor.toString());
                outColorAdditive.AddColor(refractColor);

                //draw refract area debug
                if (bRefractDebugDraw) {
                    outColorAdditive.AddColor(new RgbColor(0, 0, 1));
                }

            }
            //return resultColor; //mix instead.

        }
    }

    











    //// ---- AA -----

    public void RenderAA(FRenderPixelTask task, int bouncesLeft, int AASamples) {
        List<RgbColor> colorsComputed = new ArrayList<>();
        
        Vec2 offsetInner = new Vec2();
        Ray ray = new Ray(new Vec3(), new Vec3());
        for (int i = 0; i < AASamples; i++) {
            PixelOffsetAA.MakeOffset(offsetInner, i, AASamples);
            camera.MakeRayInPlace(task, offsetInner, ray);
            colorsComputed.add(
                    RecursiveBounceRayRefractionAndReflection(ray, bouncesLeft));
        }
        //System.out.println("rendered AA rays " + colorsComputed.size());

        //aus samples erstmal uniform gewichten
        float uniformWeight = 1.0f / (float) colorsComputed.size();
        RgbColor first = colorsComputed.get(0).multScalar(uniformWeight);
        for (int i = 1; i < colorsComputed.size(); i++) {
            first.AddColor(colorsComputed.get(i).multScalar(uniformWeight));
        }

        //iA + sum...
        first.AddColor(AmbientLightColor());
        task.SetColor(first);
    }








    //// ---- AA -----



    private boolean GIAllowed(int recursionsLeft) {
        return bGiEnabled && GIAllowedForRecursionsLeft(recursionsLeft);
    }
        
    private boolean GIAllowedForRecursionsLeft(int recursionsLeft) {
        if (recursionsLeft < 0) {
            return false;
        }

        int allBounces = bounces;
        int giLevelsMin = allBounces - levelsGI;

        //wenn bounces left 8
        //gi level = 2
        //10 - 2 <= 8, gi noch allowed
        //10 - 8 <= 7 bounces left, gi nicht allowed!
        return giLevelsMin <= recursionsLeft; //in range of levels, >= weil immer direkt einer abgezogen wird
    }
    













    public boolean PointIsVisible(Vec3 target, Vec3 hitPoint) {
        return PointIsVisible(target, hitPoint, null);
    }




    ///prüft ob ein punkt einen anderen punkt sehen kann, und dabei nicht unterbrochen wird
    public boolean PointIsVisible(Vec3 target, Vec3 hitPoint, String debugBounceFromName) {
        if (!ValidParams(target, hitPoint)) {
            return false;
        }

        /*HitResult result = new HitResult();
        Ray ray = new Ray(null, null);
        MakeRayFromStartToTarget(hitPoint, target, ray, result);*/



        Vec3 dir = target.sub(hitPoint);//AB = B - A
        Ray ray = new Ray(hitPoint, dir.normalize());
        HitResult result = new HitResult();//HitresultPerformancePool.Find();//new HitResult();
        result.nametest = "light test";

        //um probleme mit transformations spaces zu vermeiden wird
        //der scalar t mit reiner länge überschrieben.
        //das muss passieren damit t nicht bei unendlich anfängt,
        //und alle objekte die auf sichtstrecke aber ausserhalb des abschnitts liegen
        //ignoriert werden.
        //not any hit, but between point and target hit
        float tExternal = (dir.length() / 1.0f);
        result.setTExternal(tExternal);




        if (!IntersecTest(ray, result)) {
            //wenn nichts getroffen wurde gibt es auch kein objekt dazwischen,
            //die punkte können sich sehen.
            return true;
        }
        //reiner debug zweck
        if (debugBounceFromName != null) {
            if (result.HitObjectName().contains("Top") || debugBounceFromName.contains("Top")) {
                //debug
                /*System.out.println(
                    " ( Light " + light.name + "occuluded by " + result.HitObjectName() + ") " +
                    "bounced from " + debugBounceFromName +
                    "t max: " + tExternal + " " + " tHit" + result.GetT()
                );*/
            }
        }

        

        return false;
    }

    private boolean ValidParams(Vec3 target, Vec3 hitPoint) {
        return target != null && hitPoint != null;
    }

    

    

    ///prüft ob ein punkt ein (punkt) licht sehen kann.
    public boolean LightIsVisible(Light light, Vec3 hitPoint, String debugBounceFromName) {
        return PointIsVisible(light.transform.GetTranslation(), hitPoint, debugBounceFromName);
    }


    // ---- NEW ----

    public boolean LightIsVisibleIncludingRefraction(
        Light light, 
        Vec3 hitPoint, 
        String debugBounceFromName,
        HitResultRefractVisible resultA
    ) {
        return PointIsVisibleIncludingRefraction(light.transform.GetTranslation(), hitPoint, debugBounceFromName, resultA);
    }

    ///prüft ob ein punkt einen anderen punkt sehen kann, und dabei nicht unterbrochen wird
    public boolean PointIsVisibleIncludingRefraction(
        Vec3 target, Vec3 hitPoint, String debugBounceFromName, HitResultRefractVisible refractVisible
    ) {
        if (!ValidParams(target, hitPoint)) {
            return false;
        }

        
        Vec3 dir = target.sub(hitPoint);//AB = B - A
        Ray ray = new Ray(hitPoint, dir.normalize());
        HitResult result = new HitResult();//HitresultPerformancePool.Find();//new HitResult();
        result.nametest = "light test";

        //um probleme mit transformations spaces zu vermeiden wird
        //der scalar t mit reiner länge überschrieben.
        //das muss passieren damit t nicht bei unendlich anfängt,
        //und alle objekte die auf sichtstrecke aber ausserhalb des abschnitts liegen
        //ignoriert werden.
        //not any hit, but between point and target hit
        float tExternal = (dir.length() / 1.0f);
        result.setTExternal(tExternal);



        
        if (!IntersecTestIgnoreRefract(ray, result, target, refractVisible)) {
            //wenn nichts getroffen wurde gibt es auch kein objekt dazwischen,
            //die punkte können sich sehen.
            return true;
        }



        //reiner debug zweck
        if (debugBounceFromName != null) {
            if (result.HitObjectName().contains("Top") || debugBounceFromName.contains("Top")) {
                //debug
                /*System.out.println(
                    " ( Light " + light.name + "occuluded by " + result.HitObjectName() + ") " +
                    "bounced from " + debugBounceFromName +
                    "t max: " + tExternal + " " + " tHit" + result.GetT()
                );*/
            }
        }

        

        return false;
    }


    // ---- NEW ----


    


    private boolean IntersecTest(Ray ray) {
        HitResult r = new HitResult();
        boolean result = IntersecTest(ray, r);
       
        return result;
    }


    ///geht durch alle scene objekte und wählt das beste hit result.
    public boolean IntersecTest(Ray ray, HitResult result) {
        if (ray == null) {
            //System.out.println("RAY INVALID");
            return false;
        }
        //für spätere belichtungs aufgaben wird die camera position gesetzt
        result.SetCameraPos(camera.GetPosition());

        boolean foundAnyHit = false;
        //go through all objects since a result can be beaten by distance.
        for (SceneObject current : sceneObjects) {
            if (current != null) {
                //je nach objekt wird dann eine andere intersect methode aufgerufen
                //beispiel: sphere und meshdaten / plane unterscheiden sich
                //durch subsitution und polymorphismus hier aber möglich.
                if (current.DoesIntersectWithBetterResult(ray, result)) {
                    foundAnyHit = true;
                    //System.out.println("RAY HIT");
                }
            }
        }

        return foundAnyHit;
    }

    private boolean IntersecTestIgnoreRefract(
        Ray ray, HitResult result, Vec3 target, HitResultRefractVisible refractVisible
    ) {
        if (ray == null) {
            //System.out.println("RAY INVALID");
            return false;
        }
        
        
        boolean foundAnyHit = false;
        //go through all objects since a result can be beaten by distance.
        for (SceneObject current : sceneObjects) {
            if (current != null) {
                //je nach objekt wird dann eine andere intersect methode aufgerufen
                //beispiel: sphere und meshdaten / plane unterscheiden sich
                //durch subsitution und polymorphismus hier aber möglich.
                if (current.DoesIntersectWithBetterResult(ray, result)) {
                    foundAnyHit = true; //muss man prüfen       
                    //System.out.println("RAY HIT");
                }
            }
        }
        
        if (foundAnyHit) {
            //TESTING NEEDED!
            if (result.HitMaterialHasRefraction()) {
               
                Ray refractRay = new Ray(new Vec3(), new Vec3());
                if (result.MakeRefractRay(ray, refractRay)) {
                    HitResult newResult = new HitResult();
                    if (IntersecTest(refractRay, newResult)) {
                        //System.out.println("IntersecTestIgnoreRefract test inner B");
                        if(PointIsVisibleIncludingRefraction(
                            target,
                            newResult.GetHitpoint(),
                            null,
                            refractVisible
                        )
                        ) {
                            //System.out.println("VISIBLE TROUGH REFRACT!");

                            //hier muss es mehr info geben
                            //dass das auch returned wurde
                            //schatten gedämpft!
                            refractVisible.MarkVisibleTroughRefractTrue();
                            return false;
                        }
                    }

                }
            }
            //TESTING NEEDED!
        }




        return foundAnyHit;
    }






    //kann nachträglich punkt lichter rendern, reiner debug zweck!
    private void RenderLights(FRenderPixelTask task) {
        if (DrawLightsDebug) {
            Ray ray = camera.MakeRay(task);

            //go through all objects since a result can be beaten by distance.
            for (Light light : Lights) {
                if (light != null) {
                    if (light.DoesIntersect(ray)) {
                        task.SetColor(light.color); //debug render light
                    }
                }
            }
        }
    }






    public void AddObject(SceneObject object) {
        //check is light
        if (object instanceof Light) {
            Light casted = (Light) object;
            AddLight(casted);
            return;
        }


        sceneObjects.add(object);
    }

    public void AddLight(Light light) {
        Lights.add(light);
    }




    


    //finds a scene object by name
    private SceneObject FindByName(String name) {

        for (SceneObject s : sceneObjects) {
            if (s != null) {
                SceneObject found = s.FindByName(name);
                if (found != null) {
                    return found;
                }
            }
        }
        for (SceneObject light : Lights) {
            SceneObject found = light.FindByName(name);
            if (found != null) {
                return found;
            }
        }



        return null;
    }

}

