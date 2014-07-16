package com.bbkmobile.iqoo.interfaces.advertisement.action;


import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.bbkmobile.iqoo.cache.CacheKeyGenerator;
import com.bbkmobile.iqoo.cache.CacheManager;
import com.bbkmobile.iqoo.common.json.NullResultObject;
import com.bbkmobile.iqoo.common.logging.Lg;
import com.bbkmobile.iqoo.common.page.PageVO;
import com.bbkmobile.iqoo.common.util.RequestUtil;
import com.bbkmobile.iqoo.console.constants.Constants;
import com.bbkmobile.iqoo.console.constants.LgType;
import com.bbkmobile.iqoo.console.dao.advertisement.Advertisement;
import com.bbkmobile.iqoo.console.dao.appinfo.AppInfo;
import com.bbkmobile.iqoo.console.dao.appinfo.RequestParameter;
import com.bbkmobile.iqoo.interfaces.advertisement.business.AdInfoService;
import com.bbkmobile.iqoo.interfaces.appinfo.business.AppInfoService;
import com.bbkmobile.iqoo.platform.base.BaseAction;

@Component("iAdvertisementAction")
@Scope("prototype")
public class IAdvertisementAction extends BaseAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8276679975824759097L;
	@Resource(name="iAdInfoService")
	private AdInfoService iAdInfoService;
	@Resource(name="iAppInfoService")
	private AppInfoService appInfoService;
	@Resource
	private CacheManager cacheManager;
	
	
	//begin: 手机接口@haiyan
	public String advertisings() throws Exception{
	    String model = null;
	    String cs = null;
	    String appVersion = null;
	    try {
            HttpServletRequest request = ServletActionContext.getRequest();
           
            model=request.getParameter("model");
            cs =request.getParameter("cs");
            appVersion = request.getParameter("app_version");
            Float version = null ;
            if(null!=appVersion && !appVersion.equals("")){
                version = Float.parseFloat(appVersion);
            }
            
            String key = null;
            String  data =null;
    	    String cacheName = "advertisement";
            if(Constants.USE_EHCACHE){
            	key = CacheKeyGenerator.key(cacheName, CacheKeyGenerator.csTStr(cs),appVersion,model);
	            String value = (String) cacheManager.getCache(cacheName).get(key);//(String) EhcacheUtil.getInstance().get(cacheName, key);
	            if(null!=value && !value.trim().equals("")){
	            	data = value;
	            }
            }
        	if(StringUtils.isBlank(data)){
        		if(version<Constants.APPVERSION530){
        			data = iAdInfoService.getPhoneAdvertisementListForXml(model,cs,version); 
        		}else{
        			data = iAdInfoService.getPhoneAdvertisementListForJSON(model,cs,version); 
        		}
        		if(Constants.USE_EHCACHE && StringUtils.isNotBlank(data)){
            		cacheManager.getCache(cacheName).put(key, data);
            	}
        	}
        	if(version<Constants.APPVERSION530){
        		outwrite(data, "text/xml;charset=utf-8");
        	}else{
        		outwrite(data, "text/plain;charset=utf-8");        		
        	}
        } catch (Exception e) {
            Lg.error(LgType.STDOUT, "手机获取广告信息出错，model="+model+",cs="+cs+",appVersion="+appVersion+",error=" + e.getMessage());
        	outwrite(NullResultObject.print(), "text/plain;charset=utf-8");
        } 
        return null;
	}
	public String advertisings_soft() throws Exception{
	       String ad_id = null; 
	       String model= null;
	       String cs = null;
	       String imei = null;
	       String page_index = null;
	       String appVersion = null;
	       try {
	            HttpServletRequest request = ServletActionContext.getRequest();
	            ad_id = request.getParameter("id");
	            String apps_per_page = getHttpServletRequest().getParameter("apps_per_page");
	            page_index = getHttpServletRequest().getParameter("page_index");
	            model = request.getParameter("model");
	            cs = request.getParameter("cs");
	            imei = request.getParameter("imei");
	            appVersion = request.getParameter("app_version");
	            
	            String ip = RequestUtil.getClientIP(request);
	            RequestParameter requestParameter = new RequestParameter();
	            requestParameter.setImei(imei);
	            requestParameter.setModel(model);
	            requestParameter.setIp(ip);
	            requestParameter.setCfrom(Constants.FROM_ADVERTISMENT);
	            requestParameter.setIdStr(ad_id);
	            requestParameter.setPageIndexStr(page_index);
	            requestParameter.setCsStr(cs);
	            requestParameter.setElapsedtime(request.getParameter("elapsedtime"));
	            
	            if(null!=appVersion && !appVersion.equals("")){
	                requestParameter.setApp_version(Float.parseFloat(appVersion));
	            }
	            String key = null;
	            String cacheName = "advertisings_soft";
	            String  data = null;
	            //get from cache
	            if(Constants.USE_EHCACHE && cacheManager != null){
	            	key = CacheKeyGenerator.key(cacheName, CacheKeyGenerator.csTStr(cs),appVersion,model,ad_id,page_index,apps_per_page);
	            	data = (String) cacheManager.getCache(cacheName).get(key);
	            }
	            if(StringUtils.isBlank(data)){
	            	if( requestParameter.getApp_version() >= Constants.APPVERSION530){
		            	Advertisement ad = new Advertisement();
		            	ad.setId(Long.valueOf(ad_id));
		            	ad.setAppInfo(new AppInfo());
		            	ad.getAppInfo().setFilter_model(model);
		            	PageVO page = new PageVO();
		            	page.setCurrentPageNum(Integer.valueOf(page_index));
		            	page.setNumPerPage(Integer.valueOf(apps_per_page));
		            	data = iAdInfoService.getPhoneAdvertisementAppListForJSON(ad, page, cs, appVersion);
		            }else{
		            	data = iAdInfoService.getPhoneAdvertisementAppListForXml(ad_id,apps_per_page,page_index,model,cs,appVersion); 
		            }
	            	//put into cache
	            	if(Constants.USE_EHCACHE && cacheManager != null && StringUtils.isNotBlank(data)){
	 	            	cacheManager.getCache(cacheName).put(key,data);
	 	            }
	            }
	            if(requestParameter.getApp_version() >= Constants.APPVERSION530){
	            	outwrite(data, "text/plain;charset=utf-8");
	            }else{
	            	outwrite(data, "text/xml;charset=utf-8");
	            }
	            
	            if(Constants.SAVE_BROWSE_LOG){
	                appInfoService.saveBrowseLog(requestParameter);
	            } 
	        } catch (Exception e) {
	            Lg.error(LgType.STDOUT, "手机获取广告相应的应用出错，" +
	            		"ad_id="+ad_id+
	            		 ",model=" + model + 
	                    ",cs=" + cs +
	                    ",appVersion=" + appVersion +
	                    ",page_index=" + page_index +
	                    ",error=" + e.getMessage());
	            outwrite(NullResultObject.print(), "text/plain;charset=utf-8");
	        } 
	        return null;
	    }
		
	public String getValidStartPage() throws Exception{
	        String model = null;
	        String cs = null;
	        String appVersion = null;
	        try {
	            HttpServletRequest request = ServletActionContext.getRequest();
	           
	            model=request.getParameter("model");
	            cs =request.getParameter("cs");
	            appVersion = request.getParameter("app_version");
	            float version = 0 ;
	            if(null!=appVersion && !appVersion.equals("")){
	                version = Float.parseFloat(appVersion);
	            }
	            //ip imei modle cs
	            RequestParameter requestParameter = new RequestParameter();
	            requestParameter.setApp_version(version);
	            
	            String key = null;
	            String cacheName = "startPage";
	            String  data = null;
	            //get from cache
	            if(Constants.USE_EHCACHE && cacheManager != null){
	            	key = CacheKeyGenerator.key(cacheName, CacheKeyGenerator.csTStr(cs),appVersion);
	            	data = (String) cacheManager.getCache(cacheName).get(key);
	            }
	            if(StringUtils.isBlank(data)){
	            	data = iAdInfoService.getValidStartPage(requestParameter); 
	            	
	            	if(Constants.USE_EHCACHE && cacheManager != null && StringUtils.isNotBlank(data)){
	            		cacheManager.getCache(cacheName).put(key, data);
	            	}
	            }
	            if(version >= Constants.APPVERSION530){
	            	outwrite(data, "text/plain;charset=utf-8");
	            }else{
	            	outwrite(data, "text/xml;charset=utf-8");
	            }
	            
	            if(Constants.SAVE_BROWSE_LOG){
	                appInfoService.saveBrowseLog(requestParameter);
	            } 
	        } catch (Exception e) {
	            Lg.error(LgType.STDOUT, "手机获取启动页信息出错，model="+model+",cs="+cs+",appVersion="+appVersion+",error=" + e.getMessage());
	            outwrite(NullResultObject.print(), "text/plain;charset=utf-8");
	        } 
	        return null;
	    }
}
