package teclan.lvzaotou.core.service.achieve;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.google.gson.JsonObject;

import teclan.lvzaotou.core.service.UserService;
import teclan.lvzaotou.core.service.db.AbstracActiveJdbcService;
import teclan.lvzaotou.core.utils.Strings;
import teclan.lvzaotou.example.model.User;

public class DefaultUserService extends AbstracActiveJdbcService<User>
        implements UserService {
    public static SimpleDateFormat        TOKEN_DATE_FORMAT = new SimpleDateFormat(
            "yyyyMMddhhmmss");
    private final static SimpleDateFormat DATE_FOMAT        = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");

    @Override
    public String login(String username, String password, String ip) {

        User user = User.findFirst("username = ? ", username);

        if (user != null) {
            if (password.equals(user.getString("password"))) {

                String token = Strings.toHexDigest(String.format("%s-%s",
                        username, TOKEN_DATE_FORMAT.format(new Date())), "MD5");

                user.set("status", "在线", "last_action",
                        DATE_FOMAT.format(Calendar.getInstance().getTime()),
                        "token", token, "ip", ip).saveIt();

                LOGGER.debug("登录成功，用户名：{}，角色：{}，ip：{}", username,
                        user.getString("role").toString(), ip);

                // 登录成功返回 用户id,用户名，角色
                JsonObject object = new JsonObject();

                object.addProperty("id", user.getString("id"));
                object.addProperty("username", user.getString("username"));
                object.addProperty("role", user.getString("role"));
                object.addProperty("token", token);
                JsonObject result = new JsonObject();
                result.addProperty("user", object.toString());

                return result.toString();
            }
            LOGGER.debug("登录失败，密码错误，用户名：{},角色：{}", username,
                    user.getString("role").toString());
            return null;
        }

        LOGGER.debug("登录失败，用户{} 不存在", username);

        return null;
    }

    @Override
    public String logout(long id, String ip) {

        User user = User.findById(id);
        if (user == null) {
            LOGGER.debug("注销异常，id 为 {} 的用户不存在，ip：{}", id, ip);
            return null;
        }

        if (ip.equals(user.getString("ip"))) {
            user.set("status", "不在线", "last_action", null, "token", null)
                    .saveIt();

        }
        LOGGER.debug("用户 {} 注销成功，ip：{}", user.getString("username"), ip);

        return null;
    }

    public String password(String username, String oldPwd, String newPwd,
            String ip) {

        User user = User.findFirst("username = ? ", username);

        if (user != null) {

            if (user.getString("password").equals(oldPwd)) {
                user.set("status", "不在线", "password", newPwd, "token", "", "ip",
                        ip, "last_action",
                        DATE_FOMAT.format(Calendar.getInstance().getTime()))
                        .saveIt();

                LOGGER.debug("密码修改成功，用户名：{}，角色：{}，ip：{}", username,
                        user.getString("role").toString(), ip);

                return new JsonObject().toString();
            } else {
                LOGGER.debug("密码修改失败，原密码错误，用户名：{},角色：{}", username,
                        user.getString("role").toString());
                return null;
            }
        }

        LOGGER.debug("密码修改失败，用户{} 不存在", username);
        return null;
    }
}
