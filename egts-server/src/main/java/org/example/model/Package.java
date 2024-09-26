package org.example.model;

import lombok.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.example.util.CrcUtil.calculateCrc16;
import static org.example.util.CrcUtil.calculateCrc8;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Package implements BinaryData {

    private int protocolVersion;
    private int securityKeyId;
    private String prefix;
    private String route;
    private String encryptionAlg;
    private String compression;
    private String priority;
    private int headerLength;
    private int headerEncoding;
    private int frameDataLength;
    private int packageIdentifier;
    private PacketType packetType;
    private int peerAddress;
    private int recipientAddress;
    private int timeToLive;
    private int headerCheckSum;
    private BinaryData servicesFrameData;
    private int servicesFrameDataCheckSum;

    private static final int PROTOCOL_VERSION = 1;
    private static final int SECURITY_KEY_ID = 0;
    private static final int HEADER_LENGTH = 11;
    private static final int HEADER_ENCODING = 0;

    private static final String ZERO_STRING = "0";

    public Package(int packageIdentifier, PacketType packetType, BinaryData servicesFrameData) {
        this.protocolVersion = PROTOCOL_VERSION;
        this.securityKeyId = SECURITY_KEY_ID;
        this.prefix = ZERO_STRING;
        this.route = ZERO_STRING;
        this.encryptionAlg = ZERO_STRING;
        this.compression = ZERO_STRING;
        this.priority = ZERO_STRING;
        this.headerLength = HEADER_LENGTH;
        this.headerEncoding = HEADER_ENCODING;
        this.packageIdentifier = packageIdentifier;
        this.packetType = packetType;
        this.servicesFrameData = servicesFrameData;
    }

    // Decode разбирает набор байт в структуру пакета
    @Override
    public BinaryData decode(byte[] content) {
        var inputStream = new ByteArrayInputStream(content);
        var in = new BufferedInputStream(inputStream);
        try {
            protocolVersion = Byte.toUnsignedInt(in.readNBytes(1)[0]);
            securityKeyId = Byte.toUnsignedInt(in.readNBytes(1)[0]);

            var flag = Integer.toBinaryString(in.read());
            if (flag.length() < 8) {
                flag = "0".repeat(8 - flag.length()) +
                        flag;
            }

            prefix = String.valueOf(flag.charAt(0)) + flag.charAt(1);        // flags << 7, flags << 6
            route = String.valueOf(flag.charAt(2));                   // flags << 5
            encryptionAlg = String.valueOf(flag.charAt(3)) + flag.charAt(4); // flags << 4, flags << 3
            compression = String.valueOf(flag.charAt(5));             // flags << 2
            priority = String.valueOf(flag.charAt(6)) + flag.charAt(7);                // flags << 1, flags << 0

            headerLength = Byte.toUnsignedInt(in.readNBytes(1)[0]);
            headerEncoding = Byte.toUnsignedInt(in.readNBytes(1)[0]);


            frameDataLength = ByteBuffer.wrap(in.readNBytes(2))
                    .order(ByteOrder.LITTLE_ENDIAN).getShort();
            packageIdentifier = ByteBuffer.wrap(in.readNBytes(2))
                    .order(ByteOrder.LITTLE_ENDIAN).getShort();

            packetType = PacketType.fromId((Byte.toUnsignedInt(in.readNBytes(1)[0])));

            if (route.equals("1")) {
                peerAddress = ByteBuffer.wrap(in.readNBytes(2))
                        .order(ByteOrder.LITTLE_ENDIAN).getShort();
                recipientAddress = ByteBuffer.wrap(in.readNBytes(2))
                        .order(ByteOrder.LITTLE_ENDIAN).getShort();
                timeToLive = Byte.toUnsignedInt(in.readNBytes(1)[0]);
            }

            headerCheckSum = Byte.toUnsignedInt(in.readNBytes(1)[0]);

            var dataFrameBytes = in.readNBytes(frameDataLength);
            switch (packetType) {
                case EGTS_PT_RESPONSE -> servicesFrameData = new PtResponse();
                case EGTS_PT_APPDATA -> servicesFrameData = new ServiceDataSet();
                default -> throw new RuntimeException("Unknown package type: " + packetType);
            }
            servicesFrameData.decode(dataFrameBytes);

            var crcBytes = in.readNBytes(2);
            servicesFrameDataCheckSum = ByteBuffer.wrap(crcBytes)
                    .order(ByteOrder.LITTLE_ENDIAN).getShort();

            byte[] data = new byte[frameDataLength];
            int idx = 0;
            for (int i = headerLength; i < headerLength + frameDataLength; i++) {
                data[idx++] = content[i];
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return this;
    }

    // Encode кодирует струткуру в байтовую строку
    @Override
    public byte[] encode() {
        var bytesOut = new ByteArrayOutputStream();
        try {
            bytesOut.write(protocolVersion);
            bytesOut.write(securityKeyId);

            var flagsBits = prefix + route + encryptionAlg + compression + priority;
            var flags = Short.parseShort(flagsBits);
            bytesOut.write(flags);

            bytesOut.write(headerLength);

            bytesOut.write(headerEncoding);

            byte[] sfrd = new byte[0];
            if (servicesFrameData != null) {
                sfrd = servicesFrameData.encode();
            }

            short frameDataLength = (short) sfrd.length;
            short packageIdentifierShort = (short) packageIdentifier;

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

    @Override
    public int length() {
        return 0;
    }
}
