package cn.tedu.store.controller;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import cn.tedu.store.entity.ResponseResult;
import cn.tedu.store.entity.User;
import cn.tedu.store.service.IUserService;
import cn.tedu.store.service.ex.PasswordNotMatchException;
import cn.tedu.store.service.ex.UserNotFoundException;
import cn.tedu.store.service.ex.UsernameConflictException;
import cn.tedu.store.util.Validator;

@Controller
@RequestMapping("/user")
public class UserController extends BaseController {
	
	@Autowired
	private IUserService userService;
	
	/**
	 * 上传的头像的最大尺寸，单位：KB
	 */
	public static final long MAX_AVATAR_SIZE = 50;
	
	@RequestMapping("/reg.do")
	public String showReg() {
		return "user_reg";
	}
	
	@RequestMapping("/login.do")
	public String showLogin() {
		return "user_login";
	}
	
	@RequestMapping("/change_password.do")
	public String showChangePassword() {
	    return "user_change_password";
	}
	
	@RequestMapping("/change_info.do")
	public String showChangeInfo(
			HttpSession session,
			ModelMap modelMap) {
		// 获取当前登录的用户的id
		Integer uid 
			= getUidFromSession(session);
		// 查询当前登录的用户的数据
		User user 
			= userService.findUserById(uid);
		// 将用户数据封装，以准备转发
		modelMap.addAttribute("user", user);
		// 执行转发
		return "user_change_info";
	}

	@RequestMapping(value="/handle_reg.do",
			method=RequestMethod.POST)
	@ResponseBody
	public ResponseResult<Void> handleReg(
		@RequestParam("username") String username, 
		@RequestParam("password") String password, 
		String email, 
		String phone, 
		Integer gender) {
		// 声明返回值
		ResponseResult<Void> rr;
		
		// 验证数据的有效性，即基本格式
		boolean result = Validator.checkUsername(username);
		if (!result) {
			rr = new ResponseResult<Void>(
					ResponseResult.STATE_ERR, 
					"您输入的用户名有误！");
			return rr;
		}
		result = Validator.checkPassword(password);
		if (!result) {
			rr = new ResponseResult<Void>(
					ResponseResult.STATE_ERR, 
					"您输入的密码格式有误！");
			return rr;
		}
		
		try {
			// 把各参数封装到User对象中
			User user = new User(
				username, password, gender, phone, email);
			// 调用业务层的对象的reg()方法实现注册
			userService.reg(user);
			// 封装返回结果
			rr = new ResponseResult<Void>(
					ResponseResult.STATE_OK);
		} catch(UsernameConflictException e) {
			// 封装返回结果
			rr = new ResponseResult<Void>(e);
		}
		return rr;
	}
	
	@RequestMapping(value="/handle_login.do", 
			method=RequestMethod.POST)
	@ResponseBody
	public ResponseResult<Void> handleLogin(
		@RequestParam("username") String username,
		@RequestParam("password") String password,
		HttpSession session) {
		// 声明返回值
		ResponseResult<Void> rr;
		
		// 忽略：检查数据的有效性

		try {
			// 执行登录
			User user
				= userService.login(username, password);
			// 在session中封装uid和username
			session.setAttribute("uid", user.getId());
			session.setAttribute("username", user.getUsername());
			// 返回：成功
			rr = new ResponseResult<Void>(ResponseResult.STATE_OK);
		} catch (UserNotFoundException e) {
			// 用户名不存在
			rr = new ResponseResult<Void>(-1, e.getMessage());
		} catch (PasswordNotMatchException e) {
			// 密码不存在
			rr = new ResponseResult<Void>(-2, e.getMessage());
		}

		// 执行返回
		return rr;
	}
	
	@RequestMapping(value="/handle_change_password.do",
		method=RequestMethod.POST)
	@ResponseBody
	public ResponseResult<Void> handleChangePassword(
		@RequestParam("old_password") String oldPassword,
		@RequestParam("new_password") String newPassword,
		HttpSession session) {
		// 声明返回值
		ResponseResult<Void> rr;

		try {
			// 通过session获取uid
			Integer uid 
				= getUidFromSession(session);
			// 调用业务层对象的changePassword(uid, oldPassword, newPassword)方法
			userService.changePassword(uid, oldPassword, newPassword);
			// 创建rr对象，表示成功
			rr = new ResponseResult<Void>(
					ResponseResult.STATE_OK);
		} catch (UserNotFoundException e) {
			// 创建rr对象，-1，e.getMessage()
			rr = new ResponseResult<Void>(
					-1, e.getMessage());
		} catch (PasswordNotMatchException e) {
			rr = new ResponseResult<Void>(
					-2, e.getMessage());
		}

		// 执行返回
		return rr;
	}
	
