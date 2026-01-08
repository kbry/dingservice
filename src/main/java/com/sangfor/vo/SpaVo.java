package com.sangfor.vo;

import java.util.List;


// 名称	                类型	    是否必须	    默认值	    备注
// name	               string	    非必须		用户名
// displayName	       string	    非必须		用户显示名
// phone	           string	    非必须		手机号码
// email	           string	    非必须		邮箱
// userDirectoryName	string	     必须		用户所在目录名
// expiredTime	        string	    非必须	0:永不过期	过期时间，时间戳，10位长度，单位秒
// sendMode	            string[]	非必须	["sms"]	发送模式
// 使用说明：

// name/displayName/phone/email：四个属性至少传一个
// userDirectoryName： 用户所在目录名（ 必须传用户所在目录名）
// expiredTime： 过期时间，时间戳，10位长度，单位秒，默认值0（永不过期），如不传则重置时默认0
// sendMode： 发送模式：sms （短信或邮件发送）
public class SpaVo {
    private String name;

    private String displayName;

    private String phone;

    private String email;

    private String expiredTime;

    private String userDirectoryName;

    private List<String> sendMode;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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

    public String getExpiredTime() {
        return expiredTime;
    }

    public void setExpiredTime(String expiredTime) {
        this.expiredTime = expiredTime;
    }

    public String getUserDirectoryName() {
        return userDirectoryName;
    }

    public void setUserDirectoryName(String authComposeId) {
        this.userDirectoryName = authComposeId;
    }


    public List<String> getSendMode() {
        return sendMode;
    }

    public void setSendMode(List<String> roleNameList) {
        this.sendMode = roleNameList;
    }
}
