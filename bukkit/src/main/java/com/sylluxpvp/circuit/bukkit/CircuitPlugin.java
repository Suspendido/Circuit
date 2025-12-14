package com.sylluxpvp.circuit.bukkit;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.PaperCommandManager;
import com.sylluxpvp.circuit.bukkit.api.CircuitAPI;
import com.sylluxpvp.circuit.bukkit.placeholder.CircuitExpansion;
import com.sylluxpvp.circuit.bukkit.redis.AdminChatListener;
import com.sylluxpvp.circuit.bukkit.redis.MessageListener;
import com.sylluxpvp.circuit.bukkit.redis.PunishmentUpdateListener;
import com.sylluxpvp.circuit.bukkit.redis.ServerCommandListener;
import com.sylluxpvp.circuit.bukkit.redis.ServerStatusListener;
import com.sylluxpvp.circuit.bukkit.redis.StaffChatListener;
import com.sylluxpvp.circuit.bukkit.redis.StaffStatusListener;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.spigotmc.SpigotConfig;
import com.sylluxpvp.circuit.bukkit.listener.FreezeListener;
import com.sylluxpvp.circuit.bukkit.listener.PlayerListener;
import com.sylluxpvp.circuit.bukkit.listener.ServerListener;
import com.sylluxpvp.circuit.bukkit.service.BukkitChatService;
import com.sylluxpvp.circuit.bukkit.service.BukkitGrantService;
import com.sylluxpvp.circuit.bukkit.service.BukkitProfileService;
import com.sylluxpvp.circuit.bukkit.task.GrantDurationTask;
import com.sylluxpvp.circuit.bukkit.tools.menu.MenuListener;
import com.sylluxpvp.circuit.bukkit.tools.spigot.ConfigUtil;
import com.sylluxpvp.circuit.bukkit.tools.spigot.TaskUtil;
import com.sylluxpvp.circuit.bukkit.tools.circuit.CircuitBukkitLogger;
import com.sylluxpvp.circuit.shared.CircuitShared;
import com.sylluxpvp.circuit.shared.credentials.MongoCredentials;
import com.sylluxpvp.circuit.shared.credentials.RedisCredentials;
import com.sylluxpvp.circuit.shared.gift.GiftCode;
import com.sylluxpvp.circuit.shared.rank.Rank;
import com.sylluxpvp.circuit.shared.redis.packets.misc.MessagePacket;
import com.sylluxpvp.circuit.shared.redis.packets.punish.PunishmentUpdatePacket;
import com.sylluxpvp.circuit.shared.redis.packets.server.ServerCommandPacket;
import com.sylluxpvp.circuit.shared.redis.packets.server.ServerStatusPacket;
import com.sylluxpvp.circuit.shared.redis.packets.staff.AdminChatPacket;
import com.sylluxpvp.circuit.shared.redis.packets.staff.StaffChatPacket;
import com.sylluxpvp.circuit.shared.redis.packets.staff.StaffStatusPacket;
import com.sylluxpvp.circuit.shared.server.Server;
import com.sylluxpvp.circuit.shared.server.ServerType;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.GiftService;
import com.sylluxpvp.circuit.shared.service.impl.RankService;
import com.sylluxpvp.circuit.shared.service.impl.ServerService;
import com.sylluxpvp.circuit.shared.tools.java.ClassUtils;
import com.sylluxpvp.circuit.shared.tools.string.CC;

import java.io.File;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
public class CircuitPlugin extends JavaPlugin {

    @Getter
    private static CircuitPlugin instance;

    private YamlConfiguration mainConfig, filterConfig;
    private CircuitShared shared;
    private CircuitAPI api;

    private boolean joinable = false;
    @Getter @Setter private boolean chatMuted = false;
    @Getter @Setter private int chatSlowTime = 0;

