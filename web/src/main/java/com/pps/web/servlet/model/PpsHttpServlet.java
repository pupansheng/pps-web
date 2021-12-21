
package com.pps.web.servlet.model;

import java.util.Map;

/**
 * @author Pu PanSheng, 2021/12/18
 * @version OPRA v1.0
 */
public abstract class PpsHttpServlet implements HttpServlet {

    protected Map<String,Object> serverParams;

    public PpsHttpServlet(){

    }
    @Override
    public void init(Map<String,Object> map){
        this.serverParams=map;
    }

    public String getContext(){
        return (String)serverParams.get("context");
    }
    public int getPort(){
        return (Integer)serverParams.get("port");
    }

}
