package com.oval.app.vo;

public class RegisterResultVO {
	
	
private String message;
private String username;
private String _id;
private String created;
private String approved;
private String device_type;
private String volume_id;
private String vm_ip_id;
private String vm_ip;
private String vm_id;
public String getUsername() {
	return username;
}

public void setUsername(String username) {
	this.username = username;
}

public String get_id() {
	return _id;
}

public void set_id(String _id) {
	this._id = _id;
}

public String getCreated() {
	return created;
}

public void setCreated(String created) {
	this.created = created;
}

public String getApproved() {
	return approved;
}

public void setApproved(String approved) {
	this.approved = approved;
}

public String getDevice_type() {
	return device_type;
}

public void setDevice_type(String device_type) {
	this.device_type = device_type;
}

public String getVolume_id() {
	return volume_id;
}

public void setVolume_id(String volume_id) {
	this.volume_id = volume_id;
}

public String getVm_ip_id() {
	return vm_ip_id;
}

public void setVm_ip_id(String vm_ip_id) {
	this.vm_ip_id = vm_ip_id;
}

public String getVm_ip() {
	return vm_ip;
}

public void setVm_ip(String vm_ip) {
	this.vm_ip = vm_ip;
}

public String getVm_id() {
	return vm_id;
}

public void setVm_id(String vm_id) {
	this.vm_id = vm_id;
}

public String getEmail() {
	return email;
}

public void setEmail(String email) {
	this.email = email;
}

public String getPassword_change_needed() {
	return password_change_needed;
}

public void setPassword_change_needed(String password_change_needed) {
	this.password_change_needed = password_change_needed;
}

private String email;
private String password_change_needed;
//Roles left


public String getMessage() {
	
	if(message==null)
	{
		return "";
	}
	return message;
}

public void setMessage(String message) {
	this.message = message;
}
}
