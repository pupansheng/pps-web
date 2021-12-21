
package com.pps.web.data;

import com.pps.web.constant.ContentTypeEnum;
import com.pps.web.util.BufferUtil;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Pu PanSheng, 2021/12/19
 * @version OPRA v1.0
 */
public class Application_json_HttpBodyResolve implements HttpBodyResolve {

    private Map<String, Object> serverParam;

    @Override
    public void init(Map<String, Object> serverParam) {
        this.serverParam=serverParam;
    }
    @Override
    public void resolve(HttpRequest httpRequest) throws Exception {

        InputStream inputStream = httpRequest.getInputStream();
        List<byte[]> d=new ArrayList<>();
        byte[] buffer=new byte[1024];
        int read = inputStream.read(buffer);
        while (read!=-1) {
            byte [] g=new byte[read];
            for (int i = 0; i < read; i++) {
                g[i]=buffer[i];
            }
            d.add(g);
            read=inputStream.read(buffer);
        }

        byte[] bytes = BufferUtil.listToArray(d);
        String s = BufferUtil.byteToStr(bytes);
        httpRequest.putHttpBody("body",s);

    }

    @Override
    public String getType() {
        return ContentTypeEnum.applicationjson.getType();
    }
}
