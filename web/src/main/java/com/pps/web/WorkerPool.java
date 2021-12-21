
package com.pps.web;

import com.pps.web.constant.PpsWebConstant;
import com.pps.web.servlet.model.HttpServlet;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 工作工厂
 */
public class WorkerPool {

    private int workerSize;
    private int bossSize;
    private int maxThread;
    /**
     * 负责监听读写事件
     */
    private Worker[] workers;
    /**
     * 负责监听客户端连接事件 并把该连接分配个worker 若设定bosser为0  那么work也负责连接
     */
    private BossWorker[] bossers;

    private AtomicInteger counter=new AtomicInteger(0);

    private ThreadPoolExecutor executor;

    private volatile boolean onStart=false;

    public WorkerPool(int workerSize,int bossSize,int maxThread){

        if(bossSize<0||workerSize<=0){
            throw new RuntimeException("参数不合法！");
        }
        this.workerSize = workerSize;
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
            this.bossers=new BossWorker[bossSize];
            for (int i = 0; i < bossSize; i++) {
                this.bossers[i]=new BossWorker(executor);
                this.bossers[i].setWorkers(workers);
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

        int countN = counter.addAndGet(1);
        Worker W;
        if(bossSize!=0){
            W=bossers[countN%bossSize];
        }else {
            W=workers[countN%workerSize];
        }

        W.registerEvent(serverSocketChannel, SelectionKey.OP_ACCEPT);

        if(!onStart) {

            for (Worker worker : workers) {
                worker.init(webServer);
                executor.execute(worker);
            }
            if (bossSize != 0) {
                for (Worker bosser : bossers) {
                    bosser.init(webServer);
                    executor.execute(bosser);
                }
            }
            onStart=true;
        }
    }




}
