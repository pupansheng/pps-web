
package com.pps.web.data;

import com.pps.base.Worker;
import com.pps.web.constant.PpsWebConstant;
import com.pps.web.servlet.entity.PpsInputSteram;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Pu PanSheng, 2021/12/18
 * @version OPRA v1.0
 */
public class HttpRequest {

    private String protocol;
    private String method;
    private Map<String,String> urlParams=new HashMap<>();
    private Map<String,String> headerParam=new HashMap<>();
    private Map<String,Object> httpBodyData=new HashMap<>();
    private boolean isSavleFile;
    private PpsInputSteram inputStream;
    private String url;
    private Worker worker;


    public HttpRequest(PpsInputSteram inputStream, Worker worker) throws Exception {
        this.worker=worker;
        this.inputStream = inputStream;
        httpResolve();
        inputStream.returnBuffer();
    }

    public boolean isSavleFile() {
        return isSavleFile;
    }


    public void setSavleFile(boolean savleFile) {
        isSavleFile = savleFile;
    }

    public void addTask(Runnable task){

        worker.registerTask(task);

    }
    public InputStream getInputStream() {
        return inputStream;
    }


    void putHttpBody(String key,Object v){
        httpBodyData.put(key,v);
    }


    public List<Application__multipart_form_dataHttpBodyResolve.FileEntity>getFromData(){
        Object body = httpBodyData.get("body");
        if(body instanceof ArrayList){
            return (List<Application__multipart_form_dataHttpBodyResolve.FileEntity>)body;
        }
        return new ArrayList<>(0);
    }

    public String getBodyContent(){

        Object body = httpBodyData.get("body");
        return (String)body;

    }



    private void httpResolve() throws Exception {

        //http报文 请求行和请求头
        List<String> httpReportHead=new ArrayList<>();
        byte [] line=new byte[PpsWebConstant.BUFFER_INIT_LENGTH];
        int index=0;
        while (true) {

                byte data = (byte) inputStream.read();
                if(data!=-1) {
                    line[index] = data;

                    if (data == '\n' && index != 0 && line[index - 1] == '\r') {

                        String s = new String(line, 0, index + 1, "utf-8");
                        //如果s==\r\n 那么说明这个就是请求体和请求头的那个分隔标记 下面的字节就是请求体了
                        if (s.equals("\r\n")) {
                            break;
                        }
                        httpReportHead.add(s);
                        for (int i = 0; i < line.length; i++) {
                            line[i] = 0;
                        }
                        index = 0;
                        continue;
                    }

                    index++;
                    //扩容
                    if (index >= line.length) {
                        byte[] newLine = new byte[line.length * 2];
                        System.arraycopy(line, 0, newLine, 0, index);
                        line = newLine;
                    }

                } else {
                    break;
                 }


        }


        //解析请求头
        resolveHttpHead(httpReportHead);

        //请求体解析
        String content_type = getHeader("content-type");
        if(content_type==null){
            content_type=getHeader("Content-Type");
        }
        if(content_type!=null){
            content_type=content_type.trim();
            if(content_type.contains("multipart/form-data")){
                content_type="multipart/form-data";
            }
        }
        HttpBodyResolve factory = HttpAlgoFactory.getHttpBodyResoveAlgo(content_type);
        if(factory!=null){
            factory.resolve(this);
        }


    }

    private void resolveHttpHead(List<String> httpReportHead) throws UnsupportedEncodingException {

        //解析请求头
        if(!httpReportHead.isEmpty()) {

            //请求行
            String requestLine= httpReportHead.get(0);
            String[] lineArr = requestLine.split(" ");
            setMethod(lineArr[0]);
            setProtocol(lineArr[2]);
            String url=lineArr[1];
            int i1 = url.indexOf("?");
            if(i1!=-1) {
                String pureUrl = url.substring(0, i1);
                String endUrl = url.substring(i1+1);
                setUrl(pureUrl);
                endUrl= URLDecoder.decode(endUrl, PpsWebConstant.CHAR_SET);
                String[] split1 = endUrl.split("&");
                for (String s : split1) {
                    String[] split2 = s.split("=");
                    if(split2.length==2){
                        putUrlParam(split2[0], split2[1]);
                    }
                }
            }else {
                setUrl(url);
            }

            //解析请求头
            for (int i = 1; i < httpReportHead.size(); i++) {

                String[] split1 = httpReportHead.get(i).split(":");
                if(split1.length==2){
                    putHeaderParam(split1[0],split1[1]);
                }
            }

        }


    }

    public String getProtocol() {
        return protocol;
    }

    public void putUrlParam(String key,String v){
        urlParams.put(key,v);
    }
    public void putHeaderParam(String key,String v){
        headerParam.put(key,v);
    }
    public String getHeader(String k){
        return headerParam.get(k);
    }
    public Map<String, String> getHeaderParam() {
        return headerParam;
    }

    public void setHeaderParam(Map<String, String> headerParam) {
        this.headerParam = headerParam;
    }


    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, String> getUrlParams() {
        return urlParams;
    }

    public void setUrlParams(Map<String, String> urlParams) {
        this.urlParams = urlParams;
    }

    public String getParam(String key){
        return urlParams.get(key);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
