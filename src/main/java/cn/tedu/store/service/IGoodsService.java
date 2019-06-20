package cn.tedu.store.service;

import java.util.List;

import cn.tedu.store.entity.Goods;

public interface IGoodsService {
	
	/**
	 * 每页显示多少条数据
	 */
	int COUNT_PER_PAGE = 20;

	/**
	 * 获取热销商品列表
	 * @param categoryId 商品分类id
	 * @param count 商品的数量
	 * @return 指定数量、指定分类的热销商品的集合
	 */
	List<Goods> getHotGoodsList(
			Long categoryId, Integer count);
	
	/**
	 * 根据分类id获取商品列表
	 * @param categoryId 分类id
	 * @param page 页码，即获取第几页的数据
	 * @return 商品列表
	 * @see #COUNT_PER_PAGE
	 */
	List<Goods> getListByCategoryId(
			Long categoryId, Integer page);
	
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






