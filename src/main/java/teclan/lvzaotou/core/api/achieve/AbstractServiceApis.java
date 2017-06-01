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

public abstract class AbstractServiceApis<T extends ActiveRecord> implements ServiceApis {
	private final Logger LOGGER = LoggerFactory.getLogger(AbstractServiceApis.class);

	@Inject
	@Named("config.server.name-space")
	private String nameSpace;
	@Inject
	@Named("config.model.start-with")
	private String startWith;
	@Inject
	@Named("config.model.end-with")
	private String endWith;

	@Override
	public void initApis() {

		all();

		findById();

		fetch();

		createWithPost();

		ceateWitPut();

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
		get(getUrlPrefix(), (request, response) -> {
			List<T> results = getService().all();
			
			if (results != null && !results.isEmpty()) {
				return generateResult(results).toString();
			} else {
				LOGGER.warn("\n无查询结果 {}", getResource() + "/all");
				return new JSONObject();
			}
		});
	}

	public void findById() {
		get(getUrlPrefix() + "/:id", (request, response) -> {

			String rsp = getService().findById(Long.valueOf(request.params(":id"))).toJson(getResource());
			return rsp;
		});
	}

	protected void fetch() {
		get(getUrlPrefix(), (request, response) -> {
			int page = 1;
			int limit = 20;
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
				paramsMap.put((String) param, request.queryParams((String) param));

			}

			if (paramsMap != null && !paramsMap.isEmpty()) {

				if (hasKey(startWith(),paramsMap) 
						&& hasKey(endWith(),paramsMap)) {
					query = generateQuery(paramsMap, true);
					parameters = generateParameters(paramsMap, true);
				} else {
					paramsMap.remove(startWith());
					paramsMap.remove(endWith());

					query = generateQuery(paramsMap, false);
					parameters = generateParameters(paramsMap, false);
				}

			}

			String rsp = getService().fetch(page, limit, query, parameters).toJson(getResource());

			return rsp;
		});
	}
	
	private boolean hasKey(String key,Map<String,String> params){
		return params.containsKey(key) && isNotNull(params.get(key));
		
	}
	private boolean isNotNull(String value){
		return !"".equals(value.trim()) || value==null;
	}

	protected void createWithPost() {
		post(getUrlPrefix(), (request, response) -> {
			Map<String, Object> attributes = GsonUtils
					.toMap(new JSONObject(request.body()).get(getSingleResource()).toString());

			try {
				T created = getService().create(attributes);
				return created.toJson(getResource());
			} catch (Exception e) {
				response.status(500);
				return new JSONObject();
			}

		});
	}

	protected void ceateWitPut() {
		put(getUrlPrefix(), (request, response) -> {
			Map<String, Object> attributes = GsonUtils
					.toMap(new JSONObject(request.body()).get(getSingleResource()).toString());

			try {
				T created = getService().create(attributes);
				return created.toJson(getResource());
			} catch (Exception e) {
				response.status(500);
				return new JSONObject();
			}

		});
	}

	protected void sysById() {
		put(getUrlPrefix() + "/:id", (request, response) -> {
			long id = Long.valueOf(request.params(":id"));
			Map<String, Object> attributes = GsonUtils
					.toMap(new JSONObject(request.body()).get(getSingleResource()).toString());
			T updated = getService().sync(id, attributes);
			if (updated != null) {
				return updated.toJson(getResource());
			} else {
				response.status(500);
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
			List<Map<String, Object>> maps = GsonUtils.fromJson(request.body(), getResource(),
					new TypeToken<List<Map<String, Object>>>() {
				private static final long serialVersionUID = 3731405824720413383L;
			}.getType());

			List<T> results = getService().sync(maps);
			if (results != null && !results.isEmpty()) {
				return generateResult(results);
			} else {
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
		delete(getUrlPrefix() + "/:id", (request, response) -> {
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

	protected String timeColumn() {
		return "created_at";
	}

	public String getUrlPrefix() {
		return nameSpace + "/" + getResource();
	}

	  private String generateQuery(Map<String, String> condition,
	            boolean queryTime) {

	        List<String> columns = new ArrayList<String>();

	        if (queryTime) {

	            for (String key : condition.keySet()) {
	                if (!"".equals(condition.get(key).trim())
	                        && !startWith().equals(key) && !endWith().equals(key)) {
	                    columns.add(key);
	                }
	            }

	            if (!columns.isEmpty() && columns.size() > 1) {

	                return String.join(" like ? and ", columns) + String
	                        .format(" like ? and %s between ? and ?", timeColumn());
	            } else {
	                return columns.isEmpty()
	                        ? String.format(" %s between ? and ?", timeColumn())
	                        : String.format("%s like ? and  %s between ? and ?",
	                                columns.get(0), timeColumn());
	            }
	        } else {
	            for (String key : condition.keySet()) {

	                if (!"".equals(condition.get(key).trim())) {
	                    columns.add(key);
	                }
	            }

	            if (columns.size() == 0) {
	                return " 1 = ? ";
	            }
	            return columns.size() > 1
	                    ? String.join(" like ? and ", columns) + " like ?"
	                    : String.format(" %s like ?", columns.get(0));
	        }
	    }


	  private Object[] generateParameters(Map<String, String> condition,
	            boolean queryTime) {

	        List<String> params = new ArrayList<String>();
	        Object[] parameters = null;
	        if (queryTime) {
	            String startWith = condition.remove(startWith());
	            String endWith = condition.remove(endWith());
	            for (String key : condition.keySet()) {
	                if (!"".equals(condition.get(key).trim())) {
	                    params.add(key);

	                }
	            }
	            parameters = new Object[params.size() + 2];
	            int index = 0;
	            for (String key : params) {
	                parameters[index++] = "%" + condition.get(key) + "%";
	            }
	            parameters[index++] = startWith;
	            parameters[index] = endWith;

	        } else {

	            for (String key : condition.keySet()) {
	                if (!"".equals(condition.get(key).trim())) {
	                    params.add(key);

	                }
	            }
	            if (params.size() == 0) {
	                return new Object[] { 1 };
	            }
	            parameters = new Object[params.size()];
	            int index = 0;

	            for (String key : params) {
	                parameters[index++] = "%" + condition.get(key) + "%";
	            }
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

	private String endWith() {
		return endWith;
	}

	private String startWith() {
		return startWith;
	}

	public abstract String getResource();

	public abstract String getSingleResource();

	public abstract ActiveJdbcService<T> getService();

}
