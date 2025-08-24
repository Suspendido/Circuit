package xyz.kayaaa.xenon.bukkit;

import com.jonahseguin.drink.Drink;
import com.jonahseguin.drink.command.CommandService;
import com.jonahseguin.drink.command.DrinkCommandContainer;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.kayaaa.xenon.bukkit.command.CommandBase;
import xyz.kayaaa.xenon.bukkit.listener.PlayerListener;
import xyz.kayaaa.xenon.bukkit.provider.RankProvider;
import xyz.kayaaa.xenon.bukkit.service.BukkitProfileService;
import xyz.kayaaa.xenon.shared.XenonShared;
import xyz.kayaaa.xenon.shared.mongo.MongoCredentials;
import xyz.kayaaa.xenon.shared.rank.Rank;
import xyz.kayaaa.xenon.shared.redis.RedisCredentials;
import xyz.kayaaa.xenon.shared.service.ServiceContainer;
import xyz.kayaaa.xenon.shared.tools.ClassUtils;
import xyz.kayaaa.xenon.bukkit.tools.ConfigUtil;

@Getter
public class XenonPlugin extends JavaPlugin {

    @Getter
    private static XenonPlugin instance;

    private YamlConfiguration mainConfig;
    private XenonShared shared;
    private CommandService drink;

    @Override
    public void onEnable() {
        instance = this;
        mainConfig = ConfigUtil.createConfig("config");
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

        shared = new XenonShared(redisCredentials, mongoCredentials, databaseName);
        this.setupCommands();
        this.setupServices();
        this.getServer().getPluginManager().registerEvents(new PlayerListener(), this);
    }

    private void setupCommands() {
        drink = Drink.get(this);
        drink.bind(Rank.class).toProvider(new RankProvider());
        ClassUtils.getClasses(getFile(), this.getClass().getPackage().getName() + ".command.impl").stream().filter(c -> !c.getName().contains("$")).forEach(c -> {
            try {
                CommandBase command = (CommandBase) c.newInstance();
                DrinkCommandContainer container = drink.register(command, command.getName(), command.getAliases());
                container.setDefaultCommandIsHelp(command.isDefaultHelp());
            } catch (Exception exception) {
                this.getLogger().info("Error while loading the command " + c.getSimpleName());
                exception.printStackTrace();
            }
        });
        drink.registerCommands();
    }

    private void setupServices() {
        ServiceContainer.registerService(new BukkitProfileService());
    }

    @Override
    public void onDisable() {
        ConfigUtil.saveConfig(mainConfig);
        this.shared.shutdown();
    }
}
