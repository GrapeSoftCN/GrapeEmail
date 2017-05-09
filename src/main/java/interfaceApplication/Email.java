package interfaceApplication;

import org.json.simple.JSONObject;

import cache.redis;
import esayhelper.JSONHelper;
import model.EmailModel;
import session.session;

public class Email {
	private EmailModel model = new EmailModel();

	// 新增emailhost
	public String AddEmail(String emailInfo) {
		return model.AddHost(JSONHelper.string2json(emailInfo));
	}

	// 删除emailhost
	public String DeleteEmail(String id) {
		return model.resultMessage(model.delete(id), "删除成功");
	}

	// 批量删除emailhost
	public String DeleteBatchEmail(String ids) {
		return model.resultMessage(model.delete(ids.split(",")), "删除成功");
	}

	// 修改emailhost
	public String UpdateEmail(String id, String emailInfo) {
		return model.resultMessage(
				model.update(id, JSONHelper.string2json(emailInfo)), "修改成功");
	}

	// 分页
	public String PageEmail(int ids, int pageSize) {
		return model.page(ids, pageSize);
	}

	// 条件分页
	public String PageByEmail(int ids, int pageSize, String info) {
		return model.page(ids, pageSize, JSONHelper.string2json(info));
	}

	// 用户注册网站，激活邮箱
	@SuppressWarnings("unchecked")
	public String ActiveEmail(String id, String email) {
		redis sRedis = new redis("redis");
		String num = model.getValiCode(); // 获取6位随机数
		JSONObject object = new JSONObject();
		object.put("subject", "注册网站的验证邮件");
		object.put("email", email);
		object.put("code", num);
		session session = new session();
		session.setget("emailCode", object.toString());
		sRedis.setExpire("emailCode", 5 * 60);
		return model.resultMessage(model.send(id, object), "发送成功");
	}

	// 验证用户输入的邮箱验证码
	public String VerifyEmail(String email, String ckcode) {
		session session = new session();
		if (session.get("emailCode") == null) {
			return model.resultMessage(5, "该验证码已过期，请重新验证");
		}
		JSONObject object = JSONHelper
				.string2json(session.get("emailCode").toString());
		if (object.get("email").equals(email)
				&& object.get("code").equals(ckcode)) {
			return model.resultMessage(0, "邮箱验证成功");
		}
		return model.resultMessage(6, "验证码输入错误");
	}

	// 发送邮件消息
	public String sendEmail(String id, String content) {
		return model.resultMessage(
				model.send(id, JSONHelper.string2json(content)), "发送成功");
	}
}
