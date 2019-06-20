package cn.tedu.store.entity;

import java.io.Serializable;

public class Area implements Serializable {

	private static final long serialVersionUID = -8527529604910231475L;

	private Integer id;
	private String cityCode;
	private String name;
	private String code;

	public Area() {
		super();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getCityCode() {
		return cityCode;
	}

	public void setCityCode(String cityCode) {
		this.cityCode = cityCode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

}
