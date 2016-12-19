package teclan.lvzaotou.core.service.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javalite.activejdbc.ModelDelegate;
import org.javalite.activejdbc.Paginator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.reflect.TypeToken;

import teclan.lvzaotou.core.db.Page;

public abstract class AbstracActiveJdbcService<T extends ActiveRecord>
        implements ActiveJdbcService<T> {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @SuppressWarnings("serial")
    private TypeToken<T>   type   = new TypeToken<T>(getClass()) {
                                  };

    @Override
    public T findById(long id) {
        return ModelDelegate.findById(getModelClass(), id);
    }

    @Override
    public List<T> all() {
        return ModelDelegate.findAll(getModelClass());

    }

    @Override
    public Page<T> fetch(int page, int limit, String query, Object... params) {

        @SuppressWarnings("unchecked")
        Paginator<T> paginator = new Paginator<>(getModelClass(), limit, query,
                params).orderBy("created_at DESC");
        return new Page<>(paginator.getCount(), page, limit,
                paginator.getPage(page));
    }

    @Override
    public T create(Map<String, Object> attributes) {
        try {
            attributes.remove("id");
            T created = getModelClass().newInstance();
            created.fromMap(attributes);
            created.save();
            return created;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public T update(long id, Object... namesAndValues) {
        Map<String, Object> attributes = new HashMap<>();

        for (int i = 0; i < namesAndValues.length; i = i + 2) {
            attributes.put(namesAndValues[i].toString(), namesAndValues[i + 1]);
        }

        return update(id, attributes);
    }

    @Override
    public T update(long id, Map<String, Object> attributes) {
        T origin = findById(id);

        if (origin == null) {
            return null;
        }

        origin.fromMap(attributes);
        origin.save();

        return origin;
    }

    @Override
    public void delete(long id) {
        findById(id).delete();
    }

    private void deleteAll(List<Long> toBeRemoved) {
        if (toBeRemoved.isEmpty()) {
            ModelDelegate.findAll(getModelClass())
                    .forEach(r -> toBeRemoved.add(r.getLongId()));
        }

        List<String> errors = new ArrayList<String>();
        for (Long id : toBeRemoved) {
            try {
                delete(id);
            } catch (Exception e) {
                errors.add(e.getMessage());
            }
        }

        if (!errors.isEmpty()) {
            throw new RuntimeException(String.join(", ", errors));
        }
    }

    @Override
    public void deleteAll(Long... ids) {
        List<Long> toBeRemoved = Arrays.asList(ids);
        deleteAll(toBeRemoved);
    }

    @Override
    public void deleteAll(String... ids) {
        List<Long> toBeRemoved = new ArrayList<Long>();
        for (String id : ids) {
            toBeRemoved.add(Long.valueOf(id));
        }
        deleteAll(toBeRemoved);
    }

    @Override
    public List<T> sync(List<Map<String, Object>> maps) {
        List<T> exists = all();
        for (T record : exists) {
            sync(record, maps);
        }

        for (Map<String, Object> map : maps) {
            create(map);
        }

        return all();
    }

    @Override
    public List<T> sync(long id, Map<String, Object> attributes) {
        update(id, attributes);
        return all();
    }

    protected boolean isMatch(T record, Map<String, Object> attributes) {
        // NOTE
        // By Teclan
        // id 字段是每张数据表的必须字段，如果同步请求的参数中指定 id 对应的记录存在，那么对该记录进行更新
        return record.getString("id").equals(attributes.get("id"));
    }

    private void sync(T record, List<Map<String, Object>> maps) {
        for (Map<String, Object> map : maps) {
            if (isMatch(record, map)) {
                update(record.getLongId(), map);
                // NOTE
                // By Teclan
                // 此处直接移除是否生效?
                maps.remove(map);
                return;
            }
        }

        // TO DO
        // By Teclan
        // 如果没有执行更新操作，需要处理的一些逻辑 ...
    }

    @SuppressWarnings("unchecked")
    protected Class<T> getModelClass() {
        return (Class<T>) type.getRawType();
    }

}
