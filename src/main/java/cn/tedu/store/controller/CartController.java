package cn.tedu.store.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.tedu.store.entity.Cart;
import cn.tedu.store.entity.ResponseResult;
import cn.tedu.store.service.ICartService;
import cn.tedu.store.service.ex.ServiceException;

@Controller
@RequestMapping("/cart")
public class CartController extends BaseController {
	
	@Autowired
	private ICartService cartService;
	
	@RequestMapping("/list.do")
	public String showList(ModelMap modelMap,
			HttpSession session) {
		// 获取当前登录的用户的id
		Integer uid = getUidFromSession(session);
		
		// 获取当前用户的购物车数据
		List<Cart> carts
			= cartService.getListByUid(uid);
		
		// 封装数据：当前用户的购物车数据
		modelMap.addAttribute("carts", carts);
		
		// 执行转发
		return "cart_list";
	}
	
	@RequestMapping("/add.do")
	@ResponseBody
	public ResponseResult<Void> handleAddToCart(
		@RequestParam("goods_id") Long goodsId,
		Integer num,
		HttpSession session) {
		// 声明ResponseResult<Void>返回值
		ResponseResult<Void> rr;
		
		// 从session中获取uid
		Integer uid = getUidFromSession(session);
		
		try {
			// 调用业务层的“添加到购物车”方法
			cartService.addToCart(uid, goodsId, num);
			// 操作成功：创建返回值对象，标记state为成功，并返回
			rr = new ResponseResult<Void>(
					ResponseResult.STATE_OK);
		} catch (ServiceException e) {
			// 操作失败：创建返回值对象，标记state为失败，封装message错误信息，并返回
			rr = new ResponseResult<Void>(e);
		}
		return rr;
	}
	
}








