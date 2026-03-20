package com.sylluxpvp.circuit.bukkit;

import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.PaperCommandManager;
import com.sylluxpvp.circuit.bukkit.api.CircuitAPI;
import com.sylluxpvp.circuit.bukkit.hook.HookManager;
import com.sylluxpvp.circuit.bukkit.module.ModuleManager;
import com.sylluxpvp.circuit.bukkit.module.impl.*;
import com.sylluxpvp.circuit.bukkit.profile.BukkitProfile;
import com.sylluxpvp.circuit.bukkit.tools.xenon.CircuitBukkitLogger;
import com.sylluxpvp.circuit.shared.profile.Profile;
import com.sylluxpvp.circuit.shared.service.impl.ProfileService;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.java.JavaPlugin;
import com.sylluxpvp.circuit.bukkit.tools.spigot.ConfigUtil;
import com.sylluxpvp.circuit.shared.CircuitShared;
import com.sylluxpvp.circuit.shared.credentials.MongoCredentials;
import com.sylluxpvp.circuit.shared.credentials.RedisCredentials;
import com.sylluxpvp.circuit.shared.rank.Rank;
import com.sylluxpvp.circuit.shared.redis.packets.server.ServerDiscoveryPacket;
import com.sylluxpvp.circuit.shared.redis.packets.server.ServerUpdatePacket;
import com.sylluxpvp.circuit.shared.server.Server;
import com.sylluxpvp.circuit.shared.server.ServerType;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.QueueService;
import com.sylluxpvp.circuit.shared.service.impl.RankService;
import com.sylluxpvp.circuit.shared.service.impl.ServerService;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
public class CircuitPlugin extends JavaPlugin {

    @Getter
    private static CircuitPlugin instance;

    private YamlConfiguration mainConfig, filterConfig;
    private CircuitShared shared;
    private CircuitAPI api;
    private HookManager hookManager;
    private ModuleManager moduleManager;
    private PaperCommandManager commandManager;

    private boolean joinable = false;
    @Getter @Setter private boolean chatMuted = false;
    @Getter @Setter private int chatSlowTime = 0;

    public File getPluginFile() {
        return this.getFile();
    }

    @Override
    public void onEnable() {
        instance = this;
        this.hookManager = new HookManager();
        this.moduleManager = new ModuleManager();
        mainConfig = ConfigUtil.createConfig("config");
        filterConfig = ConfigUtil.createConfig(new File(this.getDataFolder(), "modules"), "filter");
        String redisAddress = mainConfig.getString("redis.address");
        int redisPort = mainConfig.getInt("redis.port");
        if (redisAddress == null || redisPort == 0) {
            this.getLogger().info("Redis configuration is broken, please fix your config.yml!");
            System.exit(0);
        }

        RedisCredentials redisCredentials;
        if (mainConfig.getBoolean("redis.auth.enabled")) {
            String redisPassword = mainConfig.getString("redis.auth.password");
            redisCredentials = new RedisCredentials(redisAddress, redisPort, redisPassword);
        } else {
            redisCredentials = new RedisCredentials(redisAddress, redisPort);
        }

        String mongoIp = mainConfig.getString("mongo.ip");
        int mongoPort = mainConfig.getInt("mongo.port");
        String databaseName = mainConfig.getString("mongo.database");

        if (mongoIp == null || mongoPort == 0 || databaseName == null) {
            this.getLogger().info("MongoDB configuration is broken, please fix your config.yml!");
            System.exit(0);
        }

        MongoCredentials mongoCredentials;
        if (mainConfig.getBoolean("mongo.auth.enabled")) {
            String mongoUsername = mainConfig.getString("mongo.auth.username");
            String mongoPassword = mainConfig.getString("mongo.auth.password");
            mongoCredentials = new MongoCredentials(mongoIp, mongoPort, mongoUsername, mongoPassword, databaseName);
        } else {
            mongoCredentials = new MongoCredentials(mongoIp, mongoPort);
        }

        shared = new CircuitShared(new CircuitBukkitLogger(), redisCredentials, mongoCredentials, databaseName);
        this.api = new CircuitAPI();
        this.setupCommandManager();
        this.setupModules();
        this.setupServer();
    }

