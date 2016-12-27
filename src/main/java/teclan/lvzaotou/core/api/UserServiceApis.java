package teclan.lvzaotou.core.api;

import com.google.inject.ImplementedBy;

import teclan.lvzaotou.core.api.achieve.DefaultUserServiceApis;

@ImplementedBy(DefaultUserServiceApis.class)
public interface UserServiceApis extends ServiceApis {

}
