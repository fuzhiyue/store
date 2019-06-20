package cn.tedu.store.service;

import java.util.List;

import cn.tedu.store.entity.Address;
import cn.tedu.store.service.ex.InsertDataException;

public interface IAddressService {

	/**
	 * 增加新的收货地址
	 * @param username 当前登录的用户名
	 * @param address 收货地址数据
	 * @return 成功增加的收货地址数据，且包含数据的id
	 * @throws InsertDataException
	 */
	Address add(String username, Address address) throws InsertDataException;
	
	/**
	 * 获取某个用户的收货地址列表
	 * @param uid 用户的id
	 * @return 用户的收货地址列表
	 */
	List<Address> getAddressList(Integer uid);
	
	/**
	 * 获取某用户的收货地址数量
	 * @param uid 用户的id
	 * @return 用户的收货地址数量
	 */
	Integer getAddressCountByUid(Integer uid);
	
	/**
	 * 设置收货地址是否为默认
	 * @param uid 用户的id
	 * @param id 被修改的数据的id，如果为null，则该用户的所有收货地址将全部被修改
	 * @return 受影响的行数
	 */
	Integer setDefault(Integer uid, Integer id);
	
	/**
	 * 根据id获取收货地址信息
	 * @param id 收货地址id
	 * @return 收货地址信息，如果没有匹配的数据，则返回null
	 */
	Address getAddressById(Integer id);
}
