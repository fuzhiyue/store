package cn.tedu.store.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.tedu.store.entity.City;
import cn.tedu.store.mapper.CityMapper;

@Service("cityService")
public class CityServiceImpl implements ICityService {
	
	@Autowired
	private CityMapper cityMapper;

	public List<City> getCityListByProvinceCode(
			String provinceCode) {
		return cityMapper
				.getCityListByProvinceCode(
						provinceCode);
	}

	public City getCityByCode(String cityCode) {
		return cityMapper
				.getCityByCode(cityCode);
	}

}



