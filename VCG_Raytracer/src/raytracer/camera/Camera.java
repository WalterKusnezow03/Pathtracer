package raytracer.camera;

import utils.algebra.Vec2;
import utils.algebra.Vec3;

/// the camera class is a orthogonal camera class, where (0,0,1) is the world up
/// direction, x forward and y side.
/// ICH ARBEITE MIT Z UP! ES IST MIR EGAL!
public class Camera {

    //protected Vec3 upVectorWorld = new Vec3(0,0,1);
    protected Vec3 upVectorWorld = new Vec3(0,0,1);
    protected Vec3 side = new Vec3();
    protected Vec3 forward = new Vec3();
    protected Vec3 up = new Vec3(); 
    protected Vec3 pos = new Vec3();

    public Vec3 GetPosition(){
        return new Vec3(pos);
    }

    public Camera(Vec3 _pos, Vec3 lookAt){
        SetupVectors(_pos, lookAt);
    }

    protected void SetupVectors(Vec3 _pos, Vec3 lookAt) {
        pos = _pos;
        forward = lookAt.sub(_pos); //AB = B - A
        forward = forward.normalize();

        //side = upVectorWorld.cross(forward); 
        //forward liegt geometrisch rechts world Up, aber side zeigt dann nach links.
        //deshalb muss forward x world sein. 

        side = upVectorWorld.cross(forward);//forward.cross(upVectorWorld);//.multScalar(-1.0f); //debug 
        
        
        
        side = side.normalize();
        up = forward.cross(side); 
        up = up.normalize();

        //R[s,u,f]

        System.out.println("CAMERA LOOK DIR " + forward);
        System.out.println("CAMERA LOCAL SIDE DIR " + side);
        System.out.println("CAMERA LOCAL UP DIR " + up);
    }

};