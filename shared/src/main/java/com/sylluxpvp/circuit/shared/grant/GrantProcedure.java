package com.sylluxpvp.circuit.shared.grant;

import lombok.*;
import com.sylluxpvp.circuit.shared.tools.circuit.Serializable;

import java.util.UUID;

@Data
@AllArgsConstructor @NoArgsConstructor
public class GrantProcedure<T extends Serializable> {

    private UUID author;
    private UUID target;
    private T data;
    private long duration;
    private String reason;

}
