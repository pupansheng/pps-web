
package com.pps.web.servlet.defualt;

import com.pps.web.data.HttpRequest;
import com.pps.web.data.Response;
import com.pps.web.servlet.model.PpsHttpServlet;

/**
 * 服务器 错误发生的默认处理 500
 * @author Pu PanSheng, 2021/12/20
 * @version OPRA v1.0
 */
public class DefaultErrorServlet extends PpsHttpServlet {
    @Override
    public void get(HttpRequest request, Response response) {
        response.setCode(500);
        response.writeDirect("<h1>PPS-WEB-SERVER:</h1><div>SERVER ERROR</div>");
    }
}
