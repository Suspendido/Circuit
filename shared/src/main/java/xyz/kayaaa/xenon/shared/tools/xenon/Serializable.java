package xyz.kayaaa.xenon.shared.tools.xenon;

import org.bson.Document;
import xyz.kayaaa.xenon.shared.service.ServiceContainer;
import xyz.kayaaa.xenon.shared.service.impl.GiftService;
import xyz.kayaaa.xenon.shared.service.impl.PunishmentService;
import xyz.kayaaa.xenon.shared.service.impl.RankService;

public interface Serializable {

    String getID();
    String getType();
    String getExpiryMessage();
    String getRemovalMessage();
    Document toDocument();

    static Serializable fromDocument(String type, Document doc) {
        switch (type.toLowerCase()) {
            case "rank":
                return ServiceContainer.getService(RankService.class).fromDocument(doc);
            case "punishment":
                return ServiceContainer.getService(PunishmentService.class).fromDocument(doc);
            case "gift":
                return ServiceContainer.getService(GiftService.class).fromDocument(doc);
            default:
                throw new IllegalArgumentException("Unknown type: " + type);
        }
    }
}