
package com.pps.web.servlet.model;


import com.pps.web.data.HttpRequest;
import com.pps.web.data.HttpResponse;

import java.util.Map;

/**
 * @author Pu PanSheng, 2021/12/17
 * @version OPRA v1.0
 */
public interface HttpServlet {


    default void init(Map<String,Object> map){};

    default boolean isMatch(String url){
        return false;
    }

    void get(HttpRequest request, HttpResponse response);

    default int sort() {
        return -100000000;
    }

}
