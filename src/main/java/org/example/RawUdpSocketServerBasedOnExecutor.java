package org.example;

import jade.core.AID;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.pcap4j.core.PcapHandle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Slf4j
public class RawUdpSocketServerBasedOnExecutor extends RawUdpSocketServer{
    private ScheduledExecutorService es;

    @Getter
    private final List<AID> updatedAIDsList = Collections.synchronizedList(new ArrayList<>());
    private final Lock lock = new ReentrantLock();
    public RawUdpSocketServerBasedOnExecutor(ScheduledExecutorService es) {
        this.es = es;
    }

    @Override
    protected void runInThread(PcapHandle pcapHandle) {
        es.schedule(() -> this.grabPackets(pcapHandle), 0, TimeUnit.MILLISECONDS);
    }

    public void stopServer(){
        this.run = false;
    }

    @SneakyThrows
    public void startUpdatingAIDsBySituation() {
        es.scheduleAtFixedRate(() -> {
            lock.lock();
            try {
                List<AgentInfoDto> newAgents = new ArrayList<>(getDataAid());
                List<AID> newAIDList = newAgents.stream().map(AgentInfoDto::toAID).collect(Collectors.toList());
                updatedAIDsList.clear();
                updatedAIDsList.addAll(newAIDList);
            } finally {
                lock.unlock();
            }
        }, 0, 3000, TimeUnit.MILLISECONDS);
    }
}

