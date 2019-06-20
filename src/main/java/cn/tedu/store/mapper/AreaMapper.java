package cn.tedu.store.mapper;

import java.util.List;

import cn.tedu.store.entity.Area;

public interface AreaMapper {

	/**
	 * 获取某个市的所有区的列表
	 * 
	 * @param cityCode
	 *            市的代号
	 * @return 区的列表
	 */
	List<Area> getAreaListByCityCode(
			String cityCode);

	/**
	 * 根据区的代号获取区的信息
	 * 
	 * @param areaCode
	 *            区的代号
	 * @return 区的信息
	 */
	Area getAreaByCode(String areaCode);

}
