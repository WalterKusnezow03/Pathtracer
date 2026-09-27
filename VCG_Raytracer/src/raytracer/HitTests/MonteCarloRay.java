package raytracer.HitTests;

import utils.RgbColor;
import utils.algebra.Vec3;

public class MonteCarloRay extends Ray{
    private float OneOverpdf = 1.0f;


    public MonteCarloRay(Vec3 _startpoint, Vec3 _direction){
        super(_startpoint, _direction);
    }

    

    public void SetWeight(float _pdf) {
        if (Math.abs(_pdf) > 0.00000001f) {
            OneOverpdf = 1.0f / _pdf; //f(w_i) / p(w_i)
        }
    }
    
    public void ApplyPDFWeight(RgbColor color) {
        color.multScalarInPlace(OneOverpdf);
    }

    public float GetWeight() {
        return OneOverpdf;
    }



}
