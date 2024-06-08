package net.orangejewce.guild_mod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;
import net.orangejewce.guild_mod.GuildMod;
import net.orangejewce.guild_mod.container.GuildStorageContainer;
import net.orangejewce.guild_mod.guild.GuildBuffManager;
import net.orangejewce.guild_mod.guild.GuildManager;
import net.orangejewce.guild_mod.guild.GuildStorageManager;

import java.util.Map;
import java.util.Set;

public class GuildCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("guild")
                .then(Commands.literal("create")
                        .then(Commands.argument("guildName", StringArgumentType.string())
                                .executes(context -> createGuild(context, StringArgumentType.getString(context, "guildName")))))
                .then(Commands.literal("join")
                        .then(Commands.argument("guildName", StringArgumentType.string())
                                .executes(context -> joinGuild(context, StringArgumentType.getString(context, "guildName")))))
                .then(Commands.literal("leave")
                        .executes(GuildCommand::leaveGuild))
                .then(Commands.literal("status")
                        .executes(GuildCommand::checkGuildStatus))
                .then(Commands.literal("invite")
                        .then(Commands.argument("playerName", StringArgumentType.string())
                                .executes(context -> invitePlayer(context, StringArgumentType.getString(context, "playerName"), context.getSource().getPlayerOrException().getName().getString()))))
                .then(Commands.literal("accept")
                        .then(Commands.argument("guildName", StringArgumentType.string())
                                .executes(context -> acceptInvite(context, StringArgumentType.getString(context, "guildName")))))
                .then(Commands.literal("deny")
                        .then(Commands.argument("guildName", StringArgumentType.string())
                                .executes(context -> denyInvite(context, StringArgumentType.getString(context, "guildName")))))
                .then(Commands.literal("list")
                        .executes(GuildCommand::listGuildMembers))
                .then(Commands.literal("leaderboard")
                        .executes(GuildCommand::showGuildLeaderboard))
                .then(Commands.literal("additem")
                        .executes(GuildCommand::addItemToStorage))
                .then(Commands.literal("addbuff")
                        .then(Commands.argument("effectName", StringArgumentType.string())
                                .then(Commands.argument("duration", IntegerArgumentType.integer())
                                        .then(Commands.argument("amplifier", IntegerArgumentType.integer())
                                                .executes(context -> addGuildBuff(context, StringArgumentType.getString(context, "effectName"), IntegerArgumentType.getInteger(context, "duration"), IntegerArgumentType.getInteger(context, "amplifier")))))))
                .then(Commands.literal("storage")
                        .executes(GuildCommand::openGuildStorage)));
    }

    private static int createGuild(CommandContext<CommandSourceStack> context, String guildName) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        GuildManager.createGuild(guildName, player);
        context.getSource().sendSuccess(() -> Component.literal("Guild created: " + guildName), false);
        return 1;
    }

    private static int joinGuild(CommandContext<CommandSourceStack> context, String guildName) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        if (GuildManager.joinGuild(guildName, player)) {
            context.getSource().sendSuccess(() -> Component.literal("Joined guild: " + guildName), false);
            return 1;
        } else {
            context.getSource().sendFailure(Component.literal("Guild not found: " + guildName));
            return 0;
        }
    }

    private static int leaveGuild(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        GuildManager.leaveCurrentGuild(player);
        context.getSource().sendSuccess(() -> Component.literal("Left the guild."), false);
        return 1;
    }

    private static int checkGuildStatus(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String guildName = GuildManager.getGuild(player);
        if (guildName != null) {
            context.getSource().sendSuccess(() -> Component.literal("You are in guild: " + guildName), false);
            return 1;
        } else {
            context.getSource().sendFailure(Component.literal("You are not in a guild."));
            return 0;
        }
    }

    private static int invitePlayer(CommandContext<CommandSourceStack> context, String playerName, String inviterName) throws CommandSyntaxException {
        ServerPlayer inviter = context.getSource().getPlayerOrException();
        ServerPlayer invitee = inviter.getServer().getPlayerList().getPlayerByName(playerName);
        if (invitee == null) {
            context.getSource().sendFailure(Component.literal("Player not found."));
            return 0;
        }

        String guildName = GuildManager.getGuild(inviter);
        if (guildName != null) {
            // Send invitation message
            Component message = Component.literal(inviterName + " has invited you to join the guild " + guildName + ". ")
                    .append(Component.literal("[Accept]").withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/guild accept " + guildName))))
                    .append(" ")
                    .append(Component.literal("[Deny]").withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/guild deny " + guildName))));
            invitee.sendSystemMessage(message);

            context.getSource().sendSuccess(() -> Component.literal("Invitation sent to " + invitee.getName().getString()), false);
            return 1;
        } else {
            context.getSource().sendFailure(Component.literal("You are not in a guild."));
            return 0;
        }
    }

    private static int acceptInvite(CommandContext<CommandSourceStack> context, String guildName) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        if (GuildManager.joinGuild(guildName, player)) {
            context.getSource().sendSuccess(() -> Component.literal("Joined guild: " + guildName), false);
            return 1;
        } else {
            context.getSource().sendFailure(Component.literal("Failed to join guild."));
            return 0;
        }
    }

    private static int denyInvite(CommandContext<CommandSourceStack> context, String guildName) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        context.getSource().sendSuccess(() -> Component.literal("Denied invitation to join guild: " + guildName), false);
        return 1;
    }

    private static int listGuildMembers(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String guildName = GuildManager.getGuild(player);
        if (guildName != null) {
            Set<ServerPlayer> members = GuildManager.getMembers(guildName, player.getServer().getPlayerList().getPlayers());

            // Create the base message
            MutableComponent message = Component.literal("Guild members: ");

            // Append each member's name with light blue color
            for (ServerPlayer member : members) {
                Component memberName = Component.literal(member.getName().getString()).withStyle(style -> style.withColor(TextColor.fromRgb(0x55FFFF)));
                message.append(memberName).append(Component.literal(", "));
            }

            // Remove the trailing comma and space
            if (!members.isEmpty()) {
                message.getSiblings().remove(message.getSiblings().size() - 1);
            }

            context.getSource().sendSuccess(() -> message, false);
            return 1;
        } else {
            context.getSource().sendFailure(Component.literal("You are not in a guild."));
            return 0;
        }
    }

    private static int showGuildLeaderboard(CommandContext<CommandSourceStack> context) {
        Map<String, String> guildOwners = GuildManager.getGuildOwners();
        MutableComponent message = Component.literal("Guild Leaderboard:\n");

        for (Map.Entry<String, String> entry : guildOwners.entrySet()) {
            String guildName = entry.getKey();
            String ownerName = entry.getValue();
            message.append(Component.literal(guildName + " (Owner: " + ownerName + ")\n").withStyle(style -> style.withColor(TextColor.fromRgb(0x00FF00))));
        }

        context.getSource().sendSuccess(() -> message, false);
        return 1;
    }

    private static int addItemToStorage(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String guildName = GuildManager.getGuild(player);
        if (guildName != null) {
            ItemStack itemStack = player.getMainHandItem();
            GuildStorageManager.addItemsToStorage(player, guildName, itemStack);
            return 1;
        } else {
            context.getSource().sendFailure(Component.literal("You are not in a guild."));
            return 0;
        }
    }

    private static int addGuildBuff(CommandContext<CommandSourceStack> context, String effectName, int duration, int amplifier) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String guildName = GuildManager.getGuild(player);
        if (guildName != null) {
            MobEffectInstance effect;
            switch (effectName.toLowerCase()) {
                case "speed":
                    effect = new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, amplifier);
                    break;
                case "strength":
                    effect = new MobEffectInstance(MobEffects.DAMAGE_BOOST, duration, amplifier);
                    break;
                case "regeneration":
                    effect = new MobEffectInstance(MobEffects.REGENERATION, duration, amplifier);
                    break;
                // Add more cases for other effects
                default:
                    context.getSource().sendFailure(Component.literal("Invalid effect name."));
                    return 0;
            }
            GuildBuffManager.addGuildBuff(guildName, effect);
            context.getSource().sendSuccess(() -> Component.literal("Buff added to guild: " + effectName), false);
            return 1;
        } else {
            context.getSource().sendFailure(Component.literal("You are not in a guild."));
            return 0;
        }
    }

    private static int openGuildStorage(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String guildName = "default"; // Retrieve the actual guild name of the player here
        MenuProvider containerProvider = new SimpleMenuProvider(
                (id, playerInventory, playerEntity) -> new GuildStorageContainer(id, playerInventory, GuildManager.getGuildStorage(guildName), guildName),
                Component.literal("Guild Storage")
        );
        NetworkHooks.openScreen(player, containerProvider, buf -> buf.writeUtf(guildName));
        return 1;
    }
}

