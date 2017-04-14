package teclan.lvzaotou.core.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import teclan.lvzaotou.core.service.db.ActiveRecord;
import teclan.lvzaotou.core.utils.GsonUtils;

public class Page<T extends ActiveRecord> {
    private long    total;
    private long    offset;
    private long    limit;
    private List<T> records;

    public Page(long total, long offset, long limit, List<T> records) {
        this.total = total;
        this.offset = offset;
        this.limit = limit;
        this.records = records;
    }

    public long getTotal() {
        return total;
    }

    public List<T> getRecords() {
        return records;
    }

    @SuppressWarnings("rawtypes")
    public String toJson(String... excludedAttributes) {
        List<Map> maps = new ArrayList<Map>();

        for (T r : records) {
            maps.add(r.toMap(excludedAttributes));
        }

        Map<String, Object> map = new HashMap<String, Object>();

        Map<String, Object> meta = new HashMap<String, Object>();
        meta.put("total", total);
        meta.put("offset", offset);
        meta.put("limit", limit);
        meta.put("pages", Math.ceil(total * 1.0 / limit));

        map.put("meta", meta);

        map.put("data", maps);

        return GsonUtils.toJson(map);
    }
}