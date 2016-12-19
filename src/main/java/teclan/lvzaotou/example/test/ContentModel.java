package teclan.lvzaotou.example.test;

import java.util.Date;

public class ContentModel {
    public long   id;
    public String name;
    public String content;
    public String description;
    public String createdAt;
    public String updatedAt;

    public ContentModel() {
        name = "test-name";
        content = "测试 " + new Date();
        description = "无";
    }

    @Override
    public String toString() {
        return String.format(
                "id = %s,name = %s,content = %s,description = %s,createAt = %s,updatedAt = %s",
                id, name, content, description, createdAt, updatedAt);
    }
}
