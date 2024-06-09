package net.orangejewce.guild_mod.events;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.client.event.RenderPlayerEvent;
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

@Mod.EventBusSubscriber(modid = GuildMod.MOD_ID)
public class GuildModEventHandler {

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        MinecraftServer server = event.getServer();
        File worldSaveDir = server.getWorldPath(LevelResource.ROOT).toFile();
        GuildManager.setWorldSaveDirectory(worldSaveDir);

        GuildManager.loadGuildData();
        GuildStorageManager.loadGuildData();
        System.out.println("Guild and storage data loaded.");
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
    public static void onRenderPlayer(RenderPlayerEvent.Post event) {
        AbstractClientPlayer player = (AbstractClientPlayer) event.getEntity();
        if (player.isCrouching() || !GuildConfig.VALUES.showGuildNames.get()) {
            return;
        }
        String guildName = GuildManager.getGuild(player);
        if (guildName != null) {
            PoseStack poseStack = event.getPoseStack();
            poseStack.pushPose();

            double yOffset = player.getBbHeight() + 0.8;
            poseStack.translate(0.0, yOffset, 0.0);
            poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
            poseStack.scale(-0.025F, -0.025F, 0.025F);

            MultiBufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
            int packedLight = Minecraft.getInstance().getEntityRenderDispatcher().getPackedLightCoords(player, event.getPartialTick());
            float backgroundOpacity = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
            int backgroundColor = (int) (backgroundOpacity * 255.0F) << 24;

            int guildNameWidth = Minecraft.getInstance().font.width(guildName) / 2;
            Minecraft.getInstance().font.drawInBatch(guildName, -guildNameWidth, 0, Objects.requireNonNull(TextColor.parseColor("#FFD700")).getValue(), false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, backgroundColor, packedLight);

            poseStack.popPose();
        }
    }
}

