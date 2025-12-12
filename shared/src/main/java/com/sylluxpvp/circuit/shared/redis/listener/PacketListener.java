package com.sylluxpvp.circuit.shared.redis.listener;

import com.sylluxpvp.circuit.shared.redis.RedisPacket;

import java.util.function.Consumer;

public abstract class PacketListener<T extends RedisPacket> implements Consumer<T> {

    @Override
    public void accept(T t) {
        this.listen(t);
    }

    public abstract void listen(T packet);

}
