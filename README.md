
## 创建项目

1、在项目目录下创建目录 conf/,并且创建配置文件，例如 [config.conf](https://github.com/teclan/teclan-lvzaotou/blob/master/conf/config.conf)

2、创建应用程序

```
@Singleton
public class Application extends RestapiApplication {

}
```

3、创建启动入口
```
    public class Main {
       public static void main(String[] args) {
        Injector injector = Guice.createInjector(
                new ConfigModule("config.conf", "config"), new SeverModule());
        Application application = injector.getInstance(Application.class);
        application.init();
       }
    }
```
其中 new ConfigModule("config.conf", "config") 文加载配置文件，文件加载定义如下：
```
    /**
     * 加载项目目录下conf/的配置文件，如果找不到，则尝试 classPath 下的配置文件
     * 
     * @param configFile
     *            配置文件名称
     * @param root
     *            配置文件的跟对象
     */
    public ConfigModule(String configFile, String root) {
        this.configFile = configFile;
        this.root = root;
    }
```


## 如何定义对一个数据资源的增删改查？

假设有数据表如下：
```
	create table content_records
	(
	id 						INT(11) NOT NULL AUTO_INCREMENT,
	name					varchar(50),
	content					varchar(2000),
	description				varchar(100),
	created_at        		TIMESTAMP,
	updated_at        		TIMESTAMP
	);
```
1、定义模型（遵循ActiveJDBC标准）

示例如下：
```
public class ContentRecord extends ActiveRecord {
    static {
        validatePresenceOf("content"); // 字段 content 必填
    }

}

```
2、绑定针对 content_records 表的资源操作

创建针对 content_records 表的默认操作类 DefaultContentRecordService 

```
public class DefaultContentRecordService
        extends AbstracActiveJdbcService<ContentRecord>
        implements ContentRecordService {

}
```
绑定默认操作类以便使用 guice 创建注入

```
@ImplementedBy(DefaultContentRecordService.class)
public interface ContentRecordService extends ActiveJdbcService<ContentRecord> {

}
```
3、定义针对针对 content_records 表资源的路由

创建针对 content_records 表默认路由
```
public class DefaultContentServerApis extends AbstractServiceApis<ContentRecord>
        implements ContentServerApis {
    @Inject
    private ContentRecordService service;
    @Override
    public ActiveJdbcService<ContentRecord> getService() {
        return service;
    }
    @Override
    public String getResource() {
        return "contents";
    }
}
```
绑定默认路由以便使用 guice 创建注入
```
@ImplementedBy(DefaultContentServerApis.class)
public interface ContentServerApis extends ServiceApis {

}
```


3、添加路由
```
@Singleton
public class Application extends RestapiApplication {
	// 使用 guice 注入针对 content_records 表的默认路由并且调用 initApis()初始化路由
    @Inject
    private ContentServerApis contentApis;
    @Override
    public void creatApis() {
        contentApis.initApis();
    }
}

```
至此，针对 content_records 表的资源操作操作已经创建完成，创建的路由包括：

根路由参考配置文件 [config.conf](https://github.com/teclan/teclan-lvzaotou/blob/master/conf/config.conf)

其中 contents 是 DefaultContentServerApis 类中 getResource() 方法指定的，多个资源之间，此资源不允许重复，否则

路由会被覆盖
```
GET contents/all 				查询 content_records 表所有记录

GET contents/fetch/:id  		指定 id 查询 content_records 表记录

POST contents/fetch  			分页查询 content_records 表记录，可选参数 page，limit，以及其他 content_records 包含的字段和以及对应的查询参数

POST contents/new  				添加记录，指定的 id 字段会被忽略

PUT contents/new  				添加记录，指定的 id 字段会被忽略

PUT contents/sys/:id 			指定 id 更新记录

PUT contents/sys  				批量更新记录

DELTE contents/delete/:id  		删除指定 id 对应的记录

DELETE contents/deletes/:ids 	删除多条记录，ids：id列表，逗号隔开

```
附加说明：

所有更新资源，添加资源的请求参数是一个 json 字符串，并且均是 contents （DefaultContentServerApis 类中 getResource() 方法指定） 字段指定的 json 对象，

即对于 content_records 表的更新或添加资源请求的参数应该形如：{"contents":"{"id":1,...,"description":"无"}"}

对于批量更新，请求参数是 contents （DefaultContentServerApis 类中 getResource() 方法指定） 字段指定的列表，形如:
 {"contents":[{"content":"测试 Mon Dec 19 17:21:38 CST 2016","description":"无","id":1,"name":"na mei"},
 {"content":"测试 Mon Dec 19 17:21:38 CST 2016","description":"无","id":2,"name":"shan zhi"}]}


## 文件上传和下载

参考配置文件 [config.conf](https://github.com/teclan/teclan-lvzaotou/blob/master/conf/config.conf)里面的说明，默认的文件访问路由如下：
```
GET media/downloads 		单文件下载
GET media/downloads/batch 	文件夹下载（批量下载）
POST media/upload 			单文件上传
POST media/upload/batch 	多文件上传
```
其中 media 在 [DefaultMediaServiceApis 类](https://github.com/teclan/teclan-lvzaotou/blob/master/src/main/java/teclan/lvzaotou/core/service/media/DefaultMediaServiceApis.java) 中指定
```
public class DefaultMediaServiceApis extends AbstractMediaServiceApis
        implements MediaServiceApis {
    @Override
    public String getResource() {
        return "media";
    }
    @Override
    public void handle(File file) {
        // TO DO
        // 添加对刚刚上传的文件一些处理逻辑，默认不进行任何操作
    }
    @Override
    public void handle(List<File> files) {
        // TO DO
        // 添加对刚刚批量上传的文件一些处理逻辑，默认不进行任何操作
    }

}
```	

自定义文件访问资源:
1、
创建文件访问资源操作类
```
public class YouMediaServiceClassName extends AbstractMediaServiceApis
        implements MediaServiceApis {
    @Override
    public String getResource() {
        return "mymedia"; // 此处为资源前缀，跟上面的 contents 一样，不能重复，不能再写 media，默认已经存在
    }
    @Override
    public void handle(File file) {
         // 自定义
    }
    @Override
    public void handle(List<File> files) {
        // 自定义
    }
}
```
绑定你的文件访问资源操作，以便使用 guice 注入
```
@ImplementedBy(YouMediaServiceClassName.class)
public interface YouMediaServiceApis extends ServiceApis {

}
```
2、添加路由
```
@Singleton
public class Application extends RestapiApplication {
	// 使用 guice 注入针对 content_records 表的默认路由并且调用 initApis()初始化路由
    @Inject
    private ContentServerApis contentApis;
    // 注入你的文件访问资源操作
    @Inject
    private YouMediaServiceApis youMediaServiceApis;
    @Override
    public void creatApis() {
        contentApis.initApis();
        // 初始化你的文件访问资源APIS
        youMediaServiceApis.initApis();
    }
}
至此，你的路由已经被创建，你刚才创建的路由如下:
```
GET mymedia/downloads 			单文件下载
GET mymedia/downloads/batch 	文件夹下载（批量下载）
POST mymedia/upload 			单文件上传
POST mymedia/upload/batch 		多文件上传
```
```

