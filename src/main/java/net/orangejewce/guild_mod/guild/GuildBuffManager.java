package net.orangejewce.guild_mod.guild;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.HashMap;
import java.util.Map;

public class GuildBuffManager {
    private static final Map<String, MobEffectInstance> guildBuffs = new HashMap<>();

    public static void addGuildBuff(String guildName, MobEffectInstance effect) {
        guildBuffs.put(guildName, effect);
    }

    public static void applyGuildBuffs(ServerPlayer player) {
        String guildName = GuildManager.getGuild(player);
        if (guildName != null) {
            MobEffectInstance effect = guildBuffs.get(guildName);
            if (effect != null) {
                player.addEffect(new MobEffectInstance(effect));
            }
        }
    }
}
