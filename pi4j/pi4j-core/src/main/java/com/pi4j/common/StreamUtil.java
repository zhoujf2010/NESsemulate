package com.pi4j.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class StreamUtil
{
    public static byte[] readAllBytes(InputStream is) {
        byte[] content = null;
        ByteArrayOutputStream baos = null;
        try {
            byte[] buffer = new byte[1024 * 4];
            // 打开一个输出流
            baos = new ByteArrayOutputStream();
            // 记录读到缓冲buffer中的字节长度
            int ch = 0;
            while ((ch = is.read(buffer)) != -1) {
                // 因为有可能出现ch与buffer的length不一致的问题,所以用下面的写法
                baos.write(buffer, 0, ch);
            }
            content = baos.toByteArray();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
//                if (is != null)
//                    is.close();
                if (baos != null)
                    baos.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return content;
    }

    public static byte[] readNBytes(InputStream is,int len) {
        byte[] content = null;
        ByteArrayOutputStream baos = null;
        try {
            byte[] buffer = new byte[1024 * 4];
            // 打开一个输出流
            baos = new ByteArrayOutputStream();
            // 记录读到缓冲buffer中的字节长度
            int ch = 0;
            while ((ch = is.read(buffer)) != -1) {
                // 因为有可能出现ch与buffer的length不一致的问题,所以用下面的写法
                baos.write(buffer, 0, ch);
            }
            content = baos.toByteArray();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
//                if (is != null)
//                    is.close();
                if (baos != null)
                    baos.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return content;
    }

}
