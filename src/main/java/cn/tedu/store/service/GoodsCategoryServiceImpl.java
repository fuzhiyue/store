package cn.tedu.store.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.tedu.store.entity.GoodsCategory;
import cn.tedu.store.mapper.GoodsCategoryMapper;

@Service("goodsCategoryService")
public class GoodsCategoryServiceImpl 
	implements IGoodsCategoryService {
	
	@Autowired
	private GoodsCategoryMapper goodsCategoryMapper;

	public List<GoodsCategory> getListByParentId(
			Long parentId) {
		return goodsCategoryMapper
				.getListByParentId(parentId);
	}

}






