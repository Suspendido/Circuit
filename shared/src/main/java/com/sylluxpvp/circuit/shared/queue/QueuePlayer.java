package com.sylluxpvp.circuit.shared.queue;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QueuePlayer implements Comparable<QueuePlayer> {

    private UUID uuid;
    private String name;
    private int priority;
    private long insertedAt;

    @Override
    public int compareTo(QueuePlayer other) {
        // Lower priority number = higher priority (goes first)
        int result = this.priority - other.priority;
        
        if (result == 0) {
            // If same priority, earlier insertion goes first
            return Long.compare(this.insertedAt, other.insertedAt);
        }
        
        return result;
    }
}
