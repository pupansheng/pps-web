
package com.pps.web;


import com.pps.web.constant.PpsWebConstant;
import com.pps.web.hander.EventHander;

import java.io.IOException;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author Pu PanSheng, 2021/12/17
 * @version OPRA v1.0
 */
public class Worker implements Runnable{

    protected Selector selector;

    /**
     * 测试bug的计数器限制
     */

    private int testBugSize=PpsWebConstant.TEST_BUG_COUNT;


    private BlockingDeque<Runnable> task;

    private Executor executor;


    private WebServer webServer;


    public Worker(Executor executor){
        try {
            this.selector=Selector.open();
            this.executor=executor;
            this.task=new LinkedBlockingDeque<>();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void init(WebServer webServer){
        this.webServer=webServer;
    }


    protected void registerEvent(SelectableChannel selectableChannel, int type) throws ClosedChannelException {
        //如果该Selector 此时已经被阻塞在select()中了  那么这里会被阻塞住 请注意
        if(selectableChannel.isOpen()) {
            selectableChannel.register(chooseSelector(), type);
        }
    }

    /**
     * 向工作者 注册 同步执行任务
     * @param runnable
     */
    public void registerTask(Runnable runnable){

        task.addLast(runnable);

    }

    /**
     * 向工作者 注册 异步执行任务
     * @param runnable
     */
    public void registerAsyncTask(Runnable runnable){

        executor.execute(runnable);

    }

    protected Selector chooseSelector(){
        return selector;
    }

    protected void wakeUp(){
        selector.wakeup();
    }

    @Override
    public void run() {


        EventHander instance = EventHander.getInstance(webServer);

        int count=0;
        long startTime=System.nanoTime();
        int timeOut= PpsWebConstant.TIMEOUT;
        while (true){

            try {


                //同步任务执行
                runTask();



                /**
                 * 如果不设置超时时间 那么如果线程已经运行了  且阻塞在select() 上面
                 * 这个时候再注册事件  那么注册线程会一直阻塞在注册方法上
                 * 远无法注册成功
                 * 所以必须加个超时时间
                 */
                int select = selector.select(timeOut);


                if(select<=0){

                    /**
                     * nio bug 当某些因为poll和epoll对于突然中断的连接socket
                     * 会对返回的eventSet事件集合置为POLLHUP或者POLLERR，eventSet事件集合发生了变化，
                     * 这就导致Selector会被唤醒，进而导致CPU 100%问题。
                     * 根本原因就是JDK没有处理好这种情况，
                     * 比如SelectionKey中就没定义有异常事件的类型。
                     *
                     * 所以需要处理下这种情况:
                     */
                    count++;

                    if(count>testBugSize) {

                        long endTime = System.nanoTime();
                        long distance = endTime - startTime;

                        //如果小于正常情况下 该限制次数下的事件间隔 说明触发了bug
                        if(distance<((testBugSize+1)*1000*1000*timeOut)){


                            //重建selector
                            Selector newSelector = Selector.open();
                            for (SelectionKey key : selector.keys()) {
                                key.channel().register(newSelector, key.interestOps());
                            }

                            this.selector=newSelector;

                        }

                        count=0;
                        startTime=System.nanoTime();

                    }

                    continue;
                }
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

                while (iterator.hasNext()){

                    try {

                        SelectionKey next = iterator.next();

                        if(!next.isValid()){
                            next.cancel();
                            continue;
                        }

                        SelectableChannel channel = next.channel();

                        if (next.isReadable()) {

                            SocketChannel channelRead = (SocketChannel) channel;
                            instance.read(channelRead, next, this);
                            registerEvent(channelRead,SelectionKey.OP_READ);


                        }else if(next.isAcceptable()){

                            ServerSocketChannel connectChannel  = (ServerSocketChannel)channel;
                            SocketChannel accept = connectChannel.accept();
                            accept.configureBlocking(false);
                            registerEvent(accept,SelectionKey.OP_READ);


                        }else if(next.isConnectable()){




                        }else if (next.isWritable()) {

                            //应当不会出现这种情况

                        }

                    }catch(Exception e){

                        e.printStackTrace();
                    }
                    finally {
                        iterator.remove();
                        startTime=System.nanoTime();
                        count=0;
                    }


                }


            } catch (Exception e) {
                e.printStackTrace();
            }


        }


    }

    private void runTask() {

        while (!task.isEmpty()){
            Runnable poll = task.poll();
            if(poll!=null){
                poll.run();
            }
        }


    }
}
