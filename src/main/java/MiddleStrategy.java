import model.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public final class MiddleStrategy implements IExtendedStrategy {

    private DataStorage storage;

    public MiddleStrategy() {
        storage = new DataStorage();
        init();
    }

    private void init() {
        //init skills
        List<SkillType> skills = storage.getDesiredSkills();

        skills.add(SkillType.MAGICAL_DAMAGE_BONUS_PASSIVE_1);
        skills.add(SkillType.MAGICAL_DAMAGE_BONUS_AURA_1);
        skills.add(SkillType.MAGICAL_DAMAGE_BONUS_PASSIVE_2);
        skills.add(SkillType.MAGICAL_DAMAGE_BONUS_AURA_2);
        skills.add(SkillType.FROST_BOLT);

        skills.add(SkillType.RANGE_BONUS_PASSIVE_1);
        skills.add(SkillType.RANGE_BONUS_AURA_1);
        skills.add(SkillType.RANGE_BONUS_PASSIVE_2);
        skills.add(SkillType.RANGE_BONUS_AURA_2);
        skills.add(SkillType.ADVANCED_MAGIC_MISSILE);

        skills.add(SkillType.MAGICAL_DAMAGE_ABSORPTION_PASSIVE_1);
        skills.add(SkillType.MAGICAL_DAMAGE_ABSORPTION_AURA_1);
        skills.add(SkillType.MAGICAL_DAMAGE_ABSORPTION_PASSIVE_2);
        skills.add(SkillType.MAGICAL_DAMAGE_ABSORPTION_AURA_2);

        skills.add(SkillType.STAFF_DAMAGE_BONUS_PASSIVE_1);
        skills.add(SkillType.STAFF_DAMAGE_BONUS_AURA_1);
        skills.add(SkillType.STAFF_DAMAGE_BONUS_PASSIVE_2);
        skills.add(SkillType.STAFF_DAMAGE_BONUS_AURA_2);

        skills.add(SkillType.MOVEMENT_BONUS_FACTOR_PASSIVE_1);
        skills.add(SkillType.MOVEMENT_BONUS_FACTOR_AURA_1);
        skills.add(SkillType.MOVEMENT_BONUS_FACTOR_PASSIVE_2);
        skills.add(SkillType.MOVEMENT_BONUS_FACTOR_AURA_2);
        skills.add(SkillType.SHIELD);
        skills.add(SkillType.HASTE);
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
                if (skillToCheck == SkillType.FROST_BOLT) {
                    storage.nowHaveFrostBolt();
                } else if (skillToCheck == SkillType.FIREBALL) {
                    storage.nowHaveFireBall();
                } else if (skillToCheck == SkillType.SHIELD) {
                    storage.nowHaveShield();
                } else if (skillToCheck == SkillType.HASTE) {
                    storage.nowHaveHaste();
                }
                skillIterator.remove();
            }
        }
