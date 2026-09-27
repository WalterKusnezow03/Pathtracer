package raytracer.camera;




import utils.RgbColor;
import utils.algebra.Vec2;
import utils.algebra.Vec3;

import java.util.ArrayList;
import java.util.List;

import raytracer.HitTests.*;
import raytracer.Interface.FRenderPixelTask;


public class PerspectiveCamera extends Camera {

    private float w = 1.0f;
    private float h = 1.0f;

    public float GetViewPlaneWidth() {
        return w;
    }

    public float GetViewPlaneHeight() {
        return h;
    }



    //ratio aspect = w / h (16 / 9 -> 16 : 9) das seitenverhältnis :D
    public PerspectiveCamera(Vec3 _pos, Vec3 lookAt, float angle, float aspect) {
        //SetupVectors(_pos, lookAt);
        super(_pos, lookAt);

        UpdateViewAngleAndAspectRatio(angle, aspect);
    }
    
    public void UpdateViewAngleAndAspectRatio(float angle, float aspect) {
        ComputeScreenVirtualWidthAndHeightFrom(angle, aspect);
    }

    protected void ComputeScreenVirtualWidthAndHeightFrom(float angle, float aspect) {
        //der winkel wird in rad genutzt
        float angleRad = (float)Math.toRadians(angle);
        
        //um die halbe viewport höhe zu erhalten müssen wir den
        //öffnungswinkel halbieren
        float alpha2 = angleRad / 2.0f;

        //half height is tan(angle)
        float halfHeight = (float) Math.tan((float)alpha2);
        h = halfHeight * 2.0f;

        //w from aspect * h, since aspect = (w/h)
        //wir erhalten die breite aus
        //(w/h) * h = w
        w = aspect * h;


        System.out.println("camera virtual screen (" + w + "," + h + ")");

    }

    protected Vec3 CenterOfScreen(){
        return pos.add(forward); 
    }




    // --- default rendering ---


    //generates a ray from a given target pixel
    public Ray MakeRay(FRenderPixelTask task) {
        return MakeRay(
                task.GetPixelPosition(), task.GetWindowWidth(), task.GetWindowHeight());
    }
    
    

    ///generates a ray from a given screen position and the viewport size
    public Ray MakeRay(Vec2 screenPos, float ViewPortWidth, float ViewPortHeight) {
        //System.out.println("camera ray " + screenPos + " " + ViewPortHeight + " " + ViewPortHeight);
        
        Vec3 dir = MakeRayDirection(screenPos, ViewPortWidth, ViewPortHeight);
        Ray ray = new Ray(pos, dir);
        return ray;
    }


    //creates the ray direction in space of (-w/2, +w/2) and (-h/2, h/2)
    public Vec3 MakeRayDirection(Vec2 screenPos, float ViewPortWidth, float ViewPortHeight) {
        /* 
        // ---- SOLLTE SO INORDNUNG SEIN ! ----
        
        //bring to uv[0,1] ---> center pixel, in uv of ViewPort
        float u = (screenPos.x + 0.5f) / ViewPortWidth;
        float v = (screenPos.y + 0.5f) / ViewPortHeight;
        
        //bring to uv[-1,1]
        float pNormX = ((2.0f * u) - 1.0f); //dX
        float pNormY = ((2.0f * v) - 1.0f); //dY
        
        //bring from [-1,1] to [-w/2, w/2] bzw [-h/2, h/2]
        //wieso? weil side und up NORMALISIERT SIND, muss aber richtig
        //skalliert werden!
        float pWidthScaled = pNormX * w * 0.5f;
        float pHeightScaled = pNormY * h * 0.5f;
        
        Vec3 sideScaled = side.normalize().multScalar(pWidthScaled);
        Vec3 upScaled = up.normalize().multScalar(pHeightScaled);
        
        Vec3 ray = forward.add(sideScaled.add(upScaled));
        
        ray = ray.normalize();
        return ray;*/
        return MakeRayDirection(screenPos, new Vec2(0.5f, 0.5f), ViewPortWidth, ViewPortHeight);
    }
    
