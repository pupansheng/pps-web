
package com.pps.web;

import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Pu PanSheng, 2021/12/18
 * @version OPRA v1.0
 */
public class BossWorker extends Worker {

    private Worker[] workers;
    private AtomicInteger count=new AtomicInteger(0);

    public BossWorker(Executor executor) {
        super(executor);
    }

    public Worker[] getWorkers() {
        return workers;
    }

    public void setWorkers(Worker[] workers) {
        this.workers = workers;
    }

    @Override
    protected void registerEvent(SelectableChannel selectableChannel, int type) throws ClosedChannelException {

        if(type == SelectionKey.OP_ACCEPT){
            super.registerEvent(selectableChannel,type);
            return;
        }
        int cU= count.addAndGet(1);
        int workL=cU%workers.length;
        Worker worker=workers[workL];
        worker.registerEvent(selectableChannel, type);
        worker.wakeUp();

    }


}
