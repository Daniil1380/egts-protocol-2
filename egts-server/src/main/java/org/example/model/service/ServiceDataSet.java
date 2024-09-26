package org.example.model.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.model.BinaryData;
import org.example.model.subrecord.RecordDataSet;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceDataSet implements BinaryData {

    private List<BinaryData> serviceDataRecords;

    @Override
    public BinaryData decode(byte[] serviceDS) {
        serviceDataRecords = new ArrayList<>();
        var inputStream = new ByteArrayInputStream(serviceDS);
        var in = new BufferedInputStream(inputStream);
        while (true) {
            try {
                if (!(in.available() > 0)) break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            try {
                var sdr = new ServiceDataRecord();
                var recordLength = ByteBuffer.wrap(in.readNBytes(2))
                        .order(ByteOrder.LITTLE_ENDIAN).getShort();
                sdr.setRecordLength(recordLength);

                var recordNumber = ByteBuffer.wrap(in.readNBytes(2))
                        .order(ByteOrder.LITTLE_ENDIAN).getShort();
                sdr.setRecordNumber(recordNumber);

                var flags = Integer.toBinaryString(in.read());
                if (flags.length() < 8) {
                    flags = "0".repeat(8 - flags.length()) +
                            flags;
                }
                sdr.setSourceServiceOnDevice(String.valueOf(flags.charAt(0)));
                sdr.setRecipientServiceOnDevice(String.valueOf(flags.charAt(1)));
                sdr.setGroup(String.valueOf(flags.charAt(2)));
                sdr.setRecordProcessingPriority(String.valueOf(flags.charAt(3)) + flags.charAt(4));
                sdr.setTimeFieldExists(String.valueOf(flags.charAt(5)));
                sdr.setEventIdFieldExists(String.valueOf(flags.charAt(6)));
                sdr.setObjectIdFieldExists(String.valueOf(flags.charAt(7)));

                if (sdr.getObjectIdFieldExists().equals("1")) {
                    var objectIdentifier = ByteBuffer.wrap(in.readNBytes(4))
                            .order(ByteOrder.LITTLE_ENDIAN).getInt();
                    sdr.setObjectIdentifier(objectIdentifier);
                }

                if (sdr.getEventIdFieldExists().equals("1")) {
                    var eventIdentifier = ByteBuffer.wrap(in.readNBytes(4))
                            .order(ByteOrder.LITTLE_ENDIAN).getInt();
                    sdr.setEventIdentifier(eventIdentifier);
                }

                if (sdr.getTimeFieldExists().equals("1")) {
                    var time = ByteBuffer.wrap(in.readNBytes(4)).order(ByteOrder.LITTLE_ENDIAN).getInt();
                    sdr.setTime(time);
                }

                sdr.setRecipientServiceType(ServiceType.fromId(in.readNBytes(1)[0]));
                sdr.setRecipientServiceType(ServiceType.fromId(in.readNBytes(1)[0]));

                serviceDataRecords.add(sdr);

                // ? ? ?
                if (in.available() != 0) {
                    var rds = new RecordDataSet();
                    var rdsBytes = ByteBuffer.wrap(in.readNBytes(sdr.getRecordLength()))
                            .order(ByteOrder.LITTLE_ENDIAN).array();
                    rds.decode(rdsBytes);
                    sdr.setRecordDataSet(rds);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return this;
    }

    @Override
    public byte[] encode() {
        var bytesOut = new ByteArrayOutputStream();
        for (BinaryData sdr : serviceDataRecords) {
            try {
                bytesOut.write(sdr.encode());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return bytesOut.toByteArray();
    }

    @Override
    public int length() {
        var recBytes = this.encode();
        return recBytes.length;
    }
}
