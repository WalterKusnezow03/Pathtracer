package utils;


import raytracer.HitTests.HitResult;

///flags if was hitcolor
public class RgbColorTraced extends RgbColor {

    public boolean wasHitColor = false;
    private HitResult hitResult = null;

    public HitResult getHitResult() {
        return hitResult;
    }

    public RgbColorTraced() {
        super(0, 0, 0);
    }

    public RgbColorTraced(RgbColor other) {
        super(0, 0, 0);
        if (other != null) {
            SetColor(other);
        }
    }

    public RgbColorTraced(boolean hit) {
        super(0, 0, 0);
        wasHitColor = hit;
    }

    public RgbColorTraced(HitResult hit) {
        super(0, 0, 0);
        wasHitColor = hit != null;
        hitResult = hit;
    }

    

    public RgbColorTraced(HitResult result, RgbColor other) {
        super(0, 0, 0);
        wasHitColor = result != null;
        hitResult = result;
        if (other != null) {
            SetColor(other);
        }
    }
}
