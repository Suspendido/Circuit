//package com.sylluxpvp.circuit.bukkit.command.importation;
//
//import com.sylluxpvp.circuit.shared.CircuitConstants;
//import com.sylluxpvp.circuit.shared.grant.Grant;
//import com.sylluxpvp.circuit.shared.profile.Profile;
//import com.sylluxpvp.circuit.shared.rank.Rank;
//import com.sylluxpvp.circuit.shared.service.ServiceContainer;
//import com.sylluxpvp.circuit.shared.service.impl.GrantService;
//import com.sylluxpvp.circuit.shared.service.impl.ProfileService;
//import com.sylluxpvp.circuit.shared.service.impl.RankService;
//import net.luckperms.api.LuckPerms;
//import net.luckperms.api.cacheddata.CachedMetaData;
//import net.luckperms.api.model.group.Group;
//import net.luckperms.api.model.user.User;
//import org.bukkit.Bukkit;
//import org.bukkit.plugin.RegisteredServiceProvider;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Set;
//import java.util.UUID;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.TimeUnit;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import java.util.stream.Collectors;
//
//public class LuckPermsImport {
//
//    private final LuckPerms api;
//    private final Logger logger;
//
//    public LuckPermsImport(Logger logger) {
//        this.logger = logger;
//
//        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
//        this.api = (provider != null) ? provider.getProvider() : null;
//    }
//
//    public boolean isAvailable() {
//        return api != null && Bukkit.getPluginManager().getPlugin("LuckPerms") != null;
//    }
//
//    public List<Rank> importRanks() {
//        List<Rank> imported = new ArrayList<>();
//        if (api == null) return imported;
//
//        RankService rankService = ServiceContainer.getService(RankService.class);
//
//        for (Group group : api.getGroupManager().getLoadedGroups()) {
//            if (rankService.getRank(group.getName()) != null) continue;
//
//            Rank rank = new Rank(UUID.randomUUID(), capitalize(group.getName()));
//
//            CachedMetaData meta = group.getCachedData().getMetaData();
//            rank.setPrefix(meta.getPrefix() != null ? meta.getPrefix() : "&7");
//            rank.setSuffix(meta.getSuffix() != null ? meta.getSuffix() : "");
//            rank.setColor("&7");
//            rank.setWeight(group.getWeight().orElse(0));
//            rank.setDefaultRank(group.getName().equalsIgnoreCase("default"));
//
//            List<String> permissions = new ArrayList<>();
//            group.getNodes().forEach(node -> permissions.add(node.getKey()));
//            rank.setPermissions(permissions);
//
//            List<String> inheritances = getInheritances(group, new ArrayList<>());
//            inheritances.removeIf(name -> name.equalsIgnoreCase(group.getName()));
//            rank.setInheritances(inheritances.stream().map(this::capitalize).collect(Collectors.toList()));
//
//            imported.add(rank);
//        }
//
//        return imported;
//    }
//
//    public void executeImportRanks() {
//        if (!isAvailable()) {
//            logger.warning("[Circuit] LuckPerms is not available for import.");
//            return;
//        }
//
//        RankService rankService = ServiceContainer.getService(RankService.class);
//
//        new ArrayList<>(rankService.getRanks()).forEach(rank -> {
//            if (!rank.isDefaultRank()) rankService.delete(rank);
//        });
//
//        List<Rank> imported = importRanks();
//        for (Rank rank : imported) {
//            rankService.getRanks().add(rank);
//            rankService.saveSync(rank);
//        }
//
//        logger.info("[Circuit] Successfully imported " + imported.size() + " ranks from LuckPerms.");
//    }
//
//    public CompletableFuture<Void> importUsers() {
//        return CompletableFuture.runAsync(() -> {
//            if (api == null) return;
//
//            try {
//                RankService rankService = ServiceContainer.getService(RankService.class);
//                ProfileService profileService = ServiceContainer.getService(ProfileService.class);
//                GrantService grantService = ServiceContainer.getService(GrantService.class);
//
//                Set<UUID> uuids = api.getUserManager().getUniqueUsers().get(30, TimeUnit.SECONDS);
//                if (uuids == null) return;
//
//                int count = 0;
//                for (UUID uuid : uuids) {
//                    User user = api.getUserManager().loadUser(uuid).get(30, TimeUnit.SECONDS);
//                    if (user == null) continue;
//
//                    String playerName = user.getUsername();
//                    if (playerName == null) playerName = Bukkit.getOfflinePlayer(uuid).getName();
//                    if (playerName == null) continue;
//
//                    Profile profile = profileService.find(uuid);
//
//                    if (profile == null) {
//                        profile = new Profile(uuid);
//                        profile.setName(playerName);
//                        profile.setAddress("");
//                        profileService.getOnlineProfiles().add(profile);
//                    } else {
//                        if (profile.getName() == null) profile.setName(playerName);
//                        if (profile.getAddress() == null) profile.setAddress("");
//                    }
//
//                    final Profile finalProfile = profile;
//                    user.getNodes().forEach(node -> {
//                        if (!finalProfile.getPermissions().contains(node.getKey())) {
//                            finalProfile.getPermissions().add(node.getKey());
//                        }
//                    });
//
//                    for (Group group : user.getInheritedGroups(user.getQueryOptions())) {
//                        Rank rank = rankService.getRank(capitalize(group.getName()));
//                        if (rank == null) continue;
//                        if (profile.hasRank(rank)) continue;
//
//                        Grant<Rank> grant = grantService.createGrant(
//                                rank,
//                                CircuitConstants.getConsoleUUID(),
//                                "Imported from LuckPerms"
//                        );
//
//                        profile.addGrant(grant);
//                    }
//
//                    profileService.saveSync(profile);
//                    count++;
//                }
//
//                logger.info("[Circuit] Successfully imported " + count + " users from LuckPerms.");
//
//            } catch (Exception e) {
//                logger.log(Level.SEVERE, "[Circuit] Failed to import users from LuckPerms", e);
//            }
//        });
//    }
//
//    private List<String> getInheritances(Group group, List<String> list) {
//        if (!list.contains(group.getName())) {
//            list.add(group.getName());
//        }
//        for (Group child : group.getInheritedGroups(group.getQueryOptions())) {
//            getInheritances(child, list);
//        }
//        return list;
//    }
//
//    private String capitalize(String name) {
//        if (name == null || name.isEmpty()) return name;
//        return Character.toUpperCase(name.charAt(0)) + name.substring(1).toLowerCase();
//    }
//}