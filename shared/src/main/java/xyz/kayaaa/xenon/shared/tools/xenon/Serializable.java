package xyz.kayaaa.xenon.shared.tools.xenon;

import org.bson.Document;
import xyz.kayaaa.xenon.shared.service.ServiceContainer;
import xyz.kayaaa.xenon.shared.service.impl.RankService;

public interface Serializable {

    String getID();
    String getType();
    Document toDocument();

    static Serializable fromDocument(String type, Document doc) {
        if (type.equalsIgnoreCase("rank")) {

            return ServiceContainer.getService(RankService.class).fromDocument(doc);
        }
        throw new IllegalArgumentException("Unknown type: " + type);
    }
}