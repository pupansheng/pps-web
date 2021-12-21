/*
 * Copyright (c) ACCA Corp.
 * All Rights Reserved.
 */
package com.pps.web.servlet.entity;

import com.pps.web.exception.ChannelCloseException;
import com.pps.web.util.BufferUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * @author Pu PanSheng, 2021/12/18
 * @version OPRA v1.0
 */
public class PpsInputSteram extends InputStream {

    private int readIndex;
    private int nowCap;
    private SocketChannel socketChannel;
    private SelectionKey selectionKey;
    private ByteBuffer byteBuffer;

    public PpsInputSteram(SocketChannel socketChannel, SelectionKey selectionKey) {
        this.socketChannel = socketChannel;
        this.selectionKey=selectionKey;
        byteBuffer=BufferUtil.getBuffer();
    }


    public void returnBuffer(){
        BufferUtil.returnBuffer(byteBuffer);
    }

    @Override
    public int read() throws IOException {

        if(readIndex>=nowCap){
            byteBuffer.clear();
            int read=socketChannel.read(byteBuffer);
            nowCap=read;
            if(read>0){
                nowCap=read;
                readIndex=0;
                byteBuffer.flip();
            }else if(read<0){
                //待议
                //selectionKey.cancel();
                //socketChannel.close();
                BufferUtil.returnBuffer(byteBuffer);
                throw new ChannelCloseException("close");
            }else {
                BufferUtil.returnBuffer(byteBuffer);
                return -1;
            }

        }
        byte b = byteBuffer.get(readIndex++);

        return b;
    }

    @Override
    public int available() throws IOException {

        throw new UnsupportedOperationException("不支持");
    }
}
