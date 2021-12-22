
package com.pps.web.hander;

import com.pps.base.EventHander;
import com.pps.base.Server;
import com.pps.base.Worker;
import com.pps.base.exception.ChannelCloseException;
import com.pps.web.HttpServer;
import com.pps.web.constant.PpsWebConstant;
import com.pps.web.data.*;
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
public class HttpEventHander implements EventHander {


    Map<String, HttpServlet> mappingServlet;

    private HttpServlet defualtServlet;
    private HttpServlet errorServlet;
    private HttpServlet resourceServlet;
    private Server webServer;
    /**
     * 拦截全部请求的servlet  maping 固定为  \*
     */
    private HttpServlet allServlet;
    public HttpEventHander(){


    }
    @Override
    public void init(Server server) {

        this.webServer=server;
        Map<String, HttpServlet> mappingServlet = ((HttpServer)webServer).getMappingServlet();
        this.mappingServlet= mappingServlet;
        defualtServlet= mappingServlet.get(PpsWebConstant.DEFAULT_SERVLET);
        errorServlet= mappingServlet.get(PpsWebConstant.ERROR_SERVLET);
        resourceServlet= mappingServlet.get(PpsWebConstant.RESOURCE_SERVLET);
        allServlet= mappingServlet.get(PpsWebConstant.MATCH_MAPPING_ALL_URL);
        ServiceLoader<HttpBodyResolve> load = ServiceLoader.load(HttpBodyResolve.class);
        load.forEach(s->{
            s.init(webServer.getServerParams());
            HttpAlgoFactory.putHttpBodyResolveAlgo(s);
        });
        ServiceLoader<ContentEncoding> code = ServiceLoader.load(ContentEncoding.class);
        code.forEach(c->{
            c.init(webServer.getServerParams());
            HttpAlgoFactory.putContentEncodingAlgo(c);
        });

    }


    @Override
    public void read(PpsInputSteram ppsInputSteram,Worker worker) {


        SelectionKey selectionKey = ppsInputSteram.getSelectionKey();
        SocketChannel socketChannel = ppsInputSteram.getSocketChannel();
        HttpResponse response=null;
        try {

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



                response=new HttpResponse(socketChannel, webServer.getServerParams());
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
            try {
                socketChannel.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }

    @Override
    public String support() {
        return "http";
    }


}
