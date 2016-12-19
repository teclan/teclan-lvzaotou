package teclan.lvzaotou.example.model;

import teclan.lvzaotou.core.service.db.ActiveRecord;

public class ContentRecord extends ActiveRecord {
    static {
        validatePresenceOf("content");
    }

}
