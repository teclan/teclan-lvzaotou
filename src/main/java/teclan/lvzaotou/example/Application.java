package teclan.lvzaotou.example;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import teclan.lvzaotou.core.RestapiApplication;
import teclan.lvzaotou.example.api.ContentServerApis;

@Singleton
public class Application extends RestapiApplication {

    @Inject
    private ContentServerApis contentApis;

    @Override
    public void creatApis() {
        contentApis.initApis();
    }

}
