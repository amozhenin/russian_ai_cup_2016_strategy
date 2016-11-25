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

    public DirtyStrategy() throws IOException {
        System.out.println("Hello cruel world, this is dirty!");
        myStrategy = new MyStrategy();
        DataStorage storage = myStrategy.getDataStorage();
        loggerStrategy = new LoggerStrategy();
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