//        storage.saveCoordinates(new Waypoint(self.getX(), self.getY()));
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
        List<LivingUnit> friends = new ArrayList<>();
        for (Building building : world.getBuildings()) {
            if (isFoe(self.getFaction(), building)) {
                actions.add(new GameAction(Action.ATTACK, new GameTarget(building)));
                foes.add(building);
            }
            if (isFriend(self.getFaction(), building)) {
                friends.add(building);
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
            if (isFriend(self.getFaction(), wizard)) {
                friends.add(wizard);
            }
        }
        for (Minion minion: world.getMinions()) {
            if (isFoe(self.getFaction(), minion)) {
                actions.add(new GameAction(Action.ATTACK, new GameTarget(minion)));
                foes.add(minion);
            }
            if (isFriend(self.getFaction(), minion)) {
                friends.add(minion);
            }
        }
        for (Tree tree: world.getTrees()) {
            actions.add(new GameAction(Action.ATTACK, new GameTarget(tree)));
          //  foes.add(tree);
        }
        storage.setFoes(foes);
        storage.setFriends(friends);
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

    private boolean isFriend(Faction faction, CircularUnit unit) {
        return unit.getFaction() == faction;
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
//                                switch (type) {
//                                    case MIDDLE:
//                                        if (self.getX() + self.getY() == game.getMapSize()) {
//                                            estimation = 10.0;
//                                        } else {
//                                            estimation = -50.0;
//                                        }
//                                        break;
//                                    case TOP:
//                                        if (self.getX() + self.getY() < game.getMapSize()) {
//                                            estimation = 10.0;
//                                        } else {
//                                            estimation = -50.0;
//                                        }
//                                        break;
//                                    case BOTTOM:
//                                        if (self.getX() + self.getY() > game.getMapSize()) {
//                                            estimation = 10.0;
//                                        } else {
//                                            estimation = -50.0;
//                                        }
//                                        break;
//                                }
                                switch (type) {
                                    case MIDDLE:
                                        estimation = 10;
                                        break;
                                    default:
                                        estimation = -50;
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
                if (self.getDistanceTo(action.getGameTarget().getTarget()) > game.getScoreGainRange() + self.getRadius() * 3) {
                    estimation = 0.0;
                } else {
                    LivingUnit unit = (LivingUnit) action.getGameTarget().getTarget();
                    double distance = self.getDistanceTo(unit) - self.getRadius() - unit.getRadius();
                    double distanceFactor;
                    double factionFactor = 1.0;
                    double damageFactor = 0.0;
                    double killFactor = 0.0;
                    double friendFactor;
                    double targetedFactor;
                    if (Math.abs(unit.getAngle() - self.getAngleTo(unit) - StrictMath.PI) < 0.001
                            || Math.abs(unit.getAngle() - self.getAngleTo(unit) + StrictMath.PI) < 0.001) {
                        targetedFactor = 1.1;
                    } else {
                        targetedFactor = 1.0;
                    }
                    if (distance > self.getCastRange()) {
                        distanceFactor = 0.01;
                    } else if (distance > 100) {
                        distanceFactor = 1.12 - distance / 5000.0;
                    } else {
                        distanceFactor = 1.5 - 4 * distance / 1000.0;
                    }
                    friendFactor = 1.0;
                    for (LivingUnit friend : storage.getFriends()) {
                        if (friend instanceof Wizard) {
                            if (friend.getDistanceTo(unit) <= game.getScoreGainRange()) {
                                friendFactor = 1.67;
                            }
                        }
                    }
                    switch (action.getGameTarget().getTargetType()) {
                        case WIZARD:
                            damageFactor = game.getMagicMissileDirectDamage() * game.getWizardDamageScoreFactor();
                            killFactor = game.getWizardEliminationScoreFactor() * unit.getMaxLife();
                            if (distance < game.getStaffRange()) {
                                if (unit.getLife() < game.getStaffDamage()) {
                                    killFactor = killFactor * 0.97;
                                } else {
                                    killFactor = killFactor * (1.0 - Math.max(0.0, ((double)unit.getLife() - game.getStaffDamage())) / (double)unit.getMaxLife()) / Math.max(1.0, unit.getLife() / game.getMagicMissileDirectDamage());
                                }
                            } else if (distance < self.getCastRange()) {
                                if (unit.getLife() < game.getMagicMissileDirectDamage()) {
                                    killFactor = killFactor * 0.94;
                                } else {
                                    killFactor = killFactor * (1.0 - Math.max(0.0, ((double)unit.getLife() - game.getMagicMissileDirectDamage())) / (double)unit.getMaxLife()) / Math.max(1.0, unit.getLife() / game.getMagicMissileDirectDamage());
                                }
                            } else {
                                killFactor = killFactor * 0.1;
                            }
                            estimation = (killFactor + damageFactor) * factionFactor * friendFactor *  targetedFactor * distanceFactor;
                            break;
                        case MINION:
                            if (unit.getFaction() == Faction.NEUTRAL) {
                                factionFactor = 0.95;
                            } else {
                                factionFactor = 1.0;
                            }
                            Minion minion =(Minion) unit;
                            damageFactor = game.getMagicMissileDirectDamage() * game.getMinionDamageScoreFactor();
                            killFactor = game.getMinionEliminationScoreFactor() * unit.getMaxLife();
                            if (distance < game.getStaffRange()) {
                                if (minion.getType() == MinionType.ORC_WOODCUTTER) {
                                    killFactor = killFactor * 2;
                                }
                                if (unit.getLife() < game.getStaffDamage()) {
                                    killFactor = killFactor * 0.97;
                                } else {
                                    killFactor = killFactor * (1.0 - Math.max(0.0, ((double)unit.getLife() - game.getStaffDamage())) / (double)unit.getMaxLife()) / Math.max(1.0, unit.getLife() / game.getMagicMissileDirectDamage());
                                }
                            } else if (distance < game.getFetishBlowdartAttackRange()) {
                                if (minion.getType() == MinionType.FETISH_BLOWDART) {
                                    killFactor = killFactor * 2;
                                }
                                if (unit.getLife() < game.getMagicMissileDirectDamage()) {
                                    killFactor = killFactor * 0.94;
                                } else {
                                    killFactor = killFactor * (1.0 - Math.max(0.0, ((double)unit.getLife() - game.getMagicMissileDirectDamage())) / (double)unit.getMaxLife()) / Math.max(1.0, unit.getLife() / game.getMagicMissileDirectDamage());
                                }
                            } else if (distance < self.getCastRange()) {
                                if (unit.getLife() < game.getMagicMissileDirectDamage()) {
                                    killFactor = killFactor * 0.95;
                                } else {
                                    killFactor = killFactor * (1.0 - Math.max(0.0, ((double)unit.getLife() - game.getMagicMissileDirectDamage())) / (double)unit.getMaxLife()) / Math.max(1.0, unit.getLife() / game.getMagicMissileDirectDamage());
                                }
                            } else {
                                killFactor = killFactor * 0.1;
                            }
                            estimation = (killFactor + damageFactor) * factionFactor * friendFactor *  targetedFactor * distanceFactor;
                            break;
                        case BUILDING:
                            targetedFactor = 1.0; //TODO calculate if I'm nearest to building
                            Building building = (Building) unit;
                            damageFactor = game.getMagicMissileDirectDamage() * game.getBuildingDamageScoreFactor();
                            killFactor = game.getBuildingEliminationScoreFactor() * unit.getMaxLife();
                            if (building.getType() == BuildingType.FACTION_BASE) {
                                killFactor += game.getVictoryScore();
                            }
                            if (distance < game.getStaffRange()) {
                                if (unit.getLife() < game.getStaffDamage()) {
                                    killFactor = killFactor * 1;
                                } else {
                                    killFactor = killFactor * (1.0 - Math.max(0.0, ((double)unit.getLife() - game.getStaffDamage())) / (double)unit.getMaxLife()) / Math.max(1.0, unit.getLife() / game.getMagicMissileDirectDamage());
                                }
                            } else if (distance < self.getCastRange()) {
                                if (unit.getLife() < game.getMagicMissileDirectDamage()) {
                                    killFactor = killFactor * 1;
                                } else {
                                    killFactor = killFactor * (1.0 - Math.max(0.0, ((double)unit.getLife() - game.getMagicMissileDirectDamage())) / (double)unit.getMaxLife()) / Math.max(1.0, unit.getLife() / game.getMagicMissileDirectDamage());
                                }
                            } else {
                                killFactor = killFactor * 0.1;
                            }
                            estimation = (killFactor + damageFactor) * factionFactor * friendFactor *  targetedFactor * distanceFactor;
                            break;
                        case TREE:
                            targetedFactor = 1.0;
                            friendFactor  = 1.0;
                            damageFactor = 0.001;
                            factionFactor = 0.1;
                            killFactor = 0.002 * (1.0 - Math.max(0.0, ((double)unit.getLife() - game.getMagicMissileDirectDamage())) / (double)unit.getMaxLife()) / Math.max(1.0, unit.getLife() / game.getMagicMissileDirectDamage());
                            estimation = (killFactor + damageFactor) * factionFactor * friendFactor *  targetedFactor * distanceFactor;
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
            move.setStrafeSpeed(game.getWizardStrafeSpeed() * Math.sin(storage.getDestinationAngle()));
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
            if (self.getRemainingActionCooldownTicks() == 0) {
                if (storage.getTargetAngle() >= -StrictMath.PI / 12 && storage.getTargetAngle() <= StrictMath.PI / 12) {
                    if ((self.getRemainingCooldownTicksByAction()[ActionType.STAFF.ordinal()] == 0)
                            && storage.getTargetDistance() < game.getStaffRange() + storage.getTarget().getRadius()) {
                        move.setAction(ActionType.STAFF);
                    } else if (storage.hasFrostBolt()
                            && self.getMana() > game.getFrostBoltManacost()
                            && (self.getRemainingCooldownTicksByAction()[ActionType.FROST_BOLT.ordinal()] == 0)
                            && storage.getTargetDistance() < self.getCastRange() + storage.getTarget().getRadius()
                            && storage.getTarget() instanceof Wizard) {
                        move.setAction(ActionType.FROST_BOLT);
                        move.setCastAngle(storage.getTargetAngle());
                        move.setMinCastDistance(storage.getTargetDistance() - storage.getTarget().getRadius() + game.getFrostBoltRadius());

                    } else if ((self.getRemainingCooldownTicksByAction()[ActionType.MAGIC_MISSILE.ordinal()] == 0)
                            && storage.getTargetDistance() < self.getCastRange() + storage.getTarget().getRadius() ) {
                        move.setAction(ActionType.MAGIC_MISSILE);
                        move.setCastAngle(storage.getTargetAngle());
                        move.setMinCastDistance(storage.getTargetDistance() - storage.getTarget().getRadius() + game.getMagicMissileRadius());
                       // move.setMaxCastDistance(storage.getTargetDistance() + storage.getTarget().getRadius());
                    } else {
                        for (EstimatedGameAction attack : getAttackCandidates()) {
                            double castAngle = self.getAngleTo(attack.getGameTarget().getTarget());
                            double dist = self.getDistanceTo(attack.getGameTarget().getTarget());
                            LivingUnit target = (LivingUnit)attack.getGameTarget().getTarget();
                            if (castAngle < -StrictMath.PI / 12 || castAngle > StrictMath.PI / 12) {
                                continue;
                            }
                            if ((self.getRemainingCooldownTicksByAction()[ActionType.STAFF.ordinal()] == 0)
                                    && dist < game.getStaffRange() + target.getRadius()) {
                                move.setAction(ActionType.STAFF);
                                return;
                            } else if (storage.hasFrostBolt()
                                    && self.getMana() > game.getFrostBoltManacost()
                                    && (self.getRemainingCooldownTicksByAction()[ActionType.FROST_BOLT.ordinal()] == 0)
                                    && dist < self.getCastRange() + target.getRadius()
                                    && attack.getGameTarget().getTarget() instanceof Wizard) {
                                move.setAction(ActionType.FROST_BOLT);
                                move.setCastAngle(castAngle);
                                move.setMinCastDistance(dist - target.getRadius() + game.getFrostBoltRadius());

                            } else if ((self.getRemainingCooldownTicksByAction()[ActionType.MAGIC_MISSILE.ordinal()] == 0)
                                    && dist < self.getCastRange() + target.getRadius()) {
                                move.setAction(ActionType.MAGIC_MISSILE);
                                move.setCastAngle(castAngle);
                                move.setMinCastDistance(dist - target.getRadius() + game.getMagicMissileRadius());
                           //     move.setMaxCastDistance(dist + ((LivingUnit)attack.getGameTarget().getTarget()).getRadius());
                                return;
                            }
                        }
                        move.setAction(ActionType.NONE);
                    }
                } else {
                    for (EstimatedGameAction attack : getAttackCandidates()) {
                        double castAngle = self.getAngleTo(attack.getGameTarget().getTarget());
                        double dist = self.getDistanceTo(attack.getGameTarget().getTarget());
                        LivingUnit target = (LivingUnit)attack.getGameTarget().getTarget();
                        if (castAngle < -StrictMath.PI / 12 || castAngle > StrictMath.PI / 12) {
                            continue;
                        }
                        if ((self.getRemainingCooldownTicksByAction()[ActionType.STAFF.ordinal()] == 0)
                                && dist < game.getStaffRange() + target.getRadius()) {
                            move.setAction(ActionType.STAFF);
                            return;
                        } else if (storage.hasFrostBolt()
                                && self.getMana() > game.getFrostBoltManacost()
                                && (self.getRemainingCooldownTicksByAction()[ActionType.FROST_BOLT.ordinal()] == 0)
                                && dist < self.getCastRange() + target.getRadius()
                                && attack.getGameTarget().getTarget() instanceof Wizard) {
                            move.setAction(ActionType.FROST_BOLT);
                            move.setCastAngle(castAngle);
                            move.setMinCastDistance(dist - target.getRadius() + game.getFrostBoltRadius());

                        } else if ((self.getRemainingCooldownTicksByAction()[ActionType.MAGIC_MISSILE.ordinal()] == 0)
                                && dist < self.getCastRange() + target.getRadius()) {
                            move.setAction(ActionType.MAGIC_MISSILE);
                            move.setCastAngle(castAngle);
                            move.setMinCastDistance(dist - target.getRadius() + game.getMagicMissileRadius());
                      //      move.setMaxCastDistance(dist + ((LivingUnit)attack.getGameTarget().getTarget()).getRadius());
                            return;
                        }
                    }
                    move.setAction(ActionType.NONE);
                }
            } else {
                move.setAction(ActionType.NONE);
            }
        }
    }

    private List<EstimatedGameAction> getAttackCandidates() {
        return storage.getEstimatedActions().stream().filter(e -> (e.getEstimation() > 0.0) && (e.getAction() == Action.ATTACK)).sorted(
                (e1, e2) -> e1.getEstimation() == e2.getEstimation()
                          ? 0
                          : (e1.getEstimation() > e2.getEstimation()
                           ? -1
                           : 1)
                 ).collect(Collectors.toList());
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

                            Waypoint stuckPoint = storage.getStuckPoint();
                            boolean stuck = false;
                            if (stuckPoint == null) {
                                storage.setStuckPoint(new Waypoint(self.getX(), self.getY()));
                            } else {
                                double dist = self.getDistanceTo(stuckPoint);
                                if (dist < 5) {
                                    storage.incrementStuckTicks();
                                } else {
                                    storage.setStuckPoint(new Waypoint(self.getX(), self.getY()));
                                }
                                if (storage.getStuckTicks() > 5) {
                                    stuck = true;
                                }
                            }
                            List<CircularUnit> realObstacles = new ArrayList<>();
                            List<CircularUnit> nearObstacles = new ArrayList<>();
                            for (CircularUnit obstacle : storage.getObstacles()) {
                                if (isObstacle(self, obstacle)) {
                                    realObstacles.add(obstacle);
                                }
                                if (isNearObstacle(self, obstacle)) {
                                    nearObstacles.add(obstacle);
                                }
                            }
                            CircularUnit nearest = null;
                            double distance = 8000.0;
                            for (CircularUnit realObstacle : realObstacles) {
                                double obsDist = self.getDistanceTo(realObstacle) - self.getRadius() - realObstacle.getRadius();
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
                                boolean found = false;
                                for(int i = 1; i <= 360; i++) {
                                    correctedAngle -= (StrictMath.PI * i * sign) / 180;
                                    sign = -sign;
                                    if (isObstacleForAngle(self, correctedAngle, nearest, game.getMapSize())) {
                                        continue;
                                    }
                                    found = true;
                                    for (CircularUnit near : nearObstacles) {
                                        if (isObstacleForAngle(self, correctedAngle, near, game.getMapSize())) {
                                            found = false;
                                        }
                                    }
                                    if (found) {
                                        break;
                                    }
                                }
                                if (found) {
                                    angle = correctedAngle;
                                }
                                if (stuck) {
                                    if (distance < 5) {
                                        angle = obstacleAngle + StrictMath.PI;
                                    } else {
                                        //TODO
                                    }
                                }
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

    public boolean isObstacle(Wizard self, CircularUnit obstacleCandidate) {
        return self.getDistanceTo(obstacleCandidate) < self.getRadius() * 10;
    }

    public boolean isNearObstacle(Wizard self, CircularUnit obstacleCandidate) {
        return self.getDistanceTo(obstacleCandidate) - self.getRadius() - obstacleCandidate.getRadius() < self.getRadius();
    }

    public boolean isObstacleForAngle(Wizard self, double angle, CircularUnit obstacle, double mapSize) {
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
        return (Math.abs(Math.sin(obstacleAngle - angle)) * obstacleDist < self.getRadius() + obstacle.getRadius() + 5)
                //&& (obstacleAngle - angle < StrictMath.PI && obstacleAngle - angle > - StrictMath.PI)
                ;
    }

//    public double getEffectiveMagicMissileDamage(Wizard self, LivingUnit unit, Game game) {
//
//    }
//
//    public double getEffectiveFrostBoltDamage(Wizard self, LivingUnit unit, Game game) {
//
//    }
//
//    public double getEffectiveFireBallDamage(Wizard self, LivingUnit unit, Game game) {
//
//    }

//    public boolean hasSkill(Wizard wizard, SkillType skill, Game game) {
//        boolean found = false;
//        if (!game.isSkillsEnabled()) {
//            return found;
//        }
//        for (SkillType test : wizard.getSkills()) {
//            if (test == skill) {
//                found = true;
//                break;
//            }
//        }
//        return found;
//    }
}

