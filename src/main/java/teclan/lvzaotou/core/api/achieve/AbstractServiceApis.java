package teclan.lvzaotou.core.api.achieve;

import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.put;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import teclan.lvzaotou.core.api.ServiceApis;
import teclan.lvzaotou.core.service.db.ActiveJdbcService;
import teclan.lvzaotou.core.service.db.ActiveRecord;
import teclan.lvzaotou.core.utils.GsonUtils;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

public abstract class AbstractServiceApis<T extends ActiveRecord>
        implements ServiceApis {
    private final Logger LOGGER = LoggerFactory
            .getLogger(AbstractServiceApis.class);

    @Inject
    @Named("config.server.name-space")
    private String       nameSpace;

    @Override
    public void initApis() {

        all();

        findById();

        fetch();

        addWithPost();

        addWithPut();

        sysById();

        sysBatch();

        deleteById();

        deleteBatch();

        customizeApis();
    }

    protected abstract void customizeApis();

    /**
     * GET请求
     * </p>
     * 查询所有
     */
    protected void all() {
        get(getUrlPrefix() + "/all", (request, response) -> {
            List<T> results = getService().all();
            if (results != null && !results.isEmpty()) {
                return generateResult(results).toString();
            } else {
                LOGGER.warn("\n无查询结果 {}", getResource() + "/all");
                return new JSONObject();
            }
        });
    }

    /**
     * GET请求
     * </p>
     * 按id查询
     */
    protected void findById() {
        get(getUrlPrefix() + "/fetch/:id", (request, response) -> {
            long id = Long.valueOf(request.params(":id"));
            T record = getService().findById(id);
            if (record != null) {
                return generateResult(record);
            } else {
                LOGGER.warn("\n无查询结果 {}", getResource() + "/fetch/" + id);
                return new JSONObject();
            }
        });
    }

    /**
     * POST请求
     * </p>
     * 分页查询
     * </p>
     * page 从 1 开始，如果 page = all 则忽略所有条件，查询所有
     * </p>
     * 支持条件查询，所有条件包装在参数中的 condition 字段，json对象形式存放
     * 如果查询结果正确，结果信息中有以下字段：
     * date : 记录列表信息
     * mate : 返回结果头信息
     * </p>
     * 其中mate包含以下字段
     * total : 满足当前条件的所有记录条数
     * offset : 当前是第几页
     * limit : 每页多少条记录
     * pages : 总共有多少页
     */
    protected void fetch() {
        post(getUrlPrefix() + "/fetch", (request, response) -> {
            if ("all".equals(request.queryParams("page"))) {

                List<T> results = getService().all();
                if (results != null && !results.isEmpty()) {
                    return generateResult(results);
                } else {
                    LOGGER.warn("\n无查询结果 {}", getResource() + "/all");
                    return new JSONObject();
                }

            } else {
                int page = 1;
                int limit = 10;
                String query = "1 = ?";
                Object[] parameters = new Object[] { 1 };
                if (request.queryParams("page") != null) {
                    page = Integer.valueOf(request.queryParams("page"));
                }
                if (request.queryParams("limit") != null) {
                    limit = Integer.valueOf(request.queryParams("limit"));
                }

                Object[] params = request.queryParams().toArray();

                Map<String, String> paramsMap = new LinkedHashMap<String, String>();

                for (Object param : params) {
                    if ("page".equals(param) || "limit".equals(param)) {
                        continue;
                    }
                    paramsMap.put((String) param,
                            request.queryParams((String) param));

                }

                if (paramsMap != null) {
                    query = generateQuery(paramsMap);
                    parameters = generateParameters(paramsMap);
                }

                return getService().fetch(page, limit, query, parameters)
                        .toJson();
            }
        });
    }

    /**
     * POST请求
     * </p>
     * 添加记录
     */
    protected void addWithPost() {
        post(getUrlPrefix() + "/new", (request, response) -> {

            Object[] params = request.queryParams().toArray();

            Map<String, Object> attributes = new LinkedHashMap<String, Object>();

            for (Object param : params) {
                attributes.put((String) param,
                        request.queryParams((String) param));
            }

            getService().create(attributes);
            return request.body();
        });
    }

    /**
     * PUT请求
     * </p>
     * 添加记录
     */
    protected void addWithPut() {
        put(getUrlPrefix() + "/new", (request, response) -> {

            LOGGER.info(request.body());
            Map<String, Object> attributes = GsonUtils
                    .toMap(new JSONObject(request.body()).get(getResource())
                            .toString());

            getService().create(attributes);
            return request.body();
        });
    }

    /**
     * PUT请求
     * </p>
     * 指定 id 更新记录
     */
    protected void sysById() {
        put(getUrlPrefix() + "/sys/:id", (request, response) -> {
            long id = Long.valueOf(request.params(":id"));
            Map<String, Object> attributes = GsonUtils
                    .toMap(new JSONObject(request.body()).get(getResource())
                            .toString());
            List<T> results = getService().sync(id, attributes);
            if (results != null && !results.isEmpty()) {
                return generateResult(results);
            } else {
                LOGGER.warn("\n无查询结果 {}", getResource() + "/sys/" + id);
                return new JSONObject();
            }
        });
    }

    /**
     * PUT请求
     * </p>
     * 批量更新记录
     */
    protected void sysBatch() {
        put(getUrlPrefix() + "/sys", (request, response) -> {
            List<Map<String, Object>> maps = GsonUtils.fromJson(request.body(),
                    getResource(), new TypeToken<List<Map<String, Object>>>() {
                        private static final long serialVersionUID = 3731405824720413383L;
                    }.getType());

            List<T> results = getService().sync(maps);
            if (results != null && !results.isEmpty()) {
                return generateResult(results);
            } else {
                LOGGER.warn("\n无查询结果 {}", getResource() + "/sys");
                return new JSONObject();
            }
        });
    }

    /**
     * DELETE请求
     * </p>
     * 指定id删除记录
     */
    protected void deleteById() {
        delete(getUrlPrefix() + "/delete/:id", (request, response) -> {
            long id = Long.valueOf(request.params(":id"));
            getService().delete(id);
            return new JSONObject();
        });
    }

    /**
     * DELETE请求
     * </p>
     * 批量删除记录
     * </p>
     * 如果 ids 不存在，将删除所有记录
     */
    protected void deleteBatch() {
        delete(getUrlPrefix() + "/deletes/:ids", (request, response) -> {
            String[] ids = null;
            if (request.params("ids") == null) {
                getService().deleteAll(ids);
            } else {
                ids = request.params("ids").split(",");
                getService().deleteAll(ids);
            }
            return new JSONObject();
        });
    }

    public abstract String getResource();

    public abstract ActiveJdbcService<T> getService();

    public String getUrlPrefix() {
        return nameSpace + "/" + getResource();
    }

    private String generateQuery(Map<String, String> condition) {

        if (condition.size() == 1) {
            for (String key : condition.keySet()) {
                return String.format("%s like ? ", key);
            }
        }

        List<String> columns = new ArrayList<String>();

        for (String key : condition.keySet()) {
            columns.add(key);
        }

        return String.join(" like ? and ", columns);
    }

    private Object[] generateParameters(Map<String, String> condition) {
        Object[] parameters = new Object[condition.size()];
        int index = 0;
        for (String key : condition.keySet()) {
            parameters[index] = "%" + condition.get(key) + "%";
            index++;
        }
        return parameters;
    }

    private JSONObject generateResult(List<T> records) {
        JSONArray items = new JSONArray();
        for (T t : records) {
            items.put(t.toJson());
        }
        try {
            return new JSONObject().put(getResource(), items);
        } catch (JSONException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new JSONObject();
    }

    private JSONObject generateResult(T record) {
        try {
            return new JSONObject().put(getResource(), record.toJson());
        } catch (JSONException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new JSONObject();
    }

}
