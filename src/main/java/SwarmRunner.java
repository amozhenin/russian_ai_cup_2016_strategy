import java.io.IOException;

/**
 * Created by dvorkin on 24.12.2016.
 */
public class SwarmRunner {

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new RuntimeException("Usage: DirtyRunner type index");
        }
        int type = Integer.valueOf(args[0]);
        int index = Integer.valueOf(args[1]);
        for (int i = 0; i < 5; i++) {
            int unitIndex = 5 * index + i;
            new StrategyRunner(type, unitIndex).start();
            Thread.sleep(1000);
        }
    }

    private static class StrategyRunner extends Thread {
        private int type;
        private int index;
        public StrategyRunner(int type, int index) {
            super("Thread " + index);
            this.type = type;
            this.index = index;
        }

        @Override
        public void run() {
            try {
                String[] args = {String.valueOf(type), String.valueOf(index)};
                DirtyRunner.main(args);
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }
}
