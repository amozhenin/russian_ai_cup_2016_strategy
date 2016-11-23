import model.LaneType;

/**
 * Created by dvorkin on 23.11.2016.
 */
public class Lane {

    private final LaneType type;

    public Lane(LaneType type) {
        this.type = type;
    }

    public LaneType getType() {
        return type;
    }
}
