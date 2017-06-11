package teclan.lvzaotou.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;

import teclan.lvzaotou.core.guice.ConfigModule;

public class Main {
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {

		String env = System.getProperty("REST_ENV");

		if (env == null) {
			env = "development";
		}
		Injector injector = Guice.createInjector(new ConfigModule("config.conf", "config"), new SeverModule());
		Application application = injector.getInstance(Application.class);
		application.setEnv(env);
		application.init();
	}

}
