package cn.tedu.store.entity;

import java.io.Serializable;
import java.util.Date;

public class User implements Serializable {

	private static final long serialVersionUID = 4420588256084249458L;

	private Integer id;
	private String username;
	private String password;
	private Integer gender;
	private String phone;
	private String email;
	private String avatar;
	private String uuid;
	private String createdUser;
	private Date createdTime;
	private String modifiedUser;
	private Date modifiedTime;

	public User() {
		super();
	}

	public User(Integer id, String username, Integer gender, String phone, String email) {
		super();
		setId(id);
		setUsername(username);
		setGender(gender);
		setPhone(phone);
		setEmail(email);
	}

	public User(Integer id, String username, Integer gender, String phone, String email, String avatar) {
		super();
		setId(id);
		setUsername(username);
		setGender(gender);
		setPhone(phone);
		setEmail(email);
		setAvatar(avatar);
	}

	public User(String username, String password, Integer gender, String phone, String email) {
		super();
		setUsername(username);
		setPassword(password);
		setGender(gender);
		setPhone(phone);
		setEmail(email);
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Integer getGender() {
		return gender;
	}

	public void setGender(Integer gender) {
		this.gender = gender;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
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
		return "User [id=" + id + ", username=" + username + ", password=" + password + ", gender=" + gender
				+ ", phone=" + phone + ", email=" + email + ", avatar=" + avatar + ", uuid=" + uuid + ", createdUser="
				+ createdUser + ", createdTime=" + createdTime + ", modifiedUser=" + modifiedUser + ", modifiedTime="
				+ modifiedTime + "]";
	}

}
