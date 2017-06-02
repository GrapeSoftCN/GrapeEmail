package test;

import httpServer.booter;

public class testEmail {
	public static void main(String[] args) {
		booter booter = new booter();
		System.out.println("GrapeEmail!");
		try {
			System.setProperty("AppName", "GrapeEmail");
			booter.start(6002);
		} catch (Exception e) {

		}
	}
}
