package net.orangejewce.guild_mod.guild;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import java.util.HashMap;
import java.util.Map;

public class GuildStorageManager {
    private static final Map<String, IItemHandler> guildStorages = new HashMap<>();

    public static void setGuildStorage(String guildName, BlockPos pos) {
        IItemHandler storage = new ItemStackHandler(27); // Example with 27 slots
        guildStorages.put(guildName, storage);
        GuildManager.saveGuildData(); // Save storage location if necessary
    }

    public static IItemHandler getGuildStorage(String guildName) {
        return guildStorages.get(guildName);
    }

    public static void addItemsToStorage(ServerPlayer player, String guildName, ItemStack itemStack) {
        IItemHandler storage = getGuildStorage(guildName);
        if (storage != null) {
            for (int i = 0; i < storage.getSlots(); i++) {
                if (storage.insertItem(i, itemStack, false).isEmpty()) {
                    player.getInventory().removeItem(itemStack);
                    break;
                }
            }
        }
    }

    public static void loadGuildData() {
        // Implementation to load guild storage data
    }
    public static void clearGuildData() {
        guildStorages.clear();
    }
}
