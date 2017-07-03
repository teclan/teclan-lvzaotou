package teclan.lvzaotou.core;

import static spark.Spark.after;
import static spark.Spark.before;
import static spark.Spark.halt;
import static spark.Spark.ipAddress;
import static spark.Spark.port;
import static spark.Spark.staticFiles;
import static spark.Spark.threadPool;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import spark.Request;
import spark.servlet.SparkApplication;
import teclan.lvzaotou.core.db.Database;
import teclan.lvzaotou.core.service.media.MediaServiceApis;
import teclan.lvzaotou.example.model.Permission;
import teclan.lvzaotou.example.model.User;

public abstract class RestapiApplication implements SparkApplication {
	private final Logger     LOGGER     = LoggerFactory
            .getLogger(RestapiApplication.class);
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
    @Named("config.server.authenticate.enabled")
    private boolean          enabled;

    @Inject
    @Named("config.server.authenticate.access-user")
    private String           accessUser;

    @Inject
    @Named("config.server.authenticate.access-token")
    private String           accessToken;

    @Inject
    @Named("config.server.authenticate.invalid-time")
    private int              invalidTime;

    @Inject
    private Database         database;
    @Inject
    private MediaServiceApis mediaServiceApis;

    private String           env;
    SimpleDateFormat         DATE_FOMAT = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");

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

        initLogger(env, getClass());

    }

    public void initLogger(String env, Class<?> T) {

        if (env == null) {
            LOGGER.warn("\n\nHave you forget to set env for the app, "
                    + "if you are sure to skip this,let's skip that "
                    + "and the config on class path for logback will "
                    + "be use!\n");
            return;
        }

        LoggerContext context = (LoggerContext) LoggerFactory
                .getILoggerFactory();

        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            context.reset();
            configurator.doConfigure(
                    String.format("environments/%s/logback.xml", env));
            LOGGER.info("logback config on {} effective",
                    String.format("environments/%s/logback.xml", env));
        } catch (JoranException je) {
            // StatusPrinter will handle this
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(context);
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
            if (!database.hasConnect()) {
                database.openDatabase();
            }
            if (enabled && !authenticate(request)) {
                halt(401);
            }
        });

        after((request, response) -> {
            if (database.hasConnect()) {
                database.closeDatabase();
            }
        });
    }

    private String getPublicDir() {
        String dir = System.getProperty("user.dir") + File.separator
                + publicDir;
        new File(dir).mkdirs();

        return dir;
    }

    private boolean authenticate(Request request) {

        if (request.url().contains("/login")
                || request.url().contains("/sign-in")
                || request.url().contains("/sign-up")
                || request.url().contains("/sign-out")) {
            return true;
        }

        if (request.headers(accessUser) == null
                || request.headers(accessToken) == null) {
            return false;
        }

        User user = User.findFirst("username = ?", request.headers(accessUser));

        if (user == null || !user.getString("token")
                .equals(request.headers(accessToken))) {
            LOGGER.warn("\n用户 {} 认证失败，token 无效，尝试访问URL {}",
                    request.headers(accessUser), request.url());
            return false;
        }

        long lastAction = 0;

        try {
            lastAction = DATE_FOMAT.parse(user.getString("last_action"))
                    .getTime();
        } catch (ParseException e) {
            LOGGER.error(e.getMessage(), e);
        }
        if (new Date().getTime() - lastAction > invalidTime * 1000 * 60) {
            LOGGER.warn("\n用户 {} 认证失败，会话超时，尝试访问URL {}",
                    request.headers(accessUser), request.url());
            return false;
        }

        String role = user.getString("role");

        if (Permission.findFirst("role = ? and permisson = ?", role, request
                .url().substring(0, request.url().lastIndexOf("/"))) == null) {
            LOGGER.warn("\n用户 {} 认证失败，无访问资源权限，尝试访问URL {}",
                    request.headers(accessUser), request.url());
            return false;
        }

        return true;
    }

    public void setEnv(String env) {
        this.env = env;
    }

}
