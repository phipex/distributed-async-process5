package co.com.ies.pruebas.webservice;

import java.io.Serializable;
import java.util.Objects;

public abstract class AbstractPendingTask <DataTaskType extends Serializable> implements Serializable, Comparable<AbstractPendingTask <DataTaskType>> {

    private final DataTaskType dataTask;
    private String ipReserved;
    private Long timeMillReserved;
    private boolean finalized = false;

    protected AbstractPendingTask(DataTaskType dataTask){
        this.dataTask = dataTask;
    }

    public abstract Long getId();

    public DataTaskType getDataTask() {
        return dataTask;
    }

    public String getIpReserved() {
        return ipReserved;
    }

    public void setIpReserved(String ipReserved) {
        this.ipReserved = ipReserved;
    }

    public Long getTimeMillReserved() {
        return timeMillReserved;
    }

    public void setTimeMillReserved(Long timeMillReserved) {
        this.timeMillReserved = timeMillReserved;
    }

    public boolean isFinalized() {
        return finalized;
    }

    public void setFinalized(boolean finalized) {
        this.finalized = finalized;
    }

    public void setFinalized() {
        this.finalized = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractPendingTask<?> that = (AbstractPendingTask<?>) o;
        return Objects.equals(getId(), that.getId()) ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public int compareTo(AbstractPendingTask o) {
        return getId().compareTo(o.getId());
    }
}
