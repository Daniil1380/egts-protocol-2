package org.example.model.responseentity;

import lombok.Data;
import org.example.model.BinaryData;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Data
public class ResultCodeResponse implements BinaryData {

    private byte resultCode;

    public ResultCodeResponse(byte resultCode) {
        this.resultCode = resultCode;
    }

    public ResultCodeResponse() {
    }

    @Override
    public BinaryData decode(byte[] content) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content);
        try {
            resultCode = ByteBuffer.wrap(inputStream.readNBytes(1)).order(ByteOrder.LITTLE_ENDIAN).get();
        } catch (IOException exception) {
            System.out.println("ResultCodeResponse decode error " + exception.getMessage());
            return null;
        }
        return this;
    }

    @Override
    public byte[] encode() {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        bytesOut.write(resultCode);
        return bytesOut.toByteArray();
    }

    @Override
    public int length() {
        var recBytes = this.encode();
        return recBytes.length;
    }
}
