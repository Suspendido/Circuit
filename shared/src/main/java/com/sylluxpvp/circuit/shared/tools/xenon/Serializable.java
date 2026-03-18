package com.sylluxpvp.circuit.shared.tools.circuit;

import org.bson.Document;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.PunishmentService;
import com.sylluxpvp.circuit.shared.service.impl.RankService;

public interface Serializable {

    String getID();
    String getType();
    String getExpiryMessage();
    String getRemovalMessage();
    Document toDocument();

    static Serializable fromDocument(String type, Document doc) {
        return switch (type.toLowerCase()) {
            case "rank" -> ServiceContainer.getService(RankService.class).fromDocument(doc);
            case "punishment" -> ServiceContainer.getService(PunishmentService.class).fromDocument(doc);
            default -> throw new IllegalArgumentException("Unknown type: " + type);
        };
    }
}