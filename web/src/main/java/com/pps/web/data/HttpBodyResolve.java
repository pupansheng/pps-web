
package com.pps.web.data;

import java.util.Map;

/**
 * @author Pu PanSheng, 2021/12/19
 * @version OPRA v1.0
 */
public interface HttpBodyResolve {

   default void init(Map<String,Object> serverParam){}

   void resolve(HttpRequest httpRequest) throws Exception;

   String getType();

}
