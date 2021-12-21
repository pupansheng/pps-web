
package com.pps.web.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

/**gzip 压缩
 * @author Pu PanSheng, 2021/12/20
 * @version OPRA v1.0
 */
public class GizpContentEncoding implements ContentEncoding {

    @Override
    public byte[] convert(byte[] source,int offset,int len) {
        try {
            return compress(source,offset,len);
        } catch (Exception e) {
           throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] convert(byte[] source) {
        return convert(source,0,source.length);
    }

    @Override
    public String support() {

        return "gzip";

    }
    public static byte[] compress(byte[] data,int offset,int len) throws Exception {

        // 压缩
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        GZIPOutputStream gzipOut = new GZIPOutputStream(bout); // 创建 GZIPOutputStream 对象
        gzipOut.write(data,offset,len); // 将响应的数据写到 Gzip 压缩流中
        gzipOut.close(); // 将数据刷新到  bout 字节流数组
        byte[] bts = bout.toByteArray();
        return bts;

    }

    public static void main(String args[]) throws Exception {



        // 压缩
        byte[] data="ghhhhhhhhhhhhhhhhhhhhhhhhhfytytryr".getBytes();
        System.out.println(data.length);
        byte [] d=compress(data,0,data.length);
        System.out.println(d.length);
    }

}
