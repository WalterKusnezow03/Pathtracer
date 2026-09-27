package scene.RayGeneration;

import java.util.List;
import java.util.ArrayList;

import raytracer.HitTests.HitResult;
import raytracer.HitTests.MonteCarloRay;
import raytracer.HitTests.Ray;
import utils.algebra.Vec3;

public class SampleRayGeneratorHemiSphere extends SampleRayGenerator {
    
    public SampleRayGeneratorHemiSphere(float s) {
        super(s);
    }


    @Override
    public void MakeSampleRay(MonteCarloRay ray, HitResult fromResult) {
        float u = RandomInRangeZeroOne();
        float v = RandomInRangeZeroOne();

        float phi = 2.0f * (float)Math.PI * v;

        // cosine-weighted hemisphere
        float cosTheta = (float)Math.sqrt(1.0f - u);
        float sinTheta = (float)Math.sqrt(u);

        float cosPhi = (float)Math.cos(phi);
        float sinPhi = (float)Math.sin(phi);

        float x = cosPhi * sinTheta;
        float y = sinPhi * sinTheta;
        float z = cosTheta;


        Vec3 n = fromResult.GetHitNormal();
        Vec3 tangentX = fromResult.TangentA();
        Vec3 tangentY = fromResult.TangentB();
        
        Vec3 w_i = n.multScalar(z).add(tangentX.multScalar(x)).add(tangentY.multScalar(y));

        float pdf = cosTheta / (float) Math.PI;
        
        ray.override(fromResult.GetHitpoint(), w_i);
        ray.SetWeight(pdf);
    }





    /* 
    @Override
    public void MakeSampleRay(MonteCarloRay ray, HitResult fromResult) {
        if (fromResult != null) {
            
            
            //für montecarlo haben wir

            //sample = int (f(w_i) / pdf(w_i))
            //wobei f(w_i) eine zufalls variable ist
            //und pdf(w_i) die gleichverteilung
            //der samples

            //wenn alle samples gleich warhscheinlich sind
            //dann wäre die pdf immer = 1.
            //das wollen wir nicht.
            //Es wären immer einfach random samples

            //pdf(x) ist die funktion die die verteilung
            //der funktion angibt.


            //wir starten mit 2 zufalls zahlen:
            float u = RandomInRangeZeroOne();
            float v = RandomInRangeZeroOne();

            //um koordinaten auf eine sphere zu haben schreiben wir:
            
            //spherische koordinaten sind definiert als:
            //r: distanz zum zielpunkt
            //theta: winkel um pitch Achse
            //phi: winkel um Yaw achse
            //achse: die achse die nach oben zeigt.

            //man nennt theta auch polar winkel. Er liegt in [0,pi], also 180 grad richtung möglich
            //man nennt phi auch azimuthal winkel. Er liegt in [0, 2*pi], also 360 grad.
            
            // -> was ich dazu weiss und schonmal gemacht habe:
            //man kann eine richtung immer in 2 winkeln rekonstruieren.
            //die roll achse ist nicht notwendig, weil eine richtung keine eigen rotation hat.
            
            //um uns das leben einfach zu machen
            //sagen wir das gilt:


            //cos(theta) = u   
            //phi = 2 * pi * v //skallieren auf raum von 0 bis 2 pi

            float cosTheta = u; //nur in 0 bis 1, viertel sphere, nicht -1 bis 1
            float phi = 2.0f * (float) Math.PI * v; //einmal 360 grad

            //wir wissen das gilt:
            //cos^2 = 1 - sin^2 und
            //sin^2 = 1 - cos^2

            //wir schreiben:
            //sin^2(theta) = 1 - cos^2(theta)
            //sin^2(theta) = 1 - u^2
            //sin(theta) = sqrt(1-u^2)

            //merke: theta ist der winkel um die pitch achse,
            //das heisst der sinus des winkels ist entlang x 
            //und der cosinus entlang z


            //dadurch können wir für unser sample festlegen:
            float z = cosTheta;


            //unser phi winkel liegt in der XY ebene.
            //wenn der winkel phi entlang z gemessen wird,
            //dann gilt:
            //cos(phi) = verschiebung entlang x
            //sin(phi) = verschiebung entlang y

            float cosPhi = (float) Math.cos(phi);
            float sinPhi = (float) Math.sin(phi);
            
            //unser sin(theta) liegt entlang der ebene XY.
            float theta = (float) Math.acos(cosTheta);
            float sinTheta = (float) Math.sin(theta);

            //um das in das andere dreieck einzubinden
            //werden beide achsen einfach mit jenem wert skalliert

            float x = cosPhi * sinTheta;
            float y = sinPhi * sinTheta;


            //um diese koordinaten 
            //die übrigens die komponenten eines
            //einheits vektors sind,
            //in das koordinatensystem der normale zu bringen
            //brauchen wir nur die achsen
            //die die normale aufspannt, aus n und den beiden tangenten.

            Vec3 n = fromResult.GetHitNormal();
            Vec3 tangentX = fromResult.TangentA();
            Vec3 tangentY = fromResult.TangentB();

            
            Vec3 w_i = n.multScalar(z).add(tangentX.multScalar(x)).add(tangentY.multScalar(y));

            
            //wie erhalten wir
            //die pdf:

            //
            
            
            
            
            
            
            
            
            //-- noch eher unklar --
            //pdf(w_i))
            //die verteilung der strahlen auf einer sphere
            //heisst cosine weighted 

            //pdf = cos(theta) / pi in [0,1]

            float pdf = cosTheta / (float) Math.PI;
            
            //die verteilungs wahrscheinlichkeit brauchen wir dann wenn 
            //wir das licht auswerten.

            ray.override(fromResult.GetHitpoint(), w_i);
            ray.SetWeight(pdf);
        }
    }*/




}