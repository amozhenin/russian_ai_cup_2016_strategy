import model.*;

import java.io.IOException;

public final class DirtyRunner {
    private final RemoteProcessClient remoteProcessClient;
    private final String token;

    private static String[] defaultArgs = new String[]{"127.0.0.1", "31001", "0000000000000000"};

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            throw new RuntimeException("Usage: DirtyRunner type index");
        }
        int type = Integer.valueOf(args[0]);
        int index = Integer.valueOf(args[1]);
        String[] runnerArgs = new String[defaultArgs.length];
        runnerArgs[0] = defaultArgs[0];
        runnerArgs[1] = String.valueOf(Integer.valueOf(defaultArgs[1]) + index);
        runnerArgs[2] = defaultArgs[2];
        new DirtyRunner(runnerArgs).run(type, index);
    }

    private DirtyRunner(String[] args) throws IOException {
        remoteProcessClient = new RemoteProcessClient(args[0], Integer.parseInt(args[1]));
        token = args[2];
    }

    public void run(int type, int index) throws IOException {
        DirtyStrategy ds = new DirtyStrategy(type, index);
        try {
            remoteProcessClient.writeToken(token);
            remoteProcessClient.writeProtocolVersion();
            int teamSize = remoteProcessClient.readTeamSize();
            Game game = remoteProcessClient.readGameContext();

            Strategy[] strategies = new Strategy[teamSize];

            for (int strategyIndex = 0; strategyIndex < teamSize; ++strategyIndex) {
//                strategies[strategyIndex] = new MyStrategy();
                strategies[strategyIndex] = ds;
            }

            PlayerContext playerContext;

            while ((playerContext = remoteProcessClient.readPlayerContext()) != null) {
                Wizard[] playerWizards = playerContext.getWizards();
                if (playerWizards == null || playerWizards.length != teamSize) {
                    break;
                }

                Move[] moves = new Move[teamSize];

                for (int wizardIndex = 0; wizardIndex < teamSize; ++wizardIndex) {
                    Wizard playerWizard = playerWizards[wizardIndex];

                    Move move = new Move();
                    moves[wizardIndex] = move;
                    strategies[wizardIndex /*playerWizard.getTeammateIndex()*/].move(
                            playerWizard, playerContext.getWorld(), game, move
                    );
                }

                remoteProcessClient.writeMoves(moves);
            }
        } finally {
            ds.finish();
            remoteProcessClient.close();
        }
    }
}
