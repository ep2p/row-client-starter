package lab.idioglossia.row.client;

import lab.idioglossia.row.client.callback.RowTransportListener;
import lab.idioglossia.row.client.ws.RowWebsocketSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;

import javax.websocket.CloseReason;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class HeartbeatTransportListenerDecorator extends TransportListenerDecorator {
    private final HeartbeatRunner heartbeatRunner;

    public HeartbeatTransportListenerDecorator(RowTransportListener rowTransportListener, ThreadPoolTaskScheduler taskScheduler, int sec) {
        super(rowTransportListener);
        this.heartbeatRunner = new HeartbeatRunner(taskScheduler, sec);
    }

    @Override
    public void onOpen(RowWebsocketSession rowWebsocketSession) {
        super.onOpen(rowWebsocketSession);
        this.heartbeatRunner.run(rowWebsocketSession);
    }

    @Override
    public void onError(RowWebsocketSession rowWebsocketSession, Throwable throwable) {
        super.onError(rowWebsocketSession, throwable);
    }

    @Override
    public void onClose(RowClient rowClient, RowWebsocketSession rowWebsocketSession, CloseReason closeReason) {
        super.onClose(rowClient, rowWebsocketSession, closeReason);
        this.heartbeatRunner.stop();
    }

    @Slf4j
    private static class HeartbeatRunner {
        private final TaskScheduler taskScheduler;
        private final int seconds;

        private HeartbeatRunner(ThreadPoolTaskScheduler taskScheduler, int seconds) {
            this.taskScheduler = taskScheduler;
            this.seconds = seconds;
        }

        public void run(RowWebsocketSession rowWebsocketSession){
            Trigger trigger = new PeriodicTrigger(seconds, TimeUnit.SECONDS);
            taskScheduler.schedule(new Runnable() {
                @Override
                public void run() {
                    try {
                        if(rowWebsocketSession.isOpen()){
                            rowWebsocketSession.sendPingMessage(null);
                        }
                    } catch (IOException e) {
                        log.error("Failed to send ping packet", e);
                    }
                }
            }, trigger);
        }

        public void stop(){
            ((ThreadPoolTaskExecutor) taskScheduler).shutdown();
        }
    }
}
