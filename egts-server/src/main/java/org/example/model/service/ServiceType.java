package org.example.model.service;

import lombok.Getter;

@Getter
public enum ServiceType {
    EGTS_AUTH_SERVICE(1),
    EGTS_TELEDATA_SERVICE (2);

    private int id;

    ServiceType(int id) {
        this.id = id;
    }

    public static ServiceType fromId(int id) {
        for (ServiceType value : ServiceType.values()) {
            if (value.getId() == id ) {
                return value;
            }
        }
        return null;
    }
}
