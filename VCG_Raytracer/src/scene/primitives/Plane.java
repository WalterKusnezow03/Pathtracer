package scene.primitives;

import raytracer.HitTests.HitResult;
import raytracer.HitTests.Ray;
import scene.SceneObject;
import scene.primitives.meshdata.MeshData;
import utils.algebra.Vec2;
import utils.algebra.Vec3;

public class Plane extends SceneObject {
    
    private Vec3 tangentU;
    private Vec3 tangentV;
    private Vec3 v0Anchor;


    public Plane() {
        Setup(1);
    }

    public Plane(float scaleUniform) {
        Setup(scaleUniform);
        //this.transform.SetScaleUniform(scaleUniform);
    }

    public Plane(float scaleX, float scaleY) {
        Setup(scaleX, scaleY);
        //this.transform.SetScaleUniform(scaleUniform);
    }
    
    public Plane copyWithWorldTransform() {
        Plane p = new Plane();
        p.meshData = meshData.copy();
        p.transform = transform.copy();
        return p;
    }
    
    //eine ebene hat immer ein set an meshdaten
    //was im kern einfach nur triangle frames hält.
    private MeshData meshData = new MeshData();

    ///setup plane from a given uniform scale
    private void Setup(float scale) {
        /*
        v1-->v2
        |     |
        v0<--v3
        */
        /*Vec3 v0 = new Vec3(0, 0, 0);
        Vec3 v1 = new Vec3(scale, 0, 0);
        Vec3 v2 = new Vec3(scale, scale, 0);
        Vec3 v3 = new Vec3(0, scale, 0);*/

        float half = scale / 2.0f;
        Vec3 v0 = new Vec3(-half, -half, 0);
        Vec3 v1 = new Vec3(half, 0 - half, 0);
        Vec3 v2 = new Vec3(half, half, 0);
        Vec3 v3 = new Vec3(-half, half, 0);
        Setup(v0, v1, v2, v3);

    }
    
    /// set ups a plane from x and y scale,
    /// the pivot of the meshdata will be in the center of the plane
    private void Setup(float scaleX, float scaleY) {
        float halfX = scaleX / 2.0f;
        float halfY = scaleY / 2.0f;
        Vec3 v0 = new Vec3(-halfX, -halfY, 0);
        Vec3 v1 = new Vec3(halfX, 0 -halfY, 0);
        Vec3 v2 = new Vec3(halfX, halfY, 0);
        Vec3 v3 = new Vec3(-halfX, halfY, 0);
        Setup(v0, v1, v2, v3);
    }

    ///sets up a plane from 4 given vertices
    private void Setup(Vec3 v0, Vec3 v1, Vec3 v2, Vec3 v3) {
        meshData.AddEfficent(v0, v1, v2, v3);

        /*
        1->2
        |  |
        0<-3
        */
        v0Anchor = new Vec3(v0); //area light is centered, need bottom left for tangents / uvs
        tangentU = v1.sub(v0);
        tangentV = v3.sub(v0);
    }

    /// computes a uv coordinate from the plane and adds the translation
    /// of the plane. 
    public Vec3 GetUVWorld(float u, float v) {
        //rotate scale (?)
        return transform.GetTranslation().add(GetUVLocal(u, v));

        //return transform.TransformPositition(GetUVLocal(u, v));
    }

    ///computes a local point on the plane from the plane tangents
    private Vec3 GetUVLocal(float u, float v) {
        u = Math.clamp(u, 0.0f, 1.0f);
        v = Math.clamp(v, 0.0f, 1.0f);
        Vec3 made = tangentU.multScalar(u);
        made.AddVector(tangentV.multScalar(v));
        made.AddVector(v0Anchor);
        return made;
    }


    public Vec3 Normal() {
        return meshData.GetNormal(0);
    }




    public String toString() {
        return "Plane " + meshData.toString();
    }

    /// computes the ray intersect in the proper transform space
    /// and returns true if the hit was better
    @Override
    public boolean DoesIntersectWithBetterResult(Ray ray, HitResult resultToUpdate) {
        //if object is disabled: return false
        if (!super.DoesIntersectWithBetterResult(ray, resultToUpdate)) {
            return false;
        }


        Ray r = InverseTransformRay(ray);
        Vec3 rayDir = r.Direction();
        Vec3 Origin = r.Origin();


        /*Vec3 OriginWorld = ray.Origin();
        Vec3 rayDir = ray.Direction();

        Vec3 Origin = this.transform.InverseTransformPosition(OriginWorld); //local origin
        rayDir = this.transform.InverseTransformDirection(rayDir);*/

        //looks correct
        //System.out.println("World " + OriginWorld + "Origin local " + Origin + " dir " + rayDir);
        
        
        HitResult temporaryHitResult = new HitResult();
        if (meshData.DoesIntersectWithBetterResult(Origin, rayDir, temporaryHitResult)) {

            //copy data from local hit result and transform to
            //world
            Vec3 localHit = temporaryHitResult.GetHitpointRaw();
            Vec3 localHitNormal = temporaryHitResult.GetHitNormal();
            float t = temporaryHitResult.GetT();
            
           
            //rays can only hit forward
            if (t < 0.0f) {
                System.out.println("t plane " + t);
                
                return false;  
            }
            

            Vec3 worldHit = transform.TransformPositition(localHit);
            Vec3 worldNormal = transform.TransformNormal(localHitNormal).normalize();

            
            //necessary for consistent world updates for hit order
            t = tScalarToWorldSpace(ray, worldHit);
            
            /// update the result with the world data and this component since the cached
            /// result for meshdata
            /// does not hold any further information
            if(resultToUpdate.IsCloser(
                t,
                this,
                worldHit,
                worldNormal,
                ray
            )) {
                /*System.out.println(
                    "Origin World " +
                    OriginWorld +
                    " origin local " +
                    Origin +
                    " hit local " +
                    localHit +
                    " world hit" +
                    transform.TransformPositition(localHit)
                );
                System.out.println("NORMAL LENGTH " + worldNormal.length() + " dir " + worldNormal);
                
                
                
                System.out.println("t better  " + t + " old t " + tCopy);*/
                //System.out.println("PLANE HIT " + name);

                //debug
                if (name.contains("Top")) {
                    //System.out.println("Cornell Top hit: normal " + worldNormal);
                }

                
                return true;
            }
            
        }
        


        //perform mesh data hit
        
        
        return false;
    }



};