    private void setupServer() {
        if (this.mainConfig.getString("server.name") == null || this.mainConfig.getString("server.type") == null) {
            this.shared.getLogger().log("Server configuration is broken, please fix your config.yml!");
            System.exit(0);
        }

        if (this.mainConfig.getString("server.name").equalsIgnoreCase("unconfigured") && this.mainConfig.getString("server.type").equalsIgnoreCase("default")) {
            this.shared.getLogger().log("Please configure the server in your config.yml!");
            System.exit(0);
        }

        Optional<Server> opt = ServiceContainer.getService(ServerService.class).find(this.mainConfig.getString("server.name"));
        if (opt.isPresent() && opt.get().isOnline()) {
            this.shared.getLogger().log("A Circuit server with the name " + this.mainConfig.getString("server.name") + " already exists!");
            this.shared.getLogger().log("Please assert you're not already running a server instance with this configuration...");
            System.exit(0);
        }

        this.joinable = true;
        Server server = opt.orElseGet(() -> new Server(this.mainConfig.getString("server.name"), ServerType.valueOf(this.mainConfig.getString("server.type").toUpperCase())));
        server.setOnline(true);
        server.setWhitelisted(Bukkit.hasWhitelist());
        server.setMax(Bukkit.getMaxPlayers());
        this.shared.setServer(server);

        ServiceContainer.getService(RankService.class).setOnRankUpdate(rank -> {
            ProfileService profileService = ServiceContainer.getService(ProfileService.class);
            if (profileService == null) return;

            Bukkit.getScheduler().runTask(CircuitPlugin.getInstance(), () -> {
                for (Profile profile : profileService.getOnlineProfiles()) {
                    if (!profile.hasRank(rank)) continue;
                    Player player = Bukkit.getPlayer(profile.getUUID());
                    if (player == null || !player.isOnline()) continue;
                    BukkitProfile.applyPermissions(player, profile);
                }
            });
        });
        
        // Send our own info immediately (without status alert - setServer already sends it)
        this.shared.getRedis().sendPacket(new ServerUpdatePacket(
                server.getName(), server.getType().name(), server.isOnline(), server.isWhitelisted(),
                false, server.getPlayers(), server.getMax(), server.getWhitelistRank(), server.getWhitelistedPlayers()
        ));
        
        // Request other servers to send their info (without status alerts)
        this.shared.getRedis().sendPacket(new ServerDiscoveryPacket(server.getName()));
    }

    private void setupCommandManager() {
        this.commandManager = new PaperCommandManager(this);
        this.commandManager.enableUnstableAPI("help");
        this.commandManager.getCommandContexts().registerContext(Rank.class, c -> {
            String input = c.popFirstArg();
            Rank rank = ServiceContainer.getService(RankService.class).getRank(input);
            if (rank == null) {
                throw new InvalidCommandArgument("Rank not found!");
            }
            return rank;
        });
        this.commandManager.getCommandContexts().registerContext(Server.class, c -> {
            String input = c.popFirstArg();
            Optional<Server> server = ServiceContainer.getService(ServerService.class).find(input);
            if (!server.isPresent()) {
                throw new InvalidCommandArgument("Server not found!");
            }
            return server.get();
        });
        this.commandManager.getCommandContexts().registerContext(GameMode.class, c -> switch (c.popFirstArg().toLowerCase()) {
            case "0", "s", "survival" -> GameMode.SURVIVAL;
            case "1", "c", "creative" -> GameMode.CREATIVE;
            case "2", "a", "adventure" -> GameMode.ADVENTURE;
            case "3", "sp", "spectator" -> GameMode.SPECTATOR;
            default -> throw new InvalidCommandArgument("Please specify one of the following: CREATIVE, SURVIVAL, ADVENTURE, SPECTATOR.");
        });
        this.commandManager.getCommandCompletions().registerCompletion("gamemodes", c -> Arrays.asList("survival", "creative", "adventure", "spectator", "0", "1", "2", "3", "s", "c", "a", "sp"));
        this.commandManager.getCommandCompletions().registerCompletion("ranks", c -> ServiceContainer.getService(RankService.class).getRanks().stream().map(Rank::getName).collect(Collectors.toList()));
        this.commandManager.getCommandCompletions().registerCompletion("servers", c -> ServiceContainer.getService(ServerService.class).getServers().stream().map(Server::getName).collect(Collectors.toList()));
        this.commandManager.getCommandCompletions().registerCompletion("queues", c -> ServiceContainer.getService(QueueService.class).getQueues().values().stream().map(q -> q.getServerName()).collect(Collectors.toList()));
        this.commandManager.getCommandCompletions().registerCompletion("times", c -> Arrays.asList("perm", "permanent", "1m", "5m", "10m", "30m", "1h", "6h", "12h", "1d", "7d", "30d"));
    }


    private void setupModules() {
        this.moduleManager = new ModuleManager();
        this.moduleManager.registerModule(new CoreModule());
        this.moduleManager.registerModule(new RankModule());
        this.moduleManager.registerModule(new EssentialsModule());
        this.moduleManager.registerModule(new PlayerModule());
        this.moduleManager.registerModule(new ServerModule());
        this.moduleManager.registerModule(new PunishmentModule());
        this.moduleManager.registerModule(new StaffModule());
        this.moduleManager.registerModule(new QueueModule());
        this.moduleManager.loadModuleStates(this.mainConfig);
        this.moduleManager.enableModules();
    }


    @Override
    public void onDisable() {
        this.shared.getLogger().log("Shutting down Circuit!");
        this.shared.shutdown();
    }
}
