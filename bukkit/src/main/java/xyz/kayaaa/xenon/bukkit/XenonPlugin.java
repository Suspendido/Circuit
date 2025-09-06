package xyz.kayaaa.xenon.bukkit;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.PaperCommandManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.spigotmc.SpigotConfig;
import xyz.kayaaa.xenon.bukkit.listener.PlayerListener;
import xyz.kayaaa.xenon.bukkit.listener.ServerListener;
import xyz.kayaaa.xenon.bukkit.redis.*;
import xyz.kayaaa.xenon.bukkit.service.BukkitChatService;
import xyz.kayaaa.xenon.bukkit.service.BukkitGrantService;
import xyz.kayaaa.xenon.bukkit.service.BukkitProfileService;
import xyz.kayaaa.xenon.bukkit.task.GrantDurationTask;
import xyz.kayaaa.xenon.bukkit.tools.menu.MenuListener;
import xyz.kayaaa.xenon.bukkit.tools.spigot.ConfigUtil;
import xyz.kayaaa.xenon.bukkit.tools.spigot.TaskUtil;
import xyz.kayaaa.xenon.bukkit.tools.xenon.XenonBukkitLogger;
import xyz.kayaaa.xenon.shared.XenonShared;
import xyz.kayaaa.xenon.shared.credentials.MongoCredentials;
import xyz.kayaaa.xenon.shared.credentials.RedisCredentials;
import xyz.kayaaa.xenon.shared.gift.GiftCode;
import xyz.kayaaa.xenon.shared.rank.Rank;
import xyz.kayaaa.xenon.shared.redis.packets.misc.MessagePacket;
import xyz.kayaaa.xenon.shared.redis.packets.punish.PunishmentUpdatePacket;
import xyz.kayaaa.xenon.shared.redis.packets.server.ServerCommandPacket;
import xyz.kayaaa.xenon.shared.redis.packets.server.ServerStatusPacket;
import xyz.kayaaa.xenon.shared.redis.packets.staff.StaffChatPacket;
import xyz.kayaaa.xenon.shared.redis.packets.staff.StaffStatusPacket;
import xyz.kayaaa.xenon.shared.server.Server;
import xyz.kayaaa.xenon.shared.server.ServerType;
import xyz.kayaaa.xenon.shared.service.ServiceContainer;
import xyz.kayaaa.xenon.shared.service.impl.GiftService;
import xyz.kayaaa.xenon.shared.service.impl.RankService;
import xyz.kayaaa.xenon.shared.service.impl.ServerService;
import xyz.kayaaa.xenon.shared.tools.java.ClassUtils;
import xyz.kayaaa.xenon.shared.tools.string.CC;

import java.io.File;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
public class XenonPlugin extends JavaPlugin {

    @Getter
    private static XenonPlugin instance;

    private YamlConfiguration mainConfig, filterConfig;
    private XenonShared shared;

    private boolean joinable = false;

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
            Bukkit.getConsoleSender().sendMessage(CC.translate("&cEither way, Xenon &c&lDOES NOT &csupport"));
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

        shared = new XenonShared(new XenonBukkitLogger(), redisCredentials, mongoCredentials, databaseName);
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
            this.shared.getLogger().log("A Xenon server with the name " + this.mainConfig.getString("server.name") + " already exists!");
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
        this.getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        this.getServer().getPluginManager().registerEvents(new ServerListener(), this);
        this.getServer().getPluginManager().registerEvents(new MenuListener(), this);
    }

    @Override
    public void onDisable() {
        this.shared.getLogger().log("Shutting down Xenon!");
        this.shared.shutdown();
    }
}
