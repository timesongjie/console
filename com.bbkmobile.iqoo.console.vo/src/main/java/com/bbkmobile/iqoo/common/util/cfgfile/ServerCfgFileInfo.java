/**
 * 
 */
package com.bbkmobile.iqoo.common.util.cfgfile;

import java.io.File;

/**
 * @author wangbo
 */
public class ServerCfgFileInfo {
	private static String USER_DIR =System.getProperty("console.root");

	private static String CFG_DIRECTORY_NAME = "config";

	
//	private static String USER_DIR = System.getProperty("user.dir");
	private static String FILE_SEPARATOR = File.separator;
//	private static String CFG_DIRECTORY_NAME = "config";
	private static String IQOO_PROP = "iqoo.properties";
	
	private static String CFG_BASE_PATH = USER_DIR + FILE_SEPARATOR + CFG_DIRECTORY_NAME + FILE_SEPARATOR;
	
	private static String LOG4J_CFG_FILE_NAME = "log4j.properties";
	
	private static String EHCACHE_CFG_FILE_NAME = "ehcache.xml";
	/**
	 * 返回 iqoo.properties 文件全路径
	 * 
	 * @return String 
	 */
	public static String getPropertiesFilePath(){
		
		return getCfgBasePath() + IQOO_PROP;
	}
	
	/**
	 * 返回用户工作目录
	 * 
	 * @return String
	 */
	public static String getUserDir(){
		
		return USER_DIR;
	}
	
	/**
	 * 返回路径分隔符
	 * @return String 
	 */
	public static String getFileSeparator(){
		
		return FILE_SEPARATOR;
	}
	
	/**
	 * 返回配置目录跟路径
	 * @return String
	 */
	public static String getCfgBasePath(){
		
		return CFG_BASE_PATH;
	}
	
	public static  String getEhcacheCfgFile(){
	    return getCfgBasePath() + EHCACHE_CFG_FILE_NAME;
	}
	
	/**
	 * 
	 * @return
	 */
	public static String getLog4jCfgFile(){
		
		return getCfgBasePath() + LOG4J_CFG_FILE_NAME;
	}
}
