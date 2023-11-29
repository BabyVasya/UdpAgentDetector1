package org.example;

import com.google.gson.Gson;
import jade.core.AID;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class AgentDetector implements AgentDetectorInterface {
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(3);

    private final ScheduledExecutorService es = Executors.newScheduledThreadPool(1);
    private final Gson gson = new Gson();
    private final RawUdpSocketServerBasedOnExecutor server = new RawUdpSocketServerBasedOnExecutor(executorService);
    private final long periodSendingPacket = 3000;

    private final RawUdpSocketClientScheduled client = new RawUdpSocketClientScheduled(executorService);


    //Открытие потока для отправки информации о себе на сервер, информация отправляется с помощью AgentInfoDto
    @Override
    public void startPublishing(AID aid, int port) {
        AgentInfoDto agentInfoDto = new AgentInfoDto(aid.getLocalName(), false);
        client.initialize();
        server.setPeriodTimeToSendAgentInfo(periodSendingPacket);
        client.periodicSend(PacketCreator.createByteCode(gson.toJson(agentInfoDto), port, port), periodSendingPacket);
    }

   //Открытие потока сервера
    @Override
    public void startDiscovering(int port) {
        server.start(port);
    }

   //Возврат списка актуальных AID-ов с сервера
    @Override
    public List<AID> getActiveAgents() {
        server.startUpdatingAIDsBySituation();
        return server.getUpdatedAIDsList();
    }

    public void stop() {
        client.stop();
    }

//  Удаление самого себя из возвращаемого с сервера списка
    public List<AID> deleteMyself(AID myself, List<AID> myAidsList) {
        es.scheduleAtFixedRate(() -> {
            List<AID> aids = getActiveAgents();
            aids.remove(myself);
            myAidsList.clear();
            myAidsList.addAll(aids);
            log.info("Лист агента " + myself.getLocalName() + " " + myAidsList);
        }, 0, 3000, TimeUnit.MILLISECONDS);
        return myAidsList;
    }
}
