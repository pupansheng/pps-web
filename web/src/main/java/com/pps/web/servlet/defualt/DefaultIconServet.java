
package com.pps.web.servlet.defualt;


import com.pps.base.Server;
import com.pps.web.constant.ContentTypeEnum;
import com.pps.web.constant.PpsWebConstant;
import com.pps.web.data.HttpRequest;
import com.pps.web.data.HttpResponse;
import com.pps.web.servlet.model.PpsHttpServlet;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 图标默认处理
 * @author Pu PanSheng, 2021/12/20
 * @version OPRA v1.0
 */
public class DefaultIconServet  extends PpsHttpServlet {



    @Override
    public void get(HttpRequest request, HttpResponse response) {
        String o = (String) serverParams.get(PpsWebConstant.ICON_LOCATION);
        byte[] bytes=new byte[0];
        if(o!=null){
            try {
                bytes= Files.readAllBytes(Paths.get(o));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            URL resource = Server.class.getClassLoader().getResource("resource/icon.png");
            if (resource != null) {
                response.setContentType(ContentTypeEnum.imagexicon.getType());
                try {
                    bytes = Files.readAllBytes(Paths.get(resource.toURI()));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        }
        response.writeDirect(bytes);
    }
}
