
package com.pps.web.util;

import com.pps.web.constant.PpsWebConstant;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * @author Pu PanSheng, 2021/12/17
 * @version OPRA v1.0
 */
public class BufferUtil {

    private static ByteBuffer[] pool;
    private static AtomicBoolean[] status;
    private static int initSize=PpsWebConstant.BUFFER_INIT_LENGTH;
    static {
       int cpu =Runtime.getRuntime().availableProcessors();
       pool=new ByteBuffer[cpu];
       status=new AtomicBoolean[cpu];
        for (int i = 0; i < status.length; i++) {
            status[i]=new AtomicBoolean(false);
        }
    }

    public static byte[] strToBytes(String c) {
        try {
            return c.getBytes(PpsWebConstant.CHAR_SET);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("转换出错");
    }
    public static byte[] strToBytes(String c,String charset) {
        try {
            return c.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("转换出错");
    }

    public static String byteToStr(byte[] c) {
        try {
            return new String(c, PpsWebConstant.CHAR_SET);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("转换出错");
    }

    public static ByteBuffer getBuffer() {

        for (int i = 0; i < status.length; i++) {

            if(status[i].compareAndSet(false,true)){
                ByteBuffer byteBuffer = pool[i];
                if(byteBuffer==null){
                    pool[i]=ByteBuffer.allocateDirect(initSize);
                }
                pool[i].clear();
                return pool[i];
            }
        }

        return ByteBuffer.allocate(initSize);
    }
    public static void returnBuffer(ByteBuffer byteBuffer){

        for (int i = 0; i < pool.length; i++) {
            if(pool[i]==byteBuffer&&status[i].get()){
                byteBuffer.clear();
                status[i].getAndSet(false);
                break;
            }
        }
    }

    public static void write(String content, Consumer<ByteBuffer> consumer) {

        ByteBuffer buffer = getBuffer();
        int capacity = buffer.capacity();
        byte[] data = strToBytes(content);
        int length = data.length;
        int start = 0;
        int size = capacity;
        while (start < length) {
            buffer.clear();
            int sizeE = length - start >= size ? size : length - start;
            buffer.put(data, start, sizeE);
            start = start + size;
            if (start > length) {
                start = length;
            }
            buffer.flip();
            consumer.accept(buffer);
        }

        BufferUtil.returnBuffer(buffer);
    }
    public static void write(byte[] data, Consumer<ByteBuffer> consumer) {

        ByteBuffer buffer = getBuffer();
        int capacity = buffer.capacity();
        int length = data.length;
        int start = 0;
        int size = capacity;
        while (start < length) {
            buffer.clear();
            int sizeE = length - start >= size ? size : length - start;
            buffer.put(data, start, sizeE);
            start = start + size;
            if (start > length) {
                start = length;
            }
            buffer.flip();
            consumer.accept(buffer);
        }

        BufferUtil.returnBuffer(buffer);
    }

    public static byte[] readAll(SocketChannel socketChannel) throws IOException {


        ByteBuffer byteBuffer = ByteBuffer.allocate(10 * 1024 * 1024);
        byteBuffer.clear();
        int read = socketChannel.read(byteBuffer);
        byteBuffer.flip();
        byte[] bytes=new byte[read];
        byteBuffer.get(bytes);
        return bytes;

    }

    public static byte[] listToArray(List<byte[]> data){

        int length=0;
        for (int i = 0; i < data.size(); i++) {
            byte[] bytes = data.get(i);
            length=length+bytes.length;
        }
        byte[] bytes=new byte[length];
        int index=0;
        for (int i = 0; i < data.size(); i++) {
            byte[] bytes2 = data.get(i);
            for (int i1 = 0; i1 < bytes2.length; i1++) {
                bytes[index]=bytes2[i1];
                index++;
            }
        }

        return bytes;
    }

}
