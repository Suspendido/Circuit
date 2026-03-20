package com.sylluxpvp.circuit.bukkit.command.punishment.remove;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.Updates;
import com.sylluxpvp.circuit.shared.cache.ProfileCache;
import com.sylluxpvp.circuit.shared.tools.async.AsyncExecutor;
import com.sylluxpvp.circuit.shared.tools.string.CC;
import org.bson.Document;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.sylluxpvp.circuit.bukkit.CircuitPlugin;
import com.sylluxpvp.circuit.shared.CircuitConstants;
import com.sylluxpvp.circuit.shared.grant.Grant;
import com.sylluxpvp.circuit.shared.profile.Profile;
import com.sylluxpvp.circuit.shared.punishment.Punishment;
import com.sylluxpvp.circuit.shared.punishment.PunishmentType;
import com.sylluxpvp.circuit.shared.redis.packets.punish.PunishmentUpdatePacket;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.ProfileService;
import com.sylluxpvp.circuit.shared.service.impl.PunishmentService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@CommandAlias("unban")
@CommandPermission("circuit.punish.unban")
public class UnbanCommand extends BaseCommand {

    @Default
    @CommandCompletion("@players *")
    public void unban(CommandSender sender, @Name("target") OfflinePlayer target, @Optional @Name("reason") @Flags("remaining") String reason) {
        if (target == null) {
            sender.sendMessage(CircuitConstants.getPlayerNotFound());
            return;
        }

        ProfileService profileService = ServiceContainer.getService(ProfileService.class);
        Profile profile = profileService.find(target.getUniqueId());

        if (profile == null) {
            try {
                profile = profileService.loadAsync(target.getUniqueId()).get(10, TimeUnit.SECONDS);
            } catch (Exception e) {
                sender.sendMessage(CircuitConstants.getPlayerNotFound());
                return;
            }
        }

        if (profile == null) {
            sender.sendMessage(CircuitConstants.getPlayerNotFound());
            return;
        }

        if (profile.findActivePunishment(PunishmentType.BAN) == null) {
            sender.sendMessage(CircuitConstants.getPlayerNotPunished().replace("<punishment_type>", PunishmentType.BAN.getAction()));
            return;
        }

        Grant<Punishment> punishment = profile.findActivePunishment(PunishmentType.BAN);
        UUID authorUUID = sender instanceof Player ? ((Player) sender).getUniqueId() : CircuitConstants.getConsoleUUID();

        ServiceContainer.getService(PunishmentService.class).removePunishment(authorUUID, profile, punishment, reason);
        profileService.saveSync(profile);
        CircuitPlugin.getInstance().getShared().getRedis().sendPacket(
                new PunishmentUpdatePacket(authorUUID, target.getUniqueId(), PunishmentType.BAN.name(), -1, -1, reason, true, false)
        );
        sender.sendMessage(CC.translate("&aSuccessfully unbanned &f" + target.getName() + "&a."));
    }

