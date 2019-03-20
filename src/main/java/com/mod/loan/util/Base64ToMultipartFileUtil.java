package com.mod.loan.util;

import org.springframework.web.multipart.MultipartFile;
import sun.misc.BASE64Decoder;

import java.io.*;

public class Base64ToMultipartFileUtil implements MultipartFile {

    private final byte[] imgContent;
    private final String header;

    private Base64ToMultipartFileUtil(byte[] imgContent, String header) {
        this.imgContent = imgContent;
        this.header = header.split(";")[0];
    }

    @Override
    public String getName() {
        return System.currentTimeMillis() + Math.random() + "." + header.split("/")[1];
    }

    @Override
    public String getOriginalFilename() {
        return System.currentTimeMillis() + (int) (Math.random() * 10000) + "." + header.split("/")[1];
    }

    @Override
    public String getContentType() {
        return header.split(":")[1];
    }

    @Override
    public boolean isEmpty() {
        return imgContent == null || imgContent.length == 0;
    }

    @Override
    public long getSize() {
        return imgContent.length;
    }

    @Override
    public byte[] getBytes()  {
        return imgContent;
    }

    @Override
    public InputStream getInputStream()   {
        return new ByteArrayInputStream(imgContent);
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        new FileOutputStream(dest).write(imgContent);
    }

    //传过来的base64字符串没有带头，头就是这样的，
    //“data:image/png;base64,iVBORw0KGgo...”
    //如果base64文件中没有这部分，
    //使用代码中的baseStrs[0]，然后文件类型让前台单独给你传过来，或者让前台传完整的base64字符串过来
    public static MultipartFile base64ToMultipart(String base64) {
        try {
            String[] baseStrs = base64.split(",");

            BASE64Decoder decoder = new BASE64Decoder();
            byte[] b = new byte[0];
            b = decoder.decodeBuffer(baseStrs[0]);

            for(int i = 0; i < b.length; ++i) {
                if (b[i] < 0) {
                    b[i] += 256;
                }
            }
            return new Base64ToMultipartFileUtil(b, baseStrs[0]);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
