package org.example;

import com.sun.jna.NativeLibrary;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;

import java.util.List;


public class RawUdpSocketClient {
    private PcapHandle pcapHandle;

    static {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            NativeLibrary.addSearchPath("wpcap", "C:\\Windows\\System32\\Npcap");
        }
    }


    @SneakyThrows
    public void initialize() {
        List<PcapNetworkInterface> allDevs = Pcaps.findAllDevs();
        PcapNetworkInterface networkInterface = null;
        for (PcapNetworkInterface allDev : allDevs) {
            if (allDev.getName().equals("\\Device\\NPF_Loopback")){
                networkInterface = allDev;
                break;
            }
        }
        pcapHandle = networkInterface.openLive(65536, PcapNetworkInterface.PromiscuousMode.NONPROMISCUOUS, 50);
    }
    @SneakyThrows
    public void send(byte[] udpPacket){
        pcapHandle.sendPacket(udpPacket);
    }



}
