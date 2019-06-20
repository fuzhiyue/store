package cn.tedu.store.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.tedu.store.entity.Area;
import cn.tedu.store.entity.ResponseResult;
import cn.tedu.store.service.IAreaService;

@Controller
@RequestMapping("/area")
public class AreaController {
	
	@Autowired
	private IAreaService areaService;

	@RequestMapping("/list.do")
	@ResponseBody
	public ResponseResult<List<Area>> getList(
		@RequestParam("cityCode") String cityCode) {
		List<Area> data
			= areaService.getAreaListByCityCode(
					cityCode);
		ResponseResult<List<Area>> rr
			= new ResponseResult<List<Area>>();
		rr.setData(data);
		return rr;
	}
	
	@RequestMapping("/info.do")
	@ResponseBody
	public ResponseResult<Area> getInfo(
		@RequestParam("areaCode") String areaCode) {
		Area data
			= areaService.getAreaByCode(areaCode);
		ResponseResult<Area> rr
			= new ResponseResult<Area>();
		rr.setData(data);
		return rr;
	}
	
}





