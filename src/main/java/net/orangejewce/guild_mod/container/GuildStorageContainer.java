package net.orangejewce.guild_mod.container;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.orangejewce.guild_mod.GuildMod;
import net.orangejewce.guild_mod.guild.GuildManager;
import org.jetbrains.annotations.NotNull;

public class GuildStorageContainer extends AbstractContainerMenu {
    private final IItemHandler inventory;
    private static final int INVENTORY_SIZE = 54; // Double chest size
    private final String guildName;

    public GuildStorageContainer(int id, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(id, playerInventory, new ItemStackHandler(INVENTORY_SIZE), extraData.readUtf(32767));
    }

    public GuildStorageContainer(int id, Inventory playerInventory, IItemHandler inventory, String guildName) {
        super(GuildMod.GUILD_STORAGE_CONTAINER.get(), id);
        this.inventory = inventory;
        this.guildName = guildName;
        layoutContainer(inventory, 0);
        layoutPlayerInventorySlots(playerInventory);
    }

    public static GuildStorageContainer createMenu(int id, Inventory playerInventory, FriendlyByteBuf extraData) {
        String guildName = extraData.readUtf(32767);
        IItemHandler guildStorage = GuildManager.getGuildStorage(guildName);
        if (guildStorage == null) {
            // Handle error case, return a default or throw an exception
            return new GuildStorageContainer(id, playerInventory, new ItemStackHandler(INVENTORY_SIZE), guildName);
        }
        return new GuildStorageContainer(id, playerInventory, guildStorage, guildName);
    }

    private void layoutContainer(IItemHandler handler, int startIndex) {
        for (int row = 0; row < 6; row++) { // 6 rows for double chest
            for (int col = 0; col < 9; col++) { // 9 columns for double chest
                addSlot(new SlotItemHandler(handler, startIndex++, 8 + col * 18, 18 + row * 18));
            }
        }
    }

    private void layoutPlayerInventorySlots(Inventory playerInventory) {
        // Player inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 140 + row * 18));
            }
        }

        // Hotbar
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 140 + 58));
        }
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < this.inventory.getSlots()) {
                if (!this.moveItemStackTo(itemstack1, this.inventory.getSlots(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, this.inventory.getSlots(), false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, itemstack1);
        }

        GuildManager.saveGuildStorage(this.guildName, this.inventory);
        return itemstack;
    }

    public IItemHandler getInventory() {
        return this.inventory;
    }
}
