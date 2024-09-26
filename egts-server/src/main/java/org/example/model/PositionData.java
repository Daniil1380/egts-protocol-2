package org.example.model;

import lombok.Data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Instant;

import static org.example.Main.byteArrayToHex;

@Data
public class PositionData implements BinaryData {

    private int navigationTime;
    private int latitude;
    private int longitude;

    private String altitudeExists;
    private String southSide;
    private String westSide;
    private String isMoving;
    private String fromMemory;
    private String is3dFix;
    private String isGovernmentCoordinate;
    private String isValidData;
    private double speed;
    private int direction;
    private int odometer;
    private byte digitalInputs;
    private byte source;

    private static final String NOT_EXISTS_OR_NOT = "0";
    private static final String TRUE = "1";

    private final static Long TIMESTAMP_IN_2010 = 1262304000L;

    public PositionData(Instant navigationTime, int latitude, int longitude, String southSide, String westSide, String isMoving, double speed, int direction, int odometer) {
        this.navigationTime = (int) (navigationTime.getEpochSecond() - TIMESTAMP_IN_2010);
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitudeExists = NOT_EXISTS_OR_NOT;
        this.southSide = southSide;
        this.westSide = westSide;
        this.isMoving = isMoving;
        this.fromMemory = NOT_EXISTS_OR_NOT;
        this.is3dFix = NOT_EXISTS_OR_NOT;
        this.isGovernmentCoordinate = NOT_EXISTS_OR_NOT;
        this.isValidData = TRUE;
        this.speed = speed;
        this.direction = direction;
        this.odometer = odometer;
        this.digitalInputs = (byte) 0;
        this.source = (byte) 0;
    }

    @Override
    public BinaryData decode(byte[] content) {
        return null;
    }

    @Override
    public byte[] encode() {
        try (ByteArrayOutputStream bytesOut = new ByteArrayOutputStream()) {

            bytesOut.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(navigationTime).array());

            int unsignedMultiplyForLatitude = Integer.divideUnsigned(0xFFFFFFFF, 90);
            int unsignedMultiplyForLongitude = Integer.divideUnsigned(0xFFFFFFFF, 180);

            int latitudeNormalized = latitude * unsignedMultiplyForLatitude;
            int longitudeNormalized = longitude * unsignedMultiplyForLongitude;

            bytesOut.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(latitudeNormalized).array());
            bytesOut.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(longitudeNormalized).array());

            var flagBits =
                    altitudeExists
                            + southSide
                            + westSide
                            + isMoving
                            + fromMemory
                            + is3dFix
                            + isGovernmentCoordinate
                            + isValidData;
            ;
            var flags = Integer.parseInt(flagBits, 2);

            bytesOut.write(flags);

            int speedInt = (int) (speed * 10);

            if (direction > 0xFF) {
                speedInt = speedInt + 0x8000;
            }

            bytesOut.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort((short) (speedInt)).array());

            bytesOut.write((byte) direction);

            bytesOut.write((byte) 0); //TODO изменим пробег
            bytesOut.write((byte) 0);
            bytesOut.write((byte) 0);

            bytesOut.write(digitalInputs);
            bytesOut.write(source);


            return bytesOut.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int length() { return 21;}
}
