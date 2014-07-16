/**
 * AppInfoServiceImpl.java
 * com.bbkmobile.iqoo.console.business.appinfo
 *
 * Function： TODO 
 *
 *   ver     date      		author
 * ──────────────────────────────────
 *   		 2012-1-3 		dengkehai
 *
 * Copyright (c) 2012, TNT All Rights Reserved.
 */

package com.bbkmobile.iqoo.interfaces.appinfo.business;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.stereotype.Service;

import com.bbkmobile.iqoo.common.json.JackSonParser;
import com.bbkmobile.iqoo.common.logging.Lg;
import com.bbkmobile.iqoo.common.search.SearchConstants;
import com.bbkmobile.iqoo.common.vo.AppDetailResultAppInfo;
import com.bbkmobile.iqoo.common.vo.BaseResultAppInfo;
import com.bbkmobile.iqoo.common.vo.SimpleAppComment;
import com.bbkmobile.iqoo.console.activity.dao.ActivityInfo;
import com.bbkmobile.iqoo.console.constants.Constants;
import com.bbkmobile.iqoo.console.constants.LgType;
import com.bbkmobile.iqoo.console.constants.UtilTool;
import com.bbkmobile.iqoo.console.dao.appinfo.AppComment;
import com.bbkmobile.iqoo.console.dao.appinfo.AppInfo;
import com.bbkmobile.iqoo.console.dao.appinfo.BaiduAppId;
import com.bbkmobile.iqoo.console.dao.appinfo.RequestParameter;
import com.bbkmobile.iqoo.console.dao.appinfo.SearchKey;
import com.bbkmobile.iqoo.console.dao.appinfo.TAppScreenshot;
import com.bbkmobile.iqoo.console.dao.modelmgr.Model;
import com.bbkmobile.iqoo.interfaces.activity.dao.ActivityDAO;
import com.bbkmobile.iqoo.interfaces.appinfo.dao.AppInfoDAO;
import com.bbkmobile.iqoo.interfaces.appinfo.vo.AppInfoListVO;
import com.bbkmobile.iqoo.interfaces.appinfo.vo.AppUpdateResultObject;
import com.bbkmobile.iqoo.interfaces.model.business.ModelInfoService;
import com.bbkmobile.iqoo.interfaces.review.dao.CommentReviewDAO;
//import com.bbkmobile.iqoo.common.util.RequestUtil;

//import com.bbkmobile.iqoo.platform.auth.User;
/**
 * ClassName:AppInfoServiceImpl Function: TODO ADD FUNCTION
 * 
 * @author dengkehai
 * @version
 * @since Ver 1.1
 * @Date 2012-1-3 下午3:24:12
 * 
 */
@Service("iAppInfoService")
public class AppInfoServiceImpl implements AppInfoService {
	@Resource
	private AppInfoDAO iAppInfoDAO;
	@Resource(name="iCommentReviewDAO")
	private CommentReviewDAO commentReviewDAO;
	@Resource(name="iModelInfoService")
	private ModelInfoService modelInfoService;
	@Resource(name="iActivityDAO")
	private ActivityDAO activityDAO;
	// begin:手机接口@haiyan

	public String getAppInfoForXml(String app_id, String content_complete,
			String need_comment, String model, String cs, String imei,
			String package_name, String search_type, String version)
			throws Exception {
		StringBuilder sb = new StringBuilder();
		try {
			AppInfo appInfo = null;
			Long appId = null;

			if (null != search_type && search_type.equals("2")) { // 根据包名获取详情
				appInfo = iAppInfoDAO.findOnSaleAppByPackage(package_name);
				if (null != appInfo) {
					appId = appInfo.getId();
				}
			} else { // 根据id获取详情
				appId = Long.parseLong(app_id);

				appInfo = iAppInfoDAO.findValidAppById(appId);
				// 根据id没有找打app，可能是来自原来机器汇中内置的app
				long appId_hub = 0;
				if (null == appInfo) {
					appId_hub = findMappingId(appId);
					if (0 != appId_hub) {
						appInfo = iAppInfoDAO.findAppById(appId_hub);
					}
				}
			}

			sb.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?><Package info=");

			if (null != appInfo) {
				sb.append("\"1\" comment=\"Y\">");

				boolean is_exist_raters_count = true;
				if (null != content_complete && content_complete.equals("1")) { // 补全详细
					is_exist_raters_count = false;
					sb.append(appInfoContentComplete(appInfo));
				}

				sb.append(getAppInfoXml(appInfo, version)); // 基本信息

				sb.append("<CommentList>");
				if (null != need_comment && need_comment.equals("1")) {
					List<AppComment> appComments = commentReviewDAO
							.findAppCommentList(appId,
									Constants.COMMENT_COUNT_FOR_PACKAGE, 1);
					sb.append(appInfoCommentList(appComments));
				}
				sb.append("</CommentList>");

				if (is_exist_raters_count) {
					sb.append("<raters_count><![CDATA[");
					sb.append(appInfo.getCommentCount());
					sb.append("]]></raters_count><download_count><![CDATA[");
					sb.append(appInfo.getDownloadCount());
					sb.append("]]></download_count>");
				}
			} else {
				sb.append("\"0\">");
			}
			sb.append("</Package>");
			return sb.toString();
		} catch (Exception e) {
			throw e;
		}
	}

