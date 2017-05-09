package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import email.emailhost;
import email.mail;
import esayhelper.DBHelper;
import esayhelper.formHelper;
import esayhelper.formHelper.formdef;
import esayhelper.jGrapeFW_Message;

public class EmailModel {
	private static DBHelper emails;
	private static formHelper form;
	private JSONObject _obj = new JSONObject();

	static {
		emails = new DBHelper("mysql", "emailhost");
		form = emails.getChecker();
	}

	public EmailModel() {
		form.putRule("smtp", formdef.notNull);
		form.putRule("pop3", formdef.notNull);
		form.putRule("userid", formdef.notNull);
		form.putRule("password", formdef.notNull);
	}

	// 新增emailhost
	public String AddHost(JSONObject info) {
		String ownid = "";
		if (!form.checkRuleEx(info)) {
			return resultMessage(1, "");
		}
		String email = info.get("userid").toString();
		if (checkEmail(email)) {
			return resultMessage(2, "");
		}
		if (FindEmail(email) != null) {
			return resultMessage(3, "");
		}
		if (info.containsKey("ownid")) {
			ownid = info.get("ownid").toString();
		}
		String tips = emailhost.addHost(ownid, info.get("userid").toString(),
				info.get("password").toString(), info.get("smtp").toString(),
				info.get("pop3").toString());
		return resultMessage(find(tips));
	}

	public int delete(String id) {
		return emailhost.removeHost(id) == true ? 0 : 99;
	}

	public int delete(String[] id) {
		emails.or();
		int len = id.length;
		for (int i = 0; i < len; i++) {
			emails.eq("id", id[i]);
		}
		return emails.deleteAll() == id.length ? 0 : 99;
	}

	public int update(String id, JSONObject info) {
		if (info.containsKey("userid")) {
			String email = info.get("userid").toString();
			if (!checkEmail(email)) {
				return 2;
			}
		}
		return emailhost.editHost(Integer.parseInt(id), info) == true ? 0 : 99;
	}

	@SuppressWarnings("unchecked")
	public String page(int idx, int pageSize) {
		JSONArray array = emails.page(idx, pageSize);
		JSONObject object = new JSONObject();
		object.put("totalSize",
				(int) Math.ceil((double) emails.count() / pageSize));
		object.put("currentPage", idx);
		object.put("pageSize", pageSize);
		object.put("data", array);
		return resultMessage(object);
	}

	@SuppressWarnings("unchecked")
	public String page(int idx, int pageSize, JSONObject info) {
		JSONArray array = emails.page(idx, pageSize);
		JSONObject object = new JSONObject();
		object.put("totalSize",
				(int) Math.ceil((double) emails.count() / pageSize));
		object.put("currentPage", idx);
		object.put("pageSize", pageSize);
		object.put("data", array);
		return resultMessage(object);
	}

	// 发送消息，包含字段：发件人信息，收件人邮箱，抄送人，邮件主题，邮件正文，附件内容
	@SuppressWarnings("unchecked")
	public int send(String id, JSONObject object) {
		boolean flag = false;
		String CC = "";
		List<String> list = new ArrayList<String>();
		if (!object.containsKey("to")) {
			return 4;
		}
		if (object.containsKey("cc")) {
			CC = object.get("cc").toString();
		}
		if (object.containsKey("attachments")) {
			list = (List<String>) object.get("attachments");
		}
		mail mails = mail.defaultEntity(Integer.parseInt(id),
				object.get("to").toString(), CC,
				object.get("subject").toString(),
				object.get("content").toString(), list);
		try {
			flag = mails.send();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return flag ? 0 : 99;
	}

	// 根据id查找email
	public JSONObject find(String id) {
		return emails.eq("id", id).find();
	}

	// 邮箱格式是否存在
	public JSONObject FindEmail(String mail) {
		return emails.eq("userid", "mail").find();
	}

	// 邮箱格式验证
	@SuppressWarnings("unchecked")
	public boolean checkEmail(String mail) {
		form.putRule("userid", formdef.email);
		JSONObject obj = new JSONObject();
		obj.put("email", mail);
		return form.checkRule(obj);
	}

	// 获取6位随机验证码
	public String getValiCode() {
		String num = "";
		for (int i = 0; i < 6; i++) {
			num = num + String.valueOf((int) Math.floor(Math.random() * 9 + 1));
		}
		return num;
	}

	// 将map添加至JSONObject中
	@SuppressWarnings("unchecked")
	public JSONObject AddMap(HashMap<String, Object> map, JSONObject object) {
		if (map.entrySet() != null) {
			Iterator<Entry<String, Object>> iterator = map.entrySet()
					.iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, Object> entry = (Map.Entry<String, Object>) iterator
						.next();
				if (!object.containsKey(entry.getKey())) {
					object.put(entry.getKey(), entry.getValue());
				}
			}
		}
		return object;
	}

	@SuppressWarnings("unchecked")
	private String resultMessage(JSONObject object) {
		_obj.put("records", object);
		return resultMessage(0, _obj.toString());
	}

	// @SuppressWarnings("unchecked")
	// private String resultMessage(JSONArray array) {
	// _obj.put("records", array);
	// return resultMessage(0, _obj.toString());
	// }

	public String resultMessage(int num, String message) {
		String msg = "";
		switch (num) {
		case 0:
			msg = message;
			break;
		case 1:
			msg = "必填字段没有填";
			break;
		case 2:
			msg = "邮箱格式错误";
			break;
		case 3:
			msg = "该邮箱已存在";
			break;
		case 4:
			msg = "收件人邮箱为空";
			break;
		case 5:
			msg = "该验证码已过期，请重新验证";
			break;
		case 6:
			msg = "验证码输入错误";
			break;
		default:
			msg = "其他操作错误";
			break;
		}
		return jGrapeFW_Message.netMSG(num, msg);
	}
}
