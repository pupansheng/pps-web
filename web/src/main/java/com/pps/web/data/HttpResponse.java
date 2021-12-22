
package com.pps.web.data;


import com.pps.web.constant.PpsWebConstant;
import com.pps.base.exception.ChannelCloseException;
import com.pps.base.util.BufferUtil;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Pu PanSheng, 2021/12/18
 * @version OPRA v1.0
 */
public class HttpResponse {

    private SocketChannel socketChannel;
    private String protocol="HTTP/1.1";
    private String code="200";
    private String contentType="text/html";
    private String charset="utf-8";
    private Map<String,String> headerParams=new HashMap<>();
    private Map<String,String> requestHeader=new HashMap<>();

    /**
     * 客户端可支持压缩格式
     */
    private String[] acceptEncoding;

    /**
     * 应用的压缩算法
     */
    private ContentEncoding applyEncoding;

    /**
     * 压缩文件支持类型
     */
    private Set<String> compressContentType=new HashSet<>();

    /**
     * 是否开启压缩
     */
    private boolean cancompress=false;


    private boolean flag;

    private Map<String, Object> serverParam;


    public HttpResponse(SocketChannel socketChannel,Map<String, Object> serverParam) {

        this.serverParam=serverParam;
        this.socketChannel = socketChannel;
        headerParams.put("Connection","keep-alive");
        cancompress= (Boolean)serverParam
                .getOrDefault(PpsWebConstant.OPEN_CONPRECESS_KEY,false);
       String[] ss=(((String)serverParam
                .get(PpsWebConstant.CONPRECESS_TYPE_KEY)).split(","));
        for (String s : ss) {
            compressContentType.add(s);
        }

    }

    public void setRequestHeader(Map<String, String> requestHeader) {
        String s = requestHeader.get("Accept-Encoding");
        if(s==null){
            s=requestHeader.get("accept-encoding");
        }
        if(s!=null){
            acceptEncoding=s.split(",");
            for (int i = 0; i < acceptEncoding.length; i++) {
                acceptEncoding[i]=acceptEncoding[i].trim();
            }
        }
        this.requestHeader = requestHeader;
    }

    public void setCode(int code){
        this.code=String.valueOf(code);
    }
    public void setCharset(String charset){
       this.charset=charset;
    }
    public void setContentType(String contentType){
        this.contentType=contentType;
    }
    public void putHeaderParam(String key,String v){
        headerParams.put(key,v);
    }

    public void write(byte [] bytes){
        write(bytes,0,bytes.length);
    }
    public void write(String s){
        write(BufferUtil.strToBytes(s,charset));
    }
    public void write(byte [] bytes,int offset, int len){

        if(!flag){//第一次 那么需要写入 http报文头
            byte[] messageChunckHead = createMessageChunckHead();
            doWrite(messageChunckHead);
            flag=true;
        }
        //写入chunk 内容
        byte[] content = createChunckBody(bytes,offset,len);
        doWrite(content);
    }

    /**
     * 结束发送 如果是write 那么必须要用flush 结束发送 不然浏览器无法结束
     */
    public void flush(){
        byte[] encoding = createEndChunckBody();
        doWrite(encoding);
    }
    /**
     * 压缩编码
     * @param bytes
     * @return
     */
    private byte[] encoding(byte[] bytes,int offset,int len){

        if(isSupportCompress()){
            return applyEncoding.convert(bytes,offset,len);
        }
        return bytes;
    }


    /**
     * 当前应用是否支持压缩
     * @return
     */
    private boolean isSupportCompress(){

        boolean f1=cancompress
                &&applyEncoding!=null
                &&acceptEncoding!=null
                &&acceptEncoding.length>0;
        if(f1) {
            String s = headerParams.get("content-type");
            if (s == null) {
                s = headerParams.get("Content-Type");
            }
            return compressContentType.contains(s);
        }

        return false;
    }
    /**
     * 直接发送 一次发送完毕  只能调用一次  不必调用flush
     * @param content
     */
    public void writeDirect(String content){
        byte[] co=createMessage(content);
        doWrite(co);
    }
    /**
     * 直接发送 一次发送完毕  只能调用一次 不必调用flush
     * @param bytes
     */
    public void writeDirect(byte [] bytes)  {
        bytes=createMessage(bytes);
        doWrite(bytes);
    }

