package cn.tedu.store.service;

import java.util.List;

import cn.tedu.store.entity.City;

public interface ICityService {

	/**
	 * 获取某个省的所有市的列表
	 * 
	 * @param provinceCode
	 *            省的代号
	 * @return 市的列表
	 */
	List<City> getCityListByProvinceCode(
			String provinceCode);

	/**
	 * 根据市的代号获取市的信息
	 * 
	 * @param provinceCode
	 *            市的代号
	 * @return 市的信息
	 */
	City getCityByCode(String cityCode);
	
}
