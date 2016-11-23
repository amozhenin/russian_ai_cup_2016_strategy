import model.Game;
import model.Move;
import model.Wizard;
import model.World;

import java.io.IOException;

/**
 * Created by dvorkin on 21.11.2016.
 */
public class DirtyStrategy implements Strategy {

    private Strategy myStrategy;
    private Strategy loggerStrategy;

    public DirtyStrategy() throws IOException {
        System.out.println("Hello cruel world, this is dirty!");
        myStrategy = new MyStrategy();
        loggerStrategy = new LoggerStrategy();
    }

    @Override
    public void move(Wizard self, World world, Game game, Move move) {
        myStrategy.move(self, world, game, move);
        loggerStrategy.move(self, world, game, move);
    }
}
