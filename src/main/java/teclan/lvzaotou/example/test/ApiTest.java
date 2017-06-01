package teclan.lvzaotou.example.test;

import static us.monoid.web.Resty.delete;
import static us.monoid.web.Resty.form;
import static us.monoid.web.Resty.put;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import teclan.lvzaotou.core.utils.GsonUtils;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONObject;
import us.monoid.web.Content;
import us.monoid.web.Resty;

// 测试之前先执行 teclan.web.example 的 Main.class
public class ApiTest {
    private final Logger LOGGER                         = LoggerFactory
            .getLogger(ApiTest.class);
    private final String GET_ALL_URL                    = "http://localhost:%d/%s/all";
    private final String FETCH_BY_ID_URL                = "http://localhost:%d/%s/fetch/%d";
    private final String FETCH_WITH_QUERY_URL           = "http://localhost:%d/%s/fetch";
    private final String ADD_RECORD_WITH_POST_URL       = "http://localhost:%d/%s/new";
    private final String ADD_RECORD_WITH_PUT_URL        = "http://localhost:%d/%s/new";
    private final String SYS_RECORD_BY_ID_URL           = "http://localhost:%d/%s/sys/%d";
    private final String SYS_MUTIL_RECORD_URL           = "http://localhost:%d/%s/sys";
    private final String DELETE_RECORD_BY_ID_URL        = "http://localhost:%d/%s/delete/%d";
    private final String DELETE_MUTIL_RECORD_BY_IDS_URL = "http://localhost:%d/%s/deletes/%s";

    // @Test
    public void deleteMutilById() {
        Resty resty = new Resty();
        try {
            LOGGER.info("{}",
                    resty.text(
                            String.format(DELETE_MUTIL_RECORD_BY_IDS_URL,
                                    getPort(), getUrlResource(), "1,2,3,35"),
                            delete()));
        } catch (Exception e) {
        }

    }

    // @Test
    public void deleteById() {
        Resty resty = new Resty();
        try {
            LOGGER.info("{}", resty.text(String.format(DELETE_RECORD_BY_ID_URL,
                    getPort(), getUrlResource(), 1), delete()));
        } catch (Exception e) {
        }

    }

    // @Test
    public void sysMutilRecord() {

        ContentModel model1 = new ContentModel();
        model1.id = 1;
        model1.name = "na mei";

        ContentModel model2 = new ContentModel();
        model2.id = 2;
        model2.name = "shan zhi";

        List<ContentModel> models = new ArrayList<ContentModel>();

        models.add(model1);
        models.add(model2);

        try {

            List<Map<String, Object>> maps = new ArrayList<Map<String, Object>>();

            maps.add(GsonUtils.toMap(GsonUtils.toJson(model1)));
            maps.add(GsonUtils.toMap(GsonUtils.toJson(model2)));

            JSONObject object = new JSONObject();

            object.put(getResource(), maps);

            new Resty().text(
                    String.format(SYS_MUTIL_RECORD_URL, getPort(),
                            getUrlResource()),
                    put(new Content("application/json; charset=utf-8",
                            object.toString().getBytes("UTF-8"))));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // @Test
    public void sysById() {

        ContentModel model = new ContentModel();
        model.id = 1;
        model.name = "lu fei";
        Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss")
                .setFieldNamingPolicy(
                        FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        try {
            JSONObject object = new JSONObject();
            object.put(getResource(), GSON.toJson(model));

            new Resty().text(
                    String.format(SYS_RECORD_BY_ID_URL, getPort(),
                            getResource(), model.id),
                    put(new Content("application/json; charset=utf-8",
                            object.toString().getBytes("UTF-8"))));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // @Test
    public void newWithPut() {
        ContentModel model = new ContentModel();

        Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss")
                .setFieldNamingPolicy(
                        FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        try {
            JSONObject object = new JSONObject();
            object.put(getUrlResource(), GSON.toJson(model));

            new Resty().text(
                    String.format(ADD_RECORD_WITH_PUT_URL, getPort(),
                            getUrlResource()),
                    put(new Content("application/json; charset=utf-8",
                            object.toString().getBytes("UTF-8"))));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // @Test
    public void newWithPost() {
        try {
            new Resty().text(
                    String.format(ADD_RECORD_WITH_POST_URL, getPort(),
                            getUrlResource()),
                    form("name=Teclan&content=lvzaotou"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // @Test
    public void fetchWithQuery() {
        try {
            LOGGER.info("{}",
                    new Resty()
                            .json(String.format(FETCH_WITH_QUERY_URL, getPort(),
                                    getUrlResource()), form("page=all"))
                    .object().toString());

            LOGGER.info("\n=================");

            LOGGER.info("{}",
                    new Resty()
                            .json(String.format(FETCH_WITH_QUERY_URL, getPort(),
                                    getUrlResource()),
                            form("page=1&limit=2&content=测试")).object()
                    .toString());

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void getAll() {
        try {
            JSONObject json = new Resty().json(
                    String.format(GET_ALL_URL, getPort(), getUrlResource()))
                    .object();
            JSONArray array = ((JSONArray) json.get(getResource()));
            for (int i = 0; i < array.length(); i++) {
                LOGGER.info("{}", array.get(i));

                ContentModel model = GsonUtils.fromJson(array.get(i).toString(),
                        ContentModel.class);
                LOGGER.info("model : {}", model.toString());
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    // @Test
    public void fectById() {
        try {
            String json = new Resty().json(String.format(FETCH_BY_ID_URL,
                    getPort(), getUrlResource(), 2)).object().toString();
            LOGGER.info("\n{}", json);
            ContentModel model = GsonUtils.fromJson(json, ContentModel.class);
            LOGGER.info("model : {}", model.toString());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private String getUrlResource() {
        return "api/v1/contents";
    }

    private String getResource() {
        return "contents";
    }

    private int getPort() {
        return 3770;
    }
}