    //super sampling support
    public Vec3 MakeRayDirection(Vec2 screenPos, Vec2 innerOffset, float ViewPortWidth, float ViewPortHeight) {

        // ---- SOLLTE SO INORDNUNG SEIN ! ----

        //verschieben des pixels um einen gewählten offset.
        //darf nur in 0 und 1 liegen
        float xOff = Math.clamp(innerOffset.x, 0.0f, 1.0f);
        float yOff = Math.clamp(innerOffset.y, 0.0f, 1.0f);

        //bring to uv[0,1] ---> center pixel, in uv of ViewPort
        float u = (screenPos.x + xOff) / ViewPortWidth;
        float v = (screenPos.y + yOff) / ViewPortHeight;

        //das stimmt so wenn z nach oben zeigt und wird auch nicht angefasst!
        //bring to uv[-1,1]
        float pNormX = ((2.0f * u) - 1.0f); //dX
        //float pNormY = (1.0f - (2.0f * v)); //hier so nicht.
        float pNormY = ((2.0f * v) - 1.0f); //dY

        //bring from [-1,1] to [-w/2, w/2] bzw [-h/2, h/2]
        //wieso? weil side und up NORMALISIERT SIND, muss aber richtig
        //skalliert werden!

        //auf viewport grösse bringen aber berücksichtigten dass
        //unser space aktuell doppelt so gross ist
        float pWidthScaled = pNormX * w * 0.5f;
        float pHeightScaled = pNormY * h * 0.5f;

        //ray aus forward, mit w auf side und h auf camera up vektor erzeugt wird
        Vec3 sideScaled = side.normalize().multScalar(pWidthScaled);
        Vec3 upScaled = up.normalize().multScalar(pHeightScaled);

        Vec3 ray = forward.add(sideScaled.add(upScaled));

        //rayrichtung an sich wird normalisiert
        ray = ray.normalize();
        return ray;
    }



    //for AA
    public void MakeRayInPlace(FRenderPixelTask task, Vec2 offsetInner, Ray outRay) {
        Vec3 dir = MakeRayDirection(
            task.GetPixelPosition(), offsetInner, task.GetWindowWidth(), task.GetWindowHeight()
        );
        outRay.override(new Vec3(pos), dir);
    }








    
    
    

    





    public RgbColor MakeRayColorRaw(
        Vec2 screenPos,
        float ViewPortWidth,
        float ViewPortHeight
    ) {
        Vec3 dir = rayDirectionNormalizedSpace(screenPos, ViewPortWidth, ViewPortHeight);

        RgbColor color = new RgbColor(0, 0, 0);
        color.SetColor(dir);
        return color;
    }
    
    public Vec3 rayDirectionNormalizedSpace(
        Vec2 screenPos,
        float ViewPortWidth,
        float ViewPortHeight
    ) {
        //erwarte werte zwischen -1 und 1, wegen cos.
        Vec3 r = MakeRayDirection(screenPos, ViewPortWidth, ViewPortHeight).normalize();
        
        //wenn das skalarprodukt zweier
        //vektoren 0 ergibt sind sie orthogonal zu einander

        //wenn das skalarprodukt zweier vektoren 1 ergibt sind sie
        //paralell zu einander

        //-1 anti paralell
        //cos(theta) = a dot b, wenn a und b normalisiert

        //acos(a*b) = theta wenn a und b normalisiert

        //vermessen der vektor komponenten als winkel relativ zur "x achse", der eigenen achse.
        float x = (float) (Math.acos(r.x)); //0 bis pi (0 bis 180 grad)
        float y = (float) (Math.acos(r.y)); //0 bis pi
        float z = (float) (Math.acos(r.z)); //0 bis pi        

        //auf 0 bis 1
        x /= Math.PI;
        y /= Math.PI;
        z /= Math.PI;

       
        return new Vec3(x, y, z);//.normalize();
    }







    // ---- sepcial rays for camera colors ----
    public RgbColor MakeRayColorNormalizedSpace(
        Vec2 screenPos,
        float ViewPortWidth,
        float ViewPortHeight
    ){
        //bring to uv[0,1] ---> center pixel, in uv of ViewPort
        float u = (screenPos.x + 0.5f) / ViewPortWidth;
        float v = (screenPos.y + 0.5f) / ViewPortHeight;
        
        //bring to uv[-1,1]
        float pNormX = ((2.0f * u) - 1.0f); //dX
        float pNormY = ((2.0f * v) - 1.0f); //dY

        Vec3 colorPos = new Vec3(pNormX, pNormY, 0.0f);
        colorPos = colorPos.normalize();

        float zValue = 0.0f;
        if(colorPos.x < 0.0f){
            zValue += Math.abs(colorPos.x);
        }
        if(colorPos.y < 0.0f){
            zValue += Math.abs(colorPos.y);
        }
        if(zValue > 1.0f){
            zValue = 1.0f; //clamp
        }

        //-1, 1 to 0,1 for color 
        colorPos.x += 1.0f;
        colorPos.y += 1.0f;
        colorPos.x *= 0.5f;
        colorPos.y *= 0.5f;

        colorPos.z = zValue;
        
        //not tested!
        colorPos = colorPos.normalize();


        RgbColor color = new RgbColor(0,0,0);
        color.SetColor(colorPos);
    
        return color;
    }









};