    @Subcommand("force")
    @CommandPermission("circuit.punish.unban.force")
    @CommandCompletion("@players")
    public void forceUnban(CommandSender sender, @Name("target") OfflinePlayer target) {
        if (target == null) {
            sender.sendMessage(CircuitConstants.getPlayerNotFound());
            return;
        }

        ProfileService profileService = ServiceContainer.getService(ProfileService.class);

        AsyncExecutor.runAsync(() -> {
            try {
                // 1. Limpiar memoria y cache
                Profile memProfile = profileService.find(target.getUniqueId());
                if (memProfile != null) {
                    memProfile.getPunishments().forEach(p -> p.setRemoved(true));
                }
                ProfileCache.remove(target.getUniqueId());
                profileService.getOnlineProfiles().removeIf(p -> p.getUUID().equals(target.getUniqueId()));

                // 2. Modificar cada punishment en el documento raw y marcar removed: true
                Document doc = profileService.getProfilesCollection()
                        .find(Filters.eq("uuid", target.getUniqueId().toString())).first();

                if (doc != null) {
                    List<Document> punishments = (List<Document>) doc.get("punishments");
                    if (punishments != null) {
                        for (Document p : punishments) {
                            if (!p.getBoolean("removed", false)) {
                                p.put("removed", true);
                                p.put("removedBy", CircuitConstants.getConsoleUUID().toString());
                                p.put("removedAt", System.currentTimeMillis());
                                p.put("removalReason", "Force unbanned by " + sender.getName());
                            }
                        }
                        doc.put("punishments", punishments);
                    }

                    profileService.getProfilesCollection().replaceOne(
                            Filters.eq("uuid", target.getUniqueId().toString()),
                            doc,
                            new ReplaceOptions().upsert(true)
                    );
                }

                sender.sendMessage("&aForce unbanned &f" + target.getName() + "&a successfully.");
            } catch (Exception e) {
                sender.sendMessage("&cFailed: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @Subcommand("info")
    @CommandPermission("circuit.punish.unban.force")
    @CommandCompletion("@players")
    public void info(CommandSender sender, @Name("target") OfflinePlayer target) {
        if (target == null) {
            sender.sendMessage(CircuitConstants.getPlayerNotFound());
            return;
        }

        ProfileService profileService = ServiceContainer.getService(ProfileService.class);

        AsyncExecutor.runAsync(() -> {
            try {
                // Buscar por UUID con y sin guiones
                String uuidStr = target.getUniqueId().toString();
                String uuidNoDashes = uuidStr.replace("-", "");

                Document docWithDashes = profileService.getProfilesCollection()
                        .find(Filters.eq("uuid", uuidStr)).first();
                Document docNoDashes = profileService.getProfilesCollection()
                        .find(Filters.eq("uuid", uuidNoDashes)).first();
                Document docById = profileService.getProfilesCollection()
                        .find(Filters.eq("_id", uuidStr)).first();

                sender.sendMessage("&fUUID with dashes: &e" + (docWithDashes != null ? "FOUND - punishments: " + ((List<?>) docWithDashes.getOrDefault("punishments", new ArrayList<>())).size() : "NOT FOUND"));
                sender.sendMessage("&fUUID no dashes: &e" + (docNoDashes != null ? "FOUND - punishments: " + ((List<?>) docNoDashes.getOrDefault("punishments", new ArrayList<>())).size() : "NOT FOUND"));
                sender.sendMessage("&fBy _id: &e" + (docById != null ? "FOUND - punishments: " + ((List<?>) docById.getOrDefault("punishments", new ArrayList<>())).size() : "NOT FOUND"));

            } catch (Exception e) {
                sender.sendMessage("&cFailed: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @Subcommand("fixduplicate")
    @CommandPermission("circuit.admin")
    @CommandCompletion("@players")
    public void fixDuplicate(CommandSender sender, @Name("target") String name) {
        ProfileService profileService = ServiceContainer.getService(ProfileService.class);

        AsyncExecutor.runAsync(() -> {
            try {
                // Encontrar todos los documentos con ese nombre
                List<Document> docs = new ArrayList<>();
                profileService.getProfilesCollection()
                        .find(Filters.regex("name", "^" + name + "$", "i"))
                        .into(docs);

                sender.sendMessage("&fFound &e" + docs.size() + " &fdocuments with name &e" + name);

                if (docs.size() <= 1) {
                    sender.sendMessage("&cNo duplicates found.");
                    return;
                }

                // Mantener el que tiene UUID correcto, borrar el resto
                // El "correcto" es el que tiene menos punishments activos
                Document toKeep = docs.stream()
                        .min(Comparator.comparingInt(doc -> {
                            List<?> punishments = (List<?>) doc.getOrDefault("punishments", new ArrayList<>());
                            return (int) punishments.stream()
                                    .filter(p -> p instanceof Document && !((Document) p).getBoolean("removed", false))
                                    .count();
                        }))
                        .orElse(null);

                for (Document doc : docs) {
                    if (doc == toKeep) continue;
                    profileService.getProfilesCollection()
                            .deleteOne(Filters.eq("_id", doc.get("_id")));
                    sender.sendMessage("&aDeleted duplicate with UUID: &f" + doc.getString("uuid"));
                }

                sender.sendMessage("&aKept document with UUID: &f" + toKeep.getString("uuid"));

            } catch (Exception e) {
                sender.sendMessage("&cFailed: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
