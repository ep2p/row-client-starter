package io.ep2p.row.client;

import io.ep2p.row.client.callback.RowTransportListener;
import io.ep2p.row.client.ws.WebsocketSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;

import javax.websocket.CloseReason;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

public class HeartbeatTransportListenerDecorator<S extends WebsocketSession> extends TransportListenerDecorator<S> {
    private final HeartbeatRunner heartbeatRunner;

    public HeartbeatTransportListenerDecorator(RowTransportListener<S> rowTransportListener, ThreadPoolTaskScheduler taskScheduler, int sec) {
        super(rowTransportListener);
        this.heartbeatRunner = new HeartbeatRunner(taskScheduler, sec);
    }

    @Override
    public void onOpen(S rowWebsocketSession) {
        super.onOpen(rowWebsocketSession);
        this.heartbeatRunner.run(rowWebsocketSession);
    }

    @Override
    public void onError(S rowWebsocketSession, Throwable throwable) {
        super.onError(rowWebsocketSession, throwable);
    }

    @Override
    public void onClose(RowClient rowClient, S rowWebsocketSession, CloseReason closeReason) {
        super.onClose(rowClient, rowWebsocketSession, closeReason);
        this.heartbeatRunner.stop();
    }

    @Slf4j
    private static class HeartbeatRunner<S extends WebsocketSession> {
        private final ThreadPoolTaskScheduler taskScheduler;
        private final int seconds;

        private HeartbeatRunner(ThreadPoolTaskScheduler taskScheduler, int seconds) {
            this.taskScheduler = taskScheduler;
            this.seconds = seconds;
        }

        public void run(S rowWebsocketSession){
            Trigger trigger = new PeriodicTrigger(seconds, TimeUnit.SECONDS);
            taskScheduler.schedule(new Runnable() {
                @Override
                public void run() {
                    try {
                        if(rowWebsocketSession.isOpen()){
                            rowWebsocketSession.sendPingMessage(ByteBuffer.allocate(0));
                        }
                    } catch (Exception e) {
                        log.error("Failed to send ping packet", e);
                        stop();
                    }
                }
            }, trigger);
        }

        public void stop(){
            taskScheduler.shutdown();
        }
    }
}
