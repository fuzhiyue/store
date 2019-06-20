package cn.tedu.store.entity;

import java.io.Serializable;

public class Province implements Serializable {

	private static final long serialVersionUID = -9161344164507587377L;

	private Integer id;
	private String name;
	private String code;

	public Province() {
		super();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
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
