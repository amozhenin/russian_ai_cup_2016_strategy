import model.CircularUnit;

import java.util.List;

/**
 * Created by dvorkin on 23.11.2016.
 */
public class DataStorage {

    //Immutable data below
    private ZoneMapper mapper;

    //Mutable data below
    private Lane lane;

    //Tick data below
    private List<GameAction> actions;
    private List<EstimatedGameAction> estimatedActions;
    private List<EstimatedGameAction> bestActions;
    private List<CircularUnit> obstacles;

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
}
