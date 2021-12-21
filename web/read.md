# 这是一个使用java nio sdk 开发的web服务器   类似嵌入式Tomcat      #
---
+ 支持文件上传  chunk传输    gzip 压缩     
+ 也仿照着 netty处理了NIO的空轮询BUG

=====使用举例



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

