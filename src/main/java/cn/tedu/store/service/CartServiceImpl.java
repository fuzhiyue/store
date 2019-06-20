package cn.tedu.store.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.tedu.store.entity.Cart;
import cn.tedu.store.entity.Goods;
import cn.tedu.store.mapper.CartMapper;
import cn.tedu.store.service.ex.DataNotFoundException;
import cn.tedu.store.service.ex.IllegalParamException;
import cn.tedu.store.service.ex.InsertDataException;

@Service("cartService")
public class CartServiceImpl implements ICartService {
	
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private CartMapper cartMapper;

	public void addToCart(
			Integer uid, Long goodsId, Integer num) {
		// 查询数据表中是否已经存在数据：
		Cart cart = getCartByUidAndGoodsId(uid, goodsId);
	    // 判断返回值是否为null
		if (cart == null) {
		    // 为null：该用户没有将该商品添加到购物车
		    // 直接调用insert()方法执行增加
			insert(uid, goodsId, num);
		} else {
			// 非null：该用户已经将该商品添加到购物车
			// 则需要更新商品的数量
			// 从查询结果中获取数据id：cart.getId()
			Integer id = cart.getId();
			// 从查询结果中获取原来的数量：
	    	Integer n = cart.getGoodsNum();
	    	// 计算新的数量应该是多少goodsNum = num + n
	    	Integer goodsNum = n + num;
		    // 判断：新数量goodsNum > 0
	    	if (goodsNum > 0) {
	    		// 允许修改数量
	    		changeGoodsNum(id, goodsNum);
	    	} else {
	    		// 否则，goodsNum <= 0，则抛出异常IllegalParamException
				throw new IllegalParamException(
					"不允许将购物车中的商品数量修改为小于1的值！");
	    	}
		}
	}

	private void insert(
			Integer uid, Long goodsId, Integer num) {
		// 通过goodsId获取商品信息：image, price, title
	    Goods goods = goodsService.getGoodsById(goodsId);
	    // 封装Cart对象
	    Cart cart = new Cart();
	    cart.setUid(uid);
	    cart.setGoodsId(goodsId);
	    cart.setGoodsImage(goods.getImage());
	    cart.setGoodsPrice(goods.getPrice());
	    cart.setGoodsTitle(goods.getTitle());
	    cart.setGoodsNum(num);
	    // 忽略：在Cart对象中封装日志信息
	    // 执行增加
	    Integer rows = cartMapper.insert(cart);
	    // 判断增加结果
	    if (rows != 1) {
	        throw new InsertDataException(
	            "添加购物车信息失败：" + cart);
	    }
	}

	public Integer changeGoodsNum(Integer id, Integer goodsNum) {
		Integer rows = cartMapper
				.changeGoodsNum(id, goodsNum);
		if (rows == 1) {
			return rows; // return 1;
		} else {
			throw new DataNotFoundException(
				"尝试操作的购物车数据(id=" + id +")不存在！");
		}
	}

	public Cart getCartByUidAndGoodsId(Integer uid, Long goodsId) {
		return cartMapper.getCartByUidAndGoodsId(uid, goodsId);
	}

	public List<Cart> getListByUid(Integer uid) {
		return cartMapper.getListByUid(uid);
	}
	
	public Cart getCartById(Integer id) {
		return cartMapper.getCartById(id);
	}

}
