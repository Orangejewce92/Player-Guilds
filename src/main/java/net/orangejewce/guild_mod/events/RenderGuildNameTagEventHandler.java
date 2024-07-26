package net.orangejewce.guild_mod.events;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.orangejewce.guild_mod.GuildMod;
import net.orangejewce.guild_mod.guild.GuildManager;

@Mod.EventBusSubscriber(modid = GuildMod.MOD_ID, value = Dist.CLIENT)
public class RenderGuildNameTagEventHandler {

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            String guildName = GuildManager.getGuild(player);
            if (guildName != null) {
                ChatFormatting color = GuildManager.getGuildColor(String.valueOf(player));

                // Log the fetched color for debugging
                System.out.println("Color fetched for player " + player.getName().getString() + ": " + (color != null ? color.getName() : "NULL"));

                MutableComponent nameTag = Component.literal(player.getName().getString() + " [" + guildName + "]")
                        .withStyle(style -> style.withColor(color != null ? color : ChatFormatting.YELLOW));
                event.setContent(nameTag);
            }
        }
    }
}
