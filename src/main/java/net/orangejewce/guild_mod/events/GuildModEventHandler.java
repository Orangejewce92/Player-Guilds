package net.orangejewce.guild_mod.events;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.orangejewce.guild_mod.GuildMod;
import net.orangejewce.guild_mod.config.GuildConfig;
import net.orangejewce.guild_mod.guild.GuildManager;
import net.orangejewce.guild_mod.guild.GuildBuffManager;
import net.orangejewce.guild_mod.guild.GuildStorageManager;

import java.io.File;
import java.util.Objects;
import java.util.Set;

@Mod.EventBusSubscriber(modid = GuildMod.MOD_ID)
public class GuildModEventHandler {

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        MinecraftServer server = event.getServer();
        File worldSaveDir = server.getWorldPath(LevelResource.ROOT).toFile();
        GuildManager.setWorldSaveDirectory(worldSaveDir);

        // Clear existing guild data to ensure it's specific to the world being loaded
        GuildManager.clearGuildData();
        GuildStorageManager.clearGuildData();

        // Load guild data for the specific world
        GuildManager.loadGuildData();
        GuildStorageManager.loadGuildData();
        System.out.println("Guild and storage data loaded for world: " + worldSaveDir.getName());
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        String guildName = GuildManager.getGuild(player);
        if (guildName != null) {
            System.out.println("Player " + player.getName().getString() + " is in guild: " + guildName);
            GuildBuffManager.applyGuildBuffs(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        System.out.println("Player logged out: " + player.getName().getString());
    }

    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof Player attacker && event.getEntity() instanceof Player target) {
            if (GuildConfig.VALUES.friendlyFire.get()) {
                return; // Friendly fire is enabled, do nothing
            }
            if (GuildManager.arePlayersInSameGuild(attacker, target)) {
                event.setCanceled(true); // Cancel the event if the players are in the same guild and friendly fire is disabled
            }
        }
    }
    @SubscribeEvent
    public static void onPlayerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        if (GuildManager.isGuildChatEnabled(player)) {
            String guildName = GuildManager.getGuild(player);
            if (guildName != null) {
                event.setCanceled(true); // Cancel the default chat message
                sendGuildMessage(player, guildName, event.getMessage().getString());
            }
        }
    }
    private static void sendGuildMessage(ServerPlayer sender, String guildName, String message) {
        Set<ServerPlayer> guildMembers = GuildManager.getMembers(guildName, sender.getServer().getPlayerList().getPlayers());
        Component guildMessage = Component.literal("[" + guildName + " Guild] ")
                .withStyle(ChatFormatting.GREEN)
                .append(Component.literal("[" + sender.getName().getString() + "] ").withStyle(ChatFormatting.GOLD))
                .append(Component.literal(message).withStyle(ChatFormatting.WHITE));
        for (ServerPlayer member : guildMembers) {
            member.sendSystemMessage(guildMessage);
        }
    }
}

