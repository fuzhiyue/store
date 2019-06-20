package cn.tedu.store.service;

import cn.tedu.store.entity.Order;
import cn.tedu.store.entity.OrderItem;

public interface IOrderService {
	
	/**
	 * 创建订单
	 * @param uid 当前登录的用户id
	 * @param addressId 选定的收货地址id
	 * @param cartIds 选定的购物车中的数据id
	 * @return 订单号
	 */
	Integer createOrder(
		Integer uid, Integer addressId, Integer[] cartIds);
	
	/**
	 * 新增订单数据
	 * @param order 订单数据
	 * @return 受影响的行数
	 * @see #insert(OrderItem)
	 */
	Integer insertOrder(Order order);

	/**
	 * 新增订单商品数据
	 * @param order 订单商品数据
	 * @return 受影响的行数
	 * @see #insert(Order)
	 */
	Integer insertOrderItem(OrderItem orderItem);
}
