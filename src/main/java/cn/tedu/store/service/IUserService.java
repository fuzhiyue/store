package cn.tedu.store.service;

import cn.tedu.store.entity.User;
import cn.tedu.store.service.ex.PasswordNotMatchException;
import cn.tedu.store.service.ex.UserNotFoundException;
import cn.tedu.store.service.ex.UsernameConflictException;

public interface IUserService {
	
	/**
	 * 用户注册 
	 * @param user 注册的用户数据对象
	 * @return 成功注册的用户数据对象，包含用户的id
	 * @throws UsernameConflictException 用户名已经被占用
	 */
	User reg(User user) 
		throws UsernameConflictException;
	
	/**
	 * 登录
	 * @param username 用户名
	 * @param password 密码
	 * @return 登录成功的用户数据
	 * @throws UserNotFoundException
	 * @throws PasswordNotMatchException
	 */
	User login(String username, String password) 
		throws UserNotFoundException, 
				PasswordNotMatchException;
	
	/**
	 * 修改密码
	 * @param uid 用户id
	 * @param oldPassword 原密码
	 * @param newPassword 新密码
	 * @return 受影响的行数
	 * @return UserNotFoundException
	 * @throws PasswordNotMatchException
	 */
	Integer changePassword(
		Integer uid, String oldPassword, String newPassword) 
			throws UserNotFoundException,
				PasswordNotMatchException;
	
	/**
	 * 修改用户个人信息
	 * @param uid 用户id
	 * @param avatar 新头像路径，如果不修改，则使用null值
	 * @param username 新用户名，如果不修改，则使用null值
	 * @param gender 新性别，如果不修改，则使用null值
	 * @param phone 新手机号码，如果不修改，则使用null值
	 * @param email 新电子邮箱，如果不修改，则使用null值
	 * @return 受影响的行数
	 * @throws UserNotFoundException
	 * @throws UsernameConflictException
	 */
	Integer changeInfo(
			Integer uid, 
			String avatar,
			String username, 
			Integer gender, 
			String phone, 
			String email) throws UserNotFoundException,
					UsernameConflictException;
	
	/**
	 * 根据用户名查询用户数据
	 * @param username 用户名
	 * @return 与参数用户名匹配的用户数据，如果没有匹配的数据，则返回null
	 */
	User findUserByUsername(String username);

	/**
	 * 根据用户id查询用户数据
	 * @param id 用户id
	 * @return 与参数用户id匹配的用户数据，如果没有匹配的数据，则返回null
	 */
	User findUserById(Integer id);
	
	/**
	 * 获取加密后的密码
	 * @param password 原密码
	 * @param salt 盐
	 * @return 加密后的密码
	 */
	String getMd5Password(
			String password, String salt);

}




