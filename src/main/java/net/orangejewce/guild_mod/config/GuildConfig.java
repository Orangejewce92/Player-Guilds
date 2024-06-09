package net.orangejewce.guild_mod.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.tuple.Pair;

@Mod.EventBusSubscriber
public class GuildConfig {
    public static final ForgeConfigSpec COMMON_CONFIG;
    public static final ConfigValues VALUES;

    static {
        Pair<ConfigValues, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ConfigValues::new);
        COMMON_CONFIG = specPair.getRight();
        VALUES = specPair.getLeft();
    }

    public static class ConfigValues {
        public final ForgeConfigSpec.BooleanValue friendlyFire;
        public final ForgeConfigSpec.BooleanValue showGuildNames;

        public ConfigValues(ForgeConfigSpec.Builder builder) {
            builder.push("guild");
            friendlyFire = builder.comment("Enable or disable friendly fire for guild members.")
                    .define("friendlyFire", false);
            showGuildNames = builder.comment("Show guild names above players' heads")
                    .define("showGuildNames", true);
            builder.pop();
        }
    }
}
