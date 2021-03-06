import model.*;

/**
 * Created by dvorkin on 23.11.2016.
 */
public class GameTarget {

    private final TargetType type;
    private final CircularUnit target;
    private final Lane lane;
    private final SkillType skill;

    public GameTarget(Building building) {
        type = TargetType.BUILDING;
        target = building;
        lane = null;
        skill = null;
    }

    public GameTarget(Wizard wizard) {
        type = TargetType.WIZARD;
        target = wizard;
        lane = null;
        skill = null;
    }

    public GameTarget(Minion minion) {
        type = TargetType.MINION;
        target = minion;
        lane = null;
        skill = null;
    }

    public GameTarget(Tree tree) {
        type = TargetType.TREE;
        target = tree;
        lane = null;
        skill = null;
    }

    public GameTarget(Bonus bonus) {
        type = TargetType.BONUS;
        target = bonus;
        lane = null;
        skill = null;
    }

    public GameTarget(SkillType skill) {
        type = TargetType.SKILL;
        lane = null;
        target = null;
        this.skill = skill;
    }

    public GameTarget(Lane lane, Zone zone, double size, Action action) {
        type = TargetType.LANE;
        skill = null;
        this.lane = lane;
        Waypoint w1;
        Waypoint w2 = new Waypoint(size - size / 10, size / 10);
        Waypoint w3 = new Waypoint(size / 100, size - size / 100);
        if (action == Action.ADVANCE) {
            switch (lane.getType()) {
                case TOP:
                    w1 = new Waypoint(size / 20, size / 20);
                    switch (zone) {
                        case OUR_BASE:
                        case TOP_BEFORE_TURN:
                        case FOREST_OUR_TOP:
                        case FOREST_OUR_BOTTOM:
                        case TOP_BONUS:
                        case CENTER:
                        case BOTTOM_BONUS:
                        case BOTTOM_BEFORE_TURN:
                        case MIDDLE_BEFORE_CENTER:
                            target = w1;
                            break;
                        case TOP_TURN:
                        case TOP_AFTER_TURN:
                        case ENEMY_BASE:
                        case BOTTOM_AFTER_TURN:
                        case FOREST_ENEMY_TOP:
                        case FOREST_ENEMY_BOTTOM:
                        case BOTTOM_TURN:
                        case MIDDLE_AFTER_CENTER:
                            target = w2;
                            break;
                        default:
                            target = w2;
                    }
                    break;
                case BOTTOM:
                    w1 = new Waypoint(size - size / 20, size - size / 20);
                    switch (zone) {
                        case OUR_BASE:
                        case TOP_BEFORE_TURN:
                        case FOREST_OUR_TOP:
                        case FOREST_OUR_BOTTOM:
                        case TOP_BONUS:
                        case CENTER:
                        case BOTTOM_BONUS:
                        case BOTTOM_BEFORE_TURN:
                        case MIDDLE_BEFORE_CENTER:
                            target = w1;
                            break;
                        case TOP_TURN:
                        case TOP_AFTER_TURN:
                        case ENEMY_BASE:
                        case BOTTOM_AFTER_TURN:
                        case FOREST_ENEMY_TOP:
                        case FOREST_ENEMY_BOTTOM:
                        case BOTTOM_TURN:
                        case MIDDLE_AFTER_CENTER:
                            target = w2;
                            break;
                        default:
                            target = w2;
                    }
                    break;
                case MIDDLE:
                    w1 = new Waypoint(size / 2, size / 2);
                    switch (zone) {
                        case OUR_BASE:
                        case TOP_BEFORE_TURN:
                        case FOREST_OUR_TOP:
                        case FOREST_OUR_BOTTOM:
                        case TOP_BONUS:
                        case BOTTOM_BONUS:
                        case BOTTOM_BEFORE_TURN:
                        case MIDDLE_BEFORE_CENTER:
                        case TOP_TURN:
                        case BOTTOM_TURN:
                            target = w1;
                            break;

                        case CENTER:
                        case TOP_AFTER_TURN:
                        case ENEMY_BASE:
                        case BOTTOM_AFTER_TURN:
                        case FOREST_ENEMY_TOP:
                        case FOREST_ENEMY_BOTTOM:
                        case MIDDLE_AFTER_CENTER:
                            target = w2;
                            break;
                        default:
                            target = w2;
                    }
                    break;
                default:
                    target = w2;
            }
        } else if (action == Action.RETREAT) {
            switch (lane.getType()) {
                case TOP:
                    w1 = new Waypoint(size / 20, size / 20);
                    switch (zone) {
                        case OUR_BASE:
                        case TOP_BEFORE_TURN:
                        case FOREST_OUR_TOP:
                        case FOREST_OUR_BOTTOM:
                        case TOP_BONUS:
                        case CENTER:
                        case BOTTOM_BONUS:
                        case BOTTOM_BEFORE_TURN:
                        case MIDDLE_BEFORE_CENTER:
                        case TOP_TURN:
                            target = w3;
                            break;
                        case TOP_AFTER_TURN:
                        case ENEMY_BASE:
                        case BOTTOM_AFTER_TURN:
                        case FOREST_ENEMY_TOP:
                        case FOREST_ENEMY_BOTTOM:
                        case BOTTOM_TURN:
                        case MIDDLE_AFTER_CENTER:
                            target = w1;
                            break;
                        default:
                            target = w3;
                    }
                    break;
                case BOTTOM:
                    w1 = new Waypoint(size - size / 20, size - size / 20);
                    switch (zone) {
                        case OUR_BASE:
                        case TOP_BEFORE_TURN:
                        case FOREST_OUR_TOP:
                        case FOREST_OUR_BOTTOM:
                        case TOP_BONUS:
                        case CENTER:
                        case BOTTOM_BONUS:
                        case BOTTOM_BEFORE_TURN:
                        case MIDDLE_BEFORE_CENTER:
                        case BOTTOM_TURN:
                            target = w3;
                            break;
                        case TOP_TURN:
                        case TOP_AFTER_TURN:
                        case ENEMY_BASE:
                        case BOTTOM_AFTER_TURN:
                        case FOREST_ENEMY_TOP:
                        case FOREST_ENEMY_BOTTOM:
                        case MIDDLE_AFTER_CENTER:
                            target = w1;
                            break;
                        default:
                            target = w3;
                    }
                    break;
                case MIDDLE:
                    w1 = new Waypoint(size / 2, size / 2);
                    switch (zone) {
                        case OUR_BASE:
                        case TOP_BEFORE_TURN:
                        case FOREST_OUR_TOP:
                        case FOREST_OUR_BOTTOM:
                        case TOP_BONUS:
                        case BOTTOM_BONUS:
                        case BOTTOM_BEFORE_TURN:
                        case MIDDLE_BEFORE_CENTER:
                        case TOP_TURN:
                        case BOTTOM_TURN:
                        case CENTER:
                            target = w3;
                            break;
                        case TOP_AFTER_TURN:
                        case ENEMY_BASE:
                        case BOTTOM_AFTER_TURN:
                        case FOREST_ENEMY_TOP:
                        case FOREST_ENEMY_BOTTOM:
                        case MIDDLE_AFTER_CENTER:
                            target = w1;
                            break;
                        default:
                            target = w3;
                    }
                    break;
                default:
                    target = w3;
            }
        } else {
            target = w3;
        }
    }


    public TargetType getTargetType() {
        return type;
    }

    public CircularUnit getTarget() {
        return target;
    }

    public Lane getLane() {
        return lane;
    }

    public SkillType getSkill() {
        return this.skill;
    }
}
