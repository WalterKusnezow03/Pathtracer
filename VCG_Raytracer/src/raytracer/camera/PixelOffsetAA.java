package raytracer.camera;

import utils.algebra.Vec2;

public class PixelOffsetAA {
    

    public static void MakeOffset(
        Vec2 offset,
        int index, 
        int samples
    ) {
        DefaultOffset(offset); //fallback
        if (samples == 2) {
            MakeOffset2AA(offset, index);
        }
        if (samples == 4) {
            MakeOffset4AA(offset, index);
        }
        if (samples == 8) {
            MakeOffset8AA(offset, index);
        }
        RandomOffset(offset);
        Clamp(offset);
    }

    private static void RandomOffset(Vec2 offset) {
        offset.x += RandomInRange(0.0f, 1.0f);
        offset.y += RandomInRange(0.0f, 1.0f);
    }

    private static void Clamp(Vec2 offset) {
        offset.x = Math.clamp(offset.x, 0.0f, 1.0f);
        offset.y = Math.clamp(offset.x, 0.0f, 1.0f);
    }


    private static void MakeOffset2AA(Vec2 offset, int index) {
        
        DefaultOffset(offset);
        
        return;

        //wurde noch nicht hergeleitet.
        //R2 Sample
        /*offset.x = 0.5f;
        offset.y = 0.5f;

        //float g1 = 0.7548776662466927f;
        //float g2 = 0.5698402909980532f;

        float g1 = 0.754877f;
        float g2 = 0.569840f;

        offset.x += 1.0f / (offset.x * g1);
        offset.y += 1.0f / (offset.y * g2);*/


    }

    private static void MakeOffset4AA(Vec2 offset, int index) {
        switch (index) {
            case 0: {
                offset.x = 0;
                offset.y = 0;
                return;
            }
            case 1: {
                offset.x = 0;
                offset.y = 1.0f;
                return;
            }
            case 2: {
                offset.x = 1.0f;
                offset.y = 1.0f;
                return;
            }
            case 3: {
                offset.x = 1.0f;
                offset.y = 0.0f;
                return;
            }
        }
    }
    
    private static void MakeOffset8AA(Vec2 offset, int index){
        switch (index) {
            case 4: {
                offset.x = 0f;
                offset.y = 0.5f;
                return;
            }
            case 5: {
                offset.x = 0.5f;
                offset.y = 1.0f;
                return;
            }
            case 6: {
                offset.x = 1.0f;
                offset.y = 0.5f;
                return;
            }
            case 7: {
                offset.x = 0.5f;
                offset.y = 0.0f;
                return;
            }
        }
    }


    
    private static void DefaultOffset(Vec2 offset){
        offset.x = 0.5f;
        offset.y = 0.5f;
    }


    private static float RandomInRange(float i, float j) {
        float range = j - i;

        float u = i + ((float) Math.random() * range % range);
        return u;
    }
    
    public static int RandomIntInRange(int i, int j) {
        float u = RandomInRange(i, j);
        return (int) u;
    }


}
