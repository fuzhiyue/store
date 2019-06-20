package cn.tedu.store.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.tedu.store.entity.Cart;

public interface CartMapper {

	/**
	 * 向购物车表中添加数据
	 * 
	 * @param cart
	 *            购物车信息
	 * @return 受影响的行数
	 */
	Integer insert(Cart cart);

	/**
	 * 修改购物车中某用户的某商品的数量
	 * 
	 * @param id
	 *            购物车数据id
	 * @param goodsNum
	 *            商品的新数量
	 * @return 受影响的行数
	 */
	Integer changeGoodsNum(@Param("id") Integer id, @Param("goodsNum") Integer goodsNum);

	/**
	 * 根据用户id和商品id，获取购物车中的数据
	 * 
	 * @param uid
	 *            用户id
	 * @param goodsId
	 *            商品id
	 * @return 匹配的数据，如果没有匹配的数据，则返回null
	 */
	Cart getCartByUidAndGoodsId(@Param("uid") Integer uid, @Param("goodsId") Long goodsId);

	/**
	 * 获取某用户的购物车数据列表
	 * 
	 * @param uid
	 *            用户id
	 * @return 购物车数据列表
	 */
	List<Cart> getListByUid(Integer uid);
	
	/**
	 * 根据数据id获取购物车数据
	 * @param id 数据id
	 * @return 购物车数据
	 */
	Cart getCartById(Integer id);

}
