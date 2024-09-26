package org.example.model;

import lombok.Data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Data
public class RecordData implements BinaryData {
    private SubrecordType subrecordType;
    private short subrecordLength;
    private BinaryData subrecordData;

    public RecordData() {
    }

    public RecordData(BinaryData subrecordData, SubrecordType subrecordType) {
        this.subrecordData = subrecordData;
        this.subrecordType = subrecordType;
    }

    @Override
    public BinaryData decode(byte[] content) {
        return null;
    }

    @Override
    public byte[] encode() {
        try (ByteArrayOutputStream bytesOut = new ByteArrayOutputStream()) {
            bytesOut.write(subrecordType.getId());

            short subRecordLength = ((short) subrecordData.length());


            bytesOut.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(subRecordLength).array());
            bytesOut.write(subrecordData.encode());

            return bytesOut.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int length() {
        return 0;
    }
}
