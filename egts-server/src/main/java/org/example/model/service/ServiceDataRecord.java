package org.example.model.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.model.BinaryData;
import org.example.model.subrecord.RecordDataSet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Instant;

import static org.example.util.BooleanUtil.getStringFromBool;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDataRecord implements BinaryData {

    private int recordLength;
    private int recordNumber;
    private boolean sourceServiceOnDevice;
    private boolean recipientServiceOnDevice;
    private boolean group;
    private boolean recordProcessingPriority;
    private boolean timeFieldExists;
    private boolean eventIdFieldExists;
    private boolean objectIdFieldExists;
    private int objectIdentifier;
    private int eventIdentifier;
    private int time;
    private ServiceType sourceServiceType;
    private ServiceType recipientServiceType;
    private RecordDataSet recordDataSet;

    private final static Long TIMESTAMP_IN_2010 = 1262304000L;

    public ServiceDataRecord(int recordNumber, ServiceType recipientServiceType, RecordDataSet recordDataSet, Instant now) {
        this.recordNumber = recordNumber;
        this.sourceServiceOnDevice = false;
        this.recipientServiceOnDevice = false;
        this.group = false;
        this.recordProcessingPriority = false;
        this.timeFieldExists = false;
        this.time = (int) (now.getEpochSecond() - TIMESTAMP_IN_2010);
        this.eventIdFieldExists = false;
        this.objectIdFieldExists = false;
        this.sourceServiceType = recipientServiceType;
        this.recipientServiceType = recipientServiceType;
        this.recordDataSet = recordDataSet;
    }

    @Override
    public BinaryData decode(byte[] content) {
        return null;
    }

    @Override
    public byte[] encode() {
        var bytesOut = new ByteArrayOutputStream();
            var rd = recordDataSet.encode();

            short recordLength = (short) rd.length;
            short recordNumberShort = (short) recordNumber;


            try {
                bytesOut.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(recordLength).array());
                bytesOut.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(recordNumberShort).array());

                // составной байт
                var flagBits = calculateFlags();
                bytesOut.write(flagBits);

                if (objectIdFieldExists) {
                    bytesOut.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(objectIdentifier).array());
                }

                if (eventIdFieldExists) {
                    bytesOut.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(eventIdentifier).array());
                }

                if (timeFieldExists) {
                    bytesOut.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(time).array());

                }

                bytesOut.write(sourceServiceType.getId());
                bytesOut.write(recipientServiceType.getId());

                bytesOut.write(rd);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        return bytesOut.toByteArray();
    }

    private byte calculateFlags() {
        var flagsBits = getStringFromBool(sourceServiceOnDevice)
                + getStringFromBool(recipientServiceOnDevice)
                + getStringFromBool(group)
                + getStringFromBool(recordProcessingPriority)
                + getStringFromBool(timeFieldExists)
                + getStringFromBool(objectIdFieldExists);
        return Byte.parseByte(flagsBits);
    }

    @Override
    public int length() {
        return 0;
    }
}
