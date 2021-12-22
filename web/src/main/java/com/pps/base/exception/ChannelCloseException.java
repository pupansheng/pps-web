/*
 * Copyright (c) ACCA Corp.
 * All Rights Reserved.
 */
package com.pps.base.exception;

/**
 * @author Pu PanSheng, 2021/12/19
 * @version OPRA v1.0
 */
public class ChannelCloseException extends RuntimeException {

    public ChannelCloseException(String message) {
        super(message);
    }

    public ChannelCloseException(Throwable cause) {
        super(cause);
    }

}
