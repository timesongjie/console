/**
 * 
 */
package com.bbkmobile.iqoo.common.util.cfgfile;

/**
 * 配置文件接口
 * 当前用到propertes类型配置文件
 * 以后若用到xml配置文件，可以实现该接口
 * @author wangbo
 */
public interface CfgFileInterface {

	/**
	 * 
	 * @return String
	 */
	public String getCfgFilePath();
	
	/**
	 * 
	 * @return String
	 */
	public String getValue(String key);
	
}
