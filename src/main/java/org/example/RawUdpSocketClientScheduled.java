package org.example;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
@Slf4j
    public class RawUdpSocketClientScheduled extends RawUdpSocketClient {
        private ScheduledExecutorService ses;
        private ScheduledFuture<?> scheduledFuture;

        public RawUdpSocketClientScheduled(ScheduledExecutorService ses) {
            this.ses = ses;
        }

        @SneakyThrows
        public void periodicSend(byte[] data, long period){
            scheduledFuture = ses.scheduleAtFixedRate(() -> {
                send(data);
            }, 0, period, TimeUnit.MILLISECONDS);
        }

        public void stop(){
            scheduledFuture.cancel(false);
        }

    }

