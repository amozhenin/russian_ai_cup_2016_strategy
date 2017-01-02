import model.CircularUnit;
import model.Faction;
import model.Unit;

/**
 * Created by dvorkin on 25.11.2016.
 */
public class Waypoint extends CircularUnit {
    public Waypoint(double x, double y) {
        super(-1l, x, y, 0.0, 0.0, 0.0, Faction.OTHER, 0.0);
    }
}
