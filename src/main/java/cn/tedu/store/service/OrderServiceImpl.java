package cn.tedu.store.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.tedu.store.entity.Address;
import cn.tedu.store.entity.Cart;
import cn.tedu.store.entity.Order;
import cn.tedu.store.entity.OrderItem;
import cn.tedu.store.mapper.OrderMapper;
import cn.tedu.store.service.ex.InsertDataException;

@Service("orderService")
public class OrderServiceImpl implements IOrderService {
	
	@Autowired
	private OrderMapper orderMapper;
	@Autowired
	private ICartService cartService;
	@Autowired
	private IAddressService addressService;

	public Integer createOrder(
			Integer uid, Integer addressId, Integer[] cartIds) {
		// 准备即将插入的订单商品数据
		List<OrderItem> orderItems
			= new ArrayList<OrderItem>();
		// 准备计算total_price
		Long totalPrice = 0L;
		// 遍历cartIds，逐一获取购物车信息，得到订单商品中所需数据
		for (Integer cartId : cartIds) {
			// 获取购物车中的数据
			Cart cart = cartService.getCartById(cartId);
			// 累加商品的价格
			totalPrice += 
				cart.getGoodsPrice() * cart.getGoodsNum();
			// 准备OrderItem数据，后续将插入该数据
			OrderItem orderItem = new OrderItem();
			orderItem.setGoodsId(cart.getGoodsId());
			orderItem.setGoodsImage(cart.getGoodsImage());
			orderItem.setGoodsNum(cart.getGoodsNum());
			orderItem.setGoodsPrice(cart.getGoodsPrice());
			orderItem.setGoodsTitle(cart.getGoodsTitle());
			// 将OrderItem对象添加到集合中
			orderItems.add(orderItem);
		}

		// 收货相关信息由addressId查询后得到
		Address address
			= addressService.getAddressById(addressId);
		
		// status固定为1，表示默认的“未支付”
		Integer status = 1; // 1:未支付
		
		// create_time取当前系统时间
		Date now = new Date();
		
		
		// 插入订单数据
		Order order = new Order();
		order.setUid(uid);
		order.setRecvName(address.getRecvName());
		order.setRecvPhone(address.getRecvPhone());
		order.setRecvAddress(
				address.getRecvDistrict() + " " 
						+ address.getRecvAddress());
		order.setStatus(status);
		order.setCreateTime(now);
		order.setPayTime(null);
		order.setTotalPrice(totalPrice);
		insertOrder(order);
		
		// 获取新插入的订单数据的id
		Integer orderId = order.getId();

		// 插入订单商品数据
		for (OrderItem orderItem : orderItems) {
			orderItem.setOrderId(orderId);
			insertOrderItem(orderItem);
		}
		
		// 返回
		return orderId;
	}
	
	public Integer insertOrder(Order order) {
		Integer rows = orderMapper.insertOrder(order);
		if (rows == 1) {
			return rows;
		} else {
			throw new InsertDataException("创建订单数据失败！");
		}
	}

	public Integer insertOrderItem(OrderItem orderItem) {
		Integer rows = orderMapper.insertOrderItem(orderItem);
		if (rows == 1) {
			return rows;
		} else {
			throw new InsertDataException("创建订单商品数据失败！");
		}
	}

}
