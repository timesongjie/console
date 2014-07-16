/**
 * 
 */
package com.bbkmobile.iqoo.interfaces.search.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.bbkmobile.iqoo.common.logging.Lg;
import com.bbkmobile.iqoo.common.search.SearchConstants;
import com.bbkmobile.iqoo.console.business.search.xml.vo.AppVO;
import com.bbkmobile.iqoo.console.business.search.xml.vo.CateVO;
import com.bbkmobile.iqoo.console.business.search.xml.vo.ResultVO;
import com.bbkmobile.iqoo.console.business.search.xml.vo.SearchResultOneVO;
import com.bbkmobile.iqoo.console.business.search.xml.vo.SearchResultVO;
import com.bbkmobile.iqoo.console.business.search.xml.vo.UpdateResultVO;
import com.bbkmobile.iqoo.console.constants.Constants;
import com.bbkmobile.iqoo.console.constants.LgType;
import com.bbkmobile.iqoo.console.constants.UtilTool;
import com.bbkmobile.iqoo.console.dao.appinfo.AppInfo;
import com.bbkmobile.iqoo.console.dao.appinfo.BaiduAppId;
import com.bbkmobile.iqoo.console.dao.appinfo.RequestParameter;
import com.bbkmobile.iqoo.interfaces.appinfo.business.SystemPackageService;
import com.bbkmobile.iqoo.interfaces.appinfo.dao.AppInfoDAO;
import com.bbkmobile.iqoo.interfaces.appinfo.vo.AppInfoListVO;
import com.bbkmobile.iqoo.interfaces.appinfo.vo.SearchResultForm;


/**
 * @author wangbo
 *
 */

@Service("searchXmlProcessor")
@Scope("prototype")
public class SearchXmlProcessor {
    @Resource(name="iAppInfoDAO")
    private AppInfoDAO appInfoDAO;
    @Resource
	private SystemPackageService systemPackageService;
    
    private Log log = LogFactory.getLog(SearchXmlProcessor.class);

    //测试xml中是否包含无效字符
    public boolean CheckUnicodeString(String value) 
    {
        for (int i=0; i < value.length(); ++i) {  
            if (value.charAt(i) > 0xFFFD){  
                System.out.println("Invalid Unicode:" + value.charAt(i));  
                return false;  
            }  
            else if (value.charAt(i) < 0x20 && value.charAt(i) != '\t' && value.charAt(i) != '\n' && value.charAt(i) != '\r'){  
                System.out.println("Invalid Xml Characters:" + value.charAt(i));  
                return false;  
            }  
        }  
        return true;
    }

	public SearchResultVO processSearchXml(String xmlFromBaidu){
		SearchResultVO searchResult = null;
		StringReader sr = null;
		try{
			SAXReader reader = new SAXReader();
			//xmlFromBaidu = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>";
			//System.out.println("xmlFromBaidu="+xmlFromBaidu);
			if(null==xmlFromBaidu || xmlFromBaidu.equals("")){
			    return null;
			}
			sr = new StringReader(xmlFromBaidu);
			Document doc = reader.read(sr);
			Element root = doc.getRootElement();
			
			searchResult = parseSearchResult(root);
			
		}catch(Exception e){
			//e.printStackTrace();
		    Lg.error(LgType.STDOUT, "------process baidu search xml error");
		}finally{
			if(null != sr){
				sr.close();
			}
		}
		
		return searchResult;
	}

	public SearchResultOneVO processSearchOneXml(String xmlFromBaidu){
		
		SearchResultOneVO searchResult = null;
		StringReader sr = null;
		try{
			SAXReader reader = new SAXReader();
			
			sr = new StringReader(xmlFromBaidu);
			Document doc = reader.read(sr);
			Element root = doc.getRootElement();
			
			searchResult = parseSearchResultOne(root);
			
		}catch(Exception e){
			//e.printStackTrace();
		    Lg.error(LgType.STDOUT, "******process baidu app xml error");
		}finally{
			if(null != sr){
				sr.close();
			}
		}
		
		return searchResult;
	}
	
	public SearchResultOneVO parseSearchResultOne(Element root){
		SearchResultOneVO searchResult = new SearchResultOneVO();
		List<Element> ls = root.elements();
		
		for(Element ele:ls){
			if(SearchConstants.TAGS_RESPONSE_STATUSCODE.equals(ele.getName())){
				searchResult.setStatuscode(ele.getStringValue());
			}else if(SearchConstants.RESPONSE_STATUSMESSAGE.equals(ele.getName())){
				searchResult.setStatusmessage(ele.getStringValue());
			}else if(SearchConstants.RESPONSE_RESULT.equals(ele.getName())){
				List<Element> resultEleLs = ele.elements();
				if(resultEleLs.size()>0 && SearchConstants.APPS_APP.equals(resultEleLs.get(0).getName())){
					searchResult.setApp(parseApp(resultEleLs.get(0)));
				}
			}else{
				log.warn("未知标签: " + ele.getName());
			}
		}
		
		return searchResult;
	}
	
