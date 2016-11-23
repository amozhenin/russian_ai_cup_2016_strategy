import model.*;

import java.util.ArrayList;
import java.util.List;

public final class MyStrategy implements Strategy {

    private DataStorage storage;

    public MyStrategy() {
        storage = new DataStorage();
    }

    public DataStorage getDataStorage() {
        return storage;
    }

    @Override
    public void move(Wizard self, World world, Game game, Move move) {
//        move.setSpeed(game.getWizardForwardSpeed());
////        move.setStrafeSpeed(game.getWizardStrafeSpeed());
////        move.setTurn(game.getWizardMaxTurnAngle());
//        move.setAction(ActionType.MAGIC_MISSILE);

        List<GameAction> gameActions = generateActions(self, world, game);
        List<EstimatedGameAction> estimatedActions = estimate(gameActions);
        List<EstimatedGameAction> bestActions = selectActionsToDo(estimatedActions);
        applyActions(bestActions, move);
    }

    private List<GameAction> generateActions(Wizard self, World world, Game game) {
        //TODO implement
        List<GameAction> actions = new ArrayList<>();
        actions.add(new GameAction(Action.ADVANCE, null));
        //world.getBuildings()
        actions.add(new GameAction(Action.ATTACK, null));
        return actions;
    }

    private List<EstimatedGameAction> estimate(List<GameAction> actions) {
        List<EstimatedGameAction> ret = new ArrayList<>(actions.size());
        for (GameAction action: actions) {
            ret.add(estimateAction(action));
        }
        return ret;
    }

    private EstimatedGameAction estimateAction(GameAction action) {
        //TODO write this very carefully
        return new EstimatedGameAction(action.getAction(), action.getGameTarget(), 0.00);
    }

    private List<EstimatedGameAction> selectActionsToDo(List<EstimatedGameAction> actions) {
        List<EstimatedGameAction> bestActions = new ArrayList<>();

        actions.stream().filter(a -> a.getAction() == Action.ATTACK).reduce((a, b) ->
        a.getEstimation() > b.getEstimation() ? a : b).ifPresent(e -> bestActions.add(e));

        actions.stream().filter(a -> a.getAction() == Action.ADVANCE
                             || a.getAction() == Action.HOLD
                             || a.getAction() == Action.RETREAT
                             || a.getAction() == Action.TAKE_BONUS).reduce((a, b) ->
                a.getEstimation() > b.getEstimation() ? a : b).ifPresent(e -> bestActions.add(e));
        return bestActions;
    }

    private void applyActions(List<EstimatedGameAction> actions, Move move) {
        //TODO implement
    }
}

