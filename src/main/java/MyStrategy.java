import model.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public final class MyStrategy implements IExtendedStrategy {

    private DataStorage storage;

    public MyStrategy() {
        storage = new DataStorage();
        init();
    }

    private void init() {
        //init skills
        List<SkillType> skills = storage.getDesiredSkills();
        skills.add(SkillType.RANGE_BONUS_PASSIVE_1);
        skills.add(SkillType.RANGE_BONUS_AURA_1);
        skills.add(SkillType.RANGE_BONUS_PASSIVE_2);
        skills.add(SkillType.RANGE_BONUS_AURA_2);
        skills.add(SkillType.ADVANCED_MAGIC_MISSILE);
        skills.add(SkillType.MAGICAL_DAMAGE_BONUS_PASSIVE_1);
        skills.add(SkillType.MAGICAL_DAMAGE_BONUS_AURA_1);
        skills.add(SkillType.MAGICAL_DAMAGE_BONUS_PASSIVE_2);
        skills.add(SkillType.MAGICAL_DAMAGE_BONUS_AURA_2);
        skills.add(SkillType.FROST_BOLT);
        skills.add(SkillType.MAGICAL_DAMAGE_ABSORPTION_PASSIVE_1);
        skills.add(SkillType.MAGICAL_DAMAGE_ABSORPTION_AURA_1);
        skills.add(SkillType.MAGICAL_DAMAGE_ABSORPTION_PASSIVE_2);
        skills.add(SkillType.MAGICAL_DAMAGE_ABSORPTION_AURA_2);
        skills.add(SkillType.SHIELD);
        skills.add(SkillType.MOVEMENT_BONUS_FACTOR_PASSIVE_1);
        skills.add(SkillType.MOVEMENT_BONUS_FACTOR_AURA_1);
        skills.add(SkillType.MOVEMENT_BONUS_FACTOR_PASSIVE_2);
        skills.add(SkillType.MOVEMENT_BONUS_FACTOR_AURA_2);
        skills.add(SkillType.HASTE);
        skills.add(SkillType.STAFF_DAMAGE_BONUS_PASSIVE_1);
        skills.add(SkillType.STAFF_DAMAGE_BONUS_AURA_1);
        skills.add(SkillType.STAFF_DAMAGE_BONUS_PASSIVE_2);
        skills.add(SkillType.STAFF_DAMAGE_BONUS_AURA_2);
        skills.add(SkillType.FIREBALL);
        storage.setDesiredSkills(skills);
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
        List<SkillType> skills = storage.getDesiredSkills();
        SkillType[] learnedSkills = self.getSkills();
        Iterator<SkillType> skillIterator = skills.iterator();
        while(skillIterator.hasNext()) {
            SkillType skillToCheck = skillIterator.next();
            boolean found = false;
            for (SkillType skill : learnedSkills) {
                if (skill == skillToCheck) {
                    found = true;
                    break;
                }
            }
            if (found) {
                skillIterator.remove();
            }
        }
        storage.saveCoordinates(new Waypoint(self.getX(), self.getY()));
    }

    private List<GameAction> generateActions(Wizard self, World world, Game game) {

        List<GameAction> actions = new ArrayList<>();
        double size = game.getMapSize();
        Zone zone = storage.getZoneMapper().getZoneOfUnit(self);
        if (storage.getLane() != null) {
            actions.add(new GameAction(Action.ADVANCE, new GameTarget(storage.getLane(), zone, size, Action.ADVANCE)));
            actions.add(new GameAction(Action.HOLD, new GameTarget(storage.getLane(), zone, size, Action.HOLD)));
            actions.add(new GameAction(Action.RETREAT, new GameTarget(storage.getLane(), zone, size, Action.RETREAT)));
        } else {
            actions.add(new GameAction(Action.ADVANCE, new GameTarget(new Lane(LaneType.TOP), zone, size, Action.ADVANCE)));
            actions.add(new GameAction(Action.ADVANCE, new GameTarget(new Lane(LaneType.BOTTOM), zone, size, Action.ADVANCE)));
            actions.add(new GameAction(Action.ADVANCE, new GameTarget(new Lane(LaneType.MIDDLE), zone, size, Action.ADVANCE)));
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
        for (Tree tree: world.getTrees()) {
            actions.add(new GameAction(Action.ATTACK, new GameTarget(tree)));
          //  foes.add(tree);
        }
        storage.setFoes(foes);
        if (foes.isEmpty())
            storage.setTarget(null);
        if (game.isSkillsEnabled()) {
            for (SkillType skill: storage.getDesiredSkills()) {
                actions.add(new GameAction(Action.LEARN_SKILL, new GameTarget(skill)));
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

    private List<EstimatedGameAction> estimate(List<GameAction> actions, Wizard self, World world, Game game) {
        List<EstimatedGameAction> ret = new ArrayList<>(actions.size());
        for (GameAction action: actions) {
            ret.add(estimateAction(action, self, world, game));
        }
        return ret;
    }

    private EstimatedGameAction estimateAction(GameAction action, Wizard self, World world, Game game) {
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
                                if (self.getLife() < self.getMaxLife() * 0.7)
                                    estimation = 0.1;
                                if (self.getLife() < self.getMaxLife() * 0.4)
                                    estimation = -1.0;
                                double dist = getDistanceToClosestFoe(self);
                                if (dist > game.getFactionBaseAttackRange()) {
                                    estimation += 1.5;
                                } else if (dist > game.getScoreGainRange()) {
                                    estimation += 1.1;
                                } else if (dist > self.getCastRange()) {
                                    estimation += 1.0;
                                } else if (dist > game.getFetishBlowdartAttackRange()) {
                                    estimation += 0.5;
                                } else if (dist > 70) {
                                    estimation -= 1.0;
                                } else {
                                    estimation -= 5.0;
                                }
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
                    if (self.getLife() < self.getMaxLife() && self.getLife() > self.getMaxLife() * 0.6) {
                        estimation = 1.0;
                    }
                    if (self.getLife() < self.getMaxLife() * 0.4) {
                        estimation = 0.1;
                    }
                    double dist = getDistanceToClosestFoe(self);
                    if (dist > game.getFactionBaseAttackRange()) {
                        estimation -= 5.0;
                    } else if (dist > game.getScoreGainRange()) {
                        estimation -= 2.0;
                    } else if (dist > self.getCastRange()) {
                        estimation -= 1.0;
                    } else if (dist > game.getFetishBlowdartAttackRange()) {
                        estimation += 0.5;
                    } else if (dist > 70) {
                        estimation -= 1.0;
                    } else {
                        estimation -= 5.0;
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
                    if (self.getLife() < self.getMaxLife() * 0.4)
                        estimation = 1.0;
                    if (self.getLife() < self.getMaxLife() * 0.2)
                        estimation = 10.0;
                    double dist = getDistanceToClosestFoe(self);
                    if (dist > game.getFactionBaseAttackRange()) {
                        estimation -= 20.0;
                    } else if (dist > game.getScoreGainRange()) {
                        estimation -= 10.0;
                    } else if (dist > self.getCastRange()) {
                        estimation += 0.5;
                    } else if (dist > game.getFetishBlowdartAttackRange()) {
                        estimation += 1.0;
                    } else if (dist > 70) {
                        estimation += 10.0;
                    } else {
                        estimation += 25.0;
                    }
                }
                break;
            case CAST_SPELL:
                //not implemented
                break;
            case TAKE_BONUS:
                //not implemented
                break;
            case LEARN_SKILL:
                SkillType skillCandidate = action.getGameTarget().getSkill();
                boolean found = false;
                for (SkillType skill : self.getSkills()) {
                    if (skill == skillCandidate) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    estimation = -5.0;
                } else {
                    if (skillCandidate != storage.getDesiredSkills().get(0)) {
                        estimation = -3.0;
                    } else {
                        if (self.getLevel() > self.getSkills().length) {
                            estimation = 1.0;
                        } else {
                            estimation = -1.0;
                        }
                    }
                }
                break;
            case ATTACK:
                if (self.getDistanceTo(action.getGameTarget().getTarget()) > self.getCastRange()) {
                    estimation = 0.0;
                } else {
                    LivingUnit unit = (LivingUnit) action.getGameTarget().getTarget();
                    switch (action.getGameTarget().getTargetType()) {
                        case WIZARD:
                            if (unit.getLife() < game.getMagicMissileDirectDamage()) {
                                estimation = game.getWizardEliminationScoreFactor() * unit.getMaxLife();
                            } else {
                                double probability = Math.max(0.01, (unit.getMaxLife() + game.getMagicMissileDirectDamage() - unit.getLife()) / unit.getMaxLife());
                                estimation = game.getWizardDamageScoreFactor() * game.getMagicMissileDirectDamage()
                                    + probability * game.getWizardEliminationScoreFactor() * unit.getMaxLife();
                            }
                            break;
                        case MINION:
                            if (unit.getLife() < game.getMagicMissileDirectDamage()) {
                                estimation = game.getMinionEliminationScoreFactor() * unit.getMaxLife();
                            } else {
                                double probability = Math.max(0.01, (unit.getMaxLife() + game.getMagicMissileDirectDamage() - unit.getLife()) / unit.getMaxLife());
                                estimation = game.getMinionDamageScoreFactor() * game.getMagicMissileDirectDamage()
                                        + probability * game.getMinionEliminationScoreFactor() * unit.getMaxLife();
                                if (unit.getFaction() == Faction.NEUTRAL) {
                                    estimation = estimation / 2;
                                }
                            }
                            break;
                        case BUILDING:
                            Building building = (Building) unit;
                            if (unit.getLife() < game.getMagicMissileDirectDamage()) {
                                estimation = game.getBuildingEliminationScoreFactor() * unit.getMaxLife();
                                if (building.getType() == BuildingType.FACTION_BASE) {
                                    estimation += game.getVictoryScore();
                                }
                            } else {
                                double probability = Math.max(0.01, (unit.getMaxLife() + game.getMagicMissileDirectDamage() - unit.getLife()) / unit.getMaxLife());
                                estimation = game.getBuildingDamageScoreFactor() * game.getMagicMissileDirectDamage()
                                        + probability * game.getBuildingEliminationScoreFactor() * unit.getMaxLife();
                                if (building.getType() == BuildingType.FACTION_BASE) {
                                    estimation += game.getVictoryScore() * probability;
                                }
                            }
                            break;
                        case TREE:
                            estimation = 1.0 / self.getDistanceTo(action.getGameTarget().getTarget()) ;
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

    private double getDistanceToClosestFoe(Wizard self) {
        double distance = 10000.0;
        List<LivingUnit> foes = storage.getFoes();
        for (LivingUnit foe : foes) {
            if (foe.getFaction() == Faction.NEUTRAL) {
                continue;
            }
           double foeDist = self.getDistanceTo(foe);
           if (distance > foeDist) {
               distance = foeDist;
           }
        }
        return distance;
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
        actions.stream().filter(a -> a.getAction() == Action.LEARN_SKILL).reduce((a, b) ->
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
        applyMove(self, world, game, move);
    }

    private void applyMove(Wizard self, World world, Game game, Move move) {
        double angle;
        if (storage.getTarget() != null) {
            angle = storage.getTargetAngle();
        } else {
            angle = storage.getDestinationAngle();
        }
        move.setTurn(angle);
        if (angle < -StrictMath.PI / 2 || angle > StrictMath.PI / 2) {
            move.setSpeed(game.getWizardBackwardSpeed() * Math.cos(storage.getDestinationAngle()));
            move.setStrafeSpeed(game.getWizardStrafeSpeed() * Math.sin(storage.getDestinationAngle()));
        } else {
            move.setSpeed(game.getWizardForwardSpeed() * Math.cos(storage.getDestinationAngle()));
            move.setStrafeSpeed(game.getWizardForwardSpeed() * Math.sin(storage.getDestinationAngle()));
        }


        //if (action.getAction() == Action.ADVANCE)
//        move.setSpeed(game.getWizardForwardSpeed());
//        boolean stuck = true;
//        Waypoint current = new Waypoint(self.getX(), self.getY());
//        for(Waypoint wp : storage.getCoordinates()) {
//            if (wp.getX() != current.getX()) {
//                stuck = false;
//                break;
//            }
//            if (wp.getY() != current.getY()) {
//                stuck = false;
//                break;
//            }
//        }
//        if (stuck) {
//            move.setSpeed(-game.getWizardBackwardSpeed());
//        }
        // if (action.getAction() == Action.RETREAT)
        //     move.setSpeed(-game.getWizardBackwardSpeed());
//        if (storage.getAction() == Action.HOLD) {
//            move.setSpeed(0);
//            move.setStrafeSpeed(game.getWizardStrafeSpeed() * (storage.getRandom().nextInt() % 2 == 0 ? 1 : -1));
//            move.setTurn(StrictMath.PI / 12 * (storage.getRandom().nextInt() % 2 == 0 ? 1 : -1));
//        }
        if (storage.getAction() == Action.HOLD) {
            move.setSpeed(0);
            move.setStrafeSpeed(0);
        }

        if (storage.getTarget() != null) {
            if ((storage.getTargetAngle() >= -StrictMath.PI / 12 && storage.getTargetAngle() <= StrictMath.PI / 12)
                    && (self.getRemainingActionCooldownTicks() == 0)) {
                if ((self.getRemainingCooldownTicksByAction()[ActionType.MAGIC_MISSILE.ordinal()] == 0)
                        && storage.getTargetDistance() < self.getCastRange()) {
                    move.setAction(ActionType.MAGIC_MISSILE);
                    move.setCastAngle(storage.getTargetAngle());
                    move.setMinCastDistance(storage.getTargetDistance() - storage.getTarget().getRadius() + game.getMagicMissileRadius());
                    move.setMaxCastDistance(storage.getTargetDistance() + storage.getTarget().getRadius());
                }
            } else if ((self.getRemainingCooldownTicksByAction()[ActionType.STAFF.ordinal()] == 0)
                    && storage.getTargetDistance() < game.getStaffRange()) {
                move.setAction(ActionType.STAFF);
            } else {
                move.setAction(ActionType.NONE);
            }
        }
    }

    private void tryApplyAction(EstimatedGameAction action, Wizard self, World world, Game game, Move move) {
        switch (action.getAction()) {
            case HOLD:
            case RETREAT:
            case ADVANCE:
                switch(action.getGameTarget().getTargetType()) {
                    case LANE:
                            storage.setLane(action.getGameTarget().getLane());
                            Unit target = action.getGameTarget().getTarget();
                            double angle = self.getAngleTo(target);

                            List<CircularUnit> realObstacles = new ArrayList<>();
                            for (CircularUnit obstacle : storage.getObstacles()) {
                                if (isObstacle(self, angle, obstacle, game.getMapSize())) {
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
                                    if (!isObstacle(self, correctedAngle, nearest, game.getMapSize())) {
                                        break;
                                    }
                                }
                                angle = correctedAngle;
                            }
                            storage.setDestinationAngle(angle);
                            storage.setAction(action.getAction());
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
                LivingUnit _target = null;
                switch (action.getGameTarget().getTargetType()) {
                    case WIZARD:
                        _target = (Wizard)action.getGameTarget().getTarget();
                        break;
                    case MINION:
                        _target = (Minion)action.getGameTarget().getTarget();
                        break;
                    case BUILDING:
                        _target = (Building)action.getGameTarget().getTarget();
                        break;
                    case TREE:
                        _target = (Tree)action.getGameTarget().getTarget();
                        break;
                    default:
                        _target = null;
                }

                double castAngle = self.getAngleTo(target);
                double dist = self.getDistanceTo(target);
                storage.setTarget(_target);
                storage.setTargetAngle(castAngle);
                storage.setTargetDistance(dist);
                break;
            case CAST_SPELL:
                //TODO implement in Round 2
                break;
            case TAKE_BONUS:
                //TODO learn to take bonuses
                break;
            case LEARN_SKILL:
                move.setSkillToLearn(action.getGameTarget().getSkill());
                break;
            default:
        }
    }

    public boolean isObstacle(Wizard self, double angle, CircularUnit obstacle, double mapSize) {
        double obstacleAngle = self.getAngleTo(obstacle);
        double obstacleDist = self.getDistanceTo(obstacle);
        double testDist = Math.abs(Math.sin(obstacleAngle - angle)) * obstacleDist;
        Waypoint t = new Waypoint(self.getX() + testDist * Math.cos(self.getAngle() + angle),
                        self.getY() + testDist * Math.sin(self.getAngle() + angle));

        //we doesn't consider far points as obstacles
        if (obstacleDist >= self.getRadius() * 10) {
            return false;
        }
        //this checks if we are out of map
        if (t.getX() < 0.0 + self.getRadius() || t.getX() > mapSize - self.getRadius() || t.getY() < 0.0 + self.getRadius()|| t.getY() > mapSize - self.getRadius()) {
            return true;
        }
        return (Math.abs(Math.sin(obstacleAngle - angle)) * obstacleDist < self.getRadius() + obstacle.getRadius() + 3)
                && (obstacleAngle - angle < StrictMath.PI && obstacleAngle - angle > - StrictMath.PI);
    }
}