	private String getAppInfoXml(AppInfo appInfo, String version)
			throws Exception {
		try {
			StringBuilder sb = new StringBuilder();

			List<TAppScreenshot> appScreenshots = iAppInfoDAO
					.findAppScreenshot(appInfo);

			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String releaseDate = dateFormat.format(appInfo.getUpdateDate()); // getOnSaleDate

			sb.append("<patchs><![CDATA[");
			if (null != appInfo.getPatchs()) {
				sb.append(appInfo.getPatchs());
			}
			sb.append("]]></patchs><download_url><![CDATA[");
			sb.append(UtilTool.getHttpURL("/appinfo/downloadApkFile?id="
					+ appInfo.getId() + "&app_version=" + version));
			sb.append("]]></download_url><upload_time><![CDATA[");
			sb.append(releaseDate);
			sb.append("]]></upload_time><size><![CDATA[");
			sb.append(appInfo.getApkSize());
			sb.append("]]></size><score><![CDATA[");
			sb.append(appInfo.getAvgComment());
			sb.append("]]></score><introduction><![CDATA[");
			if (null != appInfo.getAppDesc()) {
				sb.append(appInfo.getAppDesc().replaceAll("\\n", "<br>"));
			}
			sb.append("]]></introduction><ScreenshotList>");
			if (null != appScreenshots && 0 != appScreenshots.size()) {
				for (TAppScreenshot appScreenshot : appScreenshots) {
					sb.append("<screenshot><![CDATA[");
					// sb.append(baseURI+appScreenshot.getScreenshot());
					sb.append(UtilTool.getDownloadImageHttpURL(appScreenshot
							.getScreenshot()));
					sb.append("]]></screenshot>");
				}
			}
			sb.append("</ScreenshotList><PermissionList>");
			/*
			 * 暂时无须权限 String android_permission =
			 * appInfo.getAndroidPermission(); if(""!=android_permission &&
			 * null!=android_permission){ //Long[] permissionIds = new
			 * Long[android_permission.length()]; AndroidPermission
			 * userPermission = null; String androidPermission[] =
			 * android_permission.split(","); for(int i=0;
			 * i<androidPermission.length;i++){
			 * if(null!=androidPermission[i].trim
			 * ()&&""!=androidPermission[i].trim()){ Long permission_id =
			 * Long.parseLong(androidPermission[i]); userPermission =
			 * iAppInfoDAO.findAndroidPermission(permission_id);
			 * if(null!=userPermission){ String permission =
			 * userPermission.getPermission(); //permissionIds[i] =
			 * permission_id; sb.append("<permission><![CDATA[");
			 * sb.append(permission); sb.append("]]></permission>"); }
			 * 
			 * } } }
			 */
			sb.append("</PermissionList><offical><![CDATA[");
			sb.append(appInfo.getOfficial());
			sb.append("]]></offical>");

			// sb.append("<comment><![CDATA[");
			// sb.append("Y");
			// sb.append("]]></comment>");

			return sb.toString();
		} catch (Exception e) {
			throw e;
		}

	}

	private String appInfoCommentList(List<AppComment> appComments) {
		StringBuilder sb = new StringBuilder();
		if (null != appComments && appComments.size() > 0) {
			for (AppComment appComment : appComments) {
				sb.append("<comment user_name=\"");
				sb.append(appComment.getUser_name()); // 要改为用户名!!!
				sb.append("\" comment_time=\"");
				sb.append(appComment.getComment_date());
				sb.append("\" comment_score=\"");
				sb.append(appComment.getScore());
				sb.append("\"><![CDATA[");
				sb.append(appComment.getComment());
				sb.append("]]></comment>");
			}
		}
		return sb.toString();
	}

