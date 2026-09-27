package scene;

import java.util.Set;

import utils.algebra.*;

/// transform to all scene objects
/// allows transforming positions and directions
public class Transform {

    private Matrix4x4 T;
    private Matrix4x4 R;
    private Matrix4x4 S;

    private Matrix4x4 s1r1;
    private Matrix4x4 s1r1t1;

    private Matrix4x4 TRS;

    public Transform() {
        T = new Matrix4x4();
        R = new Matrix4x4();
        S = new Matrix4x4();
        TRS = new Matrix4x4();
        s1r1 = new Matrix4x4();
        s1r1t1 = new Matrix4x4();
    }

    public Transform copy() {
        Transform other = new Transform();
        other.T = new Matrix4x4(T);

        
        other.R = new Matrix4x4(R);
        other.S = new Matrix4x4(S);
        other.TRS = new Matrix4x4(TRS);
        other.s1r1 = new Matrix4x4(s1r1);
        other.s1r1t1 = new Matrix4x4(s1r1t1);
        return other;
    }

    /// returns the expected transform as M = T * R * S
    Matrix4x4 GetTransform() {
        //M = T * R
        //public Matrix4x4 mult(Matrix4x4 mat)
        //return T.mult(R); //T * R 
        //return T.mult(R.mult(S)); //T * R * S <-- lese richtung --

        return TRS;
    }

    //returns the inverse transform of M^-1 = S^-1 * R^T * T^-1
    Matrix4x4 GetInverseTransform() {
        //test
        //return GetTransform().invert();
        
        
        /*//M^-1 = R^-1 * T^-1
        Matrix4x4 r1 = R.transpose(); //R^T = R^-1
        Matrix4x4 t1 = T.InvertTranslation();
        Matrix4x4 s1 = S.InvertScale();
        //return r1.mult(t1);
        
        //M^-1 = S^-1 * R^-1 * T^-1
        return s1.mult(r1.mult(t1));*/
        return s1r1t1;
    }

    ///moves a position into the local space of the transform
    public Vec3 InverseTransformPosition(Vec3 pos) {
        Matrix4x4 tr = GetInverseTransform();
        //return tr.multVec3(pos);
        return tr.multVec3InPlace(pos);
    }

    ///moves a direction into the local space of the transform
    public Vec3 InverseTransformDirection(Vec3 dir) {

        //no translation here.
        //richtung wird nicht mit translation verrechnet.
        //Matrix4x4 r1 = R.transpose(); //R^T = R^-1
        //Matrix4x4 s1 = S.invert();//S.InvertScale();

        //M = S * R
        //M^-1 = R^-1 * S^-1
        //Matrix4x4 s1r1 = s1.mult(r1);
        //s1r1 = r1.mult(s1);

        //return s1r1.multVec3(dir);
        return s1r1.multVec3InPlace(dir);

        /*//M^-1^t = M^-T
        Matrix4x4 s1r1t1 = GetInverseTransform();
        s1r1t1 = s1r1t1.transpose(); //M^-T !!! Very important!
        return s1r1t1.multVec3(pos);**/
    }
    
   private void UpdateS1R1_VectorInverse() {
        Matrix4x4 r1 = R.transpose(); //R^T = R^-1
        Matrix4x4 s1 = S.invert();//S.InvertScale();

        //M = R * S
        //M^-1 = S^-1 * R^-1
        s1r1 = s1.mult(r1);
    }

    private void UpdateFullInverse() {
        UpdateS1R1_VectorInverse();
        Matrix4x4 t1 = T.InvertTranslation();
        s1r1t1 = s1r1.mult(t1);
    }

    private void UpdateTransform() {
        TRS = T.mult(R.mult(S));
    }

    private void UpdatePreComputedMatrixMultiplications() {
        UpdateTransform();
        UpdateFullInverse();
    }


    public Vec3 TransformNormal(Vec3 n) {
        //return GetInverseTransform().transpose().multVec3(n, false);
        return GetInverseTransform().transpose().multVec3InPlace(n, false);
        //multVec3InPlace
    }


    
    ///moves a position into the relative space of transform
    public Vec3 TransformPositition(Vec3 pos) {
        Matrix4x4 trs = GetTransform();
        //return trs.multVec3(pos);
        return trs.multVec3InPlace(pos);
    }

    ///sets the translation of the transform
    public void SetTranslation(Vec3 pos) {
        //Matrix4x4.setValueAt(row, column, value)
        T.setValueAt(0, 3, pos.x);
        T.setValueAt(1, 3, pos.y);
        T.setValueAt(2, 3, pos.z);
        UpdatePreComputedMatrixMultiplications();
        
    }


    public Vec3 GetTranslation() {
        Vec3 result = new Vec3();
        Vec4 asVec4 = T.getColumn(3);
        result.x = asVec4.x;
        result.y = asVec4.y;
        result.z = asVec4.z;
        return result;
    }

    ///sets a uniform scale on the scale matrix
    public void SetScaleUniform(float scale) {
        if (scale > 0.0f) {
            S = new Matrix4x4();
            S = S.scale(scale);
            UpdatePreComputedMatrixMultiplications();
        }
    }

    ///sets the scale to the given values for the scale matrix
    public void SetScale(float sX, float sY, float sZ) {
        S = new Matrix4x4();
        S.setValueAt(0, 0, sX);
        S.setValueAt(1, 1, sY);
        S.setValueAt(2, 2, sZ);
        UpdatePreComputedMatrixMultiplications();
    }

    public void SetScale(Vec3 scale) {
        SetScale(scale.x, scale.y, scale.z);
    }

    ///sets a rotation in roll pitch yaw order
    public void SetRotation(double yaw, double pitch, double roll) {
        Matrix4x4 yawMat = Matrix4x4.Yaw(yaw);
        Matrix4x4 pitchMat = Matrix4x4.Pitch(pitch);
        Matrix4x4 rollMat = Matrix4x4.Roll(roll);

        Matrix4x4 Rnew = rollMat.mult(pitchMat.mult(yawMat));
        R = Rnew;
        UpdatePreComputedMatrixMultiplications();
    }

    //sets the rotation of the transform
    public void SetRotation(Matrix4x4 rIn) {
        R = new Matrix4x4(rIn);
        UpdatePreComputedMatrixMultiplications();
    }

    //returns a copy of the rotation matrix
    public Matrix4x4 GetRotation() {
        return new Matrix4x4(R);
    }




};