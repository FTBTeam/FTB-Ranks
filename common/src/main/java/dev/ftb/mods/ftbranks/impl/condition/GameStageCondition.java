package dev.ftb.mods.ftbranks.impl.condition;

import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftblibrary.integration.stages.StageHelper;
import dev.ftb.mods.ftblibrary.json5.Json5Util;
import dev.ftb.mods.ftbranks.api.RankCondition;
import dev.ftb.mods.ftbranks.api.RankException;
import net.minecraft.server.level.ServerPlayer;

public class GameStageCondition implements RankCondition {
    protected final String stage;

    public GameStageCondition(Json5Object json) {
        stage = Json5Util.getString(json, "stage").orElseThrow(() -> new RankException("missing 'stage' field"));
    }

    @Override
    public String getType() {
        return "gamestage";
    }

    @Override
    public boolean isRankActive(ServerPlayer player) {
        return StageHelper.getInstance().getProvider().has(player, stage);
    }

    @Override
    public Json5Object save(Json5Object json) {
        json.addProperty("stage", stage);
        return json;
    }
}