    public String downloadurl(String url) {
        String location=null;
        HttpClient httpClient = new HttpClient();
        //GetMethod method = null;
        PostMethod method = null;
       // httpClient.setConnectionTimeout(5000);
        int statusCode = 0;
        httpClient.getParams().setParameter(HttpMethodParams.USER_AGENT,
                "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/14.0.835.202 Safari/535.1");
        //postMethod.setRequestBody(data);输入参数
        int retry=0;
        
        for (retry = 0; retry < 3; retry++) {
            
            try {
                method = new PostMethod(url);
                method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler()); //使用系统提供的默认的恢复策略   
                statusCode = httpClient.executeMethod(method);
                Header locationHeader = method.getResponseHeader("location");
                if (locationHeader != null) {
                    location = locationHeader.getValue();
                    url=location;
                }                 
            } catch (HttpException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally{
                method.releaseConnection(); 
            }
            /*
            if (HttpStatus.SC_OK == statusCode) {
                Header locationHeader = method.getResponseHeader("location");
                if (locationHeader != null) {
                    location = locationHeader.getValue();
                } 
                break;
            }
            */
            /*
            if (statusCode == HttpStatus.SC_MOVED_PERMANENTLY || statusCode == HttpStatus.SC_MOVED_TEMPORARILY) {
                 // 从头中取出转向的地址
                 Header locationHeader = method.getResponseHeader("location");
                 if (locationHeader != null) {
                     location = locationHeader.getValue();
                 } 
             }
             */
        }
        return location;
    }

	public String createXmlOneForPhone(RequestParameter requestParameter,String xmlFromBaidu) {
	   
	    String noApp = "";
		SearchResultOneVO searchResult = processSearchOneXml(xmlFromBaidu);
		AppVO app = searchResult.getApp();
		if(app ==null){
			return "";
		}
		Document doc = DocumentHelper.createDocument();
		Element packageEle = doc.addElement("Package");
		packageEle.addAttribute("info", "1");
		packageEle.addAttribute("comment", "N");
		String url = app.getUrl();
		/*
		if(version<300){
		    
	        //if(!url.matches(".*\\.apk")){
	            String urlRedirect = downloadurl(app.getUrl());
	            //System.out.println("urlRedirect="+urlRedirect);
	            if(null!=urlRedirect){
	                url=urlRedirect;
	            }
	       // }
		} 
		*/
		String baidu_id = "";
		String  local_url = "";
		float version = requestParameter.getApp_version(); 
		if(version>=500){
		    baidu_id = app.getDocid();
		    local_url = app.getUrl();
		}else{
		    BaiduAppId baiduAppId = new BaiduAppId();
	        baiduAppId.setBaidu_id(app.getDocid());

            BaiduAppId baiduAppIdResult = null;
            try {
                baiduAppIdResult = appInfoDAO.getLocalBaiduAppId(baiduAppId);
                if(null!=baiduAppIdResult){
                    baidu_id = "-" + baiduAppIdResult.getId().toString(); // 使id变成负数
                }else{
                    noApp = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><Package info=\"0\" comment=\"N\"></Package>";
                    return noApp;
                }
                
                String baidu_url = baiduAppIdResult.getUrl();

                if (null == baidu_url) {
                    appInfoDAO.saveBaiduAppUrl(baiduAppIdResult.getId(), url);
                } else if (!baiduAppIdResult.getUrl().equals(url)) {
                    appInfoDAO.saveBaiduAppUrl(baiduAppIdResult.getId(), url);
                }
            } catch (Exception e) {
                //e.printStackTrace();
                return noApp;
            }
            local_url = UtilTool.getHttpURL("/appinfo/downloadApkFile?id=" + baidu_id+"&app_version="+requestParameter.getApp_version());
		}
		
		String offical = "0";
		/*
		if(null!=app.getAuthentication() && "y".equalsIgnoreCase(app.getAuthentication())){
		    offical ="1"; 
		}
		*/
		packageEle.addElement("patchs").setText("");
		packageEle.addElement("offical").setText(offical);

		packageEle.addElement("download_url").setText(local_url);
		//packageEle.addElement("download_url").setText(url);
		packageEle.addElement("upload_time").setText(app.getReleasedate());
		packageEle.addElement("size").setText(app.getPackagesize()/1024 + "");

        if(null==app.getScore()){            
            //packageEle.addElement("score").setText(0/20f + "");
        }else{
            packageEle.addElement("score").setText(Integer.parseInt(app.getScore())/20f + "");
        }
		
        String introduction=app.getDescription()==null?"":app.getDescription();
        
        if(version<400){
            introduction=Constants.BAIDU_APP_DISCLAIMER+introduction; 
        }else {
            introduction=Constants.BAIDU_APP_DISCLAIMER_FORMAT+introduction; 
        }
        
		packageEle.addElement("introduction").setText(introduction);
		
		if(null!=requestParameter.getContent_complete() && requestParameter.getContent_complete().equals("1")){   //补全详细 
            AppendAppInfoContentComplete(app, packageEle);
        }

		Element screenshotListEle = packageEle.addElement("ScreenshotList");
		screenshotListEle.addElement("screenshot").setText(app.getBigmaplink1()==null?"":app.getBigmaplink1());
		screenshotListEle.addElement("screenshot").setText(app.getBigmaplink2()==null?"":app.getBigmaplink2());
//		screenshotListEle.addElement("screenshot").setText(app.getSmallmaplink1()==null?"":app.getSmallmaplink1());
//		screenshotListEle.addElement("screenshot").setText(app.getSmallmaplink2()==null?"":app.getSmallmaplink2());
		
		Element permissionListEle = packageEle.addElement("PermissionList");
		permissionListEle.addElement("permission");//需要完善？
		
		packageEle.addElement("CommentList");//需要完善？
		
		packageEle.addElement("download_count").setText(app.getDownload_count()+"");
		
		return doc.asXML();
	}
	
