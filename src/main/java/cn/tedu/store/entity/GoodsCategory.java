package cn.tedu.store.entity;

import java.io.Serializable;
import java.util.Date;

public class GoodsCategory implements Serializable {

	private static final long serialVersionUID = 3551478342728450284L;

	private Long id;
	private Long parentId;
	private String name;
	private Integer status;
	private Integer sortOrder;
	private Integer isParent;
	private Date created_time;
	private Date modified_time;
	private String created_user;
	private String modified_user;

	public GoodsCategory() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(Integer sortOrder) {
		this.sortOrder = sortOrder;
	}

	public Integer getIsParent() {
		return isParent;
	}

	public void setIsParent(Integer isParent) {
		this.isParent = isParent;
	}

	public Date getCreated_time() {
		return created_time;
	}

	public void setCreated_time(Date created_time) {
		this.created_time = created_time;
	}

	public Date getModified_time() {
		return modified_time;
	}

	public void setModified_time(Date modified_time) {
		this.modified_time = modified_time;
	}

	public String getCreated_user() {
		return created_user;
	}

	public void setCreated_user(String created_user) {
		this.created_user = created_user;
	}

	public String getModified_user() {
		return modified_user;
	}

	public void setModified_user(String modified_user) {
		this.modified_user = modified_user;
	}

	@Override
	public String toString() {
		return "GoodsCategory [id=" + id + ", parentId=" + parentId + ", name=" + name + ", status=" + status
				+ ", sortOrder=" + sortOrder + ", isParent=" + isParent + ", created_time=" + created_time
				+ ", modified_time=" + modified_time + ", created_user=" + created_user + ", modified_user="
				+ modified_user + "]";
	}

}
