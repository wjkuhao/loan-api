package com.mod.loan.util.kuaiqian.mgw.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * <p>Title:mip</p>
 * <p>Description:</p>
 * <p>Copyright:</p>
 * <p>Company: </p>
 *
 * @author panqr
 */
public class PropFile {
    private static Properties props = null;

    private PropFile() {

    }

    public static synchronized Properties getProps(String propFile) {
        if (props == null) {
            props = new Properties();
            InputStream in = null;
            try {
                in = PropFile.class.getResourceAsStream(propFile);
                props.load(in);
                return props;
            } catch (IOException e) {
                System.out.println("读取配置文件失败");
                e.printStackTrace();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            return props;
        }
        return null;
    }

}
