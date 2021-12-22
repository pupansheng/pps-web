
package com.pps.base;

import com.pps.base.util.BufferUtil;

/**
 * @author Pu PanSheng, 2021/12/22
 * @version OPRA v1.0
 */
public class PpsBoot {

    /**
     * 启动
     * @param server
     */
    public static void run(Server server){

        WorkerPool workerPool=null;
        workerPool= new WorkerPool(server);
        try {
            workerPool.start();
            System.out.println(server.support()+" 服务 ["+server.getPort()+"]"+"启动成功");
        } catch (Exception e) {
            BufferUtil.clearBufferPool();
            e.printStackTrace();
            throw new RuntimeException(e);
        }


    }

}
