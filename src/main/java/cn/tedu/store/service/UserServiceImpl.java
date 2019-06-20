package cn.tedu.store.service;

import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import cn.tedu.store.entity.User;
import cn.tedu.store.mapper.UserMapper;
import cn.tedu.store.service.ex.PasswordNotMatchException;
import cn.tedu.store.service.ex.UserNotFoundException;
import cn.tedu.store.service.ex.UsernameConflictException;
import cn.tedu.store.util.Validator;

@Service("userService")
public class UserServiceImpl 
	implements IUserService {
	
	@Autowired
	private UserMapper userMapper;

	public User reg(User user) throws UsernameConflictException {
		User data = findUserByUsername(user.getUsername());
		if (data == null) {
			String uuid = UUID.randomUUID().toString();
			String md5Password
				= getMd5Password(user.getPassword(), uuid);
			user.setPassword(md5Password);
			user.setUuid(uuid);
			
			Date now = new Date();
			user.setCreatedUser(user.getUsername());
			user.setCreatedTime(now);
			user.setModifiedUser(user.getUsername());
			user.setModifiedTime(now);
			
			userMapper.insert(user);
			return user;
		} else {
			throw new UsernameConflictException(
				"用户名(" + user.getUsername() + ")已经被注册！");
		}
	}

	public User login(String username, String password) throws UserNotFoundException, PasswordNotMatchException {
		// 调用自身的findUserByUsername()查询用户名匹配的数据
		User data = findUserByUsername(username);
		// 判断是否查询到匹配的数据
		if (data != null) {
			// 存在：用户名存在，得到了与用户名匹配的完整数据
			// 从查询到的数据中取出uuid
			String salt = data.getUuid();
			// 调用getMd5Password()将password参数加密
			String md5Password
				= getMd5Password(password, salt);
			// 判断密码是否匹配
			if (data.getPassword().equals(md5Password)) {
				// 匹配：登录成功，返回查询到的User对象
				data.setPassword(null);
				data.setUuid(null);
				return data;
			} else {
				// 不匹配：密码错误，抛出异常：PasswordNotMatchException
				throw new PasswordNotMatchException("密码错误！");
			}
		} else {
			// 不存在：用户名不存在，即用户名错误
			// 抛出异常：UserNotFoundException
			throw new UserNotFoundException("用户名不存在！");
		}
	}
	
	public Integer changePassword(Integer uid, String oldPassword, String newPassword)
			throws UserNotFoundException, PasswordNotMatchException {
		// 根据uid获取用户数据
		User data = findUserById(uid);
		// 判断是否获取到数据，如果没有，则抛出异常：UserNotFoundException
		if (data == null) {
			throw new UserNotFoundException("尝试操作的用户数据不存在！");
		}
		
		// 从用户数据中获取uuid
		String salt = data.getUuid();
		// 调用getMd5Password(oldPassword, uuid)将用户提供的原密码进行加密
		String oldMd5Password = 
				getMd5Password(oldPassword, salt);
		// 判断加密后的原密码，与用户数据中的密码，是否正确
		if (data.getPassword().equals(oldMd5Password)) {
			// 原密码正确：
			// 调用getMd5Password(newPassword, uuid)将用户提供的新密码进行加密
			String newMd5Password = 
					getMd5Password(newPassword, salt);
			// 执行修改：调用持久层对象的Integer changePassword(9, newMd5Password)，并获取返回值
			Integer rows = 
					userMapper.changePassword(
						uid, newMd5Password,
						data.getUsername(), new Date());
			// 判断返回值是否为1
			if (rows == 1) {
				// 值为1：操作正确，返回1
				return 1;
			} else {
				// 值不为1：操作失败，抛出异常：UserNotFoundException
				throw new UserNotFoundException("尝试操作的用户数据不存在！");
			}
		} else {
			// 原密码错误：抛出异常：PasswordNotMatchException
			throw new PasswordNotMatchException("密码错误！");
		}
	}

	public Integer changeInfo(
			Integer uid, String avatar, 
			String username, Integer gender, 
			String phone, String email)
			throws UserNotFoundException {
		// 检查数据的有效性
		// 如果用户名格式错误，视为null
		if (!Validator.checkUsername(username)) {
			username = null;
		}
		// （略）如果电话号码格式错误，视为null，同上
		// （略）如果电子邮箱格式错误，视为null，同上
		
		// 如果修改用户名，则用户名不可是其他用户已经注册的
		// 判断用户名是否为null，仅当不为null时执行此项检查
		if (username != null) {
			// 调用自身的findUserByUsername()根据用户名查询
			User data = findUserByUsername(username);
			if (data == null) {
				// 结果为null：用户名没被占用，可以使用，用户名保持不变，等待后续代码将其更新到数据表中
			} else {
				// 如果不为null：根据用户名找到数据，则根据id还需要判断是不是当前用户
				if (data.getId().equals(uid)) {
					// 如果id匹配：用户名是自己的，没必要修改：设置为null
					username = null;
				} else {
					// 如果id不匹配：用户名是别人：抛出异常UsernameConflictException
					throw new UsernameConflictException(
							"用户名(" + username + ")已经被注册！");
				}
			}
		}

		// 将各参数都封装到User类型的对象中
		User user = new User(
			uid, username, gender, phone, email, avatar);
		// 封装日志数据
		User data = findUserById(uid);
		user.setModifiedUser(data.getUsername());
		user.setModifiedTime(new Date());
		// 调用持久层对象的changeInfo(User)方法，并获取返回值
		Integer rows = userMapper.changeInfo(user);
		// 判断返回值是否为：1
		if (rows == 1) {
			// 如果为1：返回1
			return 1;
		} else {
			// 如果不为1：抛出异常UserNotFoundException
			throw new UserNotFoundException("尝试操作的用户数据不存在！");
		}
	}

	public User findUserByUsername(String username) {
		return userMapper.findUserByUsername(username);
	}
	
	public User findUserById(Integer id) {
		return userMapper.findUserById(id);
	}

	public String getMd5Password(
			String password, String salt) {
		// ----------------------------------------
		// -------- 以下加密规则是自行设计的 --------
		// ----------------------------------------
		// 先把原密码使用MD5加密，并且把密文转大写
		String str1 = DigestUtils.md5DigestAsHex(
				password.getBytes()).toUpperCase();
		// 然后把结果和盐值拼接
		String str2 = str1 + salt.toUpperCase();
		// 再次加密，得到最终密码
		String str3 = DigestUtils.md5DigestAsHex(
				str2.getBytes()).toUpperCase();
		// 返回
		return str3;
	}

}





