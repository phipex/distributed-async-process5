package co.com.ies.pruebas.webservice;

import org.redisson.api.RLock;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class AsyncProcessServiceImpl implements AsyncProcessService{

    private final GreetingRepository greetingRepository;

    private final QeueuAsyncRedis qeueuAsyncRedis;

    private final ProcessorDelayedRedis processorDelayed;

    private final String hostAddress;

    private final AtomicBoolean onProcess = new AtomicBoolean(false);

    public AsyncProcessServiceImpl(GreetingRepository greetingRepository, QeueuAsyncRedis qeueuAsyncRedis, ProcessorDelayedRedis processorDelayed) {

        this.greetingRepository = greetingRepository;
        this.qeueuAsyncRedis = qeueuAsyncRedis;
        this.processorDelayed = processorDelayed;
        String hostAddress1;
        try {
            hostAddress1 = InetAddress.getLocalHost().getHostAddress() ;
        } catch (UnknownHostException e) {
            e.printStackTrace();
            hostAddress1 = "no found";
        }
        hostAddress = hostAddress1;
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
    public void adquireTasks() {
        final Set<GreetingPendingTask> queue = qeueuAsyncRedis.getQueue();

        if(queue.isEmpty()){

            return;
        }

        List<GreetingPendingTask> lista = new ArrayList<>(queue);
        System.out.println("AsyncProcessServiceImpl.adquireTasks lista original = " + lista.size());

        Predicate<GreetingPendingTask> vacios =
                value -> value.getIpReserved() == null && value.getDataTask().getIpTramited() == null;

        lista = lista.stream().filter(vacios).collect(Collectors.toList());
        System.out.println("AsyncProcessServiceImpl.adquireTasks lista filtrada = " + lista.size());
        for (GreetingPendingTask next : lista) {
            System.out.println("AsyncProcessServiceImpl.adquireTasks next = " + next);
            next.setIpReserved(hostAddress);
            qeueuAsyncRedis.updateElement(next);
            System.out.println("AsyncProcessServiceImpl.adquireTasks cantidad despues = " + qeueuAsyncRedis.size());

        }

    }

    @Async
    @Override
    public void processTaskList() {
        final boolean noOnProcess = !onProcess.get();
        System.out.println("noOnProcess = " + noOnProcess);
        if (noOnProcess) {
            onProcess.set(true);
            processTask();
            onProcess.set(false);
            return;
        }
        System.out.println("AsyncProcessServiceImpl.processTaskList skip");
    }

    private void processTask() {
        final Set<GreetingPendingTask> queue = qeueuAsyncRedis.getQueue();

        if(queue.isEmpty()){

            return;
        }

        final Predicate<GreetingPendingTask> propios = value -> hostAddress.equals(value.getIpReserved());
        final Predicate<GreetingPendingTask> sinTramitar = value -> value.getDataTask().getIpTramited() == null;

        final List<GreetingPendingTask> taskList = queue.stream()
                .filter(propios)
                .filter(sinTramitar)
                .collect(Collectors.toList());

        processTaskList(taskList);
    }

    private void processTaskList(List<GreetingPendingTask> lista) {
        System.out.println("AsyncProcessServiceImpl.processTaskList iniciando lista = " + lista.size());
        for(GreetingPendingTask element: lista){

            processorDelayed.processElement(element);
            //qeueuAsyncRedis.updateElement(element);
            qeueuAsyncRedis.remove(element);

        }
        System.out.println("AsyncProcessServiceImpl.processTaskList finalizando lista = " + lista.size());
    }
}
