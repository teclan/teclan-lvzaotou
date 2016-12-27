package teclan.lvzaotou.core.service.media;

import com.google.inject.ImplementedBy;

import teclan.lvzaotou.core.api.ServiceApis;

@ImplementedBy(DefaultMediaServiceApis.class)
public interface MediaServiceApis extends ServiceApis {

}
