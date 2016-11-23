import model.*;

/**
 * Created by dvorkin on 23.11.2016.
 */
public class GameTarget {

    private final TargetType type;

    public GameTarget(Building building) {
        type = TargetType.BUILDING;
    }

    public GameTarget(Wizard wizard) {
        type = TargetType.WIZARD;
    }

    public GameTarget(Minion minion) {
        type = TargetType.MINION;
    }

    public GameTarget(Tree tree) {
        type = TargetType.TREE;
    }

    public GameTarget(Bonus bonus) {
        type = TargetType.BONUS;
    }

    public GameTarget(Lane lane) {
        type = TargetType.LANE;
    }

    public TargetType getTargetType() {
        return type;
    }
}
