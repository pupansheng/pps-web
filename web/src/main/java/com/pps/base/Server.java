
package com.pps.base;

import java.nio.channels.ServerSocketChannel;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author Pu PanSheng, 2021/12/22
 * @version OPRA v1.0
 */
public interface Server {

    int getPort();
    int getWorkerSize();
    int getBosserSize();
    String support();
    void init();
    default Class getListenerChannel(){
        return ServerSocketChannel.class;
    }
    default int getMaxThread(){
        return Runtime.getRuntime().availableProcessors();
    }
    default int keepAliveTimeBySECONDS(){
        return 60;
    }
    default BlockingQueue getBlockQeque(){
        return new ArrayBlockingQueue(1000);
    }
    Map<String, Object> getServerParams();

}
