package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import apps.appsProxy;
import check.formHelper;
import check.formHelper.formdef;
import database.DBHelper;
import email.emailhost;
import email.mail;
import nlogger.nlogger;
import esayhelper.jGrapeFW_Message;

public class EmailModel {
	private static DBHelper emails;
	private static formHelper form;
	private JSONObject _obj = new JSONObject();

	static {
		System.out.println(appsProxy.configValue());
		emails = new DBHelper(appsProxy.configValue().get("db").toString(), "emailhost");
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
		String tips = "";
		if (info != null) {
			String ownid = "";
			if (!form.checkRuleEx(info)) {
				return resultMessage(1, "");
			}
			String email = info.get("userid").toString();
			if (!checkEmail(email)) {
				return resultMessage(2, "");
			}
			if (FindEmail(email) != null) {
				return resultMessage(3, "");
			}
			if (info.containsKey("ownid")) {
				info.remove("ownid");
			}
			ownid = String.valueOf(appsProxy.appid());
			System.out.println(ownid);
			tips = emailhost.addHost(ownid, info.get("userid").toString(), info.get("password").toString(),
					info.get("smtp").toString(), info.get("pop3").toString());
			System.out.println(tips);
		}
		if (("").equals(tips)) {
			return resultMessage(99);
		}
		JSONObject object = find(tips);
		return resultMessage(object);
	}

	public String delete(String id) {
		if (id.contains(",")) {
			return resultMessage(99);
		}
		return emailhost.removeHost(id) == true ? resultMessage(0, "删除成功") : resultMessage(99);
	}

	public String delete(String[] id) {
		emails.or();
		int len = id.length;
		for (int i = 0; i < len; i++) {
			emails.eq("id", Integer.parseInt(id[i]));
		}
		long s = emails.deleteAll();
		return s == len ? resultMessage(0, "删除成功") : resultMessage(99);
	}

	public String update(String id, JSONObject info) {
		if (info != null) {
			if (info.containsKey("userid")) {
				String email = info.get("userid").toString();
				if (!checkEmail(email)) {
					return resultMessage(2);
				}
			}
			if (info.containsKey("time")) {
				info.remove("time");
			}
		}
		return emailhost.editHost(Integer.parseInt(id), info) == true ? resultMessage(0, "修改成功") : resultMessage(99);
	}

	@SuppressWarnings("unchecked")
	public String page(int idx, int pageSize) {
		JSONObject object = null;
		try {
			object = new JSONObject();
			JSONArray array = emails.page(idx, pageSize);
			object.put("totalSize", (int) Math.ceil((double) emails.count() / pageSize));
			object.put("currentPage", idx);
			object.put("pageSize", pageSize);
			object.put("data", array);
		} catch (Exception e) {
			nlogger.logout(e);
			object = null;
		}
		return resultMessage(object);
	}

	@SuppressWarnings("unchecked")
	public String page(int idx, int pageSize, JSONObject info) {
		JSONObject object = null;
		if (info != null) {
			try {
				object = new JSONObject();
				for (Object object2 : info.keySet()) {
					if (info.containsKey("_id")) {
						emails.eq("_id", new ObjectId(info.get("_id").toString()));
					}
					emails.eq(object2.toString(), info.get(object2.toString()));
				}
				JSONArray array = emails.dirty().page(idx, pageSize);
				object.put("totalSize", (int) Math.ceil((double) emails.count() / pageSize));
				object.put("currentPage", idx);
				object.put("pageSize", pageSize);
				object.put("data", array);
			} catch (Exception e) {
				nlogger.logout(e);
				object = null;
			}
		}
		return resultMessage(object);
	}

	// 发送消息，包含字段：发件人信息，收件人邮箱，抄送人，邮件主题，邮件正文，附件内容
	@SuppressWarnings("unchecked")
	public String send(String ownid, JSONObject object) {
		boolean flag = false;
		if (object != null) {
			String id = "";
			JSONObject obj = search(ownid);
			if (obj != null) {
				id = search(ownid).get("id").toString();
			}
			String CC = "";
			String subject = "";
			String content = "";
			List<String> list = new ArrayList<String>();
			if (!object.containsKey("to")) {
				return resultMessage(4);
			}
			if (!checkEmail(object.get("to").toString())) {
				return resultMessage(2);
			}
			if (object.containsKey("subject")) {
				subject = object.get("subject").toString();
			}
			if (object.containsKey("content")) {
				content = object.get("content").toString();
			}
			if (object.containsKey("cc")) {
				CC = object.get("cc").toString();
			}
			if (object.containsKey("attachments")) {
				list = (List<String>) object.get("attachments");
			}
			try {
				mail mails = mail.defaultEntity(("").equals(id) ? 0 : Integer.parseInt(id), object.get("to").toString(),
						CC, subject, content, list);
				flag = mails.send();
			} catch (Exception e) {
				flag = false;
				nlogger.logout(e);
			}
		}
		return flag ? resultMessage(0, "发送成功") : resultMessage(99);
	}

	// 根据id查找email
	public JSONObject find(String id) {
		JSONObject object = emails.eq("id", Integer.parseInt(id)).find();
		return object != null ? object : null;
	}

	// 根据ownid查找email
	public JSONObject search(String ownid) {
		JSONObject object = emails.eq("ownid", ownid).find();
		return object != null ? object : null;
	}

	// 邮箱是否存在
	public JSONObject FindEmail(String mail) {
		JSONObject object = emails.eq("userid", "mail").find();
		return object != null ? object : null;
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
		if (object != null) {
			if (map.entrySet() != null) {
				Iterator<Entry<String, Object>> iterator = map.entrySet().iterator();
				while (iterator.hasNext()) {
					Map.Entry<String, Object> entry = (Map.Entry<String, Object>) iterator.next();
					if (!object.containsKey(entry.getKey())) {
						object.put(entry.getKey(), entry.getValue());
					}
				}
			}
		}
		return object;
	}

	private String resultMessage(int num) {
		return resultMessage(num, "");
	}

	@SuppressWarnings("unchecked")
	private String resultMessage(JSONObject object) {
		if (object == null) {
			object = new JSONObject();
		}
		_obj.put("records", object);
		return resultMessage(0, _obj.toString());
	}

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
		case 7:
			msg = "有效时间内，请勿重复获取验证码";
			break;
		default:
			msg = "其他操作错误";
			break;
		}
		return jGrapeFW_Message.netMSG(num, msg);
	}
}
