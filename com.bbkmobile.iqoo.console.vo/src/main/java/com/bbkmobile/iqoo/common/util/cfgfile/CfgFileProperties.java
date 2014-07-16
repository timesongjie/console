/**
 * 
 */
package com.bbkmobile.iqoo.common.util.cfgfile;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author wangbo
 */
public class CfgFileProperties implements CfgFileInterface {

	private Properties pro;
	private final Log log = LogFactory.getLog(CfgFileProperties.class);
	
	protected CfgFileProperties(){
		init();
	}
	
	/* (non-Javadoc)
	 * @see com.bbkmobile.iqoo.common.util.CfgFileInterface#getCfgFilePath()
	 */
	public String getCfgFilePath() {
		// TODO Auto-generated method stub
		return ServerCfgFileInfo.getPropertiesFilePath();
	}

	/* (non-Javadoc)
	 * @see com.bbkmobile.iqoo.common.util.CfgFileInterface#getValue()
	 */
	public String getValue(String key) {
		// TODO Auto-generated method stub
		return pro.getProperty(key);
	}

	private void init(){
		
		FileInputStream fis;
		
		try{
			fis = new FileInputStream(getCfgFilePath());
			pro = new Properties(); 
			pro.load(fis);
		}catch(IOException e){
			log.error(getCfgFilePath()+ "加载失败："+e);
		}
		
	}
	
}
