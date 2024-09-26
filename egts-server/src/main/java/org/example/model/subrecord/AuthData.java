package org.example.model.subrecord;

import lombok.Data;
import org.example.model.BinaryData;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Data
public class AuthData implements BinaryData {

    private byte dispatcherType;
    private int dispatcherId;

    private final static byte ZERO_BYTE = 0;
    private final static byte SIZE_OF_DATA = 5;

    public AuthData(int dispatcherId) {
        this.dispatcherType = ZERO_BYTE;
        this.dispatcherId = dispatcherId;
    }

    @Override
    public BinaryData decode(byte[] content) {
        return null;
    }

    @Override
    public byte[] encode() {
        try (ByteArrayOutputStream bytesOut = new ByteArrayOutputStream()) {

            bytesOut.write(dispatcherType);
            bytesOut.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(dispatcherId).array());

            return bytesOut.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int length() {
        return SIZE_OF_DATA;
    }
}
