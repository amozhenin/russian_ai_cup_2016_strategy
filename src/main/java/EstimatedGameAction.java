/**
 * Created by dvorkin on 23.11.2016.
 */
public class EstimatedGameAction extends GameAction {

    private final double estimation;
    public EstimatedGameAction(Action action, GameTarget target, double estimation) {
        super(action, target);
        this.estimation = estimation;
    }

    public double getEstimation() {
        return estimation;
    }
}
