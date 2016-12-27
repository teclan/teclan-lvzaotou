package teclan.lvzaotou.example.api;

import com.google.inject.ImplementedBy;

import teclan.lvzaotou.core.api.ServiceApis;
import teclan.lvzaotou.example.api.achieve.DefaultContentServerApis;

@ImplementedBy(DefaultContentServerApis.class)
public interface ContentServerApis extends ServiceApis {

}
