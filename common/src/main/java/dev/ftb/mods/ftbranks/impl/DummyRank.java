package dev.ftb.mods.ftbranks.impl;


import com.mojang.authlib.GameProfile;
import dev.ftb.mods.ftbranks.api.PermissionValue;
import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.api.RankCondition;
import dev.ftb.mods.ftbranks.api.RankManager;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

public class DummyRank implements Rank {
    private final String id;
    // used clientside, arg parsing

    public DummyRank(String id) {
        this.id = id;
    }

    @Override
    public RankManager getManager() {
        return null;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return id;
    }

    @Override
    public int getPower() {
        return 0;
    }

    @Override
    public void setPermission(String node, @Nullable PermissionValue value) {
    }

    @Override
    public PermissionValue getPermission(String node) {
        return null;
    }

    @Override
    public RankCondition getCondition() {
        return null;
    }

    @Override
    public void setCondition(RankCondition condition) {
    }

    @Override
    public boolean add(GameProfile profile) {
        return false;
    }

    @Override
    public boolean remove(GameProfile profile) {
        return false;
    }

    @Override
    public Collection<String> getPermissions() {
        return Collections.emptyList();
    }
}
