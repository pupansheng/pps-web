/*
 * Copyright (c) ACCA Corp.
 * All Rights Reserved.
 */
package com.pps.web.hander;

import com.pps.web.WebServer;
import com.pps.web.Worker;
import com.pps.web.constant.PpsWebConstant;
import com.pps.web.data.*;
import com.pps.web.exception.ChannelCloseException;
import com.pps.web.servlet.entity.PpsInputSteram;
import com.pps.web.servlet.model.HttpServlet;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * @author Pu PanSheng, 2021/12/17
 * @version OPRA v1.0
 */
public class EventHander {


    private static volatile EventHander eventHander;

    Map<String, HttpServlet> mappingServlet;

    private HttpServlet defualtServlet;
    private HttpServlet errorServlet;
    private HttpServlet resourceServlet;
    private WebServer webServer;
    /**
     * 拦截全部请求的servlet  maping 固定为  \*
     */
    private HttpServlet allServlet;
    private EventHander(){

    }
    public static EventHander getInstance(WebServer webServer){

        if(eventHander==null){

              synchronized (EventHander.class){

                  if(eventHander==null){

                      Map<String, HttpServlet> mappingServlet = webServer.getMappingServlet();
                      eventHander=new EventHander();
                      eventHander.mappingServlet= mappingServlet;
                      eventHander.defualtServlet= mappingServlet.get(PpsWebConstant.DEFAULT_SERVLET);
                      eventHander.errorServlet= mappingServlet.get(PpsWebConstant.ERROR_SERVLET);
                      eventHander.resourceServlet= mappingServlet.get(PpsWebConstant.RESOURCE_SERVLET);
                      eventHander.allServlet= mappingServlet.get(PpsWebConstant.MATCH_MAPPING_ALL_URL);
                      eventHander.webServer=webServer;
                      ServiceLoader<HttpBodyResolve> load = ServiceLoader.load(HttpBodyResolve.class);

                      load.forEach(s->{
                          s.init(webServer.getServerParms());
                          HttpAlgoFactory.putHttpBodyResolveAlgo(s);
                      });

                      ServiceLoader<ContentEncoding> code = ServiceLoader.load(ContentEncoding.class);
                      code.forEach(c->{
                          c.init(webServer.getServerParms());
                          HttpAlgoFactory.putContentEncodingAlgo(c);
                      });


                  }
              }
        }
        return eventHander;
    }



    public void read(SocketChannel socketChannel, SelectionKey selectionKey, Worker worker) throws IOException {


        Response response=null;
        try {


            PpsInputSteram ppsInputSteram=new PpsInputSteram(socketChannel,selectionKey);
            HttpRequest request= null;
            request = new HttpRequest(ppsInputSteram, worker);
            /**
                 * 假如 服务器 context 为 /
                 * 1  servlet /           匹配  /
                 * 2  servlet /key        匹配 /key
                 * 3 servlet /*            匹配
                 * 假如 服务器 context 为 /context
                 *
                 * servlet /context  匹配 url /context/context
                 * servlet /         匹配 url /context
                 * servlet /key      匹配 url /context/key
                 * servlet /*        匹配
                 */
            try {



                response=new Response(socketChannel,webServer.getServerParms());
                response.setRequestHeader(request.getHeaderParam());

                String matchUrl=request.getUrl();

                if(matchUrl==null){
                    return;
                }
                if(matchUrl.endsWith("/")){
                    matchUrl=matchUrl.substring(0,matchUrl.length()-1);
                }


                HttpServlet httpServlet = mappingServlet.get(matchUrl);



                if(httpServlet==null){

                    //是否满足静态资源
                    if(resourceServlet.isMatch(matchUrl)){
                       httpServlet=resourceServlet;
                    }

                    //全局servlet是否存在
                    if(httpServlet==null){
                        httpServlet=allServlet;
                    }

                    //一个都没 那么就用系统默认的了
                    if(httpServlet==null){
                        httpServlet=defualtServlet;
                    }

                }



                    httpServlet.get(request,response);

                    //可以考虑异步队列任务提交 提高响应速度
                    if(request.isSavleFile()){

                        //删除临时文件
                        HttpRequest finalRequest = request;
                        worker.registerTask(()->{
                            for (Application__multipart_form_dataHttpBodyResolve.FileEntity fromDatum : finalRequest.getFromData()) {
                                String urlF = fromDatum.getInfo(PpsWebConstant.TEMP_FILE_KEY);
                                if (urlF != null) {
                                    try {
                                        Files.deleteIfExists(Paths.get(urlF));
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });

                    }

                }catch (Exception e){

                    if(e instanceof ChannelCloseException){
                        selectionKey.cancel();
                        socketChannel.close();
                    }else {
                        e.printStackTrace();
                        errorServlet.get(request, response);
                    }
                }



        } catch (Exception e) {
            if(!(e instanceof IOException)&&!(e instanceof ChannelCloseException)){
                e.printStackTrace();
            }
            selectionKey.cancel();
            socketChannel.close();
        }

    }




}
