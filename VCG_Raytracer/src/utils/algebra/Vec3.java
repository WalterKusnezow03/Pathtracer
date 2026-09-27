package utils.algebra;

public class Vec3 {
    public float x;
    public float y;
    public float z;

    //checks if the vector contains NaN 
    public boolean isNaN() {
        return Float.isNaN(x) ||
            Float.isNaN(y) ||
            Float.isNaN(z);
    }


    /**
     Standard 3D constructor taking all values given
     **/
    public Vec3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3(Vec3 other) {
        if (other != null) {
            this.x = other.x;
            this.y = other.y;
            this.z = other.z;
        }
        
    }

    /**
     Standard 3D constructor setting all values to 0
     **/
    public Vec3() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }
    
    public void set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     Compare two vectors to check if they are equal
     **/
    public boolean equals(Vec3 inputVec){
        return (this.x == inputVec.x) && (this.y == inputVec.y) && (this.z == inputVec.z);
    }

    /**
     Get normalized vector
     **/
    public Vec3 normalize(){
        float length = this.length();
        return new Vec3(this.x / length, this.y / length, this.z / length);
    }

    /**
     Get length of vector
     **/
    public float length(){
        return (float) Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }

    /**
     Get sum of vector with the given vector
     **/
    public Vec3 add(Vec3 inputVec) {
        return new Vec3(this.x + inputVec.x, this.y + inputVec.y, this.z + inputVec.z);
    }
    
    public void AddVector(Vec3 inputVec) {
        this.x += inputVec.x;
        this.y += inputVec.y;
        this.z += inputVec.z;
    }


    /**
     Get difference between vector and the given vector
     **/
    public Vec3 sub(Vec3 inputVec){
        return new Vec3(this.x - inputVec.x, this.y - inputVec.y, this.z - inputVec.z);
    }

    /**
     Get opposite vector
     **/
    public Vec3 negate(){
        return new Vec3(-this.x, -this.y, -this.z);
    }

    /**
     Get scalar product of vector and given vector ("SCALAR product. DIGGA. DOT product.")
     **/
    public float scalar(Vec3 inputVec) {
        return this.x * inputVec.x + this.y * inputVec.y + this.z * inputVec.z;
    }

    /// dot product between 2 vectors
    public static float Dot(Vec3 a, Vec3 b) {
        return a.x * b.x + a.y * b.y + a.z * b.z;
    }



    /**
     Get new vector with the given value multiplied to every component
     **/
    public Vec3 multScalar(float value) {
        return new Vec3(this.x * value, this.y * value, this.z * value);
    }
    
    public Vec3 multScalar(double value){
        return multScalar((float) value);
    }

    /**
     Get new vector through the cross product of the vector and the given vector
     **/
    public Vec3 cross(Vec3 inputVec){
        return new Vec3(
                this.y * inputVec.z - inputVec.y * this.z,
                this.z * inputVec.x - inputVec.z * this.x,
                this.x * inputVec.y - inputVec.x * this.y
        );
    }

    /**
     Print values
     **/
    @Override
    public String toString(){
        return "( " + this.x + ", " + this.y + ", " + this.z + " )";
    }

    public Vec3 invert() {
        return new Vec3(
                1f / this.x,
                1f / this.y,
                1f / this.z);
    }
    

    public static float Distance(Vec3 a, Vec3 b) {
        //|a| = sqrt(sum(a_i))
        Vec3 ab = b.sub(a);
        return ab.length();
    }

    public static Vec3 Cross(Vec3 a, Vec3 b) {
        return a.cross(b);
    }



    //reflects a direction
    public static Vec3 Reflect(Vec3 incidentDir, Vec3 HitNormal) {
        Vec3 I = incidentDir.normalize();
        Vec3 N = HitNormal.normalize();
        return I.sub(N.multScalar(2.0f * Vec3.Dot(I, N)));
    }



}