	private String appInfoContentComplete(AppInfo appInfo) throws Exception {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("<id><![CDATA[");
			sb.append(appInfo.getId());
			sb.append("]]></id><package_name><![CDATA[");
			sb.append(appInfo.getAppPackage());
			sb.append("]]></package_name><parent_id><![CDATA[");
			//sb.append(appInfo.getAppType().getParentId());
			sb.append("]]></parent_id><title_zh><![CDATA[");
			sb.append(appInfo.getAppCnName());
			sb.append("]]></title_zh><title_en><![CDATA[");
			sb.append(appInfo.getAppEnName());
			sb.append("]]></title_en><icon_url><![CDATA[");
			sb.append(UtilTool.getDownloadImageHttpURL(appInfo.getAppIcon()));
			sb.append("]]></icon_url><developer><![CDATA[");
			//sb.append(appInfo.getDeveloper().getName());
			sb.append(appInfo.getAppAuthor());
			sb.append("]]></developer><score><![CDATA[");
			sb.append(appInfo.getAvgComment());
			sb.append("]]></score><raters_count><![CDATA[");
			sb.append(appInfo.getCommentCount());
			sb.append("]]></raters_count><version_name><![CDATA[");
			sb.append(appInfo.getAppVersion());
			sb.append("]]></version_name><version_code><![CDATA[");
			sb.append(appInfo.getAppVersionCode());
			sb.append("]]></version_code><download_count><![CDATA[");
			sb.append(appInfo.getDownloadCount());
			sb.append("]]></download_count>");
			return sb.toString();
		} catch (Exception e) {
			throw e;
		}
	}

	private long findMappingId(long appId) {
		long appId_hub = 0;

		long ids[][] = new long[37][2];
		ids[0][0] = (long) 303;
		ids[0][1] = (long) 2338;
		ids[1][0] = (long) 304;
		ids[1][1] = (long) 46090;
		ids[2][0] = (long) 302;
		ids[2][1] = (long) 44678;
		ids[3][0] = (long) 307;
		ids[3][1] = (long) 40440;
		ids[4][0] = (long) 306;
		ids[4][1] = (long) 41311;
		ids[5][0] = (long) 255;
		ids[5][1] = (long) 48013;
		ids[6][0] = (long) 292;
		ids[6][1] = (long) 46713;
		ids[7][0] = (long) 293;
		ids[7][1] = (long) 40413;
		ids[8][0] = (long) 291;
		ids[8][1] = (long) 40458;
		ids[9][0] = (long) 295;
		ids[9][1] = (long) 40426;
		ids[10][0] = (long) 294;
		ids[10][1] = (long) 44808;
		ids[11][0] = (long) 296;
		ids[11][1] = (long) 44036;
		ids[12][0] = (long) 298;
		ids[12][1] = (long) 44065;
		ids[13][0] = (long) 252;
		ids[13][1] = (long) 41320;
		ids[14][0] = (long) 263;
		ids[14][1] = (long) 3208;
		ids[15][0] = (long) 3453;
		ids[15][1] = (long) 46090;
		ids[16][0] = (long) 3185;
		ids[16][1] = (long) 44678;
		ids[17][0] = (long) 3241;
		ids[17][1] = (long) 44468;
		ids[18][0] = (long) 3242;
		ids[18][1] = (long) 41311;
		ids[19][0] = (long) 6348;
		ids[19][1] = (long) 46491;
		ids[20][0] = (long) 255;
		ids[20][1] = (long) 48013;
		ids[21][0] = (long) 40443;
		ids[21][1] = (long) 46096;
		ids[22][0] = (long) 3207;
		ids[22][1] = (long) 46713;
		ids[23][0] = (long) 3166;
		ids[23][1] = (long) 45826;
		ids[24][0] = (long) 1637;
		ids[24][1] = (long) 47077;
		ids[25][0] = (long) 3248;
		ids[25][1] = (long) 40668;
		ids[26][0] = (long) 1443;
		ids[26][1] = (long) 46216;
		ids[27][0] = (long) 290;
		ids[27][1] = (long) 45070;
		ids[28][0] = (long) 305;
		ids[28][1] = (long) 40424;
		ids[29][0] = (long) 300;
		ids[29][1] = (long) 46096;
		ids[30][0] = (long) 211;
		ids[30][1] = (long) 40410;
		ids[31][0] = (long) 41388;
		ids[31][1] = (long) 48013;
		ids[32][0] = (long) 40417;
		ids[32][1] = (long) 48012;
		ids[33][0] = (long) 3232;
		ids[33][1] = (long) 48010;
		ids[34][0] = (long) 3351;
		ids[34][1] = (long) 48011;
		ids[35][0] = (long) 47914;
		ids[35][1] = (long) 48002;
		ids[36][0] = (long) 40409;
		ids[36][1] = (long) 47944;

		for (int k = 0; k < 37; k++) {
			if (appId == ids[k][0]) {
				appId_hub = ids[k][1];
				break;
			}
		}

		return appId_hub;
	}

	public AppInfoListVO getAppInfoListForKey(SearchKey searchKey)
			throws Exception {

		AppInfoListVO vo = new AppInfoListVO();

		List<AppInfo> appInfos = null;

		if (0 == searchKey.getApps_per_page()) {
			searchKey.setApps_per_page(Constants.APPS_PER_PAGE_20);
		}

		if (0 == searchKey.getPage_index()) {
			searchKey.setPage_index(1);
		}
		// cs = searchKey.getCs();

		Model modelClass = null;
		String model = searchKey.getModel();
		modelClass = modelInfoService.findModelByMdName(model);//by time
//		if (null != model && !"".equals(model)) {
//			model = model.split("@")[0];
//			modelClass = modelInfoDAO.findModelByMdName(model.trim());
//		}

		appInfos = iAppInfoDAO.getAppsByKeyNameForModel(searchKey, modelClass); // searchKey.getApp_version()>=500
		// appInfos =
		// iAppInfoDAO.getAppsByKeyNameWithFilterModel(searchKey,modelClass);
		// //连表查询返回开发者

		int total_count = appInfos.size();

		vo.setApps_per_page(searchKey.getApps_per_page());
		vo.setAppInfoLs(appInfos);
		vo.setPage_index(searchKey.getPage_index());
		vo.setTotal_count(total_count);

		return vo;
	}

	public String getAppInfoListForKeyNameForXml(AppInfoListVO vo,
			Float appVersion) throws Exception {

		// int total_count = vo.getTotal_count();
		int apps_per_page = vo.getApps_per_page();
		int page_index = vo.getPage_index();

		List<AppInfo> appInfos = vo.getAppInfoLs();

		Document doc = DocumentHelper.createDocument();
		Element root = doc.addElement("PackageList");

		root.addAttribute("maxpage", vo.getMaxPage() + "");
		root.addAttribute("PageNo", page_index + "");
		root.addAttribute("pageSize", apps_per_page + "");
		root.addAttribute("TotalCount", vo.getAppInfoLs().size() + "");
		if(vo.getTotal_count() == vo.getApps_per_page()){
			root.addAttribute("from", SearchConstants.FROM_BAIDU);
		}else{
			root.addAttribute("from", SearchConstants.FROM_LOCAL);
		}
		addAppInfosXml(appInfos, page_index, root, appVersion);

		return doc.asXML();
	}

	private void addAppInfosXml(List<AppInfo> appInfos, int page_index,
			Element root, Float appVersion) throws Exception {
		if (null != appInfos && appInfos.size() > 0) {
			int k = 1;
			for (AppInfo appInfo : appInfos) {
				Element appEle = null;
				appEle = root.addElement("Package");
				appEle.addElement("id").setText(appInfo.getId() + "");
				appEle.addElement("tag").setText(appInfo.getTag() + "");
				appEle.addElement("package_name").setText(
						appInfo.getAppPackage());
				// appEle.addElement("parent_id").setText(appInfo.getAppType().getParentId()
				// + "");
				appEle.addElement("parent_id").setText("null"); // 不在返回父类id
				appEle.addElement("title_zh").setText(appInfo.getAppCnName());
				appEle.addElement("title_en").setText(appInfo.getAppCnName());
				appEle.addElement("icon_url").setText(
						UtilTool.getDownloadImageHttpURL(appInfo.getAppIcon()));
				// appEle.addElement("developer").setText(appInfo.getAppAuthor()==null?"":appInfo.getAppAuthor());
				appEle.addElement("developer").setText(
						appInfo.getAppAuthor() == null ? "" : appInfo
								.getAppAuthor()); // app_version>=500时，页面不再显示开发者
				appEle.addElement("score")
						.setText(appInfo.getAvgComment() + "");
				appEle.addElement("raters_count").setText(
						appInfo.getCommentCount() + "");
				appEle.addElement("version_name").setText(
						appInfo.getAppVersion() == null ? "" : appInfo
								.getAppVersion());
				appEle.addElement("version_code").setText(
						appInfo.getAppVersionCode());
				appEle.addElement("download_url").setText(
						UtilTool.getHttpURL("/appinfo/downloadApkFile?id="
								+ appInfo.getId() + "&app_version="
								+ appVersion));
				appEle.addElement("size").setText(appInfo.getApkSize() + "");
				appEle.addElement("download_count").setText(
						appInfo.getDownloadCount() + "");
				appEle.addElement("patchs").setText(
						appInfo.getPatchs() == null ? "" : appInfo.getPatchs());
				appEle.addElement("from").setText(SearchConstants.FROM_LOCAL);

				if (1 == page_index && k == 1) {
					appEle.addElement("offical").setText(
							appInfo.getOfficial() + "");

					if (appVersion >= 520) {
						if (appInfo.getOfficial() == '1') {
							appEle.addElement("remark").setText(
									iAppInfoDAO.getRemarkByAppId(appInfo
											.getId())); // 第一页的第一个为官方，则返回remark
						}
					}

				} else {
					appEle.addElement("offical").setText("0");
				}

				k++;
			}
		}
	}

	@Override
	public String getAppInfoListForKeyNameForXml(SearchKey searchKey)
			throws Exception {
		String xml = null;

		try {
			AppInfoListVO vo = getAppInfoListForKey(searchKey);

			if (vo.getTotal_count() == (vo.getApps_per_page() + 1)) {
				vo.getAppInfoLs().remove(vo.getTotal_count() - 1); // 去掉最后一个
				vo.setMaxPage(vo.getPage_index() + 1);
			} else {
				vo.setMaxPage(vo.getPage_index());
			}

			xml = getAppInfoListForKeyNameForXml(vo, searchKey.getApp_version());

		} catch (Exception e) {
			throw e;
		}
		return xml;
	}

	private String downloadurl(String url) throws Exception {

		int retry = 0;
		HttpURLConnection huc = null;
		String location = null;

		for (retry = 0; retry < 5; retry++) {

			try {
				// System.out.println("url="+url);
				URL urlx = new URL(url);
				huc = (HttpURLConnection) urlx.openConnection();
				huc.setInstanceFollowRedirects(false);
				huc.setRequestMethod("GET");

				huc.setReadTimeout(3000);
				huc.setConnectTimeout(3000);
				// System.out.println("url="+url);
				int statusCode = huc.getResponseCode();
				if (statusCode == HttpStatus.SC_MOVED_PERMANENTLY
						|| statusCode == HttpStatus.SC_MOVED_TEMPORARILY) {
					// 从头中取出转向的地址
					location = huc.getHeaderField("Location");
					if (location != null) {
						// System.out.println("location="+location);
						String patternStr = "://.*";
						Pattern pattern = Pattern.compile(patternStr,
								Pattern.CASE_INSENSITIVE);
						Matcher matcher = pattern.matcher(location);
						// System.out.println("location="+location);
						if (!matcher.find()) {
							location = url.substring(0, url.indexOf("/", 9))
									+ location;
						}
						url = location;
					}
				} else if (statusCode == HttpStatus.SC_OK) {
					location = huc.getHeaderField("Location");
					if (location != null) {
						url = location;
					}
					return url;
				} else if (statusCode == HttpStatus.SC_NOT_FOUND) {
					return null;
				}
			} catch (HttpException e) {
				throw e;
				// e.printStackTrace();
			} catch (IOException e) {
				throw e;
				// e.printStackTrace();
			} finally {
				huc.disconnect();
			}
		}

		return url;
	}

	@Override
	public String getApkFilePath(String id, String appVersion, boolean isFirst,
			String target, String patch, String model) throws Exception {

		if (null != appVersion) {
			if (appVersion.matches(".*\\..*")) {
				appVersion = appVersion.replaceAll("\\..*", "");
			}
		} else {
			appVersion = "0";
		}
		String filePath = "";
		String path = "";
		try {

			Long app_id = Long.parseLong(id);
			// System.out.println("appVersion="+appVersion);
			// System.out.println("app_id="+app_id);
			if (null != target && "baidu".equals(target)) {
				// 给baidu下载地址 直接百度下载暂时不走这个流程
			} else {
				if (app_id > 0) {
					AppInfo appInfo = iAppInfoDAO.findAppById(app_id);

					if (null == appInfo) {
						long ids[][] = new long[36][2];
						ids[0][0] = (long) 303;
						ids[0][1] = (long) 2338;
						ids[1][0] = (long) 304;
						ids[1][1] = (long) 46090;
						ids[2][0] = (long) 302;
						ids[2][1] = (long) 44678;
						ids[3][0] = (long) 307;
						ids[3][1] = (long) 40440;
						ids[4][0] = (long) 306;
						ids[4][1] = (long) 41311;
						ids[5][0] = (long) 255;
						ids[5][1] = (long) 48013;
						ids[6][0] = (long) 292;
						ids[6][1] = (long) 46713;
						ids[7][0] = (long) 293;
						ids[7][1] = (long) 40413;
						ids[8][0] = (long) 291;
						ids[8][1] = (long) 40458;
						ids[9][0] = (long) 295;
						ids[9][1] = (long) 40426;
						ids[10][0] = (long) 294;
						ids[10][1] = (long) 44808;
						ids[11][0] = (long) 296;
						ids[11][1] = (long) 44036;
						ids[12][0] = (long) 298;
						ids[12][1] = (long) 44065;
						ids[13][0] = (long) 252;
						ids[13][1] = (long) 41320;
						ids[14][0] = (long) 263;
						ids[14][1] = (long) 3208;
						ids[15][0] = (long) 3453;
						ids[15][1] = (long) 46090;
						ids[16][0] = (long) 3185;
						ids[16][1] = (long) 44678;
						ids[17][0] = (long) 3241;
						ids[17][1] = (long) 44468;
						ids[18][0] = (long) 3242;
						ids[18][1] = (long) 41311;
						ids[19][0] = (long) 6348;
						ids[19][1] = (long) 46491;
						ids[20][0] = (long) 255;
						ids[20][1] = (long) 48013;
						ids[21][0] = (long) 40443;
						ids[21][1] = (long) 46096;
						ids[22][0] = (long) 3207;
						ids[22][1] = (long) 46713;
						ids[23][0] = (long) 3166;
						ids[23][1] = (long) 45826;
						ids[24][0] = (long) 1637;
						ids[24][1] = (long) 47077;
						ids[25][0] = (long) 3248;
						ids[25][1] = (long) 40668;
						ids[26][0] = (long) 1443;
						ids[26][1] = (long) 46216;
						ids[27][0] = (long) 290;
						ids[27][1] = (long) 45070;
						ids[28][0] = (long) 305;
						ids[28][1] = (long) 40424;
						ids[29][0] = (long) 300;
						ids[29][1] = (long) 46096;
						ids[30][0] = (long) 211;
						ids[30][1] = (long) 40410;
						ids[31][0] = (long) 41388;
						ids[31][1] = (long) 48013;
						ids[32][0] = (long) 40417;
						ids[32][1] = (long) 48012;
						ids[33][0] = (long) 3232;
						ids[33][1] = (long) 48010;
						ids[34][0] = (long) 3351;
						ids[34][1] = (long) 48011;
						ids[35][0] = (long) 47914;
						ids[35][1] = (long) 48002;

						for (int k = 0; k < 36; k++) {
							if (app_id == ids[k][0]) {
								app_id = ids[k][1];
								appInfo = iAppInfoDAO.findAppById(app_id);
								break;
							}
						}
					}

					if (isFirst) {
						iAppInfoDAO.updateDownloadCountForAppInfo(appInfo); // 下载量增加1
					}

					path = appInfo.getAppApk();
//					if (path.equals(com.bbkmobile.iqoo.console.apkupload.constants.Constants.FAKE_APKURL)) {
//						// TODO 根据机型 去 t_app_model
//						List<MultiApkApp> apps = multiApkAppService
//								.findMultiApkAppByAppInfo(appInfo);
//						if (apps != null) {
//							Model modelObj = modelInfoService.findModelByMdName(model);//modelInfoDAO.findModelByMdName(model);
//							for (MultiApkApp app : apps) {
//								if (!StringUtils.isEmpty(app.getModel())) {
//									String[] models = app.getModel().split(",");
//									if (Arrays.binarySearch(models,
//											String.valueOf(modelObj.getId())) > 0) {
//										path = app.getApkpath();
//										break;
//									}
//								}
//							}
//						}
//					}
					if (null != patch && !"".equals(patch)) {
						String patchs = appInfo.getPatchs();
						if (patchs.contains(patch)) {
							path = path.substring(0, path.lastIndexOf("."))
									+ "_" + patch + ".patch"; // 补丁路径
						}
					}
					filePath = UtilTool.getDownloadApkHttpURL(path);
				} else {
					BaiduAppId baiduAppIdResult = null;
					baiduAppIdResult = iAppInfoDAO
							.getLocalBaiduAppIdById(-app_id);
					filePath = baiduAppIdResult.getUrl();
					if (Integer.parseInt(appVersion) < 300) {
						filePath = downloadurl(filePath);
					}
					// System.out.println("filePath2="+filePath);
				}
			}
			/*
			 * String userDir = System.getProperty("user.dir"); filePath =
			 * userDir+path; filePath = UtilTool.getDownloadPath(path);
			 */

		} catch (Exception e) {
			throw e;
			// Lg.error(LgType.STDOUT, "手机下载apk文件时出错:app_id="+id+",error=" +
			// e.getMessage());
		}
		return filePath;
	}

	@Override
	public String getAppInfoListForUpdateForXml(String packages,
			String dbversion, String model, String cs, String imei,
			String appVersion) throws Exception {

		StringBuilder sb = new StringBuilder();
		try {
			int dbversion_local = 100;
			if (null != dbversion && !"".equals(dbversion)) {
				dbversion_local = iAppInfoDAO
						.getModuleDBVersion("update", 1000);
				if (dbversion_local == Integer.parseInt(dbversion)) {
					if (appVersion != null
							&& Float.valueOf(appVersion) >= Constants.APPVERSION530) {// by time
						return handleHighVersion(null,dbversion_local);
					}
					String reponse = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><PackageList dbversion=\""
							+ dbversion + "\"></PackageList>";
					return reponse;
				}
			}

			Model modelClass = modelInfoService.findModelByMdName(model);//by time
//			if (null != model && !"".equals(model)) {
//				model = model.split("@")[0];
//				modelClass = modelInfoDAO.findModelByMdName(model.trim());
//			}

			List<AppInfo> appInfos = iAppInfoDAO.getAppInfoListForUpdate(
					packages, modelClass);
			if (appVersion != null
					&& Float.valueOf(appVersion) >= Constants.APPVERSION530) {// by time
				return handleHighVersion(appInfos,dbversion_local);
			}
			if (null != appInfos && appInfos.size() > 0) {
				sb.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?><PackageList dbversion=\""
						+ dbversion_local + "\">");

				if (null != appInfos && 0 != appInfos.size()) {
					for (AppInfo appInfo : appInfos) {
						sb.append("<Package><id><![CDATA[");
						sb.append(appInfo.getId());
						sb.append("]]></id><title_zh><![CDATA[");
						sb.append(appInfo.getAppCnName());
						sb.append("]]></title_zh><title_en><![CDATA[");
						sb.append(appInfo.getAppEnName());
						sb.append("]]></title_en><score><![CDATA[");
						sb.append(appInfo.getAvgComment());
						sb.append("]]></score><raters_count><![CDATA[");
						sb.append(appInfo.getCommentCount());
						sb.append("]]></raters_count><package_name><![CDATA[");
						sb.append(appInfo.getAppPackage());
						sb.append("]]></package_name><version_name><![CDATA[");
						sb.append(appInfo.getAppVersion());
						sb.append("]]></version_name><version_code><![CDATA[");
						sb.append(appInfo.getAppVersionCode());
						sb.append("]]></version_code><download_url><![CDATA[");
						// sb.append("http://172.20.20.139:8080/App_Console/appinfo/downloadApkFile.action?id=1");
						// sb.append(UtilTool.getHttpURL("/appinfo/downloadApkFile.action?id=1"));
						if (null != appVersion
								&& !appVersion.equalsIgnoreCase("null")
								&& !appVersion.equals("")
								&& Float.parseFloat(appVersion) < 510) {
							sb.append(UtilTool
									.getHttpURL("/appinfo/downloadApkFile?id="
											+ appInfo.getId())
									+ "&cfrom=25&app_version=" + appVersion);
						} else {
							sb.append(UtilTool
									.getHttpURL("/appinfo/downloadApkFile?id="
											+ appInfo.getId())
									+ "&cfrom=25");
						}

						sb.append("]]></download_url><size><![CDATA[");
						sb.append(appInfo.getApkSize());
						sb.append("]]></size><icon_url><![CDATA[");
						// sb.append(appInfo.getAppIcon());
						sb.append(UtilTool.getDownloadImageHttpURL(appInfo
								.getAppIcon()));
						sb.append("]]></icon_url><patchs><![CDATA[");
						if (null != appInfo.getPatchs()) {
							sb.append(appInfo.getPatchs());
						}
						sb.append("]]></patchs><offical><![CDATA[");
						sb.append("0");
						sb.append("]]></offical></Package>");
						// sb.append("]]></icon_url><developer><![CDATA[");
						// // sb.append(appInfo.getAppAuthor());
						// sb.append(appInfo.getDeveloper().getName());
						// sb.append("]]></developer></Package>");
					}
				}
				sb.append("</PackageList>");
			}

		} catch (Exception e) {
			throw e;
		}
		return sb.toString();

	}

	private String handleHighVersion(List<AppInfo> appInfos,int dbversion_local) throws Exception {
		List<BaseResultAppInfo> bases = new ArrayList<BaseResultAppInfo>();
		if (appInfos != null && appInfos.size() > 0) {
			for (AppInfo appInfo : appInfos) {
				BaseResultAppInfo base = new BaseResultAppInfo();
				base.setId(appInfo.getId());
				base.setTitle_en(appInfo.getAppCnName());
				base.setTitle_zh(appInfo.getAppEnName());
				base.setScore(appInfo.getAvgComment());
				base.setRaters_count(appInfo.getCommentCount());
				base.setPackage_name(appInfo.getAppPackage());
				base.setVersion_name(appInfo.getAppVersion());
				base.setVersion_code(appInfo.getAppVersionCode());
				base.setDownload_url(UtilTool
						.getHttpURL("/appinfo/downloadApkFile?id="
								+ appInfo.getId())
						+ "&cfrom=25");
				base.setSize(appInfo.getApkSize());
				base.setIcon_url(UtilTool.getDownloadImageHttpURL(appInfo
						.getAppIcon()));
				if (null != appInfo.getPatchs()) {
					base.setPatchs(appInfo.getPatchs());
				} else {
					base.setPatchs("");
				}
				base.setOffical('0');
				bases.add(base);
			}
		}
		AppUpdateResultObject<List<BaseResultAppInfo>> result = new AppUpdateResultObject<List<BaseResultAppInfo>>();
		result.setResult(true);
		result.setValue(bases);
		result.setDbversion(dbversion_local);
		return JackSonParser.bean2Json(result);

	}

	@Override
	public String getAppInfoForUpdateForXml(String app_package,
			String appVersion, String model, String cs, String imei)
			throws Exception {

		StringBuilder sb = new StringBuilder();
		try {

			Model modelClass = modelInfoService.findModelByMdName(model);//by time
//			if (null != model && !"".equals(model)) {
//				model = model.split("@")[0];
//				modelClass = modelInfoDAO.findModelByMdName(model.trim());
//			}

			AppInfo appInfo = iAppInfoDAO.getAppInfoForUpdate(app_package,
					modelClass);

			if (null != appInfo) {
				sb.append("<Package><id><![CDATA[");
				sb.append(appInfo.getId());
				sb.append("]]></id><title_zh><![CDATA[");
				sb.append(appInfo.getAppCnName());
				sb.append("]]></title_zh><title_en><![CDATA[");
				sb.append(appInfo.getAppEnName());
				sb.append("]]></title_en><score><![CDATA[");
				sb.append(appInfo.getAvgComment());
				sb.append("]]></score><raters_count><![CDATA[");
				sb.append(appInfo.getCommentCount());
				sb.append("]]></raters_count><package_name><![CDATA[");
				sb.append(appInfo.getAppPackage());
				sb.append("]]></package_name><version_name><![CDATA[");
				sb.append(appInfo.getAppVersion());
				sb.append("]]></version_name><version_code><![CDATA[");
				sb.append(appInfo.getAppVersionCode());
				sb.append("]]></version_code><download_url><![CDATA[");
				// sb.append("http://172.20.20.139:8080/App_Console/appinfo/downloadApkFile.action?id=1");
				// sb.append(UtilTool.getHttpURL("/appinfo/downloadApkFile.action?id=1"));
				sb.append(UtilTool.getHttpURL("/appinfo/downloadApkFile?id="
						+ appInfo.getId() + "&app_version=" + appVersion));
				sb.append("]]></download_url><size><![CDATA[");
				sb.append(appInfo.getApkSize());
				sb.append("]]></size><icon_url><![CDATA[");
				// sb.append(appInfo.getAppIcon());
				sb.append(UtilTool.getDownloadImageHttpURL(appInfo.getAppIcon()));
				sb.append("]]></icon_url></Package>");
				// sb.append("]]></icon_url><developer><![CDATA[");
				// // sb.append(appInfo.getAppAuthor());
				// sb.append(appInfo.getDeveloper().getName());
				// sb.append("]]></developer></Package>");
				return sb.toString();
			} else {
				return "";
			}

		} catch (Exception e) {
			throw e;
		}

	}

	@Override
	public BaiduAppId getLocalBaiduAppIdById(Long id) throws Exception {
		try {
			BaiduAppId baiduAppId = null;
			baiduAppId = iAppInfoDAO.getLocalBaiduAppIdById(id);
			return baiduAppId;
		} catch (Exception e) {
			throw e;
		}
	}

	// end:手机接口
	public boolean saveBrowseLog(RequestParameter requestParameter)
			throws Exception {
		try {
			iAppInfoDAO.saveBrowseLog(requestParameter);
			return true;
		} catch (Exception e) {
			// throw e;
			Lg.error(
					LgType.STDOUT,
					"saveBrowseLog出错，cfrom=" + requestParameter.getCfrom()
							+ ",id=" + requestParameter.getIdStr() + ",cs="
							+ requestParameter.getCsStr() + "error:"
							+ e.getMessage());
		}
		return false;
	}

	public boolean savePackagesUpdateLog(RequestParameter requestParameter)
			throws Exception {
		try {
			iAppInfoDAO.savePackagesUpdateLog(requestParameter);
			return true;
		} catch (Exception e) {
			// throw e;
			Lg.error(LgType.STDOUT,
					"savePackagesUpdateLog出错，error:" + e.getMessage());
		}
		return false;
	}

	public boolean saveSeachWordLog(RequestParameter requestParameter)
			throws Exception {
		try {
			iAppInfoDAO.saveSeachWordLog(requestParameter);
			return true;
		} catch (Exception e) {
			// throw e;
			Lg.error(LgType.STDOUT, "saveSeachKeyLog出错，error:" + e.getMessage());
		}
		return false;
	}

	/*
	 * public boolean saveSeachKeyLog(SearchResultForm searchResultForm ) throws
	 * Exception{ try { iAppInfoDAO.saveSeachKeyLog(searchResultForm); return
	 * true; } catch (Exception e) { //throw e; Lg.error(LgType.STDOUT,
	 * "saveSeachKeyLog出错，error:"+e.getMessage()); } return false; }
	 */
	public boolean saveBrowseAppLog(RequestParameter requestParameter)
			throws Exception {
		try {
			iAppInfoDAO.saveBrowseAppLog(requestParameter);
			return true;
		} catch (Exception e) {
			// throw e;
			Lg.error(
					LgType.STDOUT,
					"saveBrowseAppLog出错，cfrom=" + requestParameter.getCfrom()
							+ ",id=" + requestParameter.getIdStr() + ",cs="
							+ requestParameter.getCsStr() + ",version="
							+ requestParameter.getVersion() + ",error:"
							+ e.getMessage());
		}
		return false;
	}

	public boolean saveBrowseBaiduAppLog(RequestParameter requestParameter)
			throws Exception {
		try {
			iAppInfoDAO.saveBrowseBaiduAppLog(requestParameter);
			return true;
		} catch (Exception e) {
			// throw e;
			Lg.error(
					LgType.STDOUT,
					"saveBrowseBaiduAppLog出错，cfrom="
							+ requestParameter.getCfrom() + ",id="
							+ requestParameter.getIdStr() + ",cs="
							+ requestParameter.getCsStr() + ",version="
							+ requestParameter.getVersion() + ",error:"
							+ e.getMessage());
		}
		return false;
	}

	public boolean saveBrowseAppPackageLog(RequestParameter requestParameter)
			throws Exception {
		try {
			iAppInfoDAO.saveBrowseAppPackageLog(requestParameter);
			return true;
		} catch (Exception e) {
			// throw e;
			Lg.error(
					LgType.STDOUT,
					"saveBrowseAppPackageLog出错，cfrom="
							+ requestParameter.getCfrom() + ",id="
							+ requestParameter.getIdStr() + ",cs="
							+ requestParameter.getCsStr() + ",version="
							+ requestParameter.getVersion() + ",error:"
							+ e.getMessage());
		}
		return false;
	}

	public boolean saveDownloadAppLog(RequestParameter requestParameter)
			throws Exception {
		try {
			iAppInfoDAO.saveDownloadAppLog(requestParameter);
			return true;
		} catch (Exception e) {
			// throw e;
			Lg.error(
					LgType.STDOUT,
					"saveDownLoadAppLog出错，app_id="
							+ requestParameter.getIdStr() + ",appVersion="
							+ requestParameter.getVersion() + ",model="
							+ requestParameter.getModel() + ",cs="
							+ requestParameter.getCsStr() + ",imei="
							+ requestParameter.getImei() + ",cfrom="
							+ requestParameter.getCfrom() + ",module_id="
							+ requestParameter.getModule_id() + ",related="
							+ requestParameter.getRelated() + ",update="
							+ requestParameter.getUpdate() + ",error:"
							+ e.getMessage());
		}
		return false;
	}

	public boolean savePcDownloadAppLog(RequestParameter requestParameter)
			throws Exception {
		try {
			iAppInfoDAO.savePcDownloadAppLog(requestParameter);
			return true;
		} catch (Exception e) {
			// throw e;
			e.printStackTrace();
			Lg.error(
					LgType.STDOUT,
					"savePcDownLoadAppLog出错，app_id="
							+ requestParameter.getIdStr() + ",package_name="
							+ requestParameter.getPackages() + ",ip="
							+ requestParameter.getIp() + ",cfrom="
							+ requestParameter.getCfrom() + ",version="
							+ requestParameter.getVersion() + ",error:"
							+ e.getMessage());
		}
		return false;
	}

	public boolean saveDownloadBaiduAppLog(RequestParameter requestParameter)
			throws Exception {
		try {
			iAppInfoDAO.saveDownloadBaiduAppLog(requestParameter);
			return true;
		} catch (Exception e) {
			// throw e;
			Lg.error(LgType.STDOUT,
					"saveDownloadBaiduAppLog出错，error:" + e.getMessage());
		}
		return false;
	}
	public CommentReviewDAO getCommentReviewDAO() {
		return commentReviewDAO;
	}

	public void setCommentReviewDAO(CommentReviewDAO commentReviewDAO) {
		this.commentReviewDAO = commentReviewDAO;
	}

	@Override
	public AppInfo getAppInfo(RequestParameter requestParameter)
			throws Exception {
		AppInfo appInfo = null;
		Long appId = null;

		String search_type = requestParameter.getSearch_type();
		String package_name = requestParameter.getPackages();
		String app_id = requestParameter.getIdStr();

		if (null != search_type && search_type.equals("2")) { // 根据包名获取详情
			requestParameter.setIdStr(null);
			appInfo = iAppInfoDAO.findOnSaleApp(requestParameter);// iAppInfoDAO.findOnSaleAppByPackage(package_name);
																	// by time
			// if (null != appInfo) {
			// //appId = appInfo.getId();
			// appInfo.setAppPackage(package_name);
			// }
		} else { // 根据id获取详情
			requestParameter.setPackages(null);
			appId = Long.parseLong(app_id);
			appInfo = iAppInfoDAO.findOnSaleApp(requestParameter);// iAppInfoDAO.findValidAppById(appId);
			// 根据id没有找打app，可能是来自原来机器汇中内置的app
			long appId_hub = 0;
			if (null == appInfo) {
				appId_hub = findMappingId(appId);
				if (0 != appId_hub) {
					requestParameter.setIdStr(appId_hub + "");
					appInfo = iAppInfoDAO.findOnSaleApp(requestParameter);// iAppInfoDAO.findAppById(appId_hub);
				}
			}
		}
		return appInfo;
	}

	@Override
	public AppDetailResultAppInfo getImCompleteDetailAppInfo(
			RequestParameter requestParameter) throws Exception {
		AppInfo appInfo = getAppInfo(requestParameter);
		if (null != appInfo) {
			AppDetailResultAppInfo imAppInfo = new AppDetailResultAppInfo();
			boolean is_exist_raters_count = true;
			String content_complete = requestParameter.getContent_complete();
			if (null != content_complete && content_complete.equals("1")) { // 补全详细
				is_exist_raters_count = false;
				imAppInfo.setId(appInfo.getId());
				imAppInfo.setPackage_name(appInfo.getAppPackage());
				imAppInfo.setParent_id(0);
				imAppInfo.setTitle_zh(appInfo.getAppCnName());
				imAppInfo.setTitle_en(appInfo.getAppEnName());
				imAppInfo.setIcon_url(UtilTool.getDownloadImageHttpURL(appInfo
						.getAppIcon()));
				// imAppInfo.setDeveloper(appInfo.getDeveloper().getName());
				imAppInfo.setDeveloper(appInfo.getAppAuthor());
				imAppInfo.setRaters_count(appInfo.getCommentCount());
				imAppInfo.setVersion_name(appInfo.getAppVersion());
				imAppInfo.setVersion_code(appInfo.getAppVersionCode());
				imAppInfo.setDownload_count(appInfo.getDownloadCount());
			}
			setBasicInfo(appInfo, requestParameter.getApp_version(), imAppInfo);// 基本信息
			String need_comment = requestParameter.getNeed_comment();
			if (null != need_comment && need_comment.equals("1")) {
				requestParameter.setIdStr(appInfo.getId() + "");
				setComment(requestParameter, imAppInfo);
			}
			if (is_exist_raters_count) {
				imAppInfo.setRaters_count(appInfo.getCommentCount());
			}
			return imAppInfo;
		}
		return null;
	}

	private void setComment(RequestParameter requestParameter,
			AppDetailResultAppInfo imAppInfo) throws Exception {
		List<AppComment> appComments = commentReviewDAO.findAppCommentList(
				Long.parseLong(requestParameter.getIdStr()),
				Constants.COMMENT_COUNT_FOR_PACKAGE, 1);
		if (appComments != null && appComments.size() > 0) {
			List<SimpleAppComment> simpleAppComments = new ArrayList<SimpleAppComment>(
					appComments.size());
			for (AppComment comment : appComments) {
				SimpleAppComment simpleComment = new SimpleAppComment();

				simpleComment.setComment(comment.getComment());
				simpleComment.setComment_date(comment.getComment_date());
				simpleComment.setScore(comment.getScore());
				if (comment.getUser_name() == null) {
					simpleComment.setUser_name("");
				} else {
					simpleComment.setUser_name(comment.getUser_name());
				}
				simpleAppComments.add(simpleComment);
			}
			imAppInfo.setAppComments(simpleAppComments);
		}
	}

	private void setBasicInfo(AppInfo appInfo, Float version,
			AppDetailResultAppInfo imAppInfo) throws Exception {
		try {
			//add activity
			List<ActivityInfo> list = activityDAO.getModelActivityIdByAppId(appInfo.getId());
			if(list !=null && list.size() >0){
				clearUnnessceries(list);
				imAppInfo.setActs(list);
			}
			List<TAppScreenshot> appScreenshots = iAppInfoDAO
					.findAppScreenshot(appInfo);
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String releaseDate = dateFormat.format(appInfo.getUpdateDate()); // getOnSaleDate
			if (null != appInfo.getPatchs()) {
				imAppInfo.setPatchs(appInfo.getPatchs());
			} else {
				imAppInfo.setPatchs("");
			}
			//imAppInfo.setDownload_url((UtilTool.getHttpURL("/appinfo/downloadApkFile?id="+ appInfo.getId() + "&app_version=" + version)));
			imAppInfo.setUpload_time(releaseDate);
			imAppInfo.setSize(appInfo.getApkSize());
			imAppInfo.setScore(appInfo.getAvgComment());
			if (null != appInfo.getAppDesc()) {
				imAppInfo.setIntroduction(appInfo.getAppDesc().replaceAll("\\n", "<br>"));
			}
			if (null != appScreenshots && 0 != appScreenshots.size()) {
				for (TAppScreenshot appScreenshot : appScreenshots) {
					imAppInfo.getScreenshotList().add(UtilTool.getDownloadImageHttpURL(appScreenshot.getScreenshot()));
				}
			}
			// sb.append("</ScreenshotList><PermissionList>");
			// sb.append("</PermissionList><offical><![CDATA[");
			imAppInfo.setApp_remark(appInfo.getApp_remark());
			imAppInfo.setOffical(appInfo.getOfficial());
			imAppInfo.setAd(appInfo.getAdvertise());
			imAppInfo.setFee(appInfo.getSellType() !=null ? appInfo.getSellType() :'0');
			List<String> safeResult = parseSafeInfo(appInfo.getSafe()); 
			if(safeResult != null && safeResult.size() > 0){
				imAppInfo.setStag('1');
				imAppInfo.setSafe(safeResult);
			}else{
				imAppInfo.setStag('0');
			}
		} catch (Exception e) {
			throw e;
		}

	}
	private void clearUnnessceries(List<ActivityInfo> list) {
		if(list !=null){
			for(ActivityInfo info :list){
				info.setAppInfo(null);
			}
		}
	}

	@Override
	public int countAppsByKeyNameForModel(SearchKey searchKey) throws Exception{
        Model modelClass = null;
        String model = searchKey.getModel();
//        if (null != model && !"".equals(model)) {
//            model = model.split("@")[0];
//            modelClass = modelInfoDAO.findModelByMdName(model.trim());
//        }
        modelClass = modelInfoService.findModelByMdName(model);
        int count = iAppInfoDAO.countAppsByKeyNameForModel(searchKey, modelClass);  
        return count;
    }

	private List<String> parseSafeInfo(String safe){
		if(safe != null && safe.length() == 3){//默认三家公司 i管家/360/QQ
			char[] c = safe.toCharArray();
			if( 0 > Arrays.binarySearch(c, '2')){//不存在不安全
				List<String> result = new ArrayList<String>(3);
				for(int i=0; i<c.length ;i++){
					if(c[i] == '1'){
						result.add(tips[i]);
					}
				}
				return result;
			};
		}
		return null;
	}
	String [] tips = new String[]{"i管家检测通过","360检测通过","腾讯检测通过"};
}
