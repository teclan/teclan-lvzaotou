package teclan.lvzaotou.core.guice;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class ConfigModule extends AbstractModule {
    private final Logger LOGGER = LoggerFactory.getLogger(ConfigModule.class);
    private final String configFile;
    private final String root;

    /**
     * 加载项目目录下 conf/ 的配置文件，如果找不到，则尝试 classPath 下的配置文件
     * 
     * @param configFile
     *            配置文件名称
     * @param root
     *            配置文件的跟对象
     */
    public ConfigModule(String configFile, String root) {
        this.configFile = configFile;
        this.root = root;
    }

    @Override
    protected void configure() {

        Config config;
        String currentPath = System.getProperty("user.dir");

        File configFile = new File(currentPath + "/conf/" + this.configFile);

        if (!configFile.exists()) {
            LOGGER.error("the config file is not found : {}",
                    configFile.getAbsolutePath());
            config = ConfigFactory.load(this.configFile);
        } else {
            config = ConfigFactory.parseFile(configFile);
        }

        new ConfigBinder(binder()).bind(config, this.root);
    }
}
