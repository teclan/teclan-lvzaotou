package teclan.lvzaotou.core.service.media;

import com.google.inject.ImplementedBy;

import teclan.lvzaotou.core.service.ServiceApis;

@ImplementedBy(DefaultMediaServiceApis.class)
public interface MediaServiceApis extends ServiceApis {

}
