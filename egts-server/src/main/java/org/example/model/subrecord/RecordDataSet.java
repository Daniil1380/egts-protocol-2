package org.example.model.subrecord;

import lombok.Data;
import org.example.model.BinaryData;
import org.example.model.responseentity.ResultCodeResponse;
import org.example.model.responseentity.SrResponse;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

@Data
public class RecordDataSet implements BinaryData {

    private List<BinaryData> recordDataList = new ArrayList<>();

    @Override
    public BinaryData decode(byte[] recDS) {
        //TODO переделать
        var inputStream = new ByteArrayInputStream(recDS);
        var in = new BufferedInputStream(inputStream);
        while (true) {
            try {
                if (!(in.available() > 0)) break;

                var subrecordType = SubrecordType.fromId(in.read());
                var subrecordLength = ByteBuffer.wrap(in.readNBytes(2))
                        .order(ByteOrder.LITTLE_ENDIAN).getShort();
                var subrecordBytes = in.readNBytes(subrecordLength);

                BinaryData data = subrecordType == SubrecordType.RESULT_CODE ? new ResultCodeResponse() : new SrResponse();
                data.decode(subrecordBytes);

                var rd = new RecordData(data, subrecordType);
                recordDataList.add(rd);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return this;
    }

    @Override
    public byte[] encode() {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        try {
            for (BinaryData rd : recordDataList) {
                bytesOut.write(rd.encode());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bytesOut.toByteArray();
    }

    @Override
    public int length() {
        return recordDataList.stream()
                .map(BinaryData::length)
                .reduce(Integer::sum)
                .orElse(0);
    }
}
