import model.*;

import java.io.*;

/**
 * Created by dvorkin on 21.11.2016.
 */
public class LoggerStrategy  implements IExtendedStrategy {

    private boolean firstTick;
    private Writer log;
    private DataStorage storage;

    public LoggerStrategy() throws IOException {
        firstTick = true;
        log = new OutputStreamWriter(new FileOutputStream("Game.log"), "UTF-8");
    }

    @Override
    public void finish() {
        try {
            log.flush();
            log.close();
        } catch (IOException e) {
            System.out.println("IOException " + e);
        }
    }

    @Override
    public DataStorage getDataStorage() {
        return storage;
    }

    @Override
    public void setDataStorage(DataStorage storage) {
        this.storage = storage;
    }

    @Override
    public void move(Wizard self, World world, Game game, Move move) {
        try {
            if (firstTick) {
                logInfo(self, world, game);
                firstTick = false;
            }
            logTickInfo(self, world, move);
            //log.write(String.valueOf(game.getRandomSeed()) + "\n");
        } catch (IOException e) {
            System.out.println("IOException " + e);
        }
//        move.setAction(ActionType.NONE);
//        move.setSpeed(game.getWizardForwardSpeed());
    }

    private void logInfo(Wizard self, World world, Game game) throws IOException {
        StringBuilder sb = new StringBuilder();
        logGameInfo(sb, game);
        logWizardInfo(sb, self);
        logWorldInfo(sb, world);
        log.write(sb.toString());
    }

    private void logGameInfo(StringBuilder sb, Game game) {
        sb.append("===Game===\n").append("Skills enabled = ").append(game.isSkillsEnabled());
        sb.append(", Messages enabled = ").append(game.isRawMessagesEnabled());
        sb.append(", Seed = ").append(game.getRandomSeed());
        sb.append(", Ticks = ").append(game.getTickCount());
        sb.append(", Size = ").append(game.getMapSize()).append("\n");
        sb.append("Bonus ticks = ").append(game.getBonusAppearanceIntervalTicks());
        sb.append(", Minion ticks = ").append(game.getFactionMinionAppearanceIntervalTicks()).append("\n");
        sb.append("Forward speed = ").append(game.getWizardForwardSpeed());
        sb.append(", Backward speed = ").append(game.getWizardBackwardSpeed());
        sb.append(", Strafe speed = ").append(game.getWizardStrafeSpeed()).append("\n");
        sb.append("Cast range = ").append(game.getWizardCastRange());
        sb.append(", Staff range = ").append(game.getStaffRange());
        sb.append(", Staff sector = ").append(game.getStaffSector()).append("\n");
    }

    private void logWizardInfo(StringBuilder sb, Wizard wizard) {
        sb.append("===Wizard===\n").append("Owner id = ").append(wizard.getOwnerPlayerId());
        sb.append(", Master = ").append(wizard.isMaster());
        sb.append(", Me = ").append(wizard.isMe());
        sb.append(", Vision range = ").append(wizard.getVisionRange());
        sb.append(", Radius = ").append(wizard.getRadius());
        sb.append(", Faction = ").append(wizard.getFaction());
        sb.append(", Id = ").append(wizard.getId()).append("\n");
    }

    private void logWorldInfo(StringBuilder sb, World world) {
        sb.append("===World===\n").append("Ticks = ").append(world.getTickCount());
        sb.append(", Width = ").append(world.getWidth());
        sb.append(", Height = ").append(world.getHeight());
        sb.append(", Players:\n");
        for(Player player : world.getPlayers()) {
            sb.append("Name = ").append(player.getName());
            sb.append(", Id = ").append(player.getId());
            sb.append(", Me = ").append(player.isMe());
            sb.append(", Faction = ").append(player.getFaction()).append("\n");
        }
    }

