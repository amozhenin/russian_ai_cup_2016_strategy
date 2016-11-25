import model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class MyStrategy implements IExtendedStrategy {

    private DataStorage storage;

    public MyStrategy() {
        storage = new DataStorage();
    }

    @Override
    public void finish() {

    }

    @Override
    public DataStorage getDataStorage() {
        return storage;
    }

    @Override
    public void setDataStorage(DataStorage storage) {
        this.storage = storage;
    }

    @Override
    public void move(Wizard self, World world, Game game, Move move) {
        precalc(self, world, game);
        List<GameAction> gameActions = generateActions(self, world, game);
        storage.setGeneratedActions(gameActions);
        List<EstimatedGameAction> estimatedActions = estimate(gameActions, self, world, game);
        storage.setEstimatedActions(estimatedActions);
        List<EstimatedGameAction> bestActions = selectActionsToDo(estimatedActions);
        storage.setBestActions(bestActions);
        applyActions(bestActions, self, world, game, move);
    }

    private void precalc(Wizard self, World world, Game game) {
        ZoneMapper mapper = new ZoneMapper(game.getMapSize());
        List<CircularUnit> obstacles = new ArrayList<>();
        for (Building building: world.getBuildings()) {
            obstacles.add(building);
        }
        for (Wizard wizard: world.getWizards()) {
            if (!wizard.isMe()) {
                obstacles.add(wizard);
            }
        }
        for (Minion minion: world.getMinions()) {
            obstacles.add(minion);
        }
        for (Tree tree: world.getTrees()) {
            obstacles.add(tree);
        }
        storage.setObstacles(obstacles);
        storage.setZoneMapper(mapper);
        Random random = new Random(game.getRandomSeed());
        storage.setRandom(random);
    }

    private List<GameAction> generateActions(Wizard self, World world, Game game) {

        List<GameAction> actions = new ArrayList<>();
        double size = game.getMapSize();
        Zone zone = storage.getZoneMapper().getZoneOfUnit(self);
        if (storage.getLane() != null) {
            actions.add(new GameAction(Action.ADVANCE, new GameTarget(storage.getLane(), zone, size)));
            actions.add(new GameAction(Action.HOLD, new GameTarget(storage.getLane(), zone, size)));
            actions.add(new GameAction(Action.RETREAT, new GameTarget(storage.getLane(), zone, size)));
        } else {
            actions.add(new GameAction(Action.ADVANCE, new GameTarget(new Lane(LaneType.TOP), zone, size)));
            actions.add(new GameAction(Action.ADVANCE, new GameTarget(new Lane(LaneType.BOTTOM), zone, size)));
            actions.add(new GameAction(Action.ADVANCE, new GameTarget(new Lane(LaneType.MIDDLE), zone, size)));
        }

        List<LivingUnit> foes = new ArrayList<>();
        for (Building building : world.getBuildings()) {
            if (isFoe(self.getFaction(), building)) {
                actions.add(new GameAction(Action.ATTACK, new GameTarget(building)));
                foes.add(building);
            }
        }
        for (Wizard wizard : world.getWizards()) {
            if (wizard.isMe()) {
                continue;
            }
            if (isFoe(self.getFaction(), wizard)) {
                actions.add(new GameAction(Action.ATTACK, new GameTarget(wizard)));
                foes.add(wizard);
            }
        }
        for (Minion minion: world.getMinions()) {
            if (isFoe(self.getFaction(), minion)) {
                actions.add(new GameAction(Action.ATTACK, new GameTarget(minion)));
                foes.add(minion);
            }
        }
        storage.setFoes(foes);
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

    private List<EstimatedGameAction> estimate(List<GameAction> actions, Wizard self, World world, Game game) {
        List<EstimatedGameAction> ret = new ArrayList<>(actions.size());
        for (GameAction action: actions) {
            ret.add(estimateAction(action, self, world, game));
        }
        return ret;
    }

    private EstimatedGameAction estimateAction(GameAction action, Wizard self, World world, Game game) {
        //TODO write this very carefully
        double estimation = 0.0;
        switch (action.getAction()) {
            case ADVANCE:
                switch (action.getGameTarget().getTargetType()) {
                    case LANE:
                        if (storage.getLane() == null) {
                            if (storage.getZoneMapper().getZoneOfUnit(self) == Zone.OUR_BASE) {
                                LaneType type = action.getGameTarget().getLane().getType();
                                switch (type) {
                                    case MIDDLE:
                                        if (self.getX() + self.getY() == game.getMapSize()) {
                                            estimation = 10.0;
                                        } else {
                                            estimation = -50.0;
                                        }
                                        break;
                                    case TOP:
                                        if (self.getX() + self.getY() < game.getMapSize()) {
                                            estimation = 10.0;
                                        } else {
                                            estimation = -50.0;
                                        }
                                        break;
                                    case BOTTOM:
                                        if (self.getX() + self.getY() > game.getMapSize()) {
                                            estimation = 10.0;
                                        } else {
                                            estimation = -50.0;
                                        }
                                        break;
                                }
                            }
                        } else {
                            if (storage.getLane().getType() != action.getGameTarget().getLane().getType()) {
                                estimation = -50.0;
                            } else {
                                estimation = 1.0;
                                if (self.getLife() < self.getMaxLife() * 0.9)
                                    estimation = 0.1;
                                if (self.getLife() < self.getMaxLife() * 0.5)
                                    estimation = -1.0;
                            }
                        }
                        break;
                    case BUILDING:
                        break;
                    case BONUS:
                        break;
                    case MINION:
                        break;
                    case WIZARD:
                        break;
                    case TREE:
                        break;
                    default:

                }
                break;
            case HOLD:
                if (storage.getLane().getType() != action.getGameTarget().getLane().getType()) {
                    estimation = -50.0;
                } else {
                    estimation = 0.5;
                    if (self.getLife() < self.getMaxLife() && self.getLife() > self.getMaxLife() * 0.8) {
                        estimation = 1.0;
                    }
                    if (self.getLife() < self.getMaxLife() * 0.6) {
                        estimation = 0.1;
                    }
                }
                break;
            case RETREAT:
                if (storage.getLane().getType() != action.getGameTarget().getLane().getType()) {
                    estimation = -50.0;
                } else {
                    estimation = 0.0;
                    if (self.getLife() < self.getMaxLife() * 0.9)
                        estimation = 0.1;
                    if (self.getLife() < self.getMaxLife() * 0.7)
                        estimation = 1.0;
                    if (self.getLife() < self.getMaxLife() * 0.5)
                        estimation = 10.0;
                }
                break;
            case CAST_SPELL:
                //not implemented
                break;
            case TAKE_BONUS:
                //not implemented
                break;
            case ATTACK:
                if (self.getDistanceTo(action.getGameTarget().getTarget()) > game.getWizardCastRange()) {
                    estimation = 0.0;
                } else {
                    LivingUnit unit = (LivingUnit) action.getGameTarget().getTarget();
                    switch (action.getGameTarget().getTargetType()) {
                        case WIZARD:
                            if (unit.getLife() < game.getMagicMissileDirectDamage()) {
                                estimation = game.getWizardEliminationScoreFactor() * unit.getMaxLife();
                            } else {
                                double probability = Math.max(0.01, 100 + game.getMagicMissileDirectDamage() - unit.getLife());
                                estimation = game.getWizardDamageScoreFactor() * game.getMagicMissileDirectDamage()
                                    + probability * game.getWizardEliminationScoreFactor() * unit.getMaxLife();
                            }
                            break;
                        case MINION:
                            if (unit.getLife() < game.getMagicMissileDirectDamage()) {
                                estimation = game.getMinionEliminationScoreFactor() * unit.getMaxLife();
                            } else {
                                double probability = Math.max(0.01, 100 + game.getMagicMissileDirectDamage() - unit.getLife());
                                estimation = game.getMinionDamageScoreFactor() * game.getMagicMissileDirectDamage()
                                        + probability * game.getMinionEliminationScoreFactor() * unit.getMaxLife();
                            }
                            break;
                        case BUILDING:
                            Building building = (Building) unit;
                            if (unit.getLife() < game.getMagicMissileDirectDamage()) {
                                estimation = game.getBuildingEliminationScoreFactor() * unit.getMaxLife();
                                if (building.getType() == BuildingType.FACTION_BASE) {
                                    estimation += 1000.0;
                                }
                            } else {
                                double probability = Math.max(0.01, 100 + game.getMagicMissileDirectDamage() - unit.getLife());
                                estimation = game.getBuildingDamageScoreFactor() * game.getMagicMissileDirectDamage()
                                        + probability * game.getBuildingEliminationScoreFactor() * unit.getMaxLife();
                                if (building.getType() == BuildingType.FACTION_BASE) {
                                    estimation += 1000.0 * probability;
                                }
                            }
                            break;
                        case TREE:
                            estimation = 0.1;
                            break;
                        default:
                            estimation = 0.0;
                    }
                }
                break;
            default:

        }
        return new EstimatedGameAction(action.getAction(), action.getGameTarget(), estimation);
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

    private void applyActions(List<EstimatedGameAction> actions, Wizard self, World world, Game game, Move move) {
        for (EstimatedGameAction action : actions) {
            if (action.getEstimation() < 0.0) {
                //skip action, it has negative effect
                continue;
            } else {
                tryApplyAction(action, self, world, game, move);
            }
        }
    }

    private void tryApplyAction(EstimatedGameAction action, Wizard self, World world, Game game, Move move) {
        switch (action.getAction()) {
            case HOLD:
            case RETREAT:
            case ADVANCE:
                //TODO learn to move
                switch(action.getGameTarget().getTargetType()) {
                    case LANE:
                            storage.setLane(action.getGameTarget().getLane());
                            Unit target = action.getGameTarget().getTarget();
                            double angle = self.getAngleTo(target);

                            List<CircularUnit> realObstacles = new ArrayList<>();
                            for (CircularUnit obstacle : storage.getObstacles()) {
                                if (isObstacle(self, angle, obstacle)) {
                                    realObstacles.add(obstacle);
                                }
                            }
                            CircularUnit nearest = null;
                            double distance = 8000.0;
                            for (CircularUnit realObstacle : realObstacles) {
                                double obsDist = self.getDistanceTo(realObstacle);
                                if (obsDist < distance) {
                                    distance = obsDist;
                                    nearest = realObstacle;
                                }
                            }
                            if (nearest != null) {
                                double obstacleAngle = self.getAngleTo(nearest);
                                double sign = Math.signum(obstacleAngle - angle);
                                if (sign == 0.0) {
                                    double centerAngle = self.getAngleTo(game.getMapSize() / 2, game.getMapSize() / 2);
                                    sign = Math.signum(obstacleAngle - centerAngle);
                                    if (sign == 0.0) {
                                        sign = 1.0;
                                    }
                                }
                                double correctedAngle = angle;
                                for(int i = 1; i <= 12; i++) {
                                    correctedAngle -= (StrictMath.PI * i * sign) / 18;
                                    sign = -sign;
                                    if (!isObstacle(self, correctedAngle, nearest)) {
                                        break;
                                    }
                                }
                                angle = correctedAngle;
                            }
                            move.setTurn(angle);
                            if (action.getAction() == Action.ADVANCE)
                                move.setSpeed(game.getWizardForwardSpeed());
                            if (action.getAction() == Action.RETREAT)
                                move.setSpeed(-game.getWizardBackwardSpeed());
                            if (action.getAction() == Action.HOLD) {
                                move.setSpeed(0);
                                move.setStrafeSpeed(game.getWizardStrafeSpeed() * (storage.getRandom().nextInt() % 2 == 0 ? 1 : -1));
                                move.setTurn(StrictMath.PI / 12 * (storage.getRandom().nextInt() % 2 == 0 ? 1 : -1));
                            }

                        break;
                    case WIZARD:
                        break;
                    case BONUS:
                        break;
                    case MINION:
                        break;
                    case BUILDING:
                        break;
                    case TREE:
                        break;
                    default:
                }
                break;

            case ATTACK:
                Unit target = action.getGameTarget().getTarget();

                double castAngle = self.getAngleTo(target);
                double dist = self.getDistanceTo(target);
                if ((castAngle >= - StrictMath.PI / 12 && castAngle <= StrictMath.PI / 12)
                        && (self.getRemainingActionCooldownTicks() == 0)) {
                    if ((self.getRemainingCooldownTicksByAction()[ActionType.MAGIC_MISSILE.ordinal()] == 0)
                            && dist < game.getWizardCastRange()) {
                        move.setAction(ActionType.MAGIC_MISSILE);
                        move.setCastAngle(castAngle);
                        move.setMinCastDistance(dist);
                        move.setMaxCastDistance(dist + 25);
                    } else if ((self.getRemainingCooldownTicksByAction()[ActionType.STAFF.ordinal()] == 0)
                        && dist < game.getStaffRange()) {
                        move.setAction(ActionType.STAFF);
                    }
                } else {
                    move.setAction(ActionType.NONE);
                }
                break;
            case CAST_SPELL:
                //TODO implement in Round 2
                break;
            case TAKE_BONUS:
                //TODO learn to take bonuses
                break;
            default:
        }
    }

    public boolean isObstacle(Wizard self, double angle, CircularUnit obstacle) {
        double obstacleAngle = self.getAngleTo(obstacle);
        double obstacleDist = self.getDistanceTo(obstacle);
        return (Math.abs(Math.sin(obstacleAngle - angle)) * self.getDistanceTo(obstacle) < self.getRadius() + obstacle.getRadius() + 3)
                && (obstacleAngle - angle < StrictMath.PI / 2 && obstacleAngle - angle > - StrictMath.PI / 2)
                && (obstacleDist < self.getRadius() * 10);
    }
}

