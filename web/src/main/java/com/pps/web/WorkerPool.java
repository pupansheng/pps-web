
package com.pps.web;

import com.pps.web.constant.PpsWebConstant;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ArrayBlockingQueue;
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

    private volatile boolean onStart=false;

    public WorkerPool(int workerSize,int bossSize,int maxThread){

        if(bossSize<0||workerSize<=0){
            throw new RuntimeException("参数不合法！");
        }
        this.bossSize=bossSize;
        this.maxThread=maxThread;
        if(maxThread<workerSize+bossSize){
            this.maxThread=workerSize+bossSize;
        }
        executor = new ThreadPoolExecutor(workerSize+bossSize, this.maxThread,
                60, TimeUnit.SECONDS, new ArrayBlockingQueue<>( 100));

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

    public void startWeb(Class chanelClass,WebServer webServer) throws Exception {

        ServerSocketChannel serverSocketChannel=null;

        if(chanelClass==ServerSocketChannel.class){

            serverSocketChannel=ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress((webServer.getPort())));
            serverSocketChannel.configureBlocking(false);

        }

        if(serverSocketChannel==null){
            throw new RuntimeException(chanelClass+ "暂未支持！");
        }

        //零时目录创建
        String tempDir = (String)webServer.getServerParms().get(PpsWebConstant.TEMP_DIR_KEY);
        if(!Files.exists(Paths.get(tempDir))){
            Files.createDirectories(Paths.get(tempDir));
        }


        Worker W=workers[0];

        if(!onStart) {
            for (Worker worker : workers) {
                worker.init(webServer, workers, bossers);
                executor.execute(worker);
            }
            if (bossSize != 0) {
                for (Worker bosser : bossers) {
                    bosser.init(webServer, workers, bossers);
                    executor.execute(bosser);
                }
            }

            W.registerEvent(serverSocketChannel, SelectionKey.OP_ACCEPT);

            onStart=true;
        }
    }




}
