import com.pps.web.WebServer;
import com.pps.web.data.Application__multipart_form_dataHttpBodyResolve;
import com.pps.web.data.HttpRequest;
import com.pps.web.data.Response;
import com.pps.web.servlet.model.HttpServlet;
import com.pps.web.servlet.model.PpsHttpServlet;

import java.io.InputStream;
import java.util.List;

/**
 * @author Pu PanSheng, 2021/12/20
 * @version OPRA v1.0
 */
public class AppTest {

    public static void main(String args[]){

        HttpServlet httpServlet=new PpsHttpServlet() {
            @Override
            public void get(HttpRequest request, Response response) {

                String s="Hello  PPS!";
                response.writeDirect("<p> "+s+"</p>");


            }
        };
        WebServer webServer=new WebServer();
        webServer.setPort(9090);
        webServer.setBossSize(0);
        webServer.setWorkerSize(1);
        webServer.setContext("/qps");

        /**
         * 静态资源映射设置
         */
        webServer.setStaticResourceDir("c:\\test");
        webServer.setResourceMapping("/resource");

        webServer.setComprecess(true);
        webServer.addHttpServer("/qps", httpServlet);


        webServer.start();
    }


}
