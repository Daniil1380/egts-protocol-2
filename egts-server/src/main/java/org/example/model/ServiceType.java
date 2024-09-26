package org.example.model;

import lombok.Getter;

import java.util.Optional;

@Getter
public enum ServiceType {
    EGTS_AUTH_SERVICE((byte) 1),
    EGTS_TELEDATA_SERVICE ((byte) 2);

    private byte id;

    ServiceType(byte id) {
        this.id = id;
    }

    public static ServiceType fromId(byte id) {
        for (ServiceType value : ServiceType.values()) {
            if (value.getId() == id ) {
                return value;
            }
        }
        return null;
    }
}
