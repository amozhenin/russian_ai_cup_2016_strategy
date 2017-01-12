import model.CircularUnit;
import model.LivingUnit;
import model.SkillType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by dvorkin on 23.11.2016.
 */
public class DataStorage {

    //Immutable data below
    private ZoneMapper mapper;
    private Random random;
    private GameType gameType;

    //Mutable data below
    private Lane lane;
        //Stuck detection
    //private List<Waypoint> oldCoordinates = new ArrayList<>(5);
    Waypoint stuckPoint;
    int stuckTicks = 0;
    private boolean hasFrostBolt = false;
    private boolean hasFireBall = false;
    private boolean hasShield = false;
    private boolean hasHaste = false;

    private List<SkillType> desiredSkills = new ArrayList<>(25);

    //Tick data below
    private int latestTick = -1;
    private boolean justResurrected = false;
    private int latestDeathTick = -1;
    private int latestFrozenTick = -1;
    private int latestUnknownThingTick = -1;
    private boolean justUnfrozen = false;
    private boolean justUnknownWaked = false;
    private List<GameAction> actions;
    private List<EstimatedGameAction> estimatedActions;
    private List<EstimatedGameAction> bestActions;
    private List<CircularUnit> obstacles;
    private List<LivingUnit> foes;
    private List<LivingUnit> friends;

    private double destinationAngle;
    private double targetAngle;
    private double targetDistance;
    private LivingUnit target;
    private Action action;

    public void setGeneratedActions(List<GameAction> actions) {
        this.actions = actions;
    }

    public void setEstimatedActions(List<EstimatedGameAction> estimatedActions) {
        this.estimatedActions = estimatedActions;
    }

    public void setBestActions(List<EstimatedGameAction> bestActions) {
        this.bestActions = bestActions;
    }

    public List<GameAction> getGeneratedActions() {
        return actions;
    }

    public List<EstimatedGameAction> getEstimatedActions() {
        return estimatedActions;
    }

    public List<EstimatedGameAction> getBestActions() {
        return bestActions;
    }

    public void setLane(Lane lane) {
        this.lane = lane;
    }

    public Lane getLane() {
        return lane;
    }

    public void setObstacles(List<CircularUnit> obstacles) {
        this.obstacles = obstacles;
    }

    public List<CircularUnit> getObstacles() {
        return obstacles;
    }

    public ZoneMapper getZoneMapper() {
        return mapper;
    }

    public void setZoneMapper(ZoneMapper mapper) {
        this.mapper = mapper;
    }

    public List<LivingUnit> getFoes() {
        return foes;
    }

    public void setFoes(List<LivingUnit> foes) {
        this.foes = foes;
    }

    public void setRandom(Random random) {
        this.random = random;
    }

    public Random getRandom() {
        return random;
    }

//    public void saveCoordinates(Waypoint point) {
//        if (oldCoordinates.size() < 5) {
//            oldCoordinates.add(point);
//        } else {
//            oldCoordinates.remove(0);
//            oldCoordinates.add(point);
//        }
//    }
//
//    public List<Waypoint> getCoordinates() {
//        return oldCoordinates;
//    }

    public Waypoint getStuckPoint() {
        return this.stuckPoint;
    }

    public void setStuckPoint(Waypoint stuckPoint) {
        this.stuckPoint = stuckPoint;
        stuckTicks = 0;
    }

    public int getStuckTicks() {
        return this.stuckTicks;
    }

    public void incrementStuckTicks() {
        this.stuckTicks++;
    }

    public double getDestinationAngle() {
        return this.destinationAngle;
    }

    public void setDestinationAngle(double destinationAngle) {
        this.destinationAngle = destinationAngle;
    }

    public double getTargetAngle() {
        return this.targetAngle;
    }

    public void setTargetAngle(double targetAngle) {
        this.targetAngle = targetAngle;
    }

    public double getTargetDistance() {
        return this.targetDistance;
    }

    public void setTargetDistance(double targetDistance) {
        this.targetDistance = targetDistance;
    }

    public LivingUnit getTarget() {
        return target;
    }

    public void setTarget(LivingUnit target) {
        this.target = target;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public List<SkillType> getDesiredSkills() {
        return this.desiredSkills;
    }

    public void setDesiredSkills(List<SkillType> desiredSkills) {
        this.desiredSkills = desiredSkills;
    }

    public List<LivingUnit> getFriends() {
        return this.friends;
    }

    public void setFriends(List<LivingUnit> friends) {
        this.friends = friends;
    }

    public boolean hasFrostBolt() {
        return hasFrostBolt;
    }

    public void nowHaveFrostBolt() {
        hasFrostBolt = true;
    }

    public boolean hasFireBall() {
        return hasFireBall;
    }

    public void nowHaveFireBall() {
        hasFireBall = true;
    }

    public boolean hasShield() {
        return hasShield;
    }

    public void nowHaveShield() {
        hasShield = true;
    }

    public boolean hasHaste() {
        return hasHaste;
    }

    public void nowHaveHaste() {
        hasHaste = true;
    }

    public int getLatestTick() {
        return latestTick;
    }

    public void setLatestTick(int latestTick) {
        this.latestTick = latestTick;
    }

    public boolean isJustResurrected() {
        return justResurrected;
    }

    public void setJustResurrected(boolean justResurrected) {
        this.justResurrected = justResurrected;
    }

    public boolean isJustUnfrozen() {
        return justUnfrozen;
    }

    public void setJustUnfrozen(boolean justUnfrozen) {
        this.justUnfrozen = justUnfrozen;
    }

    public int getLatestDeathTick() {
        return latestDeathTick;
    }

    public int getLatestFrozenTick() {
        return latestFrozenTick;
    }

    public void setLatestDeathTick(int latestDeathTick) {
        this.latestDeathTick = latestDeathTick;
    }

    public void setLatestFrozenTick(int latestFrozenTick) {
        this.latestFrozenTick = latestFrozenTick;
    }

    public boolean isJustUnknownWaked() {
        return justUnknownWaked;
    }

    public void setJustUnknownWaked(boolean justUnknownWaked) {
        this.justUnknownWaked = justUnknownWaked;
    }

    public int getLatestUnknownThingTick() {
        return latestUnknownThingTick;
    }

    public void setLatestUnknownThingTick(int latestUnknownThingTick) {
        this.latestUnknownThingTick = latestUnknownThingTick;
    }

    public GameType getGameType() {
        return gameType;
    }

    public void setGameType(GameType gameType) {
        this.gameType = gameType;
    }
}
