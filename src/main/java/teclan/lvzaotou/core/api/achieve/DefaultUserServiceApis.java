package teclan.lvzaotou.core.api.achieve;

import static spark.Spark.delete;
import static spark.Spark.post;

import com.google.inject.Inject;

import teclan.lvzaotou.core.api.UserServiceApis;
import teclan.lvzaotou.core.service.UserService;
import teclan.lvzaotou.core.service.db.ActiveJdbcService;
import teclan.lvzaotou.example.model.User;
import us.monoid.json.JSONObject;

public class DefaultUserServiceApis extends AbstractServiceApis<User>implements UserServiceApis {
	@Inject
	private UserService userService;

	@Override
	public String getResource() {
		return "users";
	}

	@Override
	public String getSingleResource() {
		return "user";
	}

	@Override
	public ActiveJdbcService<User> getService() {
		return userService;
	}

	@Override
	protected void customizeApis() {
		post(getUrlPrefix() + "/login", (request, response) -> {

			String username = request.queryParams("username").toString();
			String password = request.queryParams("password").toString();

			response.body(userService.login(username, password, request.ip()));

			if (response.body().contains("失败")) {
				response.status(401);
			}
			return response.body();
		});

		// 退出登录
		delete(getUrlPrefix() + "/logout/:id", (request, response) -> {
			long id = Long.valueOf(request.params(":id"));

			userService.logout(id, request.ip());

			return new JSONObject().toString();
		});
		
		// 修改密码
        post(getUrlPrefix() + "/password", (request, response) -> {

            String username = request.queryParams("user").toString();
            String oldPwd = request.queryParams("old-password").toString();
            String newPwd = request.queryParams("new-password").toString();
            
           if( userService.password(username, oldPwd, newPwd, request.ip())==null){
               response.status(500);
           }else{
               response.status(200);
           }

            return new JSONObject().toString();
        });

	}

}
