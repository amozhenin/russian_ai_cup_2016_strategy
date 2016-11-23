import model.*;

import java.io.IOException;

public final class RunnerCopy {
    private final ModifiedRemoteProcessClient modifiedRemoteProcessClient;
    private final String token;

    public static void main(String[] args) throws IOException {
        new RunnerCopy(args.length == 3 ? args : new String[]{"127.0.0.1", "31001", "0000000000000000"}).run();
    }

    private RunnerCopy(String[] args) throws IOException {
        modifiedRemoteProcessClient = new ModifiedRemoteProcessClient(args[0], Integer.parseInt(args[1]));
        token = args[2];
    }

    public void run() throws IOException {
        try {
            modifiedRemoteProcessClient.writeToken(token);
            modifiedRemoteProcessClient.writeProtocolVersion();
            int teamSize = modifiedRemoteProcessClient.readTeamSize();
            Game game = modifiedRemoteProcessClient.readGameContext();

            Strategy[] strategies = new Strategy[teamSize];

            for (int strategyIndex = 0; strategyIndex < teamSize; ++strategyIndex) {
//                strategies[strategyIndex] = new MyStrategy();
                strategies[strategyIndex] = new DirtyStrategy();
            }

            PlayerContext playerContext;

            while ((playerContext = modifiedRemoteProcessClient.readPlayerContext()) != null) {
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

                modifiedRemoteProcessClient.writeMoves(moves);
            }
        } finally {
            modifiedRemoteProcessClient.close();
        }
    }
}
