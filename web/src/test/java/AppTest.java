import com.pps.base.PpsBoot;
import com.pps.web.HttpServer;
import com.pps.web.data.HttpRequest;
import com.pps.web.data.HttpResponse;
import com.pps.web.servlet.model.HttpServlet;
import com.pps.web.servlet.model.PpsHttpServlet;

/**
 * @author Pu PanSheng, 2021/12/20
 * @version OPRA v1.0
 */
public class AppTest {

    public static void main(String args[]){

        HttpServlet httpServlet=new PpsHttpServlet() {
            @Override
            public void get(HttpRequest request, HttpResponse response) {

               // System.out.println(Thread.currentThread().getId()+":  thread");
                String s="Hello  PPS!";
                response.writeDirect("<p> "+s+"</p>");


            }
        };
        HttpServer webServer=new HttpServer();
        webServer.setPort(9090);
        webServer.setBossSize(1);
        webServer.setWorkerSize(2);
        webServer.setContext("/qps");

        /**
         * 静态资源映射设置
         */
        webServer.setStaticResourceDir("c:\\test");
        webServer.setResourceMapping("/resource");

        webServer.setComprecess(true);
        webServer.addHttpServer("/qps", httpServlet);


        PpsBoot.run(webServer);



    }


}
