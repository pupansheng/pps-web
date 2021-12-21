
package com.pps.web.data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Pu PanSheng, 2021/12/19
 * @version OPRA v1.0
 */
public class HttpAlgoFactory {

    private static Map<String, HttpBodyResolve> factory=new HashMap<>();
    private static Map<String, ContentEncoding> encodingFactory=new HashMap<>();

    public static HttpBodyResolve getHttpBodyResoveAlgo(String name){
        return factory.get(name);
    }
    public static void putHttpBodyResolveAlgo(HttpBodyResolve httpBodyResolve){
        factory.put(httpBodyResolve.getType(),httpBodyResolve);
    }

    public static ContentEncoding getContentEncodingAlgo(String name){
        return encodingFactory.get(name);
    }
    public static void putContentEncodingAlgo(ContentEncoding contentEncoding){
        encodingFactory.put(contentEncoding.support(), contentEncoding);
    }
}
