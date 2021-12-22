
package com.pps.base;

import com.pps.base.util.BufferUtil;
import com.pps.web.constant.PpsWebConstant;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 工作工厂
 */
public class WorkerPool {


    private int bossSize;

    private int maxThread;
    /**
     * 负责监听读写事件
     */
    private Worker[] workers;
    /**
     * 负责监听客户端连接事件 并把该连接分配个worker 若设定bosser为0  那么work也负责连接
     */
    private Worker[] bossers;

    private ThreadPoolExecutor executor;

    private Server server;

    private volatile boolean onStart=false;

    public WorkerPool(Server server){
        int workerSize=server.getWorkerSize();
        int bossSize=server.getBosserSize();
        int maxThread=server.getMaxThread();
        int keepAliveTime=server.keepAliveTimeBySECONDS();
        BlockingQueue blockingQueue=server.getBlockQeque();
        this.server=server;
        if(bossSize<0||workerSize<=0){
            throw new RuntimeException("参数不合法！");
        }
        this.bossSize=bossSize;
        this.maxThread=maxThread;
        if(maxThread<workerSize+bossSize){
            this.maxThread=workerSize+bossSize;
        }
        executor = new ThreadPoolExecutor(workerSize+bossSize, this.maxThread,
                keepAliveTime, TimeUnit.SECONDS, blockingQueue);
        this.workers=new Worker[workerSize];
        for (int i = 0; i < workerSize; i++) {
            workers[i] = new Worker(executor);
        }
        if(this.bossSize!=0){
            this.bossers=new Worker[bossSize];
            for (int i = 0; i < bossSize; i++) {
                this.bossers[i]=new Worker(executor);
            }
        }

    }

    public void start() throws Exception {


        server.init();

        EventHanderFactory.initEventHander(server);

        ServerSocketChannel serverSocketChannel=null;

        Class listenerChannel = server.getListenerChannel();


        if(listenerChannel ==ServerSocketChannel.class){
            serverSocketChannel=ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress((server.getPort())));
            serverSocketChannel.configureBlocking(false);
        }

        if(serverSocketChannel==null){
            throw new RuntimeException(listenerChannel + "暂未支持！");
        }

        //零时目录创建
        String tempDir = (String)server.getServerParams().get(PpsWebConstant.TEMP_DIR_KEY);
        if(!Files.exists(Paths.get(tempDir))){
            Files.createDirectories(Paths.get(tempDir));
        }


        Worker W=workers[0];

        if(!onStart) {

            for (Worker worker : workers) {
                worker.init(server, workers, bossers);
                executor.execute(worker);
            }
            if (bossSize != 0) {
                for (Worker bosser : bossers) {
                    bosser.init(server, workers, bossers);
                    executor.execute(bosser);
                }
            }

            BufferUtil.initBufferPool();

            W.registerEvent(serverSocketChannel, SelectionKey.OP_ACCEPT);

            onStart=true;
        }
    }




}
