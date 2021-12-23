# 这是一个使用java nio sdk 开发的web服务器   类似嵌入式Tomcat    

* 支持文件上传  chunk传输    gzip 压缩     
* 也仿照着 netty处理了NIO的空轮询BUG


## 使用举例

```java
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
               Server webServer=new HttpServer();
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
```


### 2021-12-22

* 最新调整项目 
<pre>
使其易于扩展其他协议  如果想要实现其他协议
那么只需要利用spi  实现我的com.pps.base.EventHander接口 并在meta-info里面添加对应实现类 即可实现自己的协议
可以参考 我的Http协议的实现 ：
com.pps.web.hander.HttpEventHander
</pre>
