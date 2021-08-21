package co.com.ies.pruebas.webservice;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class AddTaskService{

    public static final String LOCK_ADD_TASK = "Lock.AddTask";

    private final RedissonClient redissonClient;

    private final AsyncProcessService asyncProcessService;

    private final AtomicBoolean onProcess = new AtomicBoolean(false);

    public AddTaskService(RedissonClient redissonClient, AsyncProcessService asyncProcessService) {


        this.redissonClient = redissonClient;

        this.asyncProcessService = asyncProcessService;
    }

    @Scheduled(fixedDelay = 1000)
    public void scheduleFixedDelayAddTask() {
        processTask();

    }

    @Async
    public void processTask() {
        System.out.println("scheduleFixedDelayAddTask Fixed delay task - " + System.currentTimeMillis() / 1000);

        if(onProcess.get()){
            System.out.println("AddTaskService.processTask skip");
            return;
        }

        onProcess.set(true);

        final RLock lock = redissonClient.getLock(LOCK_ADD_TASK);

        lock.lock();
        System.out.println("AsyncProcessServiceImpl.scheduleFixedDelayAddTask adquire");
        try {
            asyncProcessService.addTasks();
        } finally {
            lock.unlock();
        }
        System.out.println("AsyncProcessServiceImpl.scheduleFixedDelayAddTask release");
        onProcess.set(false);
    }

}
