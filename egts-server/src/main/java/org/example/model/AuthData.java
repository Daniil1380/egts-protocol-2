package org.example.model;

import lombok.Data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Data
public class AuthData implements BinaryData {
    private int dispatcherType;
    private int dispatcherId;

    public AuthData() {
    }

    public AuthData(int dispatcherType, int dispatcherId) {
        this.dispatcherType = dispatcherType;
        this.dispatcherId = dispatcherId;
    }

    @Override
    public BinaryData decode(byte[] content) {
        return null;
    }

    @Override
    public byte[] encode() {
        try (ByteArrayOutputStream bytesOut = new ByteArrayOutputStream()) {

            byte dispatcherTypeByte = (byte) dispatcherType;
            bytesOut.write(dispatcherTypeByte);
            bytesOut.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(dispatcherId).array());

            return bytesOut.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int length() {
        return 5;
    }
}
