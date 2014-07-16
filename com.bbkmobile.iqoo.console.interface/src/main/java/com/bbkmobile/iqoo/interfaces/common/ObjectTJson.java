package com.bbkmobile.iqoo.interfaces.common;

import java.util.ArrayList;
import java.util.List;

import com.bbkmobile.iqoo.common.json.JackSonParser;
import com.bbkmobile.iqoo.common.json.ResultObject;
import com.bbkmobile.iqoo.common.search.SearchConstants;
import com.bbkmobile.iqoo.common.vo.RelationRecResultAppInfo;
import com.bbkmobile.iqoo.console.dao.appinfo.AppInfo;
import com.bbkmobile.iqoo.interfaces.appinfo.vo.AppInfoListVO;

public class ObjectTJson {
	
	public static String getAppsJSON(AppInfoListVO vo , String appVersion,String cFrom) throws Exception{
	        try { 
	            StringBuffer sb = new StringBuffer();
	            if(null != vo){
	            	 List<RelationRecResultAppInfo> value =appInfosTThis(vo.getAppInfoLs(), appVersion);
		             ResultObject<List<RelationRecResultAppInfo>> result = new ResultObject<List<RelationRecResultAppInfo>>();
		           	 result.setResult(true);
		           	 result.setValue(value);
		           	 result.setMaxPage(vo.getMaxPage());
		           	 result.setPageNo(vo.getPage_index());
		           	 result.setPageSize(vo.getApps_per_page());
		           	 
		           	 if(cFrom != null){
		           		 result.setFrom(cFrom);
		           	 }
		           	 vo.setTotal_count(vo.getTotal_count());
		           	 return JackSonParser.bean2Json(result); 
	            }
	            return sb.toString();
	        } catch (Exception e) {
	            throw e;
	        }
	    }
		/**
		 * 
		 * @param appInfos
		 * @param appVesion
		 * @param max 制定返回最大数目应用,并且不需要返回分页信息 -1 表示全返回 
		 * @return
		 * @throws Exception
		 */
	    public static String getAppsJSON(List<AppInfo> appInfos, String appVersion,int max) throws Exception{
	        try { 
	            StringBuffer sb = new StringBuffer();
	            if(null != appInfos){
	            	List<RelationRecResultAppInfo> value = new ArrayList<RelationRecResultAppInfo>();
	                 for (int i=0; i<appInfos.size(); i++) {
	                	value.add(appInfoTThis(appInfos.get(i), appVersion));
	                    if(max != -1 && ++i >= max){
	                        break;
	                    }
	                 } 
		             ResultObject<List<RelationRecResultAppInfo>> result = new ResultObject<List<RelationRecResultAppInfo>>();
		           	 result.setResult(true);
		           	 result.setValue(value);
		           	 return JackSonParser.bean2Json(result); 
	            }
	            return sb.toString();
	        } catch (Exception e) {
	            throw e;
	        }
	    }
	    public static List<RelationRecResultAppInfo> appInfosTThis(List<AppInfo> appInfos,String appVersion)throws Exception{
	    	List<RelationRecResultAppInfo> list = new ArrayList<RelationRecResultAppInfo>(appInfos.size());
	    	for(AppInfo appInfo : appInfos){
	    		RelationRecResultAppInfo rec = appInfoTThis(appInfo, appVersion);
	    		if(rec != null){
	    			list.add(rec);
	    		}
	    	}
	    	return list;
	    };
	    
	    
	    public static RelationRecResultAppInfo appInfoTThis(AppInfo appInfo, String appVersion) throws Exception{
	    	if(appInfo != null){
	    		RelationRecResultAppInfo info = new RelationRecResultAppInfo();
	    		info.setId(appInfo.getId());
	    		info.setPackage_name(appInfo.getAppPackage());
	    		info.setTitle_zh(appInfo.getAppCnName());
	    		info.setTitle_en(appInfo.getAppEnName());
	    		info.setIcon_url(appInfo.getAppIcon());
	    		info.setDeveloper(appInfo.getAppAuthor());
	    		info.setVersion_name(appInfo.getAppVersion());
	    		info.setVersion_code(appInfo.getAppVersionCode());
	    		info.setDownload_url(appInfo.getAppApk());
	    		info.setSize(appInfo.getApkSize());
	    		info.setDownload_count(appInfo.getDownloadCount());
	    		info.setOffical('0');
	    		info.setFrom(SearchConstants.FROM_LOCAL);
	    		return info;
	    	}
	    	return null;
	    }
	   
}
