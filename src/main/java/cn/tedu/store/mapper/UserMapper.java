package cn.tedu.store.mapper;

import java.util.Date;

import org.apache.ibatis.annotations.Param;

import cn.tedu.store.entity.User;

public interface UserMapper {
	
	/**
	 * 增加用户数据
	 * @param user 用户数据对象
	 * @return 受影响的行数，如果增加成功，则返回1，否则，返回0
	 */
	Integer insert(User user);

	/**
	 * 根据用户名查询用户数据
	 * @param username 用户名
	 * @return 与参数用户名匹配的用户数据，如果没有匹配的数据，则返回null
	 */
	User findUserByUsername(String username);
	
	/**
	 * 根据id查询用户数据
	 * @param id 用户id
	 * @return 与参数用户id匹配的用户数据，如果没有匹配的数据，则返回null
	 */
	User findUserById(Integer id);

	/**
	 * 修改密码
	 * @param uid 用户id
	 * @param password 新密码
	 * @return 受影响的行数，正确操作情况下返回1，否则，返回0
	 */
	Integer changePassword(
	    @Param("uid") Integer uid, 
	    @Param("password") String password,
	    @Param("modifiedUser") String modifiedUser,
	    @Param("modifiedTime") Date modifiedTime);

	/**
	 * 修改个人信息
	 * @param user 封装了被修改的用户id（必选）、新用户名（可选）、新性别（可选）、新手机号（可选）、新电子邮件（可选）的对象
	 * @return 受影响的行数，正确操作情况下返回1，否则，返回0
	 */
	Integer changeInfo(User user);
	
}
