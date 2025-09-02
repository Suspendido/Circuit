package xyz.kayaaa.xenon.shared.service.impl;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.bson.Document;
import xyz.kayaaa.xenon.shared.XenonConstants;
import xyz.kayaaa.xenon.shared.XenonShared;
import xyz.kayaaa.xenon.shared.gift.GiftCode;
import xyz.kayaaa.xenon.shared.grant.Grant;
import xyz.kayaaa.xenon.shared.profile.Profile;
import xyz.kayaaa.xenon.shared.rank.Rank;
import xyz.kayaaa.xenon.shared.service.Service;
import xyz.kayaaa.xenon.shared.service.ServiceContainer;
import xyz.kayaaa.xenon.shared.tools.string.StringHelper;
import xyz.kayaaa.xenon.shared.tools.xenon.Serializable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class GiftCodeService extends Service {

    private Map<String, GiftCode<Rank>> cache;
    private MongoCollection<Document> giftsCollection;

    @Override @NonNull
    public String getIdentifier() {
        return "giftcode";
    }

    @Override
    public void enable() {
        this.cache = new ConcurrentHashMap<>();
        this.giftsCollection = XenonShared.getInstance().getMongo().getDatabase().getCollection("gifts");
        this.loadAll();
    }

    @Override
    public void disable() {
        this.saveAll();
        this.cache.clear();
        this.cache = null;
        this.giftsCollection = null;
    }

    public GiftCode<Rank> createCode(Rank reward, long duration) {
        Validate.notNull(reward, "Reward cannot be null");
        String code = "XENON-" + StringHelper.generateString(6);
        GiftCode<Rank> gift = new GiftCode<>(
                UUID.randomUUID(),
                code,
                reward,
                System.currentTimeMillis(),
                duration,
                false,
                false,
                null,
                -1
        );
        cache.put(code.toLowerCase(), gift);
        saveGiftCode(gift);
        return gift;
    }

    public GiftCode<Rank> getCode(String code) {
        if (!code.toLowerCase().contains("xenon-")) {
            return this.cache.values().stream().filter(gift -> gift.getCode().replace("xenon-", "").equalsIgnoreCase(code)).findFirst().orElse(null);
        }

        return this.cache.values().stream().filter(gift -> gift.getCode().equalsIgnoreCase(code)).findFirst().orElse(null);
    }

    public boolean redeemCode(UUID target, String code) {
        Validate.notNull(code, "Code cannot be null");
        Profile profile = ServiceContainer.getService(ProfileService.class).find(target);
        Validate.notNull(profile, "Profile cannot be null");
        if (getCode(code) == null) {
            XenonShared.getInstance().getLogger().warn(profile.getName() + " tried redeeming a invalid gift code, ignoring...");
            return false;
        }

        GiftCode<Rank> gift = getCode(code);
        if (!gift.isAvailable()) {
            XenonShared.getInstance().getLogger().warn(profile.getName() + " tried redeeming a used/revoked gift code, ignoring...");
            return false;
        }

        gift.setRedeemed(true);
        gift.setRedeemedBy(target);
        gift.setRedeemedAt(System.currentTimeMillis());

        applyReward(target, gift.getReward());

        saveGiftCode(gift);
        return true;
    }

    private void applyReward(UUID target, Serializable reward) {
        GrantService grantService = ServiceContainer.getService(GrantService.class);
        ProfileService profileService = ServiceContainer.getService(ProfileService.class);
        Grant<Rank> grant = grantService.createGrant((Rank) reward, XenonConstants.getConsoleUUID(), "GiftCode");
        Profile profile = profileService.find(target);
        profile.addGrant(grant);
    }

    private void saveGiftCode(GiftCode<Rank> gift) {
        Validate.notNull(gift, "Gift cannot be null");
        Document doc = gift.toDocument();

        giftsCollection.replaceOne(
                Filters.eq("uuid", gift.getUUID().toString()),
                doc,
                new ReplaceOptions().upsert(true)
        );
    }

    public void saveAll() {
        this.cache.values().forEach(this::saveGiftCode);
        this.print("Saved " + this.cache.size() + " gift codes to MongoDB");
    }

    public void loadAll() {
        this.cache.clear();
        List<Document> profileDocuments = giftsCollection.find().into(new ArrayList<>());

        for (Document doc : profileDocuments) {
            GiftCode<Rank> gift = fromDocument(doc);
            if (gift == null) continue;

            this.cache.put(gift.getCode().toLowerCase(), gift);
        }

        this.print("Loaded " + this.cache.size() + " gift codes from MongoDB");
    }

    public GiftCode<Rank> fromDocument(Document doc) {
        GiftCode<Rank> code = new GiftCode<>();
        code.setUUID(UUID.fromString(doc.getString("uuid")));
        code.setCode(doc.getString("code"));
        code.setCreatedAt(doc.getLong("createdAt"));
        code.setDuration(doc.getLong("duration"));
        code.setRevoked(doc.getBoolean("revoked", false));
        code.setRedeemed(doc.getBoolean("redeemed", false));
        code.setRedeemedAt(doc.getLong("redeemedAt"));

        String redeemedByStr = doc.getString("redeemedBy");
        if (redeemedByStr != null) code.setRedeemedBy(UUID.fromString(redeemedByStr));

        Rank data = null;
        String rewardType = doc.getString("rewardType");
        if (rewardType != null) {
            if (rewardType.equalsIgnoreCase("rank")) {
                UUID rankID = null;
                try {
                    rankID = UUID.fromString(doc.getString("rewardID"));
                } catch (Exception ignored) {}
                RankService rankService = ServiceContainer.getService(RankService.class);
                data = ((rankID != null && rankService.getRank(rankID) != null) ? rankService.getRank(rankID) : rankService.getDefaultRank());
            }
        }
        code.setReward(data);
        return code;
    }

    @Override
    public List<Class<? extends Service>> getDependencies() {
        return Arrays.asList(GrantService.class, ProfileService.class, RankService.class);
    }
}
