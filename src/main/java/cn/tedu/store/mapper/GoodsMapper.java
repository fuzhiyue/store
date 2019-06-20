package cn.tedu.store.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.tedu.store.entity.Goods;

public interface GoodsMapper {

	/**
	 * 获取热销商品列表
	 * @param categoryId 商品分类id
	 * @param count 商品的数量
	 * @return 指定数量、指定分类的热销商品的集合
	 */
	List<Goods> getHotGoodsList(
			@Param("categoryId") Long categoryId, 
			@Param("count") Integer count);
	
	/**
	 * 根据分类id获取商品列表
	 * @param categoryId 分类id
	 * @param offset 获取数据时的偏移量
	 * @param count 获取的商口的数量
	 * @return 商品列表
	 */
	List<Goods> getListByCategoryId(
			@Param("categoryId") Long categoryId,
			@Param("offset") Integer offset,
			@Param("count") Integer count);
	
	/**
	 * 获取某个分类下的商品的数量
	 * @param categoryId 分类id
	 * @return 商品的数量
	 */
	Integer getCountByCategoryId(Long categoryId);
	
	/**
	 * 根据商品id获取商品信息
	 * @param id 商品id
	 * @return 商品信息，如果没有匹配的数据，则返回null
	 */
	Goods getGoodsById(Long id);
	
}



