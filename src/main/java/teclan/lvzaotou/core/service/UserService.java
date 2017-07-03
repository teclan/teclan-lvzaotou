package teclan.lvzaotou.core.service;

import com.google.inject.ImplementedBy;

import teclan.lvzaotou.core.service.achieve.DefaultUserService;
import teclan.lvzaotou.core.service.db.ActiveJdbcService;
import teclan.lvzaotou.example.model.User;

@ImplementedBy(DefaultUserService.class)
public interface UserService extends ActiveJdbcService<User> {

    public String login(String username, String password, String ip);

    public String logout(long id, String ip);
    
    public String password(String username,String oldPwd,String newPwd,String ip);
}
