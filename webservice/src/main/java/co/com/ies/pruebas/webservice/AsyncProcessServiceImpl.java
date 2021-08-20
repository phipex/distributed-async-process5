package co.com.ies.pruebas.webservice;

import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
import java.util.stream.Collectors;

public class AsyncProcessServiceImpl implements AsyncProcessService{

    public static final String LOCK_ADD_TASK = "Lock.AddTask";
    public static final String LOCK_ADQUIRE_TASK = "Lock.AdquireTask";
    public static final String LOCK_PROCESS_TASKS = "Lock.ProcessTask";

    private final GreetingRepository greetingRepository;
    private final RedissonClient redissonClient;
    private final QeueuAsyncRedis qeueuAsyncRedis;

    private final ProcessorDelayedRedis processorDelayed;

    public AsyncProcessServiceImpl(GreetingRepository greetingRepository, RedissonClient redissonClient, QeueuAsyncRedis qeueuAsyncRedis, ProcessorDelayedRedis processorDelayed) {

        this.greetingRepository = greetingRepository;
        this.redissonClient = redissonClient;
        this.qeueuAsyncRedis = qeueuAsyncRedis;
        this.processorDelayed = processorDelayed;
    }

    @Scheduled(fixedDelay = 1000)
    public void scheduleFixedDelayAddTask() {
        //System.out.println("scheduleFixedDelayAddTask Fixed delay task - " + System.currentTimeMillis() / 1000);

        RSemaphore semaphore = redissonClient.getSemaphore(LOCK_ADD_TASK);
        final int availablePermits = semaphore.availablePermits();

        if(availablePermits == 0){
            final boolean trySetPermits = semaphore.trySetPermits(1);
            System.out.println("AsyncProcessServiceImpl.scheduleFixedDelayAddTask trySetPermits" + trySetPermits);
        }
        final boolean tryAcquire = semaphore.tryAcquire();
        System.out.println("AsyncProcessServiceImpl.scheduleFixedDelayAddTask " + tryAcquire + " availablePermits = " + availablePermits);
        if(tryAcquire){
            System.out.println("AsyncProcessServiceImpl.scheduleFixedDelayAddTask adquire");
            addTasks();
            semaphore.release();
            System.out.println("AsyncProcessServiceImpl.scheduleFixedDelayAddTask release");
        }

    }

    @Override
    public void addTasks(){
        final List<Greeting> greetingsByIpTramitedIsNull = greetingRepository.findByIpTramitedIsNull();

        if(greetingsByIpTramitedIsNull.isEmpty()){
            System.out.print("-");
            return;
        }

        final List<GreetingPendingTask> collect = greetingsByIpTramitedIsNull.stream()
                .filter(greeting -> greeting.getIpTramited() == null)
                .map(GreetingPendingTask::new)
                .collect(Collectors.toList());

        qeueuAsyncRedis.offerTascks(collect);
        final int size = greetingsByIpTramitedIsNull.size();
        System.out.println("AsyncProcessServiceImpl.addTasks size = " + size);
    }
    @Override
    public void processTaskList() {

    }
}
