
package com.pps.base;


import com.pps.web.servlet.entity.PpsInputSteram;

/**
 * @author Pu PanSheng, 2021/12/22
 * @version OPRA v1.0
 */
public interface EventHander {

    void init(Server server);

    void  read(PpsInputSteram ppsInputSteram, Worker workers);

    String support();
}
