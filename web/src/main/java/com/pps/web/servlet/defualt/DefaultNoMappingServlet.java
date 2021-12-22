
package com.pps.web.servlet.defualt;

import com.pps.web.data.HttpRequest;
import com.pps.web.data.HttpResponse;
import com.pps.web.servlet.model.PpsHttpServlet;

/**
 * 没有找到资源的默认处理 404
 * @author Pu PanSheng, 2021/12/20
 * @version OPRA v1.0
 */
public class DefaultNoMappingServlet extends PpsHttpServlet {

        @Override
        public void get(HttpRequest request, HttpResponse response) {
            response.setCode(404);
            response.writeDirect("<h1>PPS-WEB-SERVER:</h1><div>url no mapping!</div>");
    }
}
