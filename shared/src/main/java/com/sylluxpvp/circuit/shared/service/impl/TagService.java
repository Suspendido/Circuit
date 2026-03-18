package com.sylluxpvp.circuit.shared.service.impl;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import lombok.Getter;
import lombok.NonNull;
import org.bson.Document;
import com.sylluxpvp.circuit.shared.CircuitShared;
import com.sylluxpvp.circuit.shared.tag.Tag;
import com.sylluxpvp.circuit.shared.service.Service;
import com.sylluxpvp.circuit.shared.tools.async.AsyncExecutor;
import com.sylluxpvp.circuit.shared.redis.packets.tag.TagUpdatePacket;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Getter
public class TagService extends Service {

    private List<Tag> tags;
    private MongoCollection<Document> tagsCollection;

    @Override
    @NonNull
    public String getIdentifier() {
        return "tag";
    }

    @Override
    public void enable() {
        this.tags = new CopyOnWriteArrayList<>();
        this.tagsCollection = CircuitShared.getInstance().getMongo().getDatabase().getCollection("tags");
        this.loadAll();
    }

    @Override
    public void disable() {
        try {
            this.saveAll();
        } catch (Exception e) {
            CircuitShared.getInstance().getLogger().warn("Could not save tags during shutdown (MongoDB may be down): " + e.getMessage());
        }
        if (this.tags != null) {
            this.tags.clear();
            this.tags = null;
        }
    }

    public void loadAll() {
        List<Document> allTags = tagsCollection.find().into(new ArrayList<>());
        for (Document doc : allTags) {
            Tag tag = fromDocument(doc);
            if (tag != null) {
                this.tags.add(tag);
            }
        }
    }

    public void saveAll() {
        this.tags.forEach(this::saveSync);
    }

    public Tag getTag(UUID uuid) {
        if (uuid == null) return null;
        return this.tags.stream().filter(tag -> tag.getUuid().equals(uuid)).findFirst().orElse(null);
    }

    public Tag getTag(String name) {
        if (name == null || name.isEmpty()) return null;
        return this.tags.stream().filter(tag -> tag.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public List<Tag> getSortedTags() {
        return this.tags.stream()
                .sorted(Comparator.comparing(Tag::getName))
                .collect(Collectors.toList());
    }

    public Tag create(String name, UUID createdBy) {
        Tag tag = new Tag(UUID.randomUUID(), name);
        tag.setDisplay("&7[" + name + "]");
        tag.setPurchasable(false);
        tag.setPermission("circuit.tag." + name.toLowerCase());
        tag.setCreatedAt(System.currentTimeMillis());
        tag.setCreatedBy(createdBy);
        this.tags.add(tag);
        return tag;
    }

    public void save(Tag tag) {
        AsyncExecutor.runAsync(() -> {
            saveSync(tag);
            CircuitShared.getInstance().getRedis().sendPacket(new TagUpdatePacket(tag.getUuid(), false));
        });
    }

    public void saveSync(Tag tag) {
        Document doc = tag.toDocument();
        tagsCollection.replaceOne(
                Filters.eq("uuid", tag.getUuid().toString()),
                doc,
                new ReplaceOptions().upsert(true)
        );
    }

    public void delete(Tag tag) {
        if (tag == null || !this.tags.contains(tag)) return;
        this.tags.remove(tag);

        AsyncExecutor.runAsync(() -> {
            tagsCollection.deleteOne(Filters.eq("uuid", tag.getUuid().toString()));
            CircuitShared.getInstance().getRedis().sendPacket(new TagUpdatePacket(tag.getUuid(), true));
        });
    }

    public Tag fromDocument(Document doc) {
        Tag tag = new Tag(UUID.fromString(doc.getString("uuid")), doc.getString("name"));
        tag.setDisplay(doc.getString("display"));
        tag.setPurchasable(doc.getBoolean("purchasable", false));
        tag.setPermission(doc.getString("permission"));
        tag.setCreatedAt(doc.getLong("createdAt"));
        String createdByStr = doc.getString("createdBy");
        if (createdByStr != null) {
            tag.setCreatedBy(UUID.fromString(createdByStr));
        }
        return tag;
    }
}
