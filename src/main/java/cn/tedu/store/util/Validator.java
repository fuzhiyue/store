package cn.tedu.store.util;

/**
 * 验证器，用于验证字符串的格式是否正确
 */
public final class Validator {
	
	private Validator() {
		super();
	}
	
	/**
	 * 验证用户名的正则表达式
	 */
	public static final String REGEX_USERNAME 
		= "^[a-zA-Z]{1}[a-zA-Z0-9_]{3,15}$";
	
	/**
	 * 验证密码的正则表达式
	 */
	public static final String REGEX_PASSWORD 
		= "^\\d{6,18}$";
	
	/**
	 * 验证用户名
	 * @param username 需要被验证格式的用户名
	 * @return 如果符合格式要求，则返回true，否则返回false
	 */
	public static boolean checkUsername(String username) {
		return username.matches(REGEX_USERNAME);
	}
	
	/**
	 * 验证密码
	 * @param password 需要被验证格式的密码
	 * @return 如果符合格式要求，则返回true，否则返回false
	 */
	public static boolean checkPassword(String password) {
		return password.matches(REGEX_PASSWORD);
	}
	
}
