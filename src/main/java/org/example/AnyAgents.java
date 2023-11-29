package org.example;

import jade.core.AID;
import jade.core.Agent;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class AnyAgents extends Agent {
    @Getter
    private List<AID> aids = new ArrayList<>();
    private final ScheduledExecutorService es = Executors.newScheduledThreadPool(1);
    public final AgentDetector agentDetector = new AgentDetector();
    @Override
    protected void setup() {
        agentDetector.startDiscovering(12);
        agentDetector.startPublishing(getAID(),12);
        aids = agentDetector.deleteMyself(getAID(), aids);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (getAID().getLocalName().equals("Agent1")) {
                    doDelete();
                    agentDetector.stop();
                }
            }
        }, 10000);

    }

    @Override
    protected void takeDown() {
        super.takeDown();
    }


}
