package co.com.ies.pruebas.webservice;

public class GreetingPendingTask extends AbstractPendingTask<Greeting>{

    public GreetingPendingTask(Greeting dataTask) {
        super(dataTask);
    }

    @Override
    public Long getId() {
        return this.getDataTask().getId();
    }

    @Override
    public String toString() {
        return "GreetingPendingTask{" +
        "dataTask=" + getDataTask() +
                ", ipReserved='" + getIpReserved() + '\'' +
                ", timeMillReserved=" + getTimeMillReserved() +
                ", finalized=" + isFinalized() +
                '}';
    }
}
