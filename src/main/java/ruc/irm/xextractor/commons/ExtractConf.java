package ruc.irm.xextractor.commons;

import org.zhinang.conf.Configuration;

/**
 * 抽取的配置文件处理
 * <p/>
 * User: xiatian
 * Date: 4/2/13 6:57 PM
 */
public class ExtractConf {
    private static Configuration conf = new Configuration();

    static {
        conf.addResource("conf-extractor.xml");
    }

    public static final Configuration create() {
        return conf;
    }

}