    private void doWrite(byte [] ccc){
        BufferUtil.write(ccc,(byteBuffer)->{
            try {
                socketChannel.write(byteBuffer);
            } catch (IOException e) {
                throw new ChannelCloseException(e);
            }
        });
    }

    /**
     * 构造 普通的响应头 带有content_length
     * @param httpr
     * @return
     */
    private byte[] createMessage(String httpr) {
      return createMessage(BufferUtil.strToBytes(httpr,charset));
    }

    /**
     * 构造 普通的响应头 带有content_length
     * @param httpr
     * @return
     */
    private byte[] createMessage(byte [] httpr)  {



        StringBuilder returnStr = new StringBuilder();
        //请求行
        appendResponLine(returnStr,String.format("%s %s ok"
                ,protocol
                ,code));//增加响应消息行

        //请求头
        compreHander(returnStr,true);

        /**
         * 可能会被压缩
         */
        httpr=encoding(httpr,0,httpr.length);

        String contentLen=String.valueOf(httpr.length);


        appendResponseHeader(returnStr,"Content-Type: "+contentType+";charset=" + charset);
        appendResponseHeader(returnStr,String.format("Content-Length: %s",contentLen));
        headerParams.forEach((k,v)->{
            appendResponseHeader(returnStr,k +": "+v);
        });

        returnStr.append("\r\n");

        //请求体
        byte[] bytes = BufferUtil.strToBytes(returnStr.toString(),charset);

        int i = httpr.length + bytes.length;
        byte[] newC=new byte[i];
        System.arraycopy(bytes,0,newC,0,bytes.length);
        System.arraycopy(httpr,0,newC,bytes.length,httpr.length);
        return newC;
    }


    /**
     * 压缩头添加
     * @param stringBuilder
     */
    private void compreHander(StringBuilder stringBuilder, boolean force){

        if(applyEncoding==null&&acceptEncoding!=null){
            for (String s : acceptEncoding) {
                ContentEncoding contentEncodingAlgo = HttpAlgoFactory.getContentEncodingAlgo(s);
                if(contentEncodingAlgo !=null){
                    applyEncoding= contentEncodingAlgo;
                    break;
                }
            }
        }
        if(force&&isSupportCompress()){
            appendResponseHeader(stringBuilder,String.format("content-encoding: %s",applyEncoding.support()));
        }
    }
    /**
     * 构造http 分块请求头 不带有content_length
     *
     * 压缩算法例如gizp 和 chunck分块传输 如何组合呢
     *
     * 答：
     * 只能先要把发送的数据 用gzip 组合起来 然后再分块传输
     * 而不是 对每一块分别进行压缩后 再发送
     * @return
     */
    private byte[] createMessageChunckHead()  {

        StringBuilder returnStr = new StringBuilder();
        //请求行
        appendResponLine(returnStr,String.format("%s %s ok"
                ,protocol
                ,code));//增加响应消息行
        //请求头
        appendResponseHeader(returnStr,"Content-Type: "+contentType+";charset=" + charset);
        appendResponseHeader(returnStr,String.format("Transfer-Encoding: %s","chunked"));

        headerParams.forEach((k,v)->{
            appendResponseHeader(returnStr,k +": "+v);
        });

        returnStr.append("\r\n");
        byte[] bytes =null;
        bytes = BufferUtil.strToBytes(returnStr.toString(),charset);
        return bytes;

    }

    private byte [] createChunckBody(byte [] bytes,int offset,int lenA){

        String len = Integer.toHexString(lenA);
        String h=len+"\r\n";
        byte[] bytes1 = BufferUtil.strToBytes(h);
        int i = lenA + bytes1.length;
        byte[] n=new byte[i+2];
        System.arraycopy(bytes1,0,n,0,bytes1.length);
        System.arraycopy(bytes,offset, n,bytes1.length,lenA);
        n[i]='\r';
        n[i+1]='\n';
        return n;

    }
    private byte [] createEndChunckBody(){

        String end="0\r\n\r\n";
        byte[] bytes = BufferUtil.strToBytes(end);
        return bytes;

    }
    private void appendResponLine(StringBuilder stringBuilder,String line){
        stringBuilder.append(line+"\r\n");
    }
    private void appendResponseHeader(StringBuilder stringBuilder,String line){
        stringBuilder.append(line+"\r\n");
    }

}
