package test;

import interfaceApplication.Email;

public class testEmail {
	public static void main(String[] args) {
		 String info =
		 "{\"to\":\"2303262967@qq.com\",\"content\":\"测试邮件\",\"subject\":\"测试\"}";
		 System.out.println(new Email().sendEmail("0", info));
		// System.out.println(new Email().VerifyEmail("2303262967@qq.com",
		// "786678"));
//		String string = "{\"pop3\":\"asdf.163.com\",\"smtp\":\"asdf.163.com\",\"ownid\":\"23\",\"userid\":\"ppp\",\"password\":\"123123\",\"state\":\"0\"}";
//		System.out.println(new Email().AddEmail(string));

	}
}
