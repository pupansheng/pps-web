/*
 * Copyright (c) ACCA Corp.
 * All Rights Reserved.
 */
package com.pps.web.constant;

/**
 * @author Pu PanSheng, 2021/12/19
 * @version OPRA v1.0
 */
public enum  ContentTypeEnum {



    texthtml("text/html","html"),
    textplain("text/plain","text"),
    texttext("text/plain","txt"),
    textxml("text/xml","xml"),
    imagegif("image/gif","gif"),
    imagejpeg("image/jpeg","jpeg"),
    imagepng("image/png","png"),
    applicationjson("application/json","json"),
    applicationjavascript("application/javascript","javascript"),
    applicationxml("pplication/xml","xml"),
    applicationpdf("application/pdf","pdf"),
    applictionmswod("application/msword","word"),
    applicationstream("application/octet-stream","0000*"),
    applicationxwwwformurlencoded("application/x-www-form-urlencoded","0000*"),
    multipartformdata("multipart/form-data","0000*"),
    imagexicon("image/x-icon","icon");



    private String type;
    private String desc;

    ContentTypeEnum(String t,String d){
        type=t;
        desc=d;
    }
    public String getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }
}
