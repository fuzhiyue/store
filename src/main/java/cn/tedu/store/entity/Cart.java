package cn.tedu.store.entity;

import java.io.Serializable;
import java.util.Date;

public class Cart implements Serializable {

	private static final long serialVersionUID = 265739145051208153L;

	private Integer id;
	private Integer uid;
	private Long goodsId;
	private String goodsImage;
	private String goodsTitle;
	private Long goodsPrice;
	private Integer goodsNum;
	private String createdUser;
	private Date createdTime;
	private String modifiedUser;
	private Date modifiedTime;

	public Cart() {
		super();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getUid() {
		return uid;
	}

	public void setUid(Integer uid) {
		this.uid = uid;
	}

	public Long getGoodsId() {
		return goodsId;
	}

	public void setGoodsId(Long goodsId) {
		this.goodsId = goodsId;
	}

	public String getGoodsImage() {
		return goodsImage;
	}

	public void setGoodsImage(String goodsImage) {
		this.goodsImage = goodsImage;
	}

	public String getGoodsTitle() {
		return goodsTitle;
	}

	public void setGoodsTitle(String goodsTitle) {
		this.goodsTitle = goodsTitle;
	}

	public Long getGoodsPrice() {
		return goodsPrice;
	}

	public void setGoodsPrice(Long goodsPrice) {
		this.goodsPrice = goodsPrice;
	}

	public Integer getGoodsNum() {
		return goodsNum;
	}

	public void setGoodsNum(Integer goodsNum) {
		this.goodsNum = goodsNum;
	}

	public String getCreatedUser() {
		return createdUser;
	}

	public void setCreatedUser(String createdUser) {
		this.createdUser = createdUser;
	}

	public Date getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}

	public String getModifiedUser() {
		return modifiedUser;
	}

	public void setModifiedUser(String modifiedUser) {
		this.modifiedUser = modifiedUser;
	}

	public Date getModifiedTime() {
		return modifiedTime;
	}

	public void setModifiedTime(Date modifiedTime) {
		this.modifiedTime = modifiedTime;
	}

	@Override
	public String toString() {
		return "Cart [id=" + id + ", uid=" + uid + ", goodsId=" + goodsId + ", goodsImage=" + goodsImage
				+ ", goodsTitle=" + goodsTitle + ", goodsPrice=" + goodsPrice + ", goodsNum=" + goodsNum
				+ ", createdUser=" + createdUser + ", createdTime=" + createdTime + ", modifiedUser=" + modifiedUser
				+ ", modifiedTime=" + modifiedTime + "]";
	}

}
