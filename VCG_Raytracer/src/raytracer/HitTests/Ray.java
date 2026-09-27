package raytracer.HitTests;

import scene.SceneObject;
import utils.algebra.Vec3;

public class Ray {

    protected Vec3 startpoint = new Vec3();
    protected Vec3 direction = new Vec3();
   
    

    public Ray(Vec3 _startpoint, Vec3 _direction) {
        this.startpoint = _startpoint;
        this.direction = _direction.normalize();
    }
    
    public Vec3 Origin() {
        return startpoint; //new Vec3(startpoint)
    }

    public Vec3 Direction() {
        return direction;//new Vec3(direction);//.normalize(); //erstmal so raus.
    }

    public Ray copy() {
        return new Ray(new Vec3(startpoint), new Vec3(direction));
    }

    public Vec3 Evaluate(float t) {
        return startpoint.add(direction.multScalar(t));
    }



    public void override(Vec3 _startpoint, Vec3 _direction){
        startpoint = _startpoint;
        direction = _direction.normalize();
    }


    
    public float rayIOR = 1.0f; // air
    public SceneObject latestHitComponent = null;

    public boolean IsRefractRay() {
        return rayIOR != 1.0f;
    }


    //deprecated
    public void markAsExitRefractRay() {
        exitRefract = true;
    }
    
    public boolean exitRefract = false;
    
}