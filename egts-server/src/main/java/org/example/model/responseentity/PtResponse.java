package org.example.model.responseentity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.model.BinaryData;
import org.example.model.service.ServiceDataSet;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PtResponse implements BinaryData {

    private int responsePacketId;
    private int processingResult;
    private BinaryData sdr;

    private static final int SIZE_OF_DATA = 3;

    @Override
    public BinaryData decode(byte[] content) {
        var inputStream = new ByteArrayInputStream(content);
        var in = new BufferedInputStream(inputStream);
        try {
            responsePacketId = ByteBuffer.wrap(in.readNBytes(2)).order(ByteOrder.LITTLE_ENDIAN).getShort();
            processingResult = in.read();
            sdr = new ServiceDataSet();
            sdr.decode(in.readNBytes(in.available()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    @Override
    public byte[] encode() {
        var bytesOut = new ByteArrayOutputStream();
        try {
            bytesOut.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort((short) responsePacketId).array());
            bytesOut.write(processingResult);
            var sdrBytes = sdr.encode();
            bytesOut.write(sdrBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bytesOut.toByteArray();
    }

    @Override
    public int length() {
        return SIZE_OF_DATA + sdr.length();
    }
}
