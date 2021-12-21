
package com.pps.web.data;

import com.pps.web.constant.ContentTypeEnum;
import com.pps.web.constant.PpsWebConstant;

import java.io.*;
import java.util.*;

/**
 * @author Pu PanSheng, 2021/12/19
 * @version OPRA v1.0
 */
public class Application__multipart_form_dataHttpBodyResolve implements HttpBodyResolve {

    private Map<String, Object> serverParam;

    @Override
    public void init(Map<String, Object> serverParam) {
        this.serverParam=serverParam;
    }

    @Override
    public void resolve(HttpRequest httpRequest) throws Exception {

        InputStream ppsInputSteram = httpRequest.getInputStream();

        List<FileEntity> list=new ArrayList<>();

        byte[] spline = getSpline(ppsInputSteram);

        while (true) {

            if (spline == null||spline[spline.length-1]=='-') {
                break;
            }

            FileEntity fileEntity = new FileEntity();
            String desc = getDesc(ppsInputSteram);
            String[] split = desc.split("\r\n");
            for (String s : split) {
                String[] split1 = s.split(";");
                for (String s1 : split1) {
                    String[] split2 = s1.split("=");
                    if (split2.length == 2) {
                        String key = split2[0].trim();
                        String v = split2[1].trim();
                        fileEntity.putInfo(key, v);
                    }
                }

            }

            //具体的内容
            byte[] lineBuff = new byte[3*1024];

            int indexL = 0;
            while (true) {

                byte data = (byte) ppsInputSteram.read();
                if (data != -1) {
                    lineBuff[indexL] = data;
                    //如果\r\n 那么说明下面 可能 就是其他文件了 但是还不一定
                    if (data == '\n' && indexL != 0 && lineBuff[indexL - 1] == '\r') {

                        //看看下一段是否位分隔
                        byte[] spline2 = getSpline2(ppsInputSteram, spline.length);
                        boolean f = true;
                        if (spline2 != null && spline2.length != spline.length) {
                            f = false;
                        }
                        if (f && spline2 != null && spline2.length == spline.length) {
                            for (int i = 0; i < spline.length - 2; i++) {
                                if (spline2[i] != spline[i]) {
                                    f = false;
                                    break;
                                }
                            }
                        }
                        //是分割线 表示确实结束了 这一段的内容就是文件了
                        if (f) {
                            spline = spline2;
                            break;
                        } else {//没有结束 那么
                            for (int i = 0; i < spline2.length; i++) {
                                indexL++;
                                //扩容
                                if (indexL >= lineBuff.length) {
                                    lineBuff = resize(lineBuff, indexL);
                                }
                                lineBuff[indexL] = spline2[i];
                            }
                        }
                    }
                    indexL++;
                    //扩容
                    if (indexL >= lineBuff.length) {
                        lineBuff = resize(lineBuff, indexL);
                    }
                } else {
                    break;
                }
            }

            int size=indexL-1;
            if(size<=0){
                break;
            }
            String filename = fileEntity.getInfo("filename");
            //说明是文件
            if(filename!=null){
                String tempDir =(String) serverParam.get(PpsWebConstant.TEMP_DIR_KEY);
                String tempFileName=tempDir+File.separator+UUID.randomUUID().toString();
                try(BufferedOutputStream fileOutputStream=new BufferedOutputStream(new FileOutputStream(tempFileName))) {
                    fileOutputStream.write(lineBuff, 0, size);
                    fileOutputStream.flush();
                    fileEntity.putInfo(PpsWebConstant.TEMP_FILE_KEY, tempFileName);
                    httpRequest.setSavleFile(true);
                }
            }else {
                byte [] t=new byte[size];
                System.arraycopy(lineBuff,0,t,0,size);
                fileEntity.setData(t);
            }
            list.add(fileEntity);
        }
        httpRequest.putHttpBody("body",list);

    }

    @Override
    public String getType() {
        return ContentTypeEnum.multipartformdata.getType();
    }
    public byte[] getSpline2(InputStream inputStream, int len) throws IOException {

        byte [] bytes=new byte[len];
        for (int i = 0; i < len; i++) {
            byte data = (byte) inputStream.read();
            if(data==-1){
                break;
            }
        }

        return bytes;
    }
    public byte[] getSpline(InputStream inputStream) throws IOException {

        //取第一行的标志位
        byte[] splitLine=new byte[1024];
        int k=0;
        while (true) {

                byte data = (byte) inputStream.read();

                if(data!=-1) {

                    splitLine[k] = data;
                    if (data == '\n' && k != 0 && splitLine[k - 1] == '\r') {
                        //----------------343434--\r\n 表示结束了
                        if (splitLine[k - 2] == '-' && splitLine[k - 3] == '-') {
                            return null;
                        }

                        k++;

                        if (k >= splitLine.length) {
                            splitLine = resize(splitLine, k);
                        }

                        break;
                    }

                    k++;
                    if (k >= splitLine.length) {
                        splitLine = resize(splitLine, k);
                    }
                }else {
                    break;
                }


        }
        byte[] newL=new byte[k];
        System.arraycopy(splitLine,0,newL,0,k);
        splitLine=newL;
        return splitLine;

    }


    public  String getDesc(InputStream inputStream) throws IOException {

        StringBuilder stringBuilder=new StringBuilder();
        byte[] lineBuff=new byte[1024];
        int indexL=0;
        while (true) {

                byte data = (byte) inputStream.read();
                if(data!=-1) {
                    lineBuff[indexL] = data;
                    if (data == '\n' && indexL != 0 && lineBuff[indexL - 1] == '\r') {
                        String s = new String(lineBuff, 0, indexL + 1, "utf-8");
                        //如果s==\r\n 那么说明这个下面就是具体的请求体字节内容
                        if (s.equals("\r\n")) {
                            break;
                        }
                        stringBuilder.append(s);
                        for (int i = 0; i < lineBuff.length; i++) {
                            lineBuff[i] = 0;
                        }
                        indexL = 0;
                        continue;
                    }

                    indexL++;
                    //扩容
                    if (indexL >= lineBuff.length) {
                        lineBuff = resize(lineBuff, indexL);
                    }
                }else {
                    break;
                }
        }
        return stringBuilder.toString();
    }
    public byte[] resize(byte[] lineBuff,int indexL){
        int resizeL = lineBuff.length * 2;
        if(resizeL>(Integer) serverParam.get("maxDataSize")){
            throw new RuntimeException("超过服务器  最大可支持数据大小！");
        }
        byte[] newLine=new byte[resizeL];
        System.arraycopy(lineBuff,0,newLine,0,indexL);
        return newLine;
    }

    public static class FileEntity{


        private Map<String,String> info=new HashMap<>();
        private byte [] data;
        private InputStream inputStream;
        public InputStream getInputStream() {
            if(inputStream!=null){
                return inputStream;
            }
            String url = info.get("pps-file-url");
            if(url==null){
                return null;
            }
            try {
                inputStream=new FileInputStream(url);
                return inputStream;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        void putInfo(String k,String v){
            info.put(k,v);
        }
        public String getInfo(String k){
            return info.get(k);
        }

        public void setData(byte[] data) {
            this.data = data;
        }

        public byte[] getData() {
            return data;
        }
    }
}
