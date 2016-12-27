package teclan.lvzaotou.core.service.media;

import java.io.File;
import java.util.List;

import teclan.lvzaotou.core.api.achieve.AbstractMediaServiceApis;

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
