import com.pps.web.data.Application__multipart_form_dataHttpBodyResolve;
import com.pps.web.data.HttpRequest;
import com.pps.web.data.HttpResponse;
import com.pps.web.servlet.model.PpsHttpServlet;

import java.io.InputStream;
import java.util.List;

/**
 * @author Pu PanSheng, 2021/12/21
 * @version OPRA v1.0
 */
public class UploadServlet extends PpsHttpServlet {
    @Override
    public void get(HttpRequest request, HttpResponse response) {


        List<Application__multipart_form_dataHttpBodyResolve.FileEntity> fromData = request.getFromData();


        fromData.forEach(f->{

            f.getInfo("name");
            String filename = f.getInfo("filename");
            //说明时文件流
            if(filename!=null) {
                //得到文件流
                InputStream inputStream = f.getInputStream();



            }else {
                //普通key  那么直接转成字符
                byte[] data = f.getData();
                String s = new String(data);


            }

        });

    }
}
