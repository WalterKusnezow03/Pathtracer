package scene.primitives.meshdata;

import raytracer.HitTests.HitResult;
import utils.algebra.Vec3;

//class to support triangle hittests
//-> compute a
public class FTriangle {
    
    //ein triangle frame besteht immer aus
    //3 eck punkten.
    //aus jenen lassen sich immer alle kanten, und die normale erzeugen
    private Vec3 v0;
    private Vec3 v1;
    private Vec3 v2;
    private Vec3 v0v1;
    private Vec3 v1v2;
    private Vec3 v2v0;
    private Vec3 normal;

    private float nQ = 0.0f;

    public Vec3 GetNormal() {
        return normal;
    }

    //copy constructor
    FTriangle(FTriangle other) {
        v0 = new Vec3(other.v0);
        v1 = new Vec3(other.v1);
        v2 = new Vec3(other.v2);

        v0v1 = new Vec3(other.v0v1);
        v1v2 = new Vec3(other.v1v2);
        v2v0 = new Vec3(other.v2v0);

        normal = new Vec3(other.normal);

        nQ = other.nQ;
    }

    FTriangle(Vec3 v0In, Vec3 v1In, Vec3 v2In, Vec3 normalIn) {
        v0 = new Vec3(v0In);
        v1 = new Vec3(v1In);
        v2 = new Vec3(v2In);

        //edges für right left test / point in triangle test konstruieren und cachen.
        v0v1 = v1.sub(v0); //AB = B - A
        v1v2 = v2.sub(v1);
        v2v0 = v0.sub(v2);

        normal = normalIn;

        nQ = Vec3.Dot(normal, v0);
    }
    
    /// checks if a given Origin and direction does hit this
    /// triangle in the positive direction
    public boolean RayTriangleIntersect(Vec3 Origin, Vec3 dir, HitResult outresult) {
        float t = -1.0f;

        if (Origin.isNaN()) {
            System.out.print("(plane hit test Origin NaN");
            return false;
        }
        if (dir.isNaN()) {
            System.out.print("(plane hit test dir NaN");
            return false;
        }

        //Ex: n(P-Q) = 0 //Q auf ebene
        //n((A+tD)-Q) = 0 //gerade einsetzen
        //nA + t*nD - nQ = 0 //alles skalarprodukte
        
        //t*nD = nQ - nA
        //t = (nQ - nA) / nD

        float nD = Vec3.Dot(dir, normal);
        if (Float.isNaN(nD)) {
            System.out.print("(plane hit test nD NaN");
            return false;
        }

        //System.out.print("(plane hit test " + nD + " " + dirN + " " + normal +")");
        if (Math.abs(nD) > 1e-6f){
            //float nQ = Vec3.Dot(normal, v0);
            float nA = Vec3.Dot(normal, Origin);
            t = (nQ - nA) / nD;
            //System.out.println("plane hit " + t +  "normal" + normal);
            if (t > 0.0f) {
                Vec3 hitPoint = Origin.add(dir.multScalar(t));
                if (PointInTriangle(hitPoint)) {
                    
                    //if the result is better: update
                    if (outresult.IsCloser(t, hitPoint, normal)) {
                        return true;
                    }
                }
            } 
        }

        return false;
    }





    

    private boolean PointInTriangle(
        Vec3 point
    ) {
        float eps = 1e-5f;
        //wenn alle kreuz produkte nach oben zeigen, mit der normale dot > 0, dann ist der punkt drinnen

        //a x b, spannt ebene auf, n  zeigt nach oben, wenn a links von b liegt

        //was wir hier machen ist neue ebenen aufzuspannen
        //aus der edge direction und dem relativen punkt

        //wenn der relative punkt rechts von der gegeben kante liegt,
        //erzeugt es eine richtung "nach oben"
                

        //wir bringen den punkt in das jeweils relative koordinaten system
        //und zeigen mit einer richtung darauf
        //das kreuzprodukt zeigt dann in eine richtung: wenn punkt links: zeigt der vektor nach "oben"
        Vec3 c0 = Vec3.Cross(v0v1, point.sub(v0)); //AB = B - A; pLocal = Point - Vertex
        Vec3 c1 = Vec3.Cross(v1v2, point.sub(v1));
        Vec3 c2 = Vec3.Cross(v2v0, point.sub(v2));

        //winding order korrigieren,
        //sodass der punkt relativ zur edge auf der linken seite liegt.
        //dazu reicht es die normale zu flippen.
        //a x b = n
        //-a x b = -n
        //b x a = -n //<-- haben point auf falscher seite im kreuz produkt: normale einfach drehen.
        c0.multScalar(-1.0f);
        c1.multScalar(-1.0f);
        c2.multScalar(-1.0f);
        
        //wenn alle aufgespannten ebenen
        //in die selbe richtung schauen ist der 
        //punkt im dreieck (geht auch so.)
        /*boolean dotA = Vec3.Dot(c0, c1) >= -eps ? true : false;
        boolean dotB = Vec3.Dot(c1, c2) >= -eps ? true : false;
        boolean dotC = Vec3.Dot(c2, c0) >= -eps ? true : false;
        return dotA && dotB && dotC;*/

        //wenn alle aufgespannten ebenen
        //in die selbe richtung schauen wie die originale ebene
        //dann liegt der punkt im dreieck.
        //wenn eine der normalen die erzeugt wurde nach unten zeigt,
        //dann weil einer der egdes nicht rechts von dem localen punkt lag.

        return (Vec3.Dot(c0, normal) >= -eps &&
                Vec3.Dot(c1, normal) >= -eps &&
                Vec3.Dot(c2, normal) >= -eps);
    }


}
