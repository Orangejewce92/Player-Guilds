package net.orangejewce.guild_mod.events;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.TextColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.orangejewce.guild_mod.GuildMod;
import net.orangejewce.guild_mod.config.GuildConfig;
import net.orangejewce.guild_mod.guild.GuildManager;

@Mod.EventBusSubscriber(modid = GuildMod.MOD_ID, value = Dist.CLIENT)
public class ClientEventSubscriber {

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

            // Retrieve the color from the GuildManager
            ChatFormatting guildColor = GuildManager.getGuildColor(guildName);
            int colorValue = guildColor.getColor() != null ? guildColor.getColor() : TextColor.fromLegacyFormat(ChatFormatting.YELLOW).getValue();

            int guildNameWidth = Minecraft.getInstance().font.width(guildName) / 2;
            Minecraft.getInstance().font.drawInBatch(guildName, -guildNameWidth, 0, colorValue, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, backgroundColor, packedLight);

            poseStack.popPose();
        }
    }
}