    private void logTickInfo(Wizard self, World world, Move move) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("==========\n");
        logWorldStatus(sb, world);
        logWizardStatus(sb, self);
        logLogic(sb);
        logMove(sb, move);
        log.write(sb.toString());
    }

    private void logLogic(StringBuilder sb) {
        logGeneratedActions(sb);
        logEstimatedActions(sb);
        logBestActions(sb);
        logData(sb);
    }

    private void logWizardStatus(StringBuilder sb, Wizard wizard) {
        sb.append("Wizard status:\n");
        sb.append("Level = ").append(wizard.getLevel());
        sb.append(", Life = ").append(wizard.getLife());
        sb.append(", Mana = ").append(wizard.getMana());
        sb.append(", [").append(wizard.getX()).append(", ").append(wizard.getY()).append("]\n");
    }


    private void logWorldStatus(StringBuilder sb, World world) {
        sb.append("Tick #").append(world.getTickIndex()).append("\n");
        sb.append("Bonuses:\n");
        for (Bonus bonus: world.getBonuses()) {
            sb.append("Type = ").append(bonus.getType().toString()).append(", Faction = ").append(bonus.getFaction());
            sb.append(", [").append(bonus.getX()).append(", ").append(bonus.getY()).append("]\n");
        }
        sb.append("Buildings:\n");
        for(Building building: world.getBuildings()) {
            sb.append("Type = ").append(building.getType().toString()).append(", Faction = ").append(building.getFaction());
            sb.append(", TicksToAttack = ").append(building.getRemainingActionCooldownTicks());
            sb.append(", Life = ").append(building.getLife());
            sb.append(", [").append(building.getX()).append(", ").append(building.getY()).append("]\n");
        }
        sb.append("Wizards:\n");
        for(Wizard wizard : world.getWizards()) {
            if (!wizard.isMe()) {
                sb.append("Faction = ").append(wizard.getFaction());
                sb.append(", Id = ").append(wizard.getId());
                sb.append(", Owner = ").append(wizard.getOwnerPlayerId());
                sb.append(", Level = ").append(wizard.getLevel());
                sb.append(", Life = ").append(wizard.getLife());
                sb.append(", Mana = ").append(wizard.getMana());
                sb.append(", [").append(wizard.getX()).append(", ").append(wizard.getY()).append("]\n");
            }
        }

    }

    private void logMove(StringBuilder sb, Move move) {
        sb.append("Move:\n").append("Action = ").append(move.getAction());
        sb.append(", Speed = ").append(move.getSpeed());
        sb.append(", Strafe = ").append(move.getStrafeSpeed());
        sb.append(", Angle = ").append(move.getTurn());
        sb.append(", Cast angle =").append(move.getCastAngle());
        sb.append(", Min dist = ").append(move.getMinCastDistance());
        sb.append(", Max dist = ").append(move.getMaxCastDistance()).append("\n");
    }

    private void logGeneratedActions(StringBuilder sb) {
        sb.append("***Generated actions***\n");
        if (storage.getGeneratedActions() == null) {
            return;
        }
        for (GameAction action : storage.getGeneratedActions()) {
            logAction(sb, action);
        }
    }

    private void logEstimatedActions(StringBuilder sb) {
        sb.append("***Estimated actions***\n");
        if (storage.getEstimatedActions() == null) {
            return;
        }
        for (EstimatedGameAction action : storage.getEstimatedActions()) {
            logEstimatedAction(sb, action);
        }
    }

    private void logBestActions(StringBuilder sb) {
        sb.append("***Best actions***\n");
        if (storage.getBestActions() == null) {
            return;
        }
        for (EstimatedGameAction action : storage.getBestActions()) {
            logEstimatedAction(sb, action);
        }
    }

    private void logEstimatedAction(StringBuilder sb, EstimatedGameAction action) {
        sb.append(action.getAction()).append(" ");
        sb.append(action.getEstimation()).append(" ");
        sb.append(action.getGameTarget().getTargetType());
        if (action.getGameTarget().getTargetType() == TargetType.LANE) {
            sb.append(" ").append(action.getGameTarget().getLane().getType());
        }
        sb.append(" [").append(action.getGameTarget().getTarget().getX()).append(",");
        sb.append(action.getGameTarget().getTarget().getY()).append("]\n");
    }

    private void logAction(StringBuilder sb, GameAction action) {
        sb.append(action.getAction()).append(" ").append(action.getGameTarget().getTargetType());
        if (action.getGameTarget().getTargetType() == TargetType.LANE) {
            sb.append(" ").append(action.getGameTarget().getLane().getType());
        }
        sb.append(" [").append(action.getGameTarget().getTarget().getX()).append(",");
        sb.append(action.getGameTarget().getTarget().getY()).append("]\n");
    }

    private void logData(StringBuilder sb) {
        sb.append("===Data===\n");
        sb.append("Lane:").append(getDataStorage().getLane() == null
                                ? "null"
                                : getDataStorage().getLane().getType().toString()).append("\n");
    }
}
