

/**
 * Created by dvorkin on 23.11.2016.
 */
public class GameAction {

    private final Action action;
    private final GameTarget target;
    public GameAction(Action action, GameTarget target) {
        this.action = action;
        this.target = target;
    }

    public Action getAction() {
        return action;
    }

    public GameTarget getGameTarget() {
        return target;
    }
}
