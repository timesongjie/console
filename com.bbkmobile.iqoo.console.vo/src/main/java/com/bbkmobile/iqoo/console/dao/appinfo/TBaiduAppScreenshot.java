package com.bbkmobile.iqoo.console.dao.appinfo;

import java.util.Date;

// Generated 2012-1-9 9:11:11 by Hibernate Tools 3.4.0.CR1

/**
 * TAppScreenshot generated by hbm2java
 */
public class TBaiduAppScreenshot implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3686965506784243633L;
	private Long id;
	private BaiduAppInfo baiduAppInfo;
	private String screenshot;
	private Integer picorder;
	private Date add_date;

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public BaiduAppInfo getBaiduAppInfo() {
		return baiduAppInfo;
	}

	public void setBaiduAppInfo(BaiduAppInfo baiduAppInfo) {
		this.baiduAppInfo = baiduAppInfo;
	}

	public String getScreenshot() {
		return this.screenshot;
	}

	public void setScreenshot(String screenshot) {
		this.screenshot = screenshot;
	}

    public Date getAdd_date() {
        return this.add_date;
    }

    public void setAdd_date(Date add_date) {
        this.add_date = add_date;
    }

	public void setPicorder(Integer picorder) {
		this.picorder = picorder;
	}

	public Integer getPicorder() {
		return picorder;
	}

}