	public AppVO getBaiduAppVO(String xmlFromBaidu) {   
        SearchResultOneVO searchResult = processSearchOneXml(xmlFromBaidu);
        AppVO app = searchResult.getApp();
        return app;
    }
	
	public void AppendAppInfoContentComplete(AppVO app,Element packageEle){
	    packageEle.addElement("package_name").setText(app.getPackagename());
	    packageEle.addElement("parent_id").setText("null");
	    packageEle.addElement("title_zh").setText(app.getSname());
	    packageEle.addElement("title_en").setText(app.getSname());
	    packageEle.addElement("icon_url").setText(app.getIconhdpi());
	    packageEle.addElement("developer").setText(SearchConstants.DEVELOPER_FROM_NETWORK);
	    packageEle.addElement("raters_count").setText(app.getScore_count() + "");
        packageEle.addElement("version_name").setText(app.getVersionname());
        packageEle.addElement("version_code").setText(app.getVersioncode() + "");
        packageEle.addElement("download_count").setText(app.getDownload_count());
	}
	
    public String createXmlForPhone2(SearchResultForm requestParas, AppInfoListVO vo,String xmlFromBaidu) throws Exception{
        
        int total_count = vo.getTotal_count();
        int apps_per_page = vo.getApps_per_page();
        
        int page_index = total_count/apps_per_page + 1;
        int vo_maxpage = (int)Math.ceil((float)total_count/apps_per_page);
        
        SearchResultVO searchResult = processSearchXml(xmlFromBaidu);
       
        int count = searchResult.getResult().getDisp_num();
        int numPerPage = searchResult.getResult().getRn();
        int pageNum = searchResult.getResult().getPn();

        Document doc = DocumentHelper.createDocument();
        Element packageListEle = doc.addElement("PackageList");
        
        if(0==vo_maxpage){
            packageListEle.addAttribute("maxpage", ((count + numPerPage - 1) / numPerPage) + "");
        }else{
            packageListEle.addAttribute("maxpage", ((count + numPerPage - 1) / numPerPage) + vo_maxpage - 1 + "");
        }
        packageListEle.addAttribute("PageNo", (pageNum / numPerPage + 1) + page_index - 1 + "");
        packageListEle.addAttribute("pageSize", numPerPage + "");
        packageListEle.addAttribute("TotalCount", count + total_count + "");
        packageListEle.addAttribute("from", SearchConstants.FROM_BAIDU);

        //Element packageEle = null;
        //String baidu_id = null;
        
        for (AppVO app : searchResult.getResult().getApps()) {
            addBaiduAppXml(requestParas, app, packageListEle);
        }
        return doc.asXML();
    } 
	
