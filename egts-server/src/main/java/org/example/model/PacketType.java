package org.example.model;

import lombok.Getter;

import java.util.Optional;

@Getter
public enum PacketType {

    EGTS_PT_RESPONSE(0),
    EGTS_PT_APPDATA(1);

    private int id;

    PacketType(int id) {
        this.id = id;
    }

    public static PacketType fromId(int id) {
        for (PacketType value : PacketType.values()) {
            if (value.getId() == id) {
                return value;
            }
        }
        return null;
    }
}
