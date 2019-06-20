package cn.tedu.store.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.tedu.store.entity.City;
import cn.tedu.store.entity.ResponseResult;
import cn.tedu.store.service.ICityService;

@Controller
@RequestMapping("/city")
public class CityController {
	
	@Autowired
	private ICityService cityService;

	@RequestMapping("/list.do")
	@ResponseBody
	public ResponseResult<List<City>> getList(
		@RequestParam("provinceCode") String provinceCode) {
		List<City> data
			= cityService.getCityListByProvinceCode(
					provinceCode);
		ResponseResult<List<City>> rr
			= new ResponseResult<List<City>>();
		rr.setData(data);
		return rr;
	}
	
	@RequestMapping("/info.do")
	@ResponseBody
	public ResponseResult<City> getInfo(
		@RequestParam("cityCode") String cityCode) {
		City data
			= cityService.getCityByCode(cityCode);
		ResponseResult<City> rr
			= new ResponseResult<City>();
		rr.setData(data);
		return rr;
	}
	
}





