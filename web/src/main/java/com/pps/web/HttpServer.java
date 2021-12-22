
package com.pps.web;

import com.pps.base.Server;
import com.pps.web.constant.PpsWebConstant;
import com.pps.web.servlet.defualt.DefaultErrorServlet;
import com.pps.web.servlet.defualt.DefaultIconServet;
import com.pps.web.servlet.defualt.DefaultNoMappingServlet;
import com.pps.web.servlet.defualt.DefaultStaticResourceServlet;
import com.pps.web.servlet.model.HttpServlet;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Pu PanSheng, 2021/12/17
 * @version OPRA v1.0
 */
public class HttpServer implements Server {


    private int port=9090;

    private String context="/";

    private Map<String, HttpServlet> mappingServlet=new HashMap<>();

    private Map<String,Object> serverParms=new HashMap<>();

    public HttpServer() {


       serverParms.put(PpsWebConstant.PORT_KEY,9090);
       serverParms.put(PpsWebConstant.CONTEXT_KEY, context);
       serverParms.put(PpsWebConstant.WORKER_SIZE_KEY, 1);
       serverParms.put(PpsWebConstant.BOSSER_SIZE_KEY, 1);
       int cpu=Runtime.getRuntime().availableProcessors();
       serverParms.put(PpsWebConstant.MAX_THREAD_KEY,cpu);
       //是否压缩 gzip
        serverParms.put(PpsWebConstant.OPEN_CONPRECESS_KEY,false);
        //压缩的文件类型 多个使用,分隔  值为content类型
        serverParms.put(PpsWebConstant.CONPRECESS_TYPE_KEY,"image/jpeg,application/javascript");

       //最大文件上传大小  默认值10M
       serverParms.put(PpsWebConstant.MAX_UPLOAD_SIZE, 10*1024*1024);
       //临时目录
       serverParms.put(PpsWebConstant.TEMP_DIR_KEY,"c:\\pps-web-temp");
       //静态资源路径
       serverParms.put(PpsWebConstant.RESOUCE_DIR_KEY,"c:\\test");
        /**
         * 静态资源映射路径  此路径将会被替代从真实路径
         * eg  /context/resource  ->  c:\\test\
         */
        /**
         * 图标目录  icon.png  没有则用默认的
         */
        serverParms.put(PpsWebConstant.ICON_LOCATION,null);


    }

    @Override
    public int getPort() {
        return port;
    }
    @Override
    public int getWorkerSize() {
        return (Integer) serverParms.get(PpsWebConstant.WORKER_SIZE_KEY);
    }

    @Override
    public int getBosserSize(){
        return (Integer) serverParms.get(PpsWebConstant.BOSSER_SIZE_KEY);
    }

    @Override
    public String support() {
        return "http";
    }

    @Override
    public void init() {


        addDefaultServlet();

        initServlet();

    }


    @Override
    public Map<String, Object> getServerParams() {
        return serverParms;
    }


    public void setWorkerSize(int workerSize) {
        serverParms.put(PpsWebConstant.WORKER_SIZE_KEY, workerSize);
    }
    /**
     * 设置压缩开关
     * @param st
     */
    public void setComprecess(Boolean st){
        serverParms.put(PpsWebConstant.OPEN_CONPRECESS_KEY, st);
    }

    /**
     * 注册servlet 服务
     * @param mapUrl 不包含 context   /context/key  应为  /key
     * @param httpServlet
     */
    public void addHttpServer(String mapUrl,HttpServlet httpServlet){

        httpServlet.init(getServerParams());
        if(!mapUrl.startsWith("/")){
            mapUrl="/"+mapUrl;
        }

        if(!mapUrl.equals(context)){
            mapUrl=context+mapUrl;
        }

        mappingServlet.put(mapUrl, httpServlet);
    }
    public void setStaticResourceDir(String dir){
        serverParms.put(PpsWebConstant.RESOUCE_DIR_KEY,dir);
    }


    public void setResourceMapping(String resource){

        if(context.equals("/")){
            if(resource.startsWith("/")){
                resource=resource.substring(1);
            }
        }else {
            if(!resource.startsWith("/")){
                resource="/"+resource;
            }
        }

        serverParms.put(PpsWebConstant.RESOUCE_MAPPING_DIR_KEY,resource);


    }


    public void setPort(int port) {

        serverParms.put("port",port);
        this.port = port;

    }

    public void setContext(String context) {
        if(!context.startsWith("/")){
            context="/"+context;
        }
        if(context.endsWith("/")){
            context=context.substring(0,context.length()-1);
        }
        serverParms.put(PpsWebConstant.CONTEXT_KEY, context);
        this.context = context;
    }


    public void setBossSize(int bosserSize){
        serverParms.put(PpsWebConstant.BOSSER_SIZE_KEY,bosserSize);
    }

    public void setParam(String k,Object v){
        serverParms.put(k,v);
    }

    public void setMaxThread(int max){
        serverParms.put(PpsWebConstant.MAX_THREAD_KEY,max);
    }

    private void addDefaultServlet(){


        // 静态资源自动映射
        if(!mappingServlet.containsKey(PpsWebConstant.RESOURCE_SERVLET)) {
            mappingServlet.put(PpsWebConstant.RESOURCE_SERVLET, new DefaultStaticResourceServlet());
        }

        //默认处理 404
        if(!mappingServlet.containsKey(PpsWebConstant.DEFAULT_SERVLET)) {
            mappingServlet.put(PpsWebConstant.DEFAULT_SERVLET, new DefaultNoMappingServlet());
        }

        //异常默认处理 500
        if(!mappingServlet.containsKey(PpsWebConstant.ERROR_SERVLET)) {
            mappingServlet.put(PpsWebConstant.ERROR_SERVLET, new DefaultErrorServlet());
        }

        //图标处理
        if(!mappingServlet.containsKey(PpsWebConstant.ICON_SERVLET)){
            mappingServlet.put("/favicon.ico", new DefaultIconServet());
        }

    }

    public Map<String, HttpServlet> getMappingServlet() {
        return mappingServlet;
    }


    private void initServlet() {


        mappingServlet.forEach((k,s)->{
            s.init(getServerParams());
        });

    }






}
