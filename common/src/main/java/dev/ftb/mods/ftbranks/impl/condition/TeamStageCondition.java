package dev.ftb.mods.ftbranks.impl.condition;

import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftbteams.api.TeamStagesHelper;
import net.minecraft.server.level.ServerPlayer;

public class TeamStageCondition extends GameStageCondition {
    public TeamStageCondition(Json5Object json) {
        super(json);
    }

    @Override
    public String getType() {
        return "teamstage";
    }

    @Override
    public boolean isRankActive(ServerPlayer player) {
        return TeamStagesHelper.hasTeamStage(player, stage);
    }
}