    private void addBaiduAppXml(SearchResultForm requestParas, AppVO app, Element packageListEle) throws Exception {
        Element appEle = null;
        AppInfo appInfo = null;
		requestParas.setAppPackageName(app.getPackagename());
		if (systemPackageService.isSystemPackage(app.getPackagename()) || (appInfo = appInfoDAO
				.getAppInfoByPackageName(requestParas)) != null ) {//by time
            if (null != appInfo && appInfo.getAppStatus() != Constants.APP_OFFSALE) {
                appEle = packageListEle.addElement("Package");
                appEle.addElement("id").setText(appInfo.getId() + "");
                appEle.addElement("tag").setText(appInfo.getTag() + "");
                appEle.addElement("package_name").setText(appInfo.getAppPackage());
                appEle.addElement("parent_id").setText("null");
                appEle.addElement("title_zh").setText(appInfo.getAppCnName());
                appEle.addElement("title_en").setText(appInfo.getAppCnName());
                // appEle.addElement("icon_url").setText(appInfo.getAppIcon());
                appEle.addElement("icon_url").setText(UtilTool.getDownloadImageHttpURL(appInfo.getAppIcon()));
                // appEle.addElement("developer").setText(appInfo.getAppAuthor()==null?"":appInfo.getAppAuthor());
                appEle.addElement("developer").setText(appInfo.getAppAuthor() == null ? "" : appInfo.getAppAuthor());
                appEle.addElement("score").setText(appInfo.getAvgComment() + "");
                appEle.addElement("raters_count").setText(appInfo.getCommentCount() + "");
                appEle.addElement("version_name").setText(appInfo.getAppVersion()==null?"":appInfo.getAppVersion());
                appEle.addElement("version_code").setText(appInfo.getAppVersionCode());
                appEle.addElement("download_url").setText(
                        UtilTool.getHttpURL("/appinfo/downloadApkFile?id=" + appInfo.getId() + "&app_version="
                                + requestParas.getVersion()));
                appEle.addElement("size").setText(appInfo.getApkSize() + "");
                appEle.addElement("download_count").setText(appInfo.getDownloadCount() + "");
                appEle.addElement("patchs").setText(appInfo.getPatchs() == null ? "" : appInfo.getPatchs());
                appEle.addElement("from").setText(SearchConstants.FROM_LOCAL);
                appEle.addElement("offical").setText("0");

            }
        } else {
            String baidu_id = null;
            if (requestParas.getVersion() >= 500) { // 直接返回百度id
                baidu_id = app.getDocid();
            } else {
                BaiduAppId baiduAppId = new BaiduAppId();
                baiduAppId.setBaidu_id(app.getDocid());
                BaiduAppId baiduAppIdResult = null;
                baiduAppIdResult = appInfoDAO.getLocalBaiduAppId(baiduAppId);

                if (null == baiduAppIdResult) {
                    baiduAppIdResult = appInfoDAO.saveBaiduAppId(baiduAppId);
                }
                baidu_id = "-" + baiduAppIdResult.getId().toString(); // 使id变成负数
            }

            appEle = packageListEle.addElement("Package");
            // appEle.addElement("id").setText(app.getDocid());
            appEle.addElement("id").setText(baidu_id);
            appEle.addElement("package_name").setText(app.getPackagename());
            appEle.addElement("parent_id").setText("null");
            appEle.addElement("title_zh").setText(app.getSname());
            appEle.addElement("title_en").setText(app.getSname());
            appEle.addElement("icon_url").setText(app.getIconhdpi());
            appEle.addElement("developer").setText(SearchConstants.DEVELOPER_FROM_NETWORK);
            appEle.addElement("score").setText(Float.parseFloat(app.getScore()) / 20 + "");
            appEle.addElement("raters_count").setText(app.getScore_count());
            appEle.addElement("version_name").setText(app.getVersionname());
            appEle.addElement("version_code").setText(app.getVersioncode() + "");
            appEle.addElement("download_url").setText(app.getUrl());
            appEle.addElement("size").setText(app.getPackagesize() / 1024 + "");
            appEle.addElement("download_count").setText(app.getDownload_count() + "");
            appEle.addElement("from").setText(SearchConstants.FROM_BAIDU);
            String offical = "0";

            appEle.addElement("offical").setText(offical);
        }
    }
	/*
	public String createXmlForPhone2(SearchResultForm requestParas, AppInfoListVO vo,String xmlFromBaidu) throws Exception{
        
        int total_count = vo.getTotal_count();
        int apps_per_page = vo.getApps_per_page();
        int page_index = total_count/apps_per_page + 1;
        int  vo_maxpage = (int)Math.ceil((float)total_count/apps_per_page);
        
        SearchResultVO searchResult = processSearchXml(xmlFromBaidu);
       
        int count = searchResult.getResult().getDisp_num();
        int numPerPage = searchResult.getResult().getRn();
        int pageNum = searchResult.getResult().getPn();

        Document doc = DocumentHelper.createDocument();
        Element packageListEle = doc.addElement("PackageList");
        
        if(requestParas.getVersion()>=300){                        //if version>=300,返回页码和app总数重新按百度开始（现在不跑这个流程），else,返回本地数据+百度数据
          packageListEle.addAttribute("maxpage", ((count + numPerPage -1)/numPerPage) + "");
          packageListEle.addAttribute("PageNo", (pageNum/numPerPage + 1) + "");
          packageListEle.addAttribute("pageSize", numPerPage + "");
          packageListEle.addAttribute("TotalCount", count + "");
          packageListEle.addAttribute("from", SearchConstants.FROM_BAIDU);
        }
        else{
            if(0==vo_maxpage){
                packageListEle.addAttribute("maxpage", ((count + numPerPage - 1) / numPerPage) + "");
            }else{
                packageListEle.addAttribute("maxpage", ((count + numPerPage - 1) / numPerPage) + vo_maxpage - 1 + "");
            }
            packageListEle.addAttribute("PageNo", (pageNum / numPerPage + 1) + page_index - 1 + "");
            packageListEle.addAttribute("pageSize", numPerPage + "");
            packageListEle.addAttribute("TotalCount", count + total_count + "");
            packageListEle.addAttribute("from", SearchConstants.FROM_BAIDU);
            
        }
        Element packageEle = null;
        String baidu_id = null;
        for (AppVO app : searchResult.getResult().getApps()) {
            
            //屏蔽来自mumayi应用市场的app
            //boolean isNotMumayi = true;
            //if(null!=app.getUrl() && app.getUrl().matches(".*mumayi.*")){
            //   isNotMumayi=false;
            //}
            
            
            if (!appInfoDAO.packageIsExist(app.getPackagename())){
                
                if(requestParas.getVersion()>=500){   //直接返回百度id
                    baidu_id=app.getDocid(); 
                }else{
                    BaiduAppId baiduAppId = new BaiduAppId();
                    baiduAppId.setBaidu_id(app.getDocid());
                    
                    BaiduAppId baiduAppIdResult = null;

                    baiduAppIdResult = appInfoDAO.getLocalBaiduAppId(baiduAppId);
                    
                    if(null==baiduAppIdResult){
                        baiduAppIdResult = appInfoDAO.saveBaiduAppId(baiduAppId);
                    }
                    baidu_id = "-"+baiduAppIdResult.getId().toString();   //使id变成负数
                }
                packageEle = packageListEle.addElement("Package");
                //packageEle.addElement("id").setText(app.getDocid());
                packageEle.addElement("id").setText(baidu_id);
                
                packageEle.addElement("package_name").setText(app.getPackagename());
                //packageEle.addElement("parent_id");
                packageEle.addElement("parent_id").setText("null");
                packageEle.addElement("title_zh").setText(app.getSname());
                packageEle.addElement("title_en").setText(app.getSname());
                packageEle.addElement("icon_url").setText(app.getIconhdpi());
                // packageEle.addElement("icon_url").setText(UtilTool.getDownloadHttpURL(app.getIcon()));

                packageEle.addElement("developer").setText(SearchConstants.DEVELOPER_FROM_NETWORK);
                packageEle.addElement("score").setText((Integer.parseInt(app.getScore()) / 20f) + "");
                packageEle.addElement("raters_count").setText(app.getScore_count() + "");
                packageEle.addElement("version_name").setText(app.getVersionname());
                packageEle.addElement("version_code").setText(app.getVersioncode() + "");
                packageEle.addElement("download_url").setText(app.getUrl());
                packageEle.addElement("size").setText(app.getPackagesize()/1024 + "");
                packageEle.addElement("download_count").setText(app.getDownload_count());
                packageEle.addElement("from").setText(SearchConstants.FROM_BAIDU);
                String offical = "0";
     
                //if(null!=app.getAuthentication() && "y".equalsIgnoreCase(app.getAuthentication())){
                //   offical ="1"; 
                //}
                
                packageEle.addElement("offical").setText(offical);
            }
        }

        return doc.asXML();
    } 
    */
	
