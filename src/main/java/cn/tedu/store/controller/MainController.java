package cn.tedu.store.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import cn.tedu.store.entity.Goods;
import cn.tedu.store.entity.GoodsCategory;
import cn.tedu.store.service.IGoodsCategoryService;
import cn.tedu.store.service.IGoodsService;

@Controller
@RequestMapping("/main")
public class MainController 
	extends BaseController {
	
	@Autowired
	private IGoodsCategoryService goodsCategoryService;
	@Autowired
	private IGoodsService goodsService;

	@RequestMapping("/index.do")
	public String showIndex(ModelMap modelMap) {
	    // ***** 声明相关变量 *****
		// 电脑的分类列表
	    Long[] parentIds = {162L, 171L, 186L};
	    // 热销电脑的分类id
	    Long hotGoodsCategoryId = 163L;
	    // 需要显示的热销商品的数量
	    Integer hotGoodsCount = 3;

	    // 获取数据：电脑的3个下级分类列表
	    List<List<GoodsCategory>> goodsCategoiesList
	        = new ArrayList<List<GoodsCategory>>();
	    for (int i = 0; i < parentIds.length; i++) {
	        List<GoodsCategory> goodsCategoies
	            = goodsCategoryService
	                .getListByParentId(parentIds[i]);
	        goodsCategoiesList.add(goodsCategoies);
	    }
		// 获取数据：热销的3台电脑
	    List<Goods> goodsList
	    	= goodsService.getHotGoodsList(
	    		hotGoodsCategoryId, hotGoodsCount);

	    // 封装首页显示需要的数据
	    modelMap.addAttribute(
	        "goodsCategoiesList", goodsCategoiesList);
	    modelMap.addAttribute("goodsList", goodsList);

	    // 执行转发
	    return "index"; // 转发至index.jsp
	}

}











