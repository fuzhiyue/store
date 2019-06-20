package cn.tedu.store.entity;

import java.io.Serializable;

public class City implements Serializable {

	private static final long serialVersionUID = 8246602759542065261L;

	private Integer id;
	private String provinceCode;
	private String name;
	private String code;

	public City() {
		super();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getProvinceCode() {
		return provinceCode;
	}

	public void setProvinceCode(String provinceCode) {
		this.provinceCode = provinceCode;
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
