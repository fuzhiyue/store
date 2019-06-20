package cn.tedu.store.mapper;

import cn.tedu.store.entity.Order;
import cn.tedu.store.entity.OrderItem;

public interface OrderMapper {

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
