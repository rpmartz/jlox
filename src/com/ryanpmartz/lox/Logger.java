package com.ryanpmartz.lox;

public class Logger {

	private static boolean enabled = false;

	public static void log(String msg, Scanner scanner) {
		if (enabled) {
			System.out.println(msg + scanner);
		}
	}
}
