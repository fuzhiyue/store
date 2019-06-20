package cn.tedu.store.mapper;

import java.util.List;

import cn.tedu.store.entity.Province;

public interface ProvinceMapper {

	/**
	 * 获取所有省的列表
	 * 
	 * @return 省的列表
	 */
	List<Province> getProvinceList();

	/**
	 * 根据省的代号获取省的信息
	 * 
	 * @param provinceCode
	 *            省的代号
	 * @return 省的信息
	 */
	Province getProvinceByCode(String provinceCode);

}