    @Override
    public void onEnable() {
        instance = this;
        if (Bukkit.getServer().getOnlineMode() && SpigotConfig.bungee || !Bukkit.getServer().getOnlineMode() && !SpigotConfig.bungee) {
            Bukkit.getConsoleSender().sendMessage(CC.translate("&c** --------------------------------------- **"));
            Bukkit.getConsoleSender().sendMessage(CC.translate("&c&lSERVER CONFIGURATION ISSUE!"));
            Bukkit.getConsoleSender().sendMessage(CC.translate("&cSomething is wrong with your Bukkit config."));
            Bukkit.getConsoleSender().sendMessage(CC.translate("&cYou probably did one of the following:"));
            Bukkit.getConsoleSender().sendMessage(CC.translate(""));
            Bukkit.getConsoleSender().sendMessage(CC.translate("&c1. Enabled bungee on spigot.yml, and left"));
            Bukkit.getConsoleSender().sendMessage(CC.translate("&c  online-mode enabled on server.properties"));
            Bukkit.getConsoleSender().sendMessage(CC.translate(""));
            Bukkit.getConsoleSender().sendMessage(CC.translate("&c2. Disabled bungee on spigot.yml, and left"));
            Bukkit.getConsoleSender().sendMessage(CC.translate("&c  online-mode disabled on server.properties"));
            Bukkit.getConsoleSender().sendMessage(CC.translate(""));
            Bukkit.getConsoleSender().sendMessage(CC.translate("&cEither way, Circuit &c&lDOES NOT &csupport"));
            Bukkit.getConsoleSender().sendMessage(CC.translate("&cthis type of server configuration. Please fix"));
            Bukkit.getConsoleSender().sendMessage(CC.translate("&cyour configurations immediately!"));
            Bukkit.getConsoleSender().sendMessage(CC.translate("&c** --------------------------------------- **"));
            TaskUtil.runTaskLater(() -> {
                System.exit(0);
            }, 1L);
            return;
        }
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
        this.setupCommands();
        this.setupServices();
        this.setupTasks();
        this.setupListeners();
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
    }

    private void setupCommands() {
        PaperCommandManager manager = new PaperCommandManager(this);
        manager.enableUnstableAPI("help");
        manager.getCommandContexts().registerContext(Rank.class, c -> {
            String input = c.popFirstArg();
            Rank rank = ServiceContainer.getService(RankService.class).getRank(input);
            if (rank == null) throw new InvalidCommandArgument("Rank not found!");
            return rank;
        });
        manager.getCommandContexts().registerContext(Server.class, c -> {
            String input = c.popFirstArg();
            Optional<Server> server = ServiceContainer.getService(ServerService.class).find(input);
            if (!server.isPresent()) throw new InvalidCommandArgument("Server not found!");
            return server.get();
        });

        manager.getCommandCompletions().registerCompletion("ranks", c -> ServiceContainer.getService(RankService.class).getRanks().stream().map(Rank::getName).collect(Collectors.toList()));
        manager.getCommandCompletions().registerCompletion("servers", c -> ServiceContainer.getService(ServerService.class).getServers().stream().map(Server::getName).collect(Collectors.toList()));
        manager.getCommandCompletions().registerCompletion("giftcodes", c -> ServiceContainer.getService(GiftService.class).getCache().values().stream().map(GiftCode::getCode).collect(Collectors.toList()));
        manager.getCommandCompletions().registerCompletion("times", c -> java.util.Arrays.asList(
                "perm", "permanent",
                "1m", "5m", "10m", "30m",
                "1h", "6h", "12h",
                "1d", "7d", "30d"
        ));
        ClassUtils.getClasses(getFile(), this.getClass().getPackage().getName() + ".command").stream().filter(c -> !c.getName().contains("$")).forEach(c -> {
            try {
                manager.registerCommand((BaseCommand) c.newInstance());
            } catch (Exception exception) {
                this.getLogger().info("Error while loading the command " + c.getSimpleName());
                exception.printStackTrace();
            }
        });

    }

    private void setupServices() {
        ServiceContainer.registerService(new BukkitChatService());
        ServiceContainer.registerService(new BukkitProfileService());
        ServiceContainer.registerService(new BukkitGrantService());
    }

    private void setupTasks() {
        new GrantDurationTask();
    }

    private void setupListeners() {
        this.shared.getRedis().registerListener(new MessagePacket(), new MessageListener());
        this.shared.getRedis().registerListener(new ServerStatusPacket(), new ServerStatusListener());
        this.shared.getRedis().registerListener(new PunishmentUpdatePacket(), new PunishmentUpdateListener());
        this.shared.getRedis().registerListener(new ServerCommandPacket(), new ServerCommandListener());
        this.shared.getRedis().registerListener(new StaffStatusPacket(), new StaffStatusListener());
        this.shared.getRedis().registerListener(new StaffChatPacket(), new StaffChatListener());
        this.shared.getRedis().registerListener(new AdminChatPacket(), new AdminChatListener());
        this.getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        this.getServer().getPluginManager().registerEvents(new ServerListener(), this);
        this.getServer().getPluginManager().registerEvents(new MenuListener(), this);
        this.getServer().getPluginManager().registerEvents(new FreezeListener(), this);
        
        // Register PlaceholderAPI expansion if available
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new CircuitExpansion().register();
            this.getLogger().info("PlaceholderAPI expansion registered!");
        }
    }

    @Override
    public void onDisable() {
        this.shared.getLogger().log("Shutting down Circuit!");
        this.shared.shutdown();
    }
}
