package org.example;

import lombok.SneakyThrows;
import java.nio.ByteBuffer;


public class PacketCreator {
    @SneakyThrows
    public static byte[] createByteCode(String jsonPacket, int dstPort, int srcPort) {
        byte[] udpPayload = jsonPacket.getBytes();
        byte[] bytes = {2, 0, 0, 0, 69, 0, 0, 75, 0, 0, 0, 0, 127, 17, 0, 0, 127, 0, 0, 1, 127, 0, 0, 1,  0, (byte) srcPort,  0, (byte) dstPort, 0, 55, 39, 82};
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length + udpPayload.length);
        buffer.put(bytes);
        buffer.put(udpPayload);
        return buffer.array();
    }
}
