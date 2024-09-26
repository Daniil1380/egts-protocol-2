package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.model.responseentity.PtResponse;
import org.example.model.service.ServiceDataSet;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.example.util.BooleanUtil.getStringFromBool;
import static org.example.util.CrcUtil.calculateCrc16;
import static org.example.util.CrcUtil.calculateCrc8;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Package implements BinaryData {

    private int protocolVersion;
    private int securityKeyId;
    private boolean prefix;
    private boolean route;
    private boolean encryptionAlg;
    private boolean compression;
    private boolean priority;
    private int headerLength;
    private int headerEncoding;
    private short frameDataLength;
    private int packageIdentifier;
    private PacketType packetType;
    private int peerAddress;
    private int recipientAddress;
    private int timeToLive;
    private int headerCheckSum;
    private BinaryData servicesFrameData;
    private int servicesFrameDataCheckSum;

    private static final byte PROTOCOL_VERSION = 1;
    private static final byte NONE = 0;
    private static final byte HEADER_LENGTH = 11;

    public Package(int packageIdentifier, PacketType packetType, BinaryData servicesFrameData) {
        this.protocolVersion = PROTOCOL_VERSION;
        this.securityKeyId = NONE;
        this.prefix = false;
        this.route = false;
        this.encryptionAlg = false;
        this.compression = false;
        this.priority = false;
        this.headerLength = HEADER_LENGTH;
        this.headerEncoding = NONE;
        this.packageIdentifier = packageIdentifier;
        this.packetType = packetType;
        this.servicesFrameData = servicesFrameData;
    }

    @Override
    public BinaryData decode(byte[] content) {
        var inputStream = new ByteArrayInputStream(content);
        var in = new BufferedInputStream(inputStream);
        try {
            protocolVersion = in.read();
            securityKeyId = in.read();
            decodeFlags(in.read());
            headerLength = in.read();
            headerEncoding = in.read();

            frameDataLength = ByteBuffer.wrap(in.readNBytes(2)).order(ByteOrder.LITTLE_ENDIAN).getShort();
            packageIdentifier = ByteBuffer.wrap(in.readNBytes(2)).order(ByteOrder.LITTLE_ENDIAN).getShort();

            packetType = PacketType.fromId(in.read());

            if (route) {
                peerAddress = ByteBuffer.wrap(in.readNBytes(2)).order(ByteOrder.LITTLE_ENDIAN).getShort();
                recipientAddress = ByteBuffer.wrap(in.readNBytes(2)).order(ByteOrder.LITTLE_ENDIAN).getShort();
                timeToLive = in.read();
            }

            headerCheckSum = in.read();

            var dataFrameBytes = in.readNBytes(frameDataLength);
            decodeService(dataFrameBytes);

            var crcBytes = in.readNBytes(2);
            servicesFrameDataCheckSum = ByteBuffer.wrap(crcBytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return this;
    }

    private void decodeFlags(int flag) {
        prefix = (flag & 0b11000000) == 0b11000000;
        route = (flag & 0b00100000) == 0b00100000;
        encryptionAlg = (flag & 0b00011000) == 0b00011000;
        compression = (flag & 0b00000100) == 0b00000100;
        priority = (flag & 0b00000011) == 0b00000011;
    }

    private void decodeService(byte[] dataFrameBytes) {
        switch (packetType) {
            case EGTS_PT_RESPONSE -> servicesFrameData = new PtResponse();
            case EGTS_PT_APPDATA -> servicesFrameData = new ServiceDataSet();
            default -> throw new RuntimeException("Unknown package type: " + packetType);
        }
        servicesFrameData.decode(dataFrameBytes);
    }


    @Override
    public byte[] encode() {
        var bytesOut = new ByteArrayOutputStream();
        try {
            byte[] sfrd = servicesFrameData.encode();

            short frameDataLength = (short) sfrd.length;
            short packageIdentifierShort = (short) packageIdentifier;

            bytesOut.write(protocolVersion);
            bytesOut.write(securityKeyId);
            bytesOut.write(calculateFlags());
            bytesOut.write(headerLength);
            bytesOut.write(headerEncoding);
            bytesOut.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(frameDataLength).array());
            bytesOut.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(packageIdentifierShort).array());
            bytesOut.write(packetType.getId());
            bytesOut.write(calculateCrc8(bytesOut.toByteArray()));

            if (frameDataLength > 0) {
                bytesOut.write(sfrd);
                bytesOut.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort((short) calculateCrc16(sfrd)).array());
            }

            return bytesOut.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private byte calculateFlags() {
        var flagsBits = getStringFromBool(prefix)
                + getStringFromBool(route)
                + getStringFromBool(encryptionAlg)
                + getStringFromBool(compression)
                + getStringFromBool(priority);
        return Byte.parseByte(flagsBits);
    }

    @Override
    public int length() {
        return 0;
    }
}
