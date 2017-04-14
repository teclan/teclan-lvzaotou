package teclan.lvzaotou.example.model;

import teclan.lvzaotou.core.service.db.ActiveRecord;

public class User extends ActiveRecord {

    @Override
    public String toJson() {
        return toJson(new String[] { "password" });
    }

}
