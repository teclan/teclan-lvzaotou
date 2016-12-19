package teclan.lvzaotou.example;

import com.google.inject.Guice;
import com.google.inject.Injector;

import teclan.lvzaotou.core.guice.ConfigModule;

public class Main {
    public static void main(String[] args) {
        Injector injector = Guice.createInjector(
                new ConfigModule("config.conf", "config"), new SeverModule());
        Application application = injector.getInstance(Application.class);
        application.init();
    }

}
