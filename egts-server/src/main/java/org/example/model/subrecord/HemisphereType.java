package org.example.model.subrecord;

import lombok.Getter;

@Getter
public enum HemisphereType {

    SOUTH("1"),
    NORTH("0"),
    WEST("1"),
    EAST("0");

    private final String value;

    HemisphereType(String value) {
        this.value = value;
    }
}
