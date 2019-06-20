package cn.tedu.store.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class LoginInterceptor 
	implements HandlerInterceptor {

	public boolean preHandle(
			HttpServletRequest request, 
			HttpServletResponse response, 
			Object handler)
			throws Exception {
		System.out.println("LoginInterceptor.preHandle()");
		System.out.println("\trequest=" + request);
		System.out.println("\tresponse=" + response);
		System.out.println("\thandler=" + handler);
		
		// 登录拦截器的目标：
		// 如果登录了，正常访问
		// 如果没登录，拦截，重定向到登录页
		
		// 判断是否登录
		HttpSession session = request.getSession();
		if (session.getAttribute("uid") == null) {
			// Session中没有登录的用户信息
			// 即：用户没有登录
			// 则：重定向到登录页
			response.sendRedirect("../user/login.do");
			// 拦截
			return false;
		} else {
			// Session中存在登录的用户信息
			// 即：用户已经登录
			// 则：放行
			return true;
		}
	}

	public void postHandle(
			HttpServletRequest request, 
			HttpServletResponse response, 
			Object handler,
			ModelAndView modelAndView) 
			throws Exception {
		System.out.println("LoginInterceptor.postHandle()");
		System.out.println("\trequest=" + request);
		System.out.println("\tresponse=" + response);
		System.out.println("\thandler=" + handler);
		System.out.println("\tmodelAndView=" + modelAndView);
	}

	public void afterCompletion(
			HttpServletRequest request, 
			HttpServletResponse response, 
			Object handler, 
			Exception ex)
			throws Exception {
		System.out.println("LoginInterceptor.afterCompletion()");
		System.out.println("\trequest=" + request);
		System.out.println("\tresponse=" + response);
		System.out.println("\thandler=" + handler);
		System.out.println("\tex=" + ex);
	}

}
