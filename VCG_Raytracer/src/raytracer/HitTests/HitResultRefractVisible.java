package raytracer.HitTests;

public class HitResultRefractVisible {
    
    private boolean visibleTroughRefract = false;

    public HitResultRefractVisible() {
        visibleTroughRefract = false;
    }
    
    public void MarkVisibleTroughRefractTrue() {
        visibleTroughRefract = true;
    }

    public boolean IsVisbleTroughRefract() {
        return visibleTroughRefract;
    }
}
