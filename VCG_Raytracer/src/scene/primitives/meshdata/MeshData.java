package scene.primitives.meshdata;

import java.util.ArrayList;

import raytracer.HitTests.HitResult;
import raytracer.HitTests.Ray;
import utils.algebra.Vec3;

///mesh data class as expected.
public class MeshData {


    private ArrayList<Integer> triangleBuffer = new ArrayList<>();
    private ArrayList<Vec3> vertexBuffer = new ArrayList<>();
    private ArrayList<Vec3> normalBuffer = new ArrayList<>();
    private float epsilon = 0.1f;

    private ArrayList<FTriangle> triangleIntersectFrames = new ArrayList<>();

    
    public MeshData() {

    }

    public MeshData copy() {
        MeshData meshData = new MeshData();
        for (int i = 0; i < triangleBuffer.size(); i++) {
            meshData.triangleBuffer.add(triangleBuffer.get(i));
        }
        for (int i = 0; i < vertexBuffer.size(); i++) {
            meshData.vertexBuffer.add(new Vec3(vertexBuffer.get(i)));
        }
        for (int i = 0; i < normalBuffer.size(); i++) {
            meshData.normalBuffer.add(new Vec3(normalBuffer.get(i)));
        }
        for (int i = 0; i < triangleIntersectFrames.size(); i++) {
            meshData.triangleIntersectFrames.add(new FTriangle(triangleIntersectFrames.get(i)));
        }
        return meshData;
    }

    public Vec3 GetNormal(int index) {
        if (index >= 0 && index < normalBuffer.size()) {
            return normalBuffer.get(index);
        }
        return new Vec3();
    }


    
    public void Add(Vec3 v0, Vec3 v1, Vec3 v2) {
        int sizeBuffer = vertexBuffer.size();
        vertexBuffer.add(v0);
        vertexBuffer.add(v1);
        vertexBuffer.add(v2);

        triangleBuffer.add(sizeBuffer);
        triangleBuffer.add(sizeBuffer + 1);
        triangleBuffer.add(sizeBuffer + 2);

        Vec3 normalShared = Normal(v0, v1, v2);
        normalBuffer.add(normalShared);
        normalBuffer.add(normalShared);
        normalBuffer.add(normalShared);
    }

    public int NumVertecies() {
        return vertexBuffer.size();
    }

    public void AddEfficent(Vec3 v0, Vec3 v1, Vec3 v2, Vec3 v3) {
        /*
        //quad winding order face to viewer.
        1-->2
        |   |
        0<--3
        */
        AddEfficent(v0, v1, v2);
        AddEfficent(v0, v2, v3);
    }


    public void AddEfficent(Vec3 v0, Vec3 v1, Vec3 v2) {
        int v0Index = HasVertex(v0);
        int v1Index = HasVertex(v1);
        int v2Index = HasVertex(v2);
        Vec3 normalShared = Normal(v0, v1, v2);

        if (v0Index == -1) {
            //vertex nicht vorhanden:
            //add
            vertexBuffer.add(v0);
            triangleBuffer.add(NumVertecies() - 1); //added: new index at -1
            normalBuffer.add(normalShared);
        } else {
            //already has vertex: add triangle index instead.
            triangleBuffer.add(v0Index);
        }
        
        if (v1Index == -1) {
            //vertex nicht vorhanden:
            //add
            vertexBuffer.add(v1);
            triangleBuffer.add(NumVertecies() - 1); //added: new index at -1
            normalBuffer.add(normalShared);
        } else {
            triangleBuffer.add(v1Index);
        }
        

        if (v2Index == -1) {
            //vertex nicht vorhanden:
            //add
            vertexBuffer.add(v2);
            triangleBuffer.add(NumVertecies() - 1); //added: new index at -1
            normalBuffer.add(normalShared);
        } else {
            triangleBuffer.add(v2Index);
        }
        AddTriangleFrame(
            triangleBuffer.size() -3,
            triangleBuffer.size() -2,
            triangleBuffer.size() -1
        );
    }
    
    ///tries to find a vertex by index.
    int HasVertex(Vec3 pos) {
        int index = -1;
        float closest = epsilon;
        for (int i = 0; i < vertexBuffer.size(); i++) {
            Vec3 compare = vertexBuffer.get(i);
            float distance = Vec3.Distance(compare, pos);
            if (distance <= closest) {
                closest = distance;
                index = i;
            }
        }
        return index; //if none found is -1
    }

    Vec3 Normal(Vec3 v0, Vec3 v1, Vec3 v2) {
        //v0v1
        //v0v2
        //n = v0v1 x v0v2
        Vec3 v0v1 = v1.sub(v0); //AB = B - A
        Vec3 v0v2 = v2.sub(v0); //AB = B - A
        Vec3 normal = Vec3.Cross(v0v1, v0v2).normalize();
        return normal;
    }


    //constructs a triangle frame from
    //given triangle indices (which point to the vertex buffer indirectly)
    //indices from triangle buffer
    void AddTriangleFrame(int t0, int t1, int t2) {
        Vec3 v0 = vertexBuffer.get(triangleBuffer.get(t0));
        Vec3 v1 = vertexBuffer.get(triangleBuffer.get(t1));
        Vec3 v2 = vertexBuffer.get(triangleBuffer.get(t2));
        FTriangle triangle = new FTriangle(v0, v1, v2, Normal(v0, v1, v2));
        triangleIntersectFrames.add(triangle);
    }





    /// local data in!
    /// checks all triangles and finds the best hit,
    /// if any better hit was found: returns true
    public boolean DoesIntersectWithBetterResult(Vec3 Origin, Vec3 rayDir, HitResult resultToUpdate) {
        //System.out.println("mesh data hit test");
        boolean found = false;
        for (int i = 0; i < triangleIntersectFrames.size(); i++) {
            //any triangle was hit sucessfully:
            FTriangle triangle = triangleIntersectFrames.get(i);
            if (triangle.RayTriangleIntersect(Origin, rayDir, resultToUpdate)) {
                found = true;
                //System.out.println("mesh data hit test ok");
            }
        }
        
        
        return found;
    }


    public String toString() {
        String out = new String("meshdata: ");

        out += "\n vertecies: ";
        for (Vec3 vertex : vertexBuffer) {
            out += vertex.toString();
        }

        out += "\n triangles ";
        for(int t = 2; t < triangleBuffer.size(); t+=3){
            int t0 = t-2;
            int t1 = t-1;
            int t2 = t;

            out += "(" + 
            triangleBuffer.get(t0) + " " +
            triangleBuffer.get(t1) + " " +
            triangleBuffer.get(t2) + " " +
            ")";
        }
        
        
        return out;
    }



};