    /*
	public String createXmlForPC(SearchResultForm requestParas, AppInfoListVO vo,String xmlFromBaidu) throws Exception{
        
        int total_count = vo.getTotal_count();
        int apps_per_page = vo.getApps_per_page();
        int page_index = total_count/apps_per_page + 1;
        int  vo_maxpage = (int)Math.ceil((float)total_count/apps_per_page);
        
        SearchResultVO searchResult = processSearchXml(xmlFromBaidu);
       
        int count = searchResult.getResult().getDisp_num();
        int numPerPage = searchResult.getResult().getRn();
        int pageNum = searchResult.getResult().getPn();

        Document doc = DocumentHelper.createDocument();
        Element packageListEle = doc.addElement("PackageList");

        packageListEle.addAttribute("maxpage", ((count + numPerPage - 1) / numPerPage) + "");

        packageListEle.addAttribute("PageNo", (pageNum / numPerPage + 1) + page_index - 1 + "");
        packageListEle.addAttribute("pageSize", numPerPage + "");
        packageListEle.addAttribute("TotalCount", count + "");
        packageListEle.addAttribute("from", SearchConstants.FROM_BAIDU);

        Element packageEle = null;
        String baidu_id = null;
        for (AppVO app : searchResult.getResult().getApps()) {
    
            if (!appInfoDAO.packageIsExist(app.getPackagename())){
                
                if(requestParas.getVersion()>=500){   //直接返回百度id
                    baidu_id=app.getDocid(); 
                }else{
                    BaiduAppId baiduAppId = new BaiduAppId();
                    baiduAppId.setBaidu_id(app.getDocid());
                    BaiduAppId baiduAppIdResult = null;

                    baiduAppIdResult = appInfoDAO.getLocalBaiduAppId(baiduAppId);
                    
                    if(null==baiduAppIdResult){
                        baiduAppIdResult = appInfoDAO.saveBaiduAppId(baiduAppId);
                    }
                    baidu_id = "-"+baiduAppIdResult.getId().toString();   //使id变成负数
                }

                packageEle = packageListEle.addElement("Package");
                //packageEle.addElement("id").setText(app.getDocid());
                packageEle.addElement("id").setText(baidu_id);

                packageEle.addElement("package_name").setText(app.getPackagename());
                //packageEle.addElement("parent_id");
                packageEle.addElement("parent_id").setText("null");
                packageEle.addElement("title_zh").setText(app.getSname());
                packageEle.addElement("title_en").setText(app.getSname());
                packageEle.addElement("icon_url").setText(app.getIconhdpi());
                // packageEle.addElement("icon_url").setText(UtilTool.getDownloadHttpURL(app.getIcon()));

                packageEle.addElement("developer").setText(SearchConstants.DEVELOPER_FROM_NETWORK);
                packageEle.addElement("score").setText((Integer.parseInt(app.getScore()) / 20f) + "");
                packageEle.addElement("raters_count").setText(app.getScore_count() + "");
                packageEle.addElement("version_name").setText(app.getVersionname());
                packageEle.addElement("version_code").setText(app.getVersioncode() + "");
                packageEle.addElement("download_url").setText(app.getUrl());
                packageEle.addElement("size").setText(app.getPackagesize()/1024 + "");
                packageEle.addElement("from").setText(SearchConstants.FROM_BAIDU);
                String offical = "0";
                packageEle.addElement("offical").setText(offical);
            }
        }

        return doc.asXML();
    } 
	*/
	public String createXmlForPhone(SearchResultForm requestParas, String xmlFromBaidu) throws Exception{
		SearchResultVO searchResult = processSearchXml(xmlFromBaidu);
		int count = searchResult.getResult().getDisp_num();
		int numPerPage = searchResult.getResult().getRn();
		int pageNum = searchResult.getResult().getPn();
		
		
		Document doc = DocumentHelper.createDocument();
		Element packageListEle = doc.addElement("PackageList");
		
		packageListEle.addAttribute("maxpage", ((count + numPerPage -1)/numPerPage) + "");
		packageListEle.addAttribute("PageNo", (pageNum/numPerPage + 1) + "");
		packageListEle.addAttribute("pageSize", numPerPage + "");
		packageListEle.addAttribute("TotalCount", count + "");
		packageListEle.addAttribute("from", SearchConstants.FROM_BAIDU);
		
        for (AppVO app : searchResult.getResult().getApps()) {
            addBaiduAppXml(requestParas, app, packageListEle);
        }

		return doc.asXML();
	}
	
