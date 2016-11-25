import model.Unit;

/**
 * Created by dvorkin on 25.11.2016.
 */
public class ZoneMapper {

    private double size;

    public ZoneMapper(double size) {
        this.size = size;
    }

    public Zone getZoneOfUnit(Unit unit) {
        double x = unit.getX();
        double y = unit.getY();

        if (x <= size / 10) {
          if (y <= size / 10) {
              return Zone.TOP_TURN;
          } else if (y >= size - size / 10) {
              return Zone.OUR_BASE;
          } else {
              return Zone.TOP_BEFORE_TURN;
          }
        } else if (x >= size - size / 10) {
           if (y <= size / 10) {
               return Zone.ENEMY_BASE;
           } else if (y >= size - size / 10) {
               return Zone.BOTTOM_TURN;
           } else {
               return Zone.BOTTOM_AFTER_TURN;
           }
        } else if (y <= size / 10) {
            return Zone.TOP_AFTER_TURN;
        } else if (y >= size - size / 10) {
            return Zone.BOTTOM_BEFORE_TURN;
        } else if (x - y < -size / 20) {
            if (x + y - size < -size / 20) {
                return Zone.FOREST_OUR_TOP;
            } else if (x + y - size <= size / 20) {
                return Zone.MIDDLE_BEFORE_CENTER;
            } else {
                return Zone.FOREST_OUR_BOTTOM;
            }
        } else if (x - y <= size / 20) {
            if (x + y - size < -size / 20) {
                return Zone.TOP_BONUS;
            } else if (x + y - size <= size / 20) {
                return Zone.CENTER;
            } else {
                return Zone.BOTTOM_BONUS;
            }
        } else {
            if (x + y - size < -size / 20) {
                return Zone.FOREST_ENEMY_TOP;
            } else if (x + y - size <= size / 20) {
                return Zone.MIDDLE_AFTER_CENTER;
            } else {
                return Zone.FOREST_ENEMY_BOTTOM;
            }
        }
    }
}
