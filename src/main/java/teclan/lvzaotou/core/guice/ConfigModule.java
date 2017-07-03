package teclan.lvzaotou.core.guice;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class ConfigModule extends AbstractModule {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigModule.class);

	private final String configFile;
	private final String root;

	public ConfigModule(String configFile, String root) {
		this.configFile = System.getProperty("user.dir") + File.separator
				+ String.format("environments/%s/config.conf", "development");
		this.root = root;
	}

	@Override
	protected void configure() {
	    
	    File file = new File(configFile);
	    
	    if(!file.exists()){
	        LOGGER.error("config file is not exists ! {}",configFile);
	        return;
	    }
		Config config = ConfigFactory.parseFile(new File(configFile));

		new ConfigBinder(binder()).bind(config, this.root);

	}
}