	public SearchResultVO parseSearchResult(Element root){
		
		SearchResultVO searchResult = new SearchResultVO();
		List<Element> ls = root.elements();
		
		for(Element ele:ls){
			if(SearchConstants.TAGS_RESPONSE_STATUSCODE.equals(ele.getName())){
				searchResult.setStatuscode(ele.getStringValue());
			}else if(SearchConstants.RESPONSE_STATUSMESSAGE.equals(ele.getName())){
				searchResult.setStatusmessage(ele.getStringValue());
			}else if(SearchConstants.RESPONSE_RESULT.equals(ele.getName())){
				searchResult.setResult(parseResult(ele));
			}else{
				log.warn("未知标签: " + ele.getName());
			}
		}
		
		return searchResult;
	}
	
	private ResultVO parseResult(Element ele){
		ResultVO result = new ResultVO();
		List<Element> ls = ele.elements();
		
		for(Element el:ls){
			if(SearchConstants.RESULT_RN.equals(el.getName())){
				result.setRn(Integer.parseInt(el.getStringValue()));
			}else if(SearchConstants.RESULT_PN.equals(el.getName())){
				result.setPn(Integer.parseInt(el.getStringValue()));
			}else if(SearchConstants.RESULT_DISPNUM.equals(el.getName())){
				result.setDisp_num(Integer.parseInt(el.getStringValue()));   //这个值有时候为null，需要处理
			}else if(SearchConstants.RESULT_RETNUM.equals(el.getName())){
				result.setRet_num(Integer.parseInt(el.getStringValue()));
			}else if(SearchConstants.RESULT_APPS.equals(el.getName())){
				result.setApps(parseApps(el));
			}else if(SearchConstants.RESULT_TITLE.equals(el.getName())){
                result.setTitle(el.getStringValue());
            }else{                                                                    
				log.warn("未知标签: " + el.getName());
			}
		}
		
		return result;
	}
	
	private List<AppVO> parseApps(Element ele){
		List<AppVO> voLs = new ArrayList<AppVO>();
		List<Element> ls = ele.elements();
		
		for(Element el:ls){
			if(SearchConstants.APPS_APP.equals(el.getName())){
				voLs.add(parseApp(el));
			}else{
				log.warn("未知标签: " + el.getName());
			}
		}
		
		
		return voLs;
	}
	
