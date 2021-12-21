/*
 * Copyright (c) ACCA Corp.
 * All Rights Reserved.
 */
package com.pps.web.constant;

/**
 * @author Pu PanSheng, 2021/12/19
 * @version OPRA v1.0
 */
public class PpsWebConstant {


    /**
     * 端口   v:  int    default: 9090
     */
    public static final String PORT_KEY="port";

    /**
     * 上下文：  v: string  default: /
     */
    public static final String CONTEXT_KEY="context";

    /**
     * 普通工作线程数
     * 处理read 事件   v:  int  default: 1
     */
    public static final String WORKER_SIZE_KEY="workerSize";


    /**
     * web处理器 最大线程数 默认为cpu核心数
     */
    public static final String MAX_THREAD_KEY="maxThread";

    /**
     * 处理socket连接线程数
     * 处理连接事件 并将连接好的socket      给worker
     * 若他配置为0 那么普通的worker  将会担任两个角色分配给工作线程
     * v: int  default: 1
     */
    public static final String BOSSER_SIZE_KEY="bosserSize";


    /**
     * 最大上传大小
     */

    public static final String MAX_UPLOAD_SIZE="maxDataSize";


    /**
     * 临时目录  key     v:    string default: c://pps-web-temp
     */
    public static final String TEMP_DIR_KEY="tempDir";

    /**
     * 静态资源目录 key  v:     string  default: c://test
     */
    public static final String RESOUCE_DIR_KEY="resoueceDir";

    /**
     * 静态资源映射路径  KEY   v:   string   default: /resource
     */
    public static final String RESOUCE_MAPPING_DIR_KEY="resourceMappingDir";

    /**
     * 网站图标位置 KEY   v:   string
     */
    public static final String ICON_LOCATION="iconLocation";


    /**
     * 能够匹配所有的请求的servlet 的maping url
     */
    public static final String MATCH_MAPPING_ALL_URL="/*";


    /**
     * byteBuffer 初始大小
     */
    public static final int BUFFER_INIT_LENGTH=1024;


    /**
     * 是否开启gzip 压缩     v:   boolean   default: false
     */
    public static final String OPEN_CONPRECESS_KEY="openCompress";
    /**
     * 压缩支持类型  v: String (content-type) 多个 使用，分隔
     */
    public static final String CONPRECESS_TYPE_KEY="compressType";

    /**
     * 默认的servlet key 如果要替换 那么key 应该为他  下同
     */
    public static final String DEFAULT_SERVLET="defaultSertvlet";
    public static final String ERROR_SERVLET="errorSertvlet";
    public static final String RESOURCE_SERVLET="staticResourceSertvlet";
    public static final String ICON_SERVLET="iconServlet";







    //以下 为内部使用  没有用处


    /**
     * worker select(timeout)
     */
    public static final int TIMEOUT=1000;

    /**
     * nio bug count计数器数值
     */
    public static final int TEST_BUG_COUNT=512;


    /**
     * 内部使用
     */
    public static final String TEMP_FILE_KEY="pps-file-url";


    /**
     * 内部使用 默认编码
     */
    public static final String CHAR_SET="utf-8";

}
