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
        List<GameAction> gameActions = generateActions(self, world, game);
        List<EstimatedGameAction> estimatedActions = estimate(gameActions);
        List<EstimatedGameAction> bestActions = selectActionsToDo(estimatedActions);
        applyActions(bestActions, move);
    }

    private List<GameAction> generateActions(Wizard self, World world, Game game) {
        List<GameAction> actions = new ArrayList<>();
        actions.add(new GameAction(Action.ADVANCE, new GameTarget(new Lane(LaneType.TOP))));
        actions.add(new GameAction(Action.ADVANCE, new GameTarget(new Lane(LaneType.MIDDLE))));
        actions.add(new GameAction(Action.ADVANCE, new GameTarget(new Lane(LaneType.BOTTOM))));
        //world.getBuildings()
        for (Building building : world.getBuildings()) {
            if (isFoe(self.getFaction(), building)) {
                actions.add(new GameAction(Action.ATTACK, new GameTarget(building)));
            }
        }
        for (Wizard wizard : world.getWizards()) {
            if (wizard.isMe()) {
                continue;
            }
            if (isFoe(self.getFaction(), wizard)) {
                actions.add(new GameAction(Action.ATTACK, new GameTarget(wizard)));
            }
        }
        for (Minion minion: world.getMinions()) {
            if (isFoe(self.getFaction(), minion)) {
                actions.add(new GameAction(Action.ATTACK, new GameTarget(minion)));
            }
        }
        return actions;
    }

    private boolean isFoe(Faction faction, CircularUnit unit) {
        if (unit.getFaction() == faction) {
            return false;
        }
        if (unit.getFaction() == Faction.OTHER) {
            return false;
        }
        return true;
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