	public AppVO parseApp(Element ele){
		AppVO vo = new AppVO();
		List<Element> ls = ele.elements();
		
		for(Element el:ls){
			if(SearchConstants.APP_SNAME.equals(el.getName())){
				vo.setSname(el.getStringValue());
			}else if(SearchConstants.APP_BIGMAPLINK1.equals(el.getName())){
				vo.setBigmaplink1(el.getStringValue());
			}else if(SearchConstants.APP_BIGMAPLINK2.equals(el.getName())){
				vo.setBigmaplink2(el.getStringValue());
			}else if(SearchConstants.APP_CATEID.equals(el.getName())){
				vo.setCateid(el.getStringValue());
			}else if(SearchConstants.APP_CATENAME.equals(el.getName())){
				vo.setCatename(el.getStringValue());
			}else if(SearchConstants.APP_DESCRIPTION.equals(el.getName())){
				vo.setDescription(el.getStringValue());
			}else if(SearchConstants.APP_DOCID.equals(el.getName())){
				vo.setDocid(el.getStringValue());
			}else if(SearchConstants.APP_FEE.equals(el.getName())){
				vo.setFee(el.getStringValue());
			}else if(SearchConstants.APP_ICON.equals(el.getName())){
				vo.setIcon(el.getStringValue());
			}else if(SearchConstants.APP_DEVELOPERNAME.equals(el.getName())){
                vo.setDevelopername(el.getStringValue());
            }else if(SearchConstants.APP_AUTHENTICATION.equals(el.getName())){
                vo.setAuthentication(el.getStringValue());
            }else if(SearchConstants.APP_LANGUAGE.equals(el.getName())){
				vo.setLanguage(el.getStringValue());
			}else if(SearchConstants.APP_PACKAGE.equals(el.getName())){
				vo.setPackagename(el.getStringValue());
			}else if(SearchConstants.APP_PACKAGEFORMAT.equals(el.getName())){
				vo.setPackageformat(el.getStringValue());
			}else if(SearchConstants.APP_PACKAGESIZE.equals(el.getName())){
				if(null != el.getStringValue() && !el.getStringValue().equals("")){
					vo.setPackagesize(Long.parseLong(el.getStringValue()));
				}else{
					log.warn(el.getName() + "= 空");
				}
			}else if(SearchConstants.APP_PERMISSIONCN.equals(el.getName())){
				vo.setPermission_cn(el.getStringValue());
			}else if(SearchConstants.APP_PLATFORM.equals(el.getName())){
				vo.setPlatform(el.getStringValue());
			}else if(SearchConstants.APP_RELEASEDATE.equals(el.getName())){
				vo.setReleasedate(el.getStringValue());
			}else if(SearchConstants.APP_SCORE.equals(el.getName())){
				vo.setScore(el.getStringValue());
			}else if(SearchConstants.APP_SCORECOUNT.equals(el.getName())){
				vo.setScore_count(el.getStringValue());
			}else if(SearchConstants.APP_SITE.equals(el.getName())){
				vo.setSite(el.getStringValue());
			}else if(SearchConstants.APP_SMALLMAPLINK1.equals(el.getName())){
				vo.setSmallmaplink1(el.getStringValue());
			}else if(SearchConstants.APP_SMALLMAPLINK2.equals(el.getName())){
				vo.setSmallmaplink2(el.getStringValue());
			}else if(SearchConstants.APP_TYPE.equals(el.getName())){
				vo.setType(el.getStringValue());
			}else if(SearchConstants.APP_URL.equals(el.getName())){
				//vo.setUrl(el.getStringValue().replace(" ", "%20"));
			    vo.setUrl(el.getStringValue());
			}else if(SearchConstants.APP_VERSIONCODE.equals(el.getName())){
				vo.setVersioncode(Integer.parseInt(el.getStringValue()));
			}else if(SearchConstants.APP_VERSIONNAME.equals(el.getName())){
				vo.setVersionname(el.getStringValue());
			}else if(SearchConstants.APP_DOWNLAD_COUNT.equals(el.getName())){
                vo.setDownload_count(el.getStringValue());
            }else if(SearchConstants.APP_ICON_LOW.equals(el.getName())){
                vo.setIconlow(el.getStringValue());
            } else if (SearchConstants.APP_ICON_HIGH.equals(el.getName())) {
                vo.setIconhigh(el.getStringValue());
            } else if (SearchConstants.APP_ICON_hdpi.equals(el.getName())) {
                vo.setIconhdpi(el.getStringValue());
            } else if (SearchConstants.APP_ICON_ALADING.equals(el.getName())) {
                vo.setIconalading(el.getStringValue());
            } else if (SearchConstants.APP_ADAPI.equals(el.getName())) {
                vo.setAdapi(el.getStringValue());
            } else if (SearchConstants.APP_SIZE.equals(el.getName())) {
                vo.setSize(el.getStringValue());
            } else if (SearchConstants.APP_UPDATETIME.equals(el.getName())) {
                vo.setUpdatetime(el.getStringValue());
            } else if (SearchConstants.APP_SIGNMD5.equals(el.getName())) {
                vo.setSignmd5(el.getStringValue());
            } else {
                log.warn("未知标签: " + el.getName());
            }
		}
		
		return vo;
	}
	
	//update app
	public UpdateResultVO processUpdateXml(String xmlFromUpdate) throws Exception{
	    UpdateResultVO updateResultVO = null;
        StringReader sr = null;
        try{
            SAXReader reader = new SAXReader();
            
            sr = new StringReader(xmlFromUpdate);
            Document doc = reader.read(sr);
            Element root = doc.getRootElement();
            
            updateResultVO = parseUpdateResult(root);
            
        }catch(Exception e){
            //e.printStackTrace();
            throw e;
        }finally{
            if(null != sr){
                sr.close();
            }
        }
        
        return updateResultVO;
    }

