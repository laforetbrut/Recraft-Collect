package com.vyrriox.recraftcollect.data;

import com.vyrriox.recraftcollect.config.ZombieValueConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Resolves the point value of a killed entity by looking up its registry id
 * in ZombieValueConfig (exact match -> "modid:*" wildcard -> 0).
 */
public class ZombieKillResolver {

    public static String getEntityId(Entity entity) {
        if (entity == null) return "";
        EntityType<?> type = entity.getType();
        var key = ForgeRegistries.ENTITY_TYPES.getKey(type);
        return key != null ? key.toString() : "";
    }

    public static int getPoints(Entity entity) {
        return ZombieValueConfig.getValue(getEntityId(entity));
    }
}
