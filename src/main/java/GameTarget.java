import model.*;

/**
 * Created by dvorkin on 23.11.2016.
 */
public class GameTarget {

    private final TargetType type;
    private final CircularUnit target;

    public GameTarget(Building building) {
        type = TargetType.BUILDING;
        target = building;
    }

    public GameTarget(Wizard wizard) {
        type = TargetType.WIZARD;
        target = wizard;
    }

    public GameTarget(Minion minion) {
        type = TargetType.MINION;
        target = minion;
    }

    public GameTarget(Tree tree) {
        type = TargetType.TREE;
        target = tree;
    }

    public GameTarget(Bonus bonus) {
        type = TargetType.BONUS;
        target = bonus;
    }

    public GameTarget(Lane lane) {
        type = TargetType.LANE;
        //TODO work this out
        target = null;
    }

    public TargetType getTargetType() {
        return type;
    }

    public CircularUnit getTarget() {
        return target;
    }
}
