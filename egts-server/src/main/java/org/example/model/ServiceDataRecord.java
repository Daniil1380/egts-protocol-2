package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDataRecord implements BinaryData {

    private int recordLength;
    private int recordNumber;
    private String sourceServiceOnDevice;
    private String recipientServiceOnDevice;
    private String group;
    private String recordProcessingPriority;
    private String timeFieldExists;
    private String eventIdFieldExists;
    private String objectIdFieldExists;
    private int objectIdentifier;
    private int eventIdentifier;
    private int time;
    private ServiceType sourceServiceType;
    private ServiceType recipientServiceType;
    private RecordDataSet recordDataSet;

    private final static String NOT_EXISTS = "0";
    private final static String EXISTS = "1";
    private final static Long TIMESTAMP_IN_2010 = 1262304000L;

    public ServiceDataRecord(int recordNumber, ServiceType recipientServiceType, RecordDataSet recordDataSet, Instant now) {
        this.recordNumber = recordNumber;
        this.sourceServiceOnDevice = NOT_EXISTS;
        this.recipientServiceOnDevice = NOT_EXISTS;
        this.group = NOT_EXISTS;
        this.recordProcessingPriority = NOT_EXISTS;
        this.timeFieldExists = NOT_EXISTS;
        this.time = (int) (now.getEpochSecond() - TIMESTAMP_IN_2010);
        this.eventIdFieldExists = NOT_EXISTS;
        this.objectIdFieldExists = NOT_EXISTS;
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
                var flagBits = sourceServiceOnDevice + recipientServiceOnDevice + group
                        + recordProcessingPriority + timeFieldExists + objectIdFieldExists;
                var flags = Integer.parseInt(flagBits, 2);
                bytesOut.write(flags);

                if (objectIdFieldExists.equals("1")) {
                    bytesOut.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(objectIdentifier).array());
                }

                if (eventIdFieldExists.equals("1")) {
                    bytesOut.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(eventIdentifier).array());
                }

                if (timeFieldExists.equals("1")) {
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

    @Override
    public int length() {
        return 0;
    }
}
