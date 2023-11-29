package org.example;

import com.google.gson.Gson;
import com.sun.jna.NativeLibrary;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.pcap4j.core.*;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;



@Slf4j
public class RawUdpSocketServer {
    public static final List<AgentInfoDto> agentsListNon = new ArrayList<>();
    private static final Lock lock = new ReentrantLock();
    @Getter
    private final List<AgentInfoDto> agentsList = Collections.synchronizedList(agentsListNon);
    private final Gson gson = new Gson();
    @Setter
    private long periodTimeToSendAgentInfo;
    private final Map<String, Long> lastActivityTimesNon = new HashMap<>();
    private final Map<String, Long> lastActivityTimes = Collections.synchronizedMap(lastActivityTimesNon);


    static {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            NativeLibrary.addSearchPath("wpcap", "C:\\Windows\\System32\\Npcap");
        }
    }

    protected boolean run = true;

    @SneakyThrows
    public void start(int port){
        run = true;
        List<PcapNetworkInterface> allDevs = Pcaps.findAllDevs();
        PcapNetworkInterface networkInterface = null;
        for (PcapNetworkInterface allDev : allDevs) {
            if (allDev.getName().equals("\\Device\\NPF_Loopback")){
                networkInterface = allDev;
                break;
            }
        }
        if (networkInterface!=null) {
            log.info("Network interface was not found " + networkInterface);
        }
        PcapHandle pcapHandle = networkInterface.openLive(65536, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 50);
        pcapHandle.setFilter("ip proto \\udp && dst port "+port, BpfProgram.BpfCompileMode.NONOPTIMIZE);
        runInThread(pcapHandle);
    }


    protected void runInThread(PcapHandle pcapHandle) {
        new Thread( ()-> {
            grabPackets(pcapHandle);
        }).start();
    }

//   основная логика обработки входящих данных
    @SneakyThrows
    protected void grabPackets(PcapHandle pcapHandle) {
        try {
            pcapHandle.loop(0, (PacketListener) packet -> {
                checkStop(pcapHandle);
                byte[] rawData = packet.getRawData();
                byte[] data = new byte[rawData.length-32];
                System.arraycopy(rawData, 32, data, 0, data.length);
                log.info("Server received " + new String(data) + agentsList);
                AgentInfoDto newAgent = gson.fromJson(new String(data), AgentInfoDto.class);
                filterData(newAgent);
            });
        } catch (PcapNativeException | InterruptedException | NotOpenException e) {
            throw new RuntimeException(e);
        }
    }
    @SneakyThrows
    protected List<AgentInfoDto> getDataAid(){
        return agentsList;
    }

    @SneakyThrows
    protected void filterData(AgentInfoDto newAgent) {
        lock.lock();
        try {
            /*Работа с HashMap lastActivityTimes куда мы помещаем дынные об агенте и время, когда он помещается
             * Удаляем этого агента из map, если время, когда он был помещен больше, чем период отправки им сообщений  */
            lastActivityTimes.put(newAgent.getAgentName(), System.currentTimeMillis());
            lastActivityTimes.entrySet().removeIf(entry -> System.currentTimeMillis() - entry.getValue() > (periodTimeToSendAgentInfo + 5000));
            /*Добавляем в массив информацию об агенте, только если массив пустой или не содержит информацию и о нем */
            if (agentsList.isEmpty() || !agentsList.contains(newAgent)) {
                agentsList.add(newAgent);
            }
            /*Удаление умершего агента из листа агентов сервера */
            agentsList.removeIf(agent -> !lastActivityTimes.containsKey(agent.getAgentName()));

        } finally {
            lock.unlock();
        }
    }
    protected void checkStop(PcapHandle pcapHandle) {
        if (!run){
            try {
                pcapHandle.breakLoop();
            } catch (NotOpenException e) {
                e.printStackTrace();
            }
        }
    }

}