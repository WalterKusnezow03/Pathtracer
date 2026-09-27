package utils;

import utils.algebra.Vec3;

public class RgbColor {

    public Vec3 colors;
    public float red(){ return colors.x; }
    public float green(){ return colors.y; }

    public float blue() {
        return colors.z;
    }
    
    

    public static RgbColor DARK_CUSTOM = new RgbColor(0.02f, 0.01f, 0.01f);
    public static RgbColor RED = new RgbColor(0.5f, 0, 0);
    public static RgbColor DARK_RED = new RgbColor(.01f, 0, 0);
    public static RgbColor GREEN = new RgbColor(0, 1, 0);
    public static RgbColor DARK_GREEN = new RgbColor(0, .01f, 0);
    public static RgbColor BLUE = new RgbColor(0, 0, 0.5f);
    public static RgbColor DARK_BLUE = new RgbColor(0, 0, .01f);
    public static RgbColor WHITE = new RgbColor(1, 1, 1);
    public static RgbColor BLACK = new RgbColor(0, 0, 0);
    public static RgbColor CYAN = new RgbColor(0, 1, 1);
    public static RgbColor MAGENTA = new RgbColor(1, 0, 1);
    public static RgbColor YELLOW = new RgbColor(1, 1, 0);
    public static RgbColor GRAY = new RgbColor(0.5f, 0.5f, 0.5f);
    public static RgbColor SOFT_GRAY = new RgbColor(0.50f, 0.50f, 0.50f);
    public static RgbColor LIGHT_GRAY = new RgbColor(0.75f, 0.75f, 0.75f);
    public static RgbColor DARK_GRAY = new RgbColor(0.1f, 0.1f, 0.1f);

    public RgbColor(float r, float g, float b) {
        colors = new Vec3(r, g, b);

        this.clamp();
    }
    
    public RgbColor(RgbColor other) {
        colors = new Vec3(other.colors);

        this.clamp();
    }

    public RgbColor(Vec3 color) {
        colors = new Vec3(color);

        this.clamp();
    }
    
    public void SetColor(Vec3 color) {
        colors = color;
    }

    public void SetColor(RgbColor color) {
        colors = new Vec3(color.colors);
    }


    public void add(float r, float g, float b){
        colors.x += r;
        colors.y += g;
        colors.z += b;

        this.clamp();
    }

    public void sub(float r, float g, float b){
        colors.x -= r;
        colors.y -= g;
        colors.z -= b;

        this.clamp();
    }

    public RgbColor sub(RgbColor color){
        return new RgbColor( colors.sub(color.colors) );
    }

    // ----- BEHINDERT -----
    public RgbColor add(RgbColor color){
        return new RgbColor( colors.add(color.colors) );
    }

    public RgbColor multRGB(RgbColor color){
        return new RgbColor( colors.x * color.red(),
                             colors.y * color.green(),
                             colors.z * color.blue() );
    }

    public RgbColor multRGBInPlace(RgbColor color){
        colors.x *= color.red();
        colors.y *= color.green();
        colors.z *= color.blue();
        return this;
    }



    public RgbColor multScalar(float value) {
        Vec3 copy = new Vec3(colors);
        return new RgbColor(copy.multScalar(value));
    }

    public RgbColor multScalarInPlace(float value) {
        colors.x *= value;
        colors.y *= value;
        colors.z *= value;
        clamp();
        return this;
    }

    public RgbColor multScalar(double value) {
        Vec3 copy = new Vec3(colors);
        return new RgbColor( copy.multScalar(value) ); 
    }

    public int getRGB(){
        return ((int) (this.red() * 255f) << 16) + ((int) (this.green() * 255f) << 8) + ((int) (this.blue() * 255f));
    }

    public RgbColor square(){
        return new RgbColor(this.red() * this.red(), this.green() * this.green(),this.blue() * this.blue());
    }

    public void clamp(){
        if( this.red() > 1.0f ) colors.x = 1f;
        if( this.green() > 1.0f ) colors.y = 1f;
        if( this.blue() > 1.0f ) colors.z = 1f;

        if( this.red() < 0.0f ) colors.x = 0f;
        if( this.green() < 0.0f ) colors.y = 0f;
        if( this.blue() < 0.0f ) colors.z = 0f;
    }

    @Override
    public String toString(){
        return "( " + this.red() + ", " + this.green() + ", " + this.blue() + " )";
    }

    public boolean equals(RgbColor inColor) {
        return inColor.red() == this.red() && inColor.green() == this.green() && inColor.blue() == this.blue();
    }
    

    public static RgbColor LerpColor(RgbColor a, RgbColor b, float t) {
        RgbColor out = null;
        if (a != null && b != null) {
            //gx: A + t (B-A)
            Vec3 ab = b.colors.sub(a.colors); //B-A
            ab = ab.multScalar(t); 
            
            
            out = new RgbColor(a.colors);
            out.colors.add(ab);
        }

        return out;
    }




    /*
    Adds a rgb color PROPERLY WITHOUT COPY CONSTRUCTOR
    */
    public void AddColor(RgbColor colorIn) {
        colors = colors.add(colorIn.colors);
        clamp(); //very important.
    }

    public void AddColorNoClamp(RgbColor colorIn) {
        colors = colors.add(colorIn.colors);
    }

    




}
