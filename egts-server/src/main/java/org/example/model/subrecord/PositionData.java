package org.example.model.subrecord;

import lombok.Data;
import org.example.model.BinaryData;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Instant;

import static org.example.util.BooleanUtil.getStringFromBool;

@Data
public class PositionData implements BinaryData {

    private Instant navigationTime;
    private int latitude;
    private int longitude;
    private boolean altitudeExists;
    private HemisphereType southSide;
    private HemisphereType westSide;
    private boolean isMoving;
    private boolean fromMemory;
    private boolean is3dFix;
    private boolean isGovernmentCoordinate;
    private boolean isValidData;
    private double speed;
    private int direction;
    private int odometer;
    private byte digitalInputs;
    private byte source;

    private final static Long TIMESTAMP_IN_2010 = 1262304000L;
    private final static int SPEED_MULTIPLIER = 10;
    private final static byte ZERO_BYTE = 0;
    private final static int MAX_BYTE = 0xFF;
    private final static int MAX_LATITUDE = 90;
    private final static int MAX_LONGITUDE = 180;
    private final static int MAX_VALUE_UNSIGNED_INT = 0xFFFFFFFF;
    private final static int HIGH_BIT_IN_BYTE = 0x8000;
    private final static int SIZE_OF_DATA = 21;

    public PositionData(Instant navigationTime, int latitude, int longitude, HemisphereType southSide, HemisphereType westSide, boolean isMoving, double speed, int direction, int odometer) {
        this.navigationTime = navigationTime;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitudeExists = false;
        this.southSide = southSide;
        this.westSide = westSide;
        this.isMoving = isMoving;
        this.fromMemory = false;
        this.is3dFix = false;
        this.isGovernmentCoordinate = false;
        this.isValidData = true;
        this.speed = speed;
        this.direction = direction;
        this.odometer = odometer;
        this.digitalInputs = ZERO_BYTE;
        this.source = ZERO_BYTE;
    }

    @Override
    public BinaryData decode(byte[] content) {
        return null;
    }

    @Override
    public byte[] encode() {
        try (ByteArrayOutputStream bytesOut = new ByteArrayOutputStream()) {
            int latitudeNormalized = normalizeCoordinate(latitude, MAX_LATITUDE);
            int longitudeNormalized = normalizeCoordinate(latitude, MAX_LONGITUDE);

            bytesOut.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(calculateTime()).array());
            bytesOut.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(latitudeNormalized).array());
            bytesOut.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(longitudeNormalized).array());
            bytesOut.write(calculateFlags());
            bytesOut.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(calculateSpeed()).array());
            bytesOut.write((byte) direction);
            bytesOut.write(ZERO_BYTE); //TODO изменим пробег
            bytesOut.write(ZERO_BYTE);
            bytesOut.write(ZERO_BYTE);
            bytesOut.write(digitalInputs);
            bytesOut.write(source);


            return bytesOut.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int normalizeCoordinate(int coordinate, int maxValue) {
        int unsignedMultiplier = Integer.divideUnsigned(MAX_VALUE_UNSIGNED_INT, maxValue);
        return coordinate * unsignedMultiplier;
    }

    private int calculateFlags() {
        var flagBits =
                getStringFromBool(altitudeExists)
                        + southSide.getValue()
                        + westSide.getValue()
                        + getStringFromBool(isMoving)
                        + getStringFromBool(fromMemory)
                        + getStringFromBool(is3dFix)
                        + getStringFromBool(isGovernmentCoordinate)
                        + getStringFromBool(isValidData);

        return Integer.parseInt(flagBits, 2);
    }

    private short calculateSpeed() {
        int speedInt = (int) (speed * SPEED_MULTIPLIER);

        if (direction > MAX_BYTE) {
            speedInt = speedInt + HIGH_BIT_IN_BYTE;
        }

        return (short) speedInt;
    }

    private int calculateTime() {
        return (int) (navigationTime.getEpochSecond() - TIMESTAMP_IN_2010);
    }

    @Override
    public int length() { return SIZE_OF_DATA;}
}
