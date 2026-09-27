package scene;

import utils.algebra.Vec3;
import scene.*;
import scene.Transform;
import scene.light.Materials.Material;

import java.util.ArrayList;
import java.util.List;

import raytracer.HitTests.*;

//scene object class to derive sphere and others from

//jedes scene objekt hat einen Transform und hat die möglichkeit intersection
//tests durchzuführen
public class SceneObject {

    public Transform transform = new Transform();
    public Material material = new Material();
    public String name = "SceneObject";
    public boolean IsEnabled = true;


    public SceneObject() {

    }

    public void SetLocation(Vec3 newLocation) {
        if (transform != null) {
            transform.SetTranslation(newLocation);
        }
    }

    public boolean DoesIntersect(Ray ray) {
        HitResult emptyHitResult = new HitResult();
        boolean result = DoesIntersectWithBetterResult(ray, emptyHitResult);
        
        return result;
        //return false;
    }

    //reine methode um intersection tests zu erlauben:
    //wenn true: pass in sub klasse für test erlaubt
    public boolean DoesIntersectWithBetterResult(Ray ray, HitResult resultToUpdate) {
        if (!IsEnabled) {
            return false;
        }
        return true; //is allowed if not implemented.
    }




    //new: testing needed for recursive childs

    //abstract interface to get attached children
    public List<SceneObject> GetChilds() {
        return new ArrayList<>();
    }

    public void UpdateAllChildTransforms() {
        List<SceneObject> childs = GetChilds();
        if (childs.size() > 0) {
            for (SceneObject s : childs) {
                if (s != null) {
                    
                }
            }
        }
    }




    public SceneObject FindByName(String name) {
        if (this.name.equals(name)) {
            return this;
        }
        List<SceneObject> childs = GetChilds();
        for (SceneObject s : childs) {
            if (s != null) {
                SceneObject found = s.FindByName(name);
                if (found != null) {
                    return found;
                }
            }

        }
        return null;
    }

    

    protected boolean HasChilds() {
        return GetChilds().size() > 0;
    }

    //brings a ray into localspace
    protected Ray InverseTransformRay(Ray other) {
        //make more efficent

        
        
        
        //override
        
        Ray copy = other.copy();
        Vec3 OriginWorld = copy.Origin();
        Vec3 rayDir = copy.Direction();

        Vec3 Origin = this.transform.InverseTransformPosition(OriginWorld); //local origin
        rayDir = this.transform.InverseTransformDirection(rayDir);
        
        copy.override(Origin, rayDir);
        //Ray r = new Ray(Origin, rayDir);
        return copy;
    }

    //converts a local t value to world space scalars
    //necessary for consistent world updates for hit order
    protected float tScalarToWorldSpace(Ray rayworld, Vec3 worldHit) {
        Vec3 delta = worldHit.sub(rayworld.Origin());
        
        float worldT = Vec3.Dot(delta, rayworld.Direction()); // a dot b = sum a_i b_i = (euklidDistanz)^2
        
        //Unnötige wurzel operation.
        //delta.length();// / rayworld.Direction().length();
        
        float t = worldT;
        return t;
    }




    ///checks all attached child components and then the current object for a hit
    public boolean DoesIntersectWithBetterResultRecursive(Ray ray, HitResult resultToUpdate) {
        
        //if any child was hit: return the child
        boolean found = DoesIntersectWithBetterResultRecursiveChilds(ray, resultToUpdate);
        
        //if no child was hit, check this object for a intersection test
        if (!found && DoesIntersectWithBetterResult(ray, resultToUpdate)) {
            found = true;
        }
    
        return found;
    }


    //checks all childs for a intersection test, and updates the result for the best one
    //the parent transform is applied to inverse the ray
    //DOES NOT HAVE RECURSIVE SCENE TREES!
    protected boolean DoesIntersectWithBetterResultRecursiveChilds(Ray ray, HitResult resultToUpdate) {
        
        //System.out.print("Intersect Child Test");
        Ray rayLocalSpace = InverseTransformRay(ray.copy());
        boolean found = false;
        List<SceneObject> childs = GetChilds();
        for (SceneObject c : childs) {
            if (c != null) {
                //if has inner hit self or childs:
                HitResult temporaryHitResult = new HitResult();
                if (c.DoesIntersectWithBetterResultRecursive(rayLocalSpace, temporaryHitResult)) {

                    //if doesnt have child:
                    //is direct hit
                    //update hit result
                    if (!c.HasChilds()) {
                        Vec3 localHit = temporaryHitResult.GetHitpointRaw();
                        Vec3 localHitNormal = temporaryHitResult.GetHitNormal();
                        float t = temporaryHitResult.GetT();

                        if (t < 0.0f) {
                            //System.out.println("t " + t);
                            
                            return false;
                        }

                        Vec3 worldHit = transform.TransformPositition(localHit);
                        Vec3 worldNormal = transform.TransformNormal(localHitNormal).normalize();

                        //t = Vec3.Distance(ray.Origin(), worldHit);
                        Vec3 gx = worldHit.sub(ray.Origin());
                        //scalar = distTarget / distAll
                        t = gx.length() / ray.Direction().length();

                        if (resultToUpdate.IsCloser(
                                t,
                                c,
                                worldHit,
                                worldNormal,
                                ray)) {
                            found = true;
                        }
                    }
                }
                
            }
        }
        if (found) {
            //System.out.print("Intersect Child Test ---> FOUND!");
        }
        
        return found;
    }




    


   
}
