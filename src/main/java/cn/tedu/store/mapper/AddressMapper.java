package cn.tedu.store.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.tedu.store.entity.Address;

public interface AddressMapper {

	/**
	 * 新增收货地址
	 * @param address 收货地址数据
	 * @return 受影响的行数
	 */
	Integer insert(Address address);
	
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
	 * @param isDefault 是否默认，1表示默认，0表示非默认
	 * @return 受影响的行数
	 */
	Integer setDefault(
			@Param("uid") Integer uid, 
			@Param("id") Integer id, 
			@Param("isDefault") Integer isDefault);
	
	/**
	 * 根据id获取收货地址信息
	 * @param id 收货地址id
	 * @return 收货地址信息，如果没有匹配的数据，则返回null
	 */
	Address getAddressById(Integer id);

}





