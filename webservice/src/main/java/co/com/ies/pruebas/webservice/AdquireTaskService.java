package co.com.ies.pruebas.webservice;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class AdquireTaskService {

    public static final String LOCK_ADQUIRE_TASK = "Lock.AdquireTask";

    private final RedissonClient redissonClient;

    private final AsyncProcessService asyncProcessService;

    private final AtomicBoolean onProcess = new AtomicBoolean(false);

    public AdquireTaskService(RedissonClient redissonClient, AsyncProcessService asyncProcessService) {


        this.redissonClient = redissonClient;

        this.asyncProcessService = asyncProcessService;
    }

    @Scheduled(fixedDelay = 1000)
    public void scheduleFixedDelayAdquireTask() {
        processTask();

    }
    @Async
    public void processTask() {
        System.out.println("scheduleFixedDelayAdquireTask Fixed delay task - " + System.currentTimeMillis() / 1000);

        if(onProcess.get()){
            System.out.println("AdquireTaskService.processTask skip");
            return;
        }

        onProcess.set(true);

        final RLock lock = redissonClient.getLock(LOCK_ADQUIRE_TASK);

        lock.lock();
        System.out.println("AsyncProcessServiceImpl.scheduleFixedDelayAdquireTask adquire");
        try {
            asyncProcessService.adquireTasks();
        } finally {
            lock.unlock();
        }
        System.out.println("AsyncProcessServiceImpl.scheduleFixedDelayAdquireTask release");
        onProcess.set(false);
    }

}
