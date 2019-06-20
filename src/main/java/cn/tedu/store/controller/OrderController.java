package cn.tedu.store.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import cn.tedu.store.entity.Address;
import cn.tedu.store.entity.Cart;
import cn.tedu.store.service.IAddressService;
import cn.tedu.store.service.ICartService;

@Controller
@RequestMapping("/order")
public class OrderController
	extends BaseController {
	
	@Autowired
	private IAddressService addressService;
	@Autowired
	private ICartService cartService;
	
	@RequestMapping("/info.do")
	public String showInfo(
		@RequestParam("cart_id") Integer[] cartIds,
		HttpSession session, ModelMap modelMap) {
		// 获取当前用户的uid
		Integer uid = getUidFromSession(session);

		// 获取当前用户的所有收货地址的列表
		// > IAddressService > List<Address> getAddressList(Integer uid)
		List<Address> addresses
			= addressService.getAddressList(uid);

		// 获取cartId数组中每个id对应的购物车信息
		// 声明List<Cart> carts集合
		List<Cart> carts = new ArrayList<Cart>();
		// 遍历cartId数组
		for (Integer cartId : cartIds) {
			// > 遍历过程中，根据id获取购物车信息
			// > ICartService > Cart getCartById(Integer id)
			Cart cart 
				= cartService.getCartById(cartId);
			// > 将获取到的Cart对象添加到carts集合中
			carts.add(cart);
		}

		// 将收货地址列表封装，以转发
		modelMap.addAttribute("addresses", addresses);
		// 将购物车数据列表封装，以转发
		modelMap.addAttribute("carts", carts);
		
		// 测试数据
		System.out.println("【OrderController > /order/info.do】");
		System.out.println("Request : " + Arrays.toString(cartIds));
		System.out.println("Address List : ");
		for (Address address : addresses) {
			System.out.println("\t" + address);
		}
		System.out.println("Cart List : ");
		for (Cart cart : carts) {
			System.out.println("\t" + cart);
		}
		System.out.println();
				
		// 执行转发
		return "order_info";
	}

}





