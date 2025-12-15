package com.sylluxpvp.circuit.shared.redis.listener;

import com.mongodb.client.model.Filters;
import org.bson.Document;
import com.sylluxpvp.circuit.shared.tag.Tag;
import com.sylluxpvp.circuit.shared.redis.packets.tag.TagUpdatePacket;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.TagService;

public class TagUpdateListener extends PacketListener<TagUpdatePacket> {

    @Override
    public void listen(TagUpdatePacket packet) {
        TagService tagService = ServiceContainer.getService(TagService.class);
        if (tagService == null) return;

        Tag existingTag = tagService.getTag(packet.getTagUUID());

        if (packet.isDeleted()) {
            if (existingTag != null) {
                tagService.getTags().remove(existingTag);
            }
            return;
        }

        // Reload tag from database
        Document doc = tagService.getTagsCollection()
                .find(Filters.eq("uuid", packet.getTagUUID().toString()))
                .first();

        if (doc == null) return;

        Tag updatedTag = tagService.fromDocument(doc);

        if (existingTag != null) {
            // Update existing tag properties
            existingTag.setDisplay(updatedTag.getDisplay());
            existingTag.setPermission(updatedTag.getPermission());
            existingTag.setPurchasable(updatedTag.isPurchasable());
        } else {
            // Add new tag
            tagService.getTags().add(updatedTag);
        }
    }
}
