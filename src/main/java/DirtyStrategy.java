import model.Game;
import model.Move;
import model.Wizard;
import model.World;

import java.io.IOException;

/**
 * Created by dvorkin on 21.11.2016.
 */
public class DirtyStrategy implements Strategy {

    private IExtendedStrategy myStrategy;
    private IExtendedStrategy loggerStrategy;

    public DirtyStrategy(int strategyType, int index) throws IOException {
        System.out.println("Hello cruel world, this is dirty!");
        String name;
        if (strategyType == 0) {
            name = "Dvorkin";
            myStrategy = new MyStrategy();
        } else if (strategyType == 1) {
            name = "Center";
            myStrategy = new MiddleStrategy();
        } else if (strategyType == 2) {
            name = "Range";
            myStrategy = new RangeStrategy();
        } else if (strategyType == 3) {
            name = "Fireball";
            myStrategy = new FireballStrategy();
        } else if (strategyType == 100) {
            name = "Dvorkin-100-";
            myStrategy = new MyStrategy100();
        } else {
            //TODO add test strategies
            throw new RuntimeException("Not supported yet");
        }
        DataStorage storage = myStrategy.getDataStorage();
        loggerStrategy = new LoggerStrategy(index, name);
        loggerStrategy.setDataStorage(storage);
    }

    @Override
    public void move(Wizard self, World world, Game game, Move move) {
        myStrategy.move(self, world, game, move);
        loggerStrategy.move(self, world, game, move);
    }

    public void finish() {
        myStrategy.finish();
        loggerStrategy.finish();
    }
}
