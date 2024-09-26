package org.example.model.subrecord;

import lombok.Getter;

@Getter
public enum SubrecordType {
    TERM_IDENTITY(1),
    EGTS_SR_DISPATCHER_IDENTITY(5),
    EGTS_SR_POS_DATA(16),
    EGTS_SR_RECORD_RESPONSE(0),
    RESULT_CODE(9),
    EXT_POS_DATA(17);

    private final Integer id;

    SubrecordType(Integer id) {
        this.id = id;
    }

    public static SubrecordType fromId(Integer id) {
        for (SubrecordType value : SubrecordType.values()) {
            if (value.getId().equals(id)) {
                return value;
            }
        }
        return null;
    }
}
