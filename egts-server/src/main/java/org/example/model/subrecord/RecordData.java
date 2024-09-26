package org.example.model.subrecord;

import lombok.Data;
import org.example.model.BinaryData;
import org.example.model.responseentity.ResultCodeResponse;
import org.example.model.responseentity.SrResponse;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Data
public class RecordData implements BinaryData {
    private SubrecordType subrecordType;
    private short subrecordLength;
    private BinaryData subrecordData;

    private static final int SIZE_OF_DATA = 3;

    public RecordData(BinaryData subrecordData, SubrecordType subrecordType) {
        this.subrecordData = subrecordData;
        this.subrecordType = subrecordType;
        this.subrecordLength = calculateSubRecordLength();
    }

    @Override
    public BinaryData decode(byte[] content) {
        //Реализация в RecordDataSet
        return null;
    }

    @Override
    public byte[] encode() {
        try (ByteArrayOutputStream bytesOut = new ByteArrayOutputStream()) {

            bytesOut.write(subrecordType.getId());
            bytesOut.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(calculateSubRecordLength()).array());
            bytesOut.write(subrecordData.encode());

            return bytesOut.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private short calculateSubRecordLength() {
        return (short) subrecordData.length();
    }

    //не используется, но считаем
    @Override
    public int length() {
        return SIZE_OF_DATA + subrecordData.length();
    }


}
