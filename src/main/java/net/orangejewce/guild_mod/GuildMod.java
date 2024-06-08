package net.orangejewce.guild_mod;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.orangejewce.guild_mod.command.GuildCommand;
import net.orangejewce.guild_mod.config.GuildConfig;
import net.orangejewce.guild_mod.container.GuildStorageContainer;
import net.orangejewce.guild_mod.screen.GuildStorageScreen;
import net.orangejewce.guild_mod.events.GuildModEventHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(GuildMod.MOD_ID)
public class GuildMod {
    public static final String MOD_ID = "guild_mod";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MOD_ID);
    public static final RegistryObject<MenuType<GuildStorageContainer>> GUILD_STORAGE_CONTAINER = MENU_TYPES.register("guild_storage_container",
            () -> IForgeMenuType.create(GuildStorageContainer::new));

    public GuildMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::doClientStuff);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, GuildConfig.COMMON_CONFIG);

        MENU_TYPES.register(modEventBus);

        // Register the event handler on the MinecraftForge event bus
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new GuildModEventHandler());
    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("Setting up mod");
        // Additional setup code here
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        LOGGER.info("Setting up client stuff");
        // Client-side initialization code
        MenuScreens.register(GUILD_STORAGE_CONTAINER.get(), GuildStorageScreen::new);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        LOGGER.info("Registering commands");
        GuildCommand.register(event.getDispatcher());
    }
}
