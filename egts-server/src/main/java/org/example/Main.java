package org.example;

import org.example.model.subrecord.AuthData;
import org.example.model.Package;
import org.example.model.PacketType;
import org.example.model.subrecord.HemisphereType;
import org.example.model.subrecord.PositionData;
import org.example.model.responseentity.PtResponse;
import org.example.model.subrecord.RecordData;
import org.example.model.subrecord.RecordDataSet;
import org.example.model.service.ServiceDataRecord;
import org.example.model.service.ServiceDataSet;
import org.example.model.responseentity.SrResponse;
import org.example.model.subrecord.SubrecordType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.time.Instant;
import java.util.List;

import static org.example.model.service.ServiceType.EGTS_AUTH_SERVICE;
import static org.example.model.service.ServiceType.EGTS_TELEDATA_SERVICE;

public class Main {

    public static void main(String[] args) throws IOException {
        AuthData authData = new AuthData(3016);
        RecordData recordData = new RecordData(authData, SubrecordType.EGTS_SR_DISPATCHER_IDENTITY);

        RecordDataSet recordDataSet = new RecordDataSet();
        recordDataSet.setRecordDataList(List.of(recordData));

        byte zero = 0;
        ServiceDataRecord serviceDataRecord = new ServiceDataRecord(zero, EGTS_AUTH_SERVICE, recordDataSet, Instant.now());
        ServiceDataSet serviceDataSet = new ServiceDataSet();
        serviceDataSet.setServiceDataRecords(List.of(serviceDataRecord));

        Package pa = new Package(0, PacketType.EGTS_PT_APPDATA, serviceDataSet);





        SrResponse srResponse = new SrResponse((short) 1, (byte) 0);
        RecordData recordData0 = new RecordData(srResponse, SubrecordType.EGTS_SR_RECORD_RESPONSE);
        RecordDataSet recordDataSet0 = new RecordDataSet();
        recordDataSet0.setRecordDataList(List.of(recordData0));
        ServiceDataRecord serviceDataRecord0 = new ServiceDataRecord(zero, EGTS_AUTH_SERVICE, recordDataSet0, Instant.now());
        PtResponse ptResponse = new PtResponse(1, 0, serviceDataRecord0);
        Package pa0 = new Package(1, PacketType.EGTS_PT_RESPONSE, ptResponse);

        Socket socket = new Socket("data.rnis.mos.ru", 4050);
        //Socket socket = new Socket("localhost", 9090);
        byte[] array = pa.encode();
        byte[] array0 = pa0.encode();

        // Получаем потоки ввода-вывода
        OutputStream outputStream = socket.getOutputStream();
        InputStream inputStream = socket.getInputStream();

        outputStream.write(array);
        outputStream.flush();

        byte[] buffer = new byte[64];

        inputStream.read(buffer);
        Package responseFirst = new Package();
        responseFirst.decode(buffer);

        System.out.println(responseFirst);

        inputStream.read(buffer);

        Package responseSecond = new Package();
        responseSecond.decode(buffer);

        System.out.println(responseSecond);


        outputStream.write(array0);
        outputStream.flush();


        for (int i = 0; i < 9; i++) {
            PositionData positionData = new PositionData(Instant.now(), 50, 51, HemisphereType.NORTH, HemisphereType.EAST, true, 5.6, 300,0);
            RecordData recordDataPosition = new RecordData(positionData, SubrecordType.EGTS_SR_POS_DATA);

            RecordDataSet recordDataSetPosition = new RecordDataSet();
            recordDataSetPosition.setRecordDataList(List.of(recordDataPosition));

            ServiceDataRecord serviceDataRecordPosition = new ServiceDataRecord((byte) 1, EGTS_TELEDATA_SERVICE, recordDataSetPosition, Instant.now());
            ServiceDataSet serviceDataSetPosition = new ServiceDataSet();
            serviceDataSetPosition.setServiceDataRecords(List.of(serviceDataRecordPosition));

            Package pa1 = new Package(1 + i, PacketType.EGTS_PT_APPDATA, serviceDataSetPosition);

            byte[] array1 = pa1.encode();

            outputStream.write(array1);
            outputStream.flush();

            inputStream.read(buffer);
            Package responsePosition = new Package();
            responsePosition.decode(buffer);

            //System.out.println(Arrays.toString(buffer));
            System.out.println(responsePosition);
        }
    }

    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (byte b : a)
            sb.append(String.format("%02X ", b));
        return sb.toString();
    }

}
