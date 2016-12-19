package teclan.lvzaotou.core;

import static spark.Spark.after;
import static spark.Spark.before;

import spark.servlet.SparkApplication;

public abstract class AbstractApplication implements SparkApplication {
    @Override
    public void init() {
        createApis();
        filter();
    }

    public void filter() {
        before((request, response) -> {
            // 请求处理之前执行逻辑，一般用于打开数据库等...
        });

        after((request, response) -> {
            // 请求处理结束执行逻辑，一般用于关闭数据库等...
        });

    }

    public abstract void createApis();

    public abstract String getServerName();
}
