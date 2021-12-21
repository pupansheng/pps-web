
package com.pps.web.servlet.defualt;

import com.pps.web.constant.ContentTypeEnum;
import com.pps.web.constant.PpsWebConstant;
import com.pps.web.data.HttpRequest;
import com.pps.web.data.Response;
import com.pps.web.servlet.model.PpsHttpServlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 讲静态资源的默认处理
 * @author Pu PanSheng, 2021/12/20
 * @version OPRA v1.0
 */
public class DefaultStaticResourceServlet  extends PpsHttpServlet {


    private volatile Map<String, String> resource;

    public void scanFile(String root){
        //检索资源目录
        String context =(String) serverParams.get(PpsWebConstant.CONTEXT_KEY);
        String mapUrl=(String) serverParams.get(PpsWebConstant.RESOUCE_MAPPING_DIR_KEY);
        String dir=(String) serverParams.get(PpsWebConstant.RESOUCE_DIR_KEY);
        File file=new File(root);
        if(file==null){
            return;
        }
        for (File listFile : file.listFiles()) {
            if(listFile.isDirectory()){
                scanFile(listFile.getPath());
            }else if(listFile.isFile()){
                String path = listFile.getPath();
                String s = context + mapUrl;
                path=path.replace(dir,s);
                path=path.replace(File.separator,"/");
                resource.put(path, listFile.getAbsolutePath());
            }
        }
    }
    @Override
    public boolean isMatch(String url) {

        if(resource==null){
            synchronized (this) {
                resource = new HashMap<>();
                scanFile((String) serverParams.get(PpsWebConstant.RESOUCE_DIR_KEY));
            }
        }
        return  resource.containsKey(url);

    }

    @Override
    public void get(HttpRequest request, Response response) {

        String url = request.getUrl();
        String u = resource.get(url);
        try {
            int i = url.lastIndexOf(".");
            String suffix = url.substring(i + 1);
            ContentTypeEnum contentTypeEnum=null;
            for (ContentTypeEnum value : ContentTypeEnum.values()) {
                String type = value.getType();
                if(type.endsWith(suffix)||suffix.equals(value.getDesc())){
                    contentTypeEnum=value;
                    break;
                }
            }
            //下载
            if(contentTypeEnum==null){
                contentTypeEnum=ContentTypeEnum.applicationstream;
            }
            response.setContentType(contentTypeEnum.getType());

            try (FileInputStream fileInputStream = new FileInputStream(u)) {

                byte[] bu = new byte[1024];
                int read = fileInputStream.read(bu);
                while (read != -1) {
                    response.write(bu, 0, read);
                    read = fileInputStream.read(bu);
                }
                response.flush();

            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("静态资源映射过程出错");
        }

    }
}
