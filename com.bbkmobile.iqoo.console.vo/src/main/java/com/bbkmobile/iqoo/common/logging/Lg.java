/**
 * 
 */
package com.bbkmobile.iqoo.common.logging;

import org.apache.commons.logging.LogFactory;


/**
 * @author wangbo
 *
 */
public class Lg {

//	static{
//		try{
//			
//			PropertyConfigurator.configure(ServerCfgFileInfo.getLog4jCfgFile());
////			PropertyConfigurator.configure("./src/log4j.properties");
//			
//		}catch(Exception e){
//			System.out.println("读取log4j配置文件出错！");
//			e.printStackTrace();
//		}
//		 
//	}
	
	
	/**
	 * 
	 * @param loggerName
	 * @param msg
	 */
	public static void debug(String loggerName, Object msg){
		
		LogFactory.getLog(loggerName).debug(msg);
	}
	
	/**
	 * 
	 * @param loggerName
	 * @param msg
	 * @param t
	 */
	public static void debug(String loggerName, Object msg, Throwable t){
		
		LogFactory.getLog(loggerName).debug(msg, t);
	}
	
	/**
	 * 
	 * @param cls
	 * @param msg
	 */
	public static <T> void debug(Class<T> cls, Object msg){
		
		debug(cls.getName(), msg);
	}
	
	/**
	 * 
	 * @param cls
	 * @param msg
	 * @param t
	 */
	public static <T> void debug(Class<T> cls, Object msg, Throwable t){
		
		debug(cls.getName(), msg, t);
	}
	
	/**
	 * 
	 * @param loggerName
	 * @param msg
	 */
	public static void info(String loggerName, Object msg){
		
		LogFactory.getLog(loggerName).info(msg);
	}
	
	/**
	 * 
	 * @param loggerName
	 * @param msg
	 * @param t
	 */
	public static void info(String loggerName, Object msg, Throwable t){
		
		LogFactory.getLog(loggerName).info(msg, t);
	}
	
	/**
	 * 
	 * @param cls
	 * @param msg
	 */
	public static <T> void info(Class<T> cls, Object msg){
		
		info(cls.getName(), msg);
	}
	
	/**
	 * 
	 * @param cls
	 * @param msg
	 * @param t
	 */
	public static <T> void info(Class<T> cls, Object msg, Throwable t){
		
		info(cls.getName(), msg, t);
	}
	
	/**
	 * 
	 * @param loggerName
	 * @param msg
	 */
	public static void warn(String loggerName, Object msg){
		
		LogFactory.getLog(loggerName).warn(msg);
	}
	
	/**
	 * 
	 * @param loggerName
	 * @param msg
	 * @param t
	 */
	public static void warn(String loggerName, Object msg, Throwable t){
		
		LogFactory.getLog(loggerName).warn(msg, t);
	}
	
	/**
	 * 
	 * @param cls
	 * @param msg
	 */
	public static <T> void warn(Class<T> cls, Object msg){
		
		warn(cls.getName(), msg);
	}
	
	/**
	 * 
	 * @param cls
	 * @param msg
	 * @param t
	 */
	public static <T> void warn(Class<T> cls, Object msg, Throwable t){
		
		warn(cls.getName(), msg, t);
	}
	
	/**
	 * 
	 * @param loggerName
	 * @param msg
	 */
	public static void error(String loggerName, Object msg){
		
		LogFactory.getLog(loggerName).error(msg);
	}
	
	/**
	 * 
	 * @param loggerName
	 * @param msg
	 * @param t
	 */
	public static void error(String loggerName, Object msg, Throwable t){
		
		LogFactory.getLog(loggerName).error(msg, t);
	}
	
	
	/**
	 * 
	 * @param cls
	 * @param msg
	 */
	public static <T> void error(Class<T> cls, Object msg){
		
		error(cls.getName(), msg);
	}
	
	/**
	 * 
	 * @param cls
	 * @param msg
	 * @param t
	 */
	public static <T> void error(Class<T> cls, Object msg, Throwable t){
		
		error(cls.getName(), msg, t);
	}
	
	
	/**
	 * 
	 * @param loggerName
	 * @param msg
	 */
	public static void fatal(String loggerName, Object msg){
		
		LogFactory.getLog(loggerName).fatal(msg);
	}
	
	/**
	 * 
	 * @param loggerName
	 * @param msg
	 * @param t
	 */
	public static void fatal(String loggerName, Object msg, Throwable t){
		
		LogFactory.getLog(loggerName).fatal(msg, t);
	}
	
	/**
	 * 
	 * @param cls
	 * @param msg
	 */
	public static <T> void fatal(Class<T> cls, Object msg){
		
		fatal(cls.getName(), msg);
	}
	
	/**
	 * 
	 * @param cls
	 * @param msg
	 * @param t
	 */
	public static <T> void fatal(Class<T> cls, Object msg, Throwable t){
		
		fatal(cls.getName(), msg, t);
	}
	
}