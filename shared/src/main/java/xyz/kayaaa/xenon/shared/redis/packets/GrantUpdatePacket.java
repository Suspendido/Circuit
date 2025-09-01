package xyz.kayaaa.xenon.shared.redis.packets;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.kayaaa.xenon.shared.grant.Grant;
import xyz.kayaaa.xenon.shared.redis.RedisPacket;
import xyz.kayaaa.xenon.shared.tools.xenon.Serializable;

@RequiredArgsConstructor @AllArgsConstructor @Getter
public class GrantUpdatePacket implements RedisPacket {

    private Grant<?> grant;

    @Override
    public String getID() {
        return "GRANT_UPDATE";
    }
}
