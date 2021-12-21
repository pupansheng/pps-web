/*
 * Copyright (c) ACCA Corp.
 * All Rights Reserved.
 */
package com.pps.web.data;

import java.util.Map;

/**
 * @author Pu PanSheng, 2021/12/20
 * @version OPRA v1.0
 */
public interface ContentEncoding {

    default void init(Map<String,Object> serverParam){}

    byte[] convert(byte[] source,int offset,int len);
    byte[] convert(byte[] source);
    String support();
}
