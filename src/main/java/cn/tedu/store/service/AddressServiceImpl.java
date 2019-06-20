package cn.tedu.store.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.tedu.store.entity.Address;
import cn.tedu.store.mapper.AddressMapper;
import cn.tedu.store.service.ex.DataNotFoundException;
import cn.tedu.store.service.ex.InsertDataException;

@Service("addressService")
public class AddressServiceImpl implements IAddressService {
	
	@Autowired
	private AddressMapper addressMapper;
	
	@Autowired
	private IProvinceService provinceService;
	@Autowired
	private ICityService cityService;
	@Autowired
	private IAreaService areaService;

	public Address add(String username, Address address) {
		// 检查address中的uid和recvName是否为null
		// 决定当前收货地址是否为默认，
		// 如果该用户此前没有任何收货地址，此次即将增加的将是第1条，则是默认，
		// 否则，不是默认
		// address.setIsDefault(getAddressCountByUid(address.getUid()) == 0 ? 1 : 0);
		if (getAddressCountByUid(address.getUid()) == 0) {
			address.setIsDefault(1);
		} else {
			address.setIsDefault(0);
		}
		// 封装省市区的中文名，即recvDistrict
		String recvDistrict = getRecvDistrict(
				address.getRecvProvince(),
				address.getRecvCity(),
				address.getRecvArea());
		address.setRecvDistrict(recvDistrict);
		// 封装日志信息
		Date now = new Date();
		address.setCreatedUser(username);
		address.setModifiedUser(username);
		address.setCreatedTime(now);
		address.setModifiedTime(now);
		// 执行增加
		Integer rows = addressMapper.insert(address);
		// 判断持久层操作的返回值
		if (rows == 1) {
			// 增加成功，返回
			return address;
		} else {
			// 增加出错，抛出异常
			throw new InsertDataException("增加收货地址信息失败！" + address);
		}
	}

	/**
	 * 获取省市区的中文名称
	 * @param recvProvince 省的代号
	 * @param recvCity 市的代号
	 * @param recvArea 区的代号
	 * @return 省市区的中文名称·
	 */
	private String getRecvDistrict(
			String recvProvince, 
			String recvCity, 
			String recvArea) {
		String provinceName
			= provinceService
				.getProvinceByCode(recvProvince)
				.getName();
		String cityName
			= cityService.getCityByCode(recvCity).getName();
		String areaName
			= areaService.getAreaByCode(recvArea).getName();
		String district
			= provinceName + "," + cityName + "," + areaName;
		return district;
	}

	public List<Address> getAddressList(Integer uid) {
		return addressMapper.getAddressList(uid);
	}

	public Integer getAddressCountByUid(Integer uid) {
		return addressMapper.getAddressCountByUid(uid);
	}

	@Transactional
	public Integer setDefault(Integer uid, Integer id) {
		// 先将该用户的所有收货地址设置为非默认
		addressMapper.setDefault(uid, null, 0);
		// 再将指定的收货地址设置为默认
		Integer rows 
			= addressMapper.setDefault(uid, id, 1);
		// 判断操作是否成功
		if (rows == 1) {
			return 2;
		} else {
			throw new DataNotFoundException("尝试将id=" + id + "数据设置为默认收货地址，操作失败！");
		}
	}

	public Address getAddressById(Integer id) {
		return addressMapper.getAddressById(id);
	}

}






