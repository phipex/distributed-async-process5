package co.com.ies.pruebas.webservice;

import org.redisson.api.*;
import org.redisson.client.codec.Codec;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Predicate;

@Component
public class QeueuAsyncRedis  extends QueueAsyncAbstract<GreetingPendingTask> {

    private static final String KEY_QEUEU = "Pending.TaskTest_Qeueu";
    private static final String KEY_EDIT_LOCK = "Edit.Lock.TaskTest_Qeueu";

    private final RedissonClient redissonClient;

    public QeueuAsyncRedis(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    protected void offer(GreetingPendingTask element) {
        final RLock lock = redissonClient.getLock(KEY_EDIT_LOCK);
        lock.lock();
        try {
            final Set<GreetingPendingTask> queue = getQueue();
            add(element, queue);
        } finally {
            lock.unlock();
        }

    }

    private void add(GreetingPendingTask element, Set<GreetingPendingTask> queue) {
        final boolean noContains = !isContains(element, queue);
        if(noContains){
            queue.add(element);
        }else{
            System.out.println("QeueuAsyncRedis.offer ya habia sido agregada >>>>>>>>>>>>>>>>"+ element);
        }
    }

    @Override
    protected void offerTascks(List<GreetingPendingTask> elements) {
        final RLock lock = redissonClient.getLock(KEY_EDIT_LOCK);
        lock.lock();
        try {
            final Set<GreetingPendingTask> queue = getQueue();
            for(GreetingPendingTask element: elements){
                add(element, queue);
            }

        } finally {
            lock.unlock();
        }
    }

    @Override
    protected void updateElement(GreetingPendingTask element) {
        final RLock lock = redissonClient.getLock(KEY_EDIT_LOCK);
        lock.lock();
        try {
            final Set<GreetingPendingTask> queue = getQueue();
            final Optional<GreetingPendingTask> byTaskId = findByTaskId(element, queue);
            byTaskId.ifPresent(queue::remove);
            queue.add(element);
        } finally {
            lock.unlock();
        }

    }

    @Override
    protected boolean isContains(GreetingPendingTask element, Set<GreetingPendingTask> queue) {

        return queue.stream().filter(Objects::nonNull).anyMatch(getGreetingPendingTaskPredicate(element));

    }

    @Override
    protected boolean isContains(GreetingPendingTask element) {
        final Set<GreetingPendingTask> queue = getQueue();
        return isContains(element, queue);
    }

    private Optional<GreetingPendingTask> findByTaskId(GreetingPendingTask element, Set<GreetingPendingTask> queue) {
        return queue.stream()
                .filter(Objects::nonNull)
                .filter(getGreetingPendingTaskPredicate(element)).findFirst();

    }

    private Predicate<GreetingPendingTask> getGreetingPendingTaskPredicate(GreetingPendingTask element) {
        return item -> {
            final Long id = item.getId();
            return id.equals(element.getId());
        };
    }

    @Override
    protected boolean remove(GreetingPendingTask element) {

        final RLock lock = redissonClient.getLock(KEY_EDIT_LOCK);
        boolean result = false;
        lock.lock();
        try {
            Set<GreetingPendingTask> queue = getQueue();
            final Optional<GreetingPendingTask> first = findByTaskId(element, queue);
            if(first.isPresent()){
                queue.remove(first.get());
                result = true;
            }
            System.out.println("QeueuAsyncRedis.remove no habia sido agregada >>>>>>>>>>>>>>>>");
        } finally {
            lock.unlock();
        }

        return result;
    }

    @Override
    protected Set<GreetingPendingTask> getQueue() {

        return redissonClient.getSet(KEY_QEUEU);

        //TODO mirar el tema de los listener

    }

    @Override
    protected void processElement(GreetingPendingTask element) {
        System.out.println("QeueuAsyncRedis.processElement "+ element);

    }

    @Override
    protected int size() {
        return getQueue().size();
    }


}



