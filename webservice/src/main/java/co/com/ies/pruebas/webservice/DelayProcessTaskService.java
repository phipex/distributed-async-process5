package co.com.ies.pruebas.webservice;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DelayProcessTaskService {

    private final AsyncProcessService asyncProcessService;

    public DelayProcessTaskService( AsyncProcessService asyncProcessService) {

        this.asyncProcessService = asyncProcessService;
    }

    @Scheduled(fixedDelay = 1000)
    public void scheduleFixedDelayProcessTask() {
        System.out.println("scheduleFixedDelayProcessTask Fixed delay task - " + System.currentTimeMillis() / 1000);

        System.out.println("AsyncProcessServiceImpl.scheduleFixedDelayProcessTask adquire");
        asyncProcessService.processTaskList();

        System.out.println("AsyncProcessServiceImpl.scheduleFixedDelayProcessTask release");

    }

}
