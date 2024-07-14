package net.orangejewce.guild_mod;

import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraft.client.gui.screens.MenuScreens;
import net.orangejewce.guild_mod.screen.GuildStorageScreen;

public class ClientSetup {
    public static void init(FMLClientSetupEvent event) {
        MenuScreens.register(GuildMod.GUILD_STORAGE_CONTAINER.get(), GuildStorageScreen::new);
    }
}
