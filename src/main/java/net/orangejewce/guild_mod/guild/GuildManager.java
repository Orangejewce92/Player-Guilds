package net.orangejewce.guild_mod.guild;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GuildManager {
    public static final String OWNER = "Owner";
    public static final String OFFICER = "Officer";
    public static final String MEMBER = "Member";
    public static final String RECRUIT = "Recruit";

    private static final Map<String, Set<String>> guildMembers = new HashMap<>();
    private static final Map<String, String> playerGuilds = new HashMap<>();
    private static final Map<String, String> guildOwners = new HashMap<>();
    private static final Map<String, String> ownerNames = new HashMap<>();
    private static Map<String, Map<String, String>> guildRanks = new HashMap<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static File dataFile;
    private static final Map<String, ItemStackHandler> guildStorages = new HashMap<>();

    public static void setWorldSaveDirectory(File worldSaveDir) {
        dataFile = new File(worldSaveDir, "guilds/guilds.json");
    }

    public static ItemStackHandler getGuildStorage(String guildName) {
        return guildStorages.computeIfAbsent(guildName, k -> new ItemStackHandler(54));
    }

    public static void saveGuildStorage(String guildName, IItemHandler inventory) {
        if (inventory instanceof ItemStackHandler) {
            guildStorages.put(guildName, (ItemStackHandler) inventory);
            // Here you should add logic to persist the storage to disk
        }
    }

    public static void createGuild(String guildName, ServerPlayer owner) {
        if (!guildMembers.containsKey(guildName)) {
            guildMembers.put(guildName, new HashSet<>());
            guildOwners.put(guildName, owner.getStringUUID());
            ownerNames.put(guildName, owner.getName().getString());
            guildRanks.put(guildName, new HashMap<>());
            guildRanks.get(guildName).put(owner.getStringUUID(), OWNER);
            joinGuild(guildName, owner);
            saveGuildData();
        }
    }

    public static boolean joinGuild(String guildName, ServerPlayer player) {
        guildMembers.computeIfAbsent(guildName, k -> new HashSet<>());
        leaveCurrentGuild(player);
        guildMembers.get(guildName).add(player.getStringUUID());
        playerGuilds.put(player.getStringUUID(), guildName);
        if (!guildRanks.containsKey(guildName)) {
            guildRanks.put(guildName, new HashMap<>());
        }
        guildRanks.get(guildName).put(player.getStringUUID(), RECRUIT);
        saveGuildData();
        return true;
    }

    public static void leaveCurrentGuild(ServerPlayer player) {
        String playerUUID = player.getStringUUID();
        String guildName = playerGuilds.remove(playerUUID);
        if (guildName != null) {
            Set<String> members = guildMembers.get(guildName);
            if (members != null) {
                members.remove(playerUUID);
                guildRanks.get(guildName).remove(playerUUID);
                if (members.isEmpty()) {
                    guildMembers.remove(guildName);
                    guildOwners.remove(guildName);
                    ownerNames.remove(guildName);
                    guildRanks.remove(guildName);
                }
                saveGuildData();
            }
        }
    }

    public static String getGuild(Player player) {
        return playerGuilds.get(player.getStringUUID());
    }

    public static Set<ServerPlayer> getMembers(String guildName, Iterable<ServerPlayer> players) {
        Set<ServerPlayer> members = new HashSet<>();
        Set<String> memberUUIDs = guildMembers.get(guildName);
        if (memberUUIDs != null) {
            for (ServerPlayer player : players) {
                if (memberUUIDs.contains(player.getStringUUID())) {
                    members.add(player);
                }
            }
        }
        return members;
    }

    public static String getRank(ServerPlayer player) {
        String guildName = getGuild(player);
        if (guildName != null && guildRanks.get(guildName) != null) {
            return guildRanks.get(guildName).get(player.getStringUUID());
        }
        return null;
    }

    public static void promoteMember(String guildName, ServerPlayer player, String newRank) {
        guildRanks.get(guildName).put(player.getStringUUID(), newRank);
        saveGuildData();
    }

    public static void demoteMember(String guildName, ServerPlayer player, String newRank) {
        guildRanks.get(guildName).put(player.getStringUUID(), newRank);
        saveGuildData();
    }

    public static void saveGuildData() {
        if (dataFile == null) {
            return; // World directory is not set
        }
        try {
            // Ensure the directories exist
            File dir = dataFile.getParentFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            try (FileWriter writer = new FileWriter(dataFile)) {
                Map<String, Object> data = new HashMap<>();
                data.put("guildMembers", guildMembers);
                data.put("playerGuilds", playerGuilds);
                data.put("guildOwners", guildOwners);
                data.put("ownerNames", ownerNames);
                data.put("guildRanks", guildRanks);
                GSON.toJson(data, writer);
                System.out.println("Guild data saved.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadGuildData() {
        if (dataFile == null || !dataFile.exists()) {
            return; // World directory is not set or file does not exist
        }
        try (FileReader reader = new FileReader(dataFile)) {
            Type dataType = new TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> data = GSON.fromJson(reader, dataType);

            if (data != null) {
                if (data.get("guildMembers") != null) {
                    Type guildMembersType = new TypeToken<Map<String, Set<String>>>() {}.getType();
                    guildMembers.clear();
                    guildMembers.putAll(GSON.fromJson(GSON.toJson(data.get("guildMembers")), guildMembersType));
                }

                if (data.get("playerGuilds") != null) {
                    Type playerGuildsType = new TypeToken<Map<String, String>>() {}.getType();
                    playerGuilds.clear();
                    playerGuilds.putAll(GSON.fromJson(GSON.toJson(data.get("playerGuilds")), playerGuildsType));
                }

                if (data.get("guildOwners") != null) {
                    Type guildOwnersType = new TypeToken<Map<String, String>>() {}.getType();
                    guildOwners.clear();
                    guildOwners.putAll(GSON.fromJson(GSON.toJson(data.get("guildOwners")), guildOwnersType));
                }

                if (data.get("ownerNames") != null) {
                    Type ownerNamesType = new TypeToken<Map<String, String>>() {}.getType();
                    ownerNames.clear();
                    ownerNames.putAll(GSON.fromJson(GSON.toJson(data.get("ownerNames")), ownerNamesType));
                }

                if (data.get("guildRanks") != null) {
                    Type guildRanksType = new TypeToken<Map<String, Map<String, String>>>() {}.getType();
                    guildRanks.clear();
                    guildRanks.putAll(GSON.fromJson(GSON.toJson(data.get("guildRanks")), guildRanksType));
                } else {
                    guildRanks = new HashMap<>();
                }
            }

            System.out.println("Guild data loaded.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, Map<String, String>> getGuildRanks() {
        return guildRanks;
    }

    public static Map<String, String> getGuildOwners() {
        return guildOwners;
    }

    public static Map<String, String> getOwnerNames() {
        return ownerNames;
    }

    public static boolean arePlayersInSameGuild(Player player1, Player player2) {
        String guild1 = getGuildName(player1);
        String guild2 = getGuildName(player2);
        return guild1 != null && guild1.equals(guild2);
    }

    private static String getGuildName(Player player) {
        return playerGuilds.get(player.getStringUUID());
    }
}
