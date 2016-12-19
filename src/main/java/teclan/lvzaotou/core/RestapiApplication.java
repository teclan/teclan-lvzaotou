package teclan.lvzaotou.core;

import static spark.Spark.after;
import static spark.Spark.before;
import static spark.Spark.ipAddress;
import static spark.Spark.port;
import static spark.Spark.staticFiles;
import static spark.Spark.threadPool;

import java.io.File;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import spark.servlet.SparkApplication;
import teclan.lvzaotou.core.db.Database;
import teclan.lvzaotou.core.service.media.MediaServiceApis;

public abstract class RestapiApplication implements SparkApplication {
    @Inject
    @Named("config.server.ip")
    private String           host;
    @Inject
    @Named("config.server.port")
    private int              port;
    @Inject
    @Named("config.media.public")
    private String           publicDir;
    @Inject
    @Named("config.server.max-threads")
    private int              maxThreads;
    @Inject
    @Named("config.server.min-threads")
    private int              minThreads = 2;
    @Inject
    @Named("config.server.time-out-millis")
    private int              timeOutMillis;

    @Inject
    private Database         database;
    @Inject
    private MediaServiceApis mediaServiceApis;

    @Override
    public void init() {
        defaultConfig();
        defaultApis();
        creatApis();
        filter();
    }

    public abstract void creatApis();

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    private void defaultConfig() {
        ipAddress(host);
        port(port);
        threadPool(maxThreads, minThreads, timeOutMillis);
    }

    private void defaultApis() {
        defaultServiceApis();
        defaultMediaServiceApis();
    }

    private void defaultServiceApis() {
        // Add something
    }

    private void defaultMediaServiceApis() {
        // staticFiles.location(getPublicDir());
        staticFiles.externalLocation(getPublicDir());
        mediaServiceApis.initApis();
    }

    public void filter() {
        before((request, response) -> {
            database.openDatabase();
        });
        after((request, response) -> {
            database.closeDatabase();
        });
    }

    private String getPublicDir() {
        String dir = System.getProperty("user.dir") + File.separator
                + publicDir;
        new File(dir).mkdirs();

        return dir;
    }
}
