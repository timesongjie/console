/**
 * 
 */
package com.bbkmobile.iqoo.interfaces.search.action;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.bbkmobile.iqoo.common.json.NullResultObject;
import com.bbkmobile.iqoo.interfaces.search.util.SearchSugService;
import com.bbkmobile.iqoo.platform.base.BaseStreamAction;

/**
 * @author wangbo
 *
 */
@Component("iSearchSugAction")
@Scope("prototype")
public class SearchSugAction extends BaseStreamAction {

	
	public void searchSug()throws Exception{
		
		try{
            //String reponse = "<response><statuscode>0</statuscode><statusmessage>done</statusmessage><sugs></sugs></response>";
            //write(reponse, "text/xml;charset=utf8");
			app_version = StringUtils.defaultIfEmpty(app_version, "500");
			if(StringUtils.isNumeric(app_version)){
				write(getSsServ().searchSugs(getWord(),Float.valueOf(app_version)), "text/plain;charset=utf8");
			}
			
		}catch(Exception e){
			e.printStackTrace();
			log.error(e.toString());
			outwrite(NullResultObject.print(), "text/plain;charset=utf-8");
		}
		
	}
	
	public String getImei() {
		return imei;
	}
	public void setImei(String imei) {
		this.imei = imei;
	}
	public String getModel() {
		return model;
	}
	public void setModel(String model) {
		this.model = model;
	}
	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}
	public SearchSugService getSsServ() {
		return ssServ;
	}
	public void setSsServ(SearchSugService ssServ) {
		this.ssServ = ssServ;
	}
	public String getApp_version() {
		return app_version;
	}
	public void setApp_version(String app_version) {
		this.app_version = app_version;
	}
	
	private String imei;// imei码
	private String model;// 机型号
	private String word;// 关键词
	private String app_version;
	
	@Resource(name="searchSugService")
	private SearchSugService ssServ;
	
	private Log log = LogFactory.getLog(SearchSugAction.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = -5879574825366808133L;
}