	@RequestMapping(value="/handle_change_info.do",
			method=RequestMethod.POST)
	@ResponseBody
	public ResponseResult<String> handleChangeInfo(
		MultipartHttpServletRequest	request,
		MultipartFile avatar,
		String username,
		Integer gender,
		String phone,
		String email,
		HttpSession session) {
		// 声明ResponseResult<Void> rr返回值
		ResponseResult<String> rr;
		// 用户上传的头像在服务器端的路径
		String avatarPath = null;
		// 从session中获取uid
		Integer uid 
			= getUidFromSession(session);
		
		// ======== 上传头像 ========
		// 判断用户是否选择了上传新的头像
		if(!avatar.isEmpty()) {
			// 验证文件类型
			String contentType = avatar.getContentType();
			if (!"image/png".equals(contentType)
					&& !"image/jpeg".equals(contentType)
						&& !"image/bmp".equals(contentType)) {
				rr = new ResponseResult<String>();
				rr.setState(ResponseResult.STATE_ERR); 
				rr.setMessage("不支持上传" + contentType + "类型的文件！");
				return rr;
			}
			// 验证文件大小
			long size = avatar.getSize();
			if (size > MAX_AVATAR_SIZE * 1024) {
				rr = new ResponseResult<String>(
					ResponseResult.STATE_ERR, 
					"上传的头像文件不允许超过" 
							+ MAX_AVATAR_SIZE + "KB！");
				return rr;
			}
			
			// 保存头像的文件夹，所有用户头像都在这个文件夹中
			String avatarDirPath
				= request.getServletContext()
					.getRealPath("/upload/image");
			File avatarDir = new File(avatarDirPath);
			System.out.println("【测试】avatarDirPath=" + avatarDirPath);
			// 如果保存头像的文件夹不存在，则创建
			// if (!avatarDir.exists()) {
			//	avatarDir.mkdirs();
			// }
	
			// 获取客户上传的原始的文件的文件名
			String originalFilename 
				= avatar.getOriginalFilename();
			// 获取客户上传的原始的文件的扩展名
			String suffix 
				= originalFilename.substring(
					originalFilename.lastIndexOf("."));
			
			// 头像文件的文件名，每个用户的头像文件名都应该不相同
			Date date = new Date();
			String filename 
				= getDateString(date) + uid + suffix; // 20181009094512.png
			
			// 目标文件，即在服务端保存的用户头像文件
			File dest = new File(avatarDir, filename);
			
			try {
				// 将用户上传的头像数据保存到文件
				avatar.transferTo(dest);
				// 确定用户头像在服务器端的路径
				avatarPath = "upload/image/" + filename;
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		// ======== 执行修改个人资料 ========
		try {
		    // 调用userService中的changeInfo()方法
			userService.changeInfo(
				uid, 
				avatarPath, 
				username, gender, phone, email);
		    // 创建成功时的ResponseResult
			rr = new ResponseResult<String>(
				ResponseResult.STATE_OK);
			// 将头像路径封装在返回对象的data属性中
			rr.setData(avatarPath);
		} catch (UserNotFoundException e) {
		    // 创建失败（用户不存在）时的ResponseResult
			rr = new ResponseResult<String>(-1, e.getMessage());
		} catch (UsernameConflictException e) {
		    // -- 创建失败（用户名冲突）时的ResponseResult
			rr = new ResponseResult<String>(-2, e.getMessage());
		}
		
		// 执行返回
		return rr;
	}
	
	private final String pattern = "yyyyMMddHHmmss";
	
	private final SimpleDateFormat sdf
		= new SimpleDateFormat(
			pattern, Locale.CHINA);
	
	private String getDateString(Date date) {
		return sdf.format(date);
	}
	
}