    public UpdateResultVO parseUpdateResult(Element root) {

        UpdateResultVO updateResultVO = new UpdateResultVO();
        List<Element> ls = root.elements();

        for (Element ele : ls) {
            if (SearchConstants.TAGS_RESPONSE_STATUSCODE.equals(ele.getName())) {
                updateResultVO.setStatuscode(ele.getStringValue());
            } else if (SearchConstants.RESPONSE_STATUSMESSAGE.equals(ele.getName())) {
                updateResultVO.setStatusmessage(ele.getStringValue());
            } else if (SearchConstants.RESPONSE_RESULT.equals(ele.getName())) {
                //searchResult.setResult(parseResult(ele));
                List<Element> elements = ele.elements();
                
                for(Element el:elements){
                   if(SearchConstants.RESULT_APPS.equals(el.getName())){
                       updateResultVO.setApps(parseApps(el));
                    }else{                                                                    
                        log.warn("未知标签: " + el.getName());
                    }
                }
            } else {
                log.warn("未知标签: " + ele.getName());
            }
        }

        return updateResultVO;
    }
	
    
   //related recommend
    public UpdateResultVO processRelatedRecXml(String relateRecXml) throws Exception{
        UpdateResultVO relateRecVO = null;
        StringReader sr = null;
        try{
            if(null==relateRecXml || relateRecXml.equals("")){
                return null;
            }
            SAXReader reader = new SAXReader();
            sr = new StringReader(relateRecXml);
            Document doc = reader.read(sr);
            Element root = doc.getRootElement();
            relateRecVO = parseRelateRecResult(root);
        }catch(Exception e){
            //e.printStackTrace();
            throw e;
        }finally{
            if(null != sr){
                sr.close();
            }
        }
        return relateRecVO;
    }

    public UpdateResultVO parseRelateRecResult(Element root) {
        UpdateResultVO relateRecVO = new UpdateResultVO();
        List<Element> ls = root.elements();
        for (Element ele : ls) {
            if (SearchConstants.TAGS_RESPONSE_STATUSCODE.equals(ele.getName())) {
                relateRecVO.setStatuscode(ele.getStringValue());
            } else if (SearchConstants.RESPONSE_STATUSMESSAGE.equals(ele.getName())) {
                relateRecVO.setStatusmessage(ele.getStringValue());
            } else if (SearchConstants.RESPONSE_RESULT.equals(ele.getName())) {
                //searchResult.setResult(parseResult(ele));
                List<Element> elements = ele.elements();
                for(Element el:elements){
                   if(SearchConstants.RESULT_APPS.equals(el.getName())){
                       relateRecVO.setApps(parseApps(el));
                    }else{                                                                    
                        log.warn("未知标签: " + el.getName());
                    }
                }
            } else {
                log.warn("未知标签: " + ele.getName());
            }
        }
        return relateRecVO;
    }
    
    
    //category
    public UpdateResultVO processCategoryXml(String categoryXml) throws Exception{
        UpdateResultVO categoryVO = null;
        StringReader sr = null;
        try{
            SAXReader reader = new SAXReader();
            sr = new StringReader(categoryXml);
            Document doc = reader.read(sr);
            Element root = doc.getRootElement();
            categoryVO = parseCategoryResult(root);
        }catch(Exception e){
            //e.printStackTrace();
            throw e;
        }finally{
            if(null != sr){
                sr.close();
            }
        }
        return categoryVO;
    }

    public UpdateResultVO parseCategoryResult(Element root) {
        UpdateResultVO categoryVO = new UpdateResultVO();
        List<Element> ls = root.elements();
        for (Element ele : ls) {
            if (SearchConstants.TAGS_RESPONSE_STATUSCODE.equals(ele.getName())) {
                categoryVO.setStatuscode(ele.getStringValue());
            } else if (SearchConstants.RESPONSE_STATUSMESSAGE.equals(ele.getName())) {
                categoryVO.setStatusmessage(ele.getStringValue());
            } else if (SearchConstants.RESULT_CATEGORIES.equals(ele.getName())) {
                categoryVO.setCates(parseCategories(ele));
            } else {
                log.warn("未知标签: " + ele.getName());
            }
        }
        return categoryVO;
    }
    
    private List<CateVO> parseCategories(Element ele){
        List<Element> elements = ele.elements();
        List<CateVO> cates = new ArrayList<CateVO>();
        for(Element el:elements){
            if(SearchConstants.CATEGORY.equals(el.getName())){
                cates.add(parseCategory(el));
            }else{                                                                    
                log.warn("Categories未知标签: " + el.getName());
            }
        }
        return cates;
    }

    private CateVO parseCategory(Element ele){
        CateVO cateVO = new CateVO();
        List<Element> ls = ele.elements();
        
        for(Element el:ls){
            if(SearchConstants.CATE_ID.equals(el.getName())){
                cateVO.setId(el.getStringValue());
            }else if(SearchConstants.CATE_TYPE.equals(el.getName())){
                cateVO.setType(el.getStringValue());
            }else if(SearchConstants.CATE_NAME.equals(el.getName())){
                cateVO.setName(el.getStringValue());
            }else{
                log.warn("category未知标签: " + el.getName());
            }  
        }
        return cateVO;
    }
    
    public AppInfoDAO getAppInfoDAO() {
        return this.appInfoDAO;
    }

    public void setAppInfoDAO(AppInfoDAO appInfoDAO) {
        this.appInfoDAO = appInfoDAO;
    }

   
	
}
