package com.bbkmobile.iqoo.common.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.bbkmobile.iqoo.common.util.cfgfile.ServerCfgFileInfo;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

public class EhcacheUtil {  
    
    private static final String path = ServerCfgFileInfo.getEhcacheCfgFile();  
//  
    private CacheManager manager;   
  
    private static class EhcacheFactory{
    	private static EhcacheUtil ehCache = new EhcacheUtil(); 
    }
  
    public EhcacheUtil() {  
        manager = CacheManager.create(path);  
    }  
    public EhcacheUtil(CacheManager manager){
    	 this.manager = manager;
    }
    public static EhcacheUtil getInstance() {  
        return EhcacheFactory.ehCache; 
    }  
  
    public void put(String cacheName, String key, Object value) {  
        Cache cache = manager.getCache(cacheName);  
        Element element = new Element(key, value);
        cache.put(element);  
    }  
  
    public Object get(String cacheName, String key) {  
        Cache cache = manager.getCache(cacheName);  
        Element element = cache.get(key);  
        return element == null ? null : element.getObjectValue();  
    }  
  
    public Cache get(String cacheName) {  
        return manager.getCache(cacheName);  
    }  
  
    public void remove(String cacheName, String key) {  
        Cache cache = manager.getCache(cacheName);  
        cache.remove(key);  
    }  
    
    public int getSize(String cacheName){
        Cache cache = manager.getCache(cacheName);  
        return cache.getSize();
    }
    
    public void removeAll(String cacheName, String key) {  
        Cache cache = manager.getCache(cacheName);  
        cache.removeAll();
    }
    
    public void addCache(String cacheName){
    	if(null==manager.getCache(cacheName)){
    		manager.addCache(cacheName);
    	}
    }
    
    public String getDateFromTimetamp(Long timetamp){
    	SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date dt = new Date(timetamp); 
		String sDateTime = sdf.format(dt);
		return sDateTime;
    }
    
    private String getCacheInfo(String cacheName,String isPrintKey) {
        StringBuffer sb = new StringBuffer();
        Cache cache = manager.getCache(cacheName);
        sb.append("<cache name=\"" + cacheName + "\" size=\"" + cache.getSize()
                + "\" memorySize=\"" + (float)cache.calculateInMemorySize()/1024f/1024f + "M" + "\" >");
        
        if (null != isPrintKey && isPrintKey.equals("1")) {
            List list_key = cache.getKeys();
            if (null != list_key && list_key.size() > 0) {

                for (Object key : list_key) {
                    Element el = cache.get(key);
                    if (null != el) {
                        sb.append("<member key=\"" + key + "\" hitCount=\"" + el.getHitCount() + "\" creationTime=\""
                                + getDateFromTimetamp(el.getCreationTime()) + "\" lastAccessTime=\""
                                + getDateFromTimetamp(el.getLastAccessTime()) + "\" >");
                        sb.append("</member>");
                    }
                }
            }
        }
        sb.append("</cache>");
        return sb.toString();
    }

    public String getEhCacheXml(String ehcacheName, String isPrintKey) {
        String xml_data = null;
        StringBuffer sb = new StringBuffer();
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?>");
        sb.append("<ehCache>");

        if (null != ehcacheName && !ehcacheName.equals("")) {
            if (ehcacheName.equals("allcaches")) {
                String[] cacheNames = manager.getCacheNames();
                if (null != cacheNames && cacheNames.length > 0) {
                    for (String cacheName : cacheNames) {

                        sb.append(getCacheInfo(cacheName, isPrintKey));
                    }
                } else {
                    sb.append("no caches");
                }
            } else {
                if (manager.cacheExists(ehcacheName)) {
                    sb.append(getCacheInfo(ehcacheName, isPrintKey));
                } else {
                    sb.append("<cache name=\"" + ehcacheName + "\">cache is inexistent</cache>");
                }

            }

        }

        sb.append("</ehCache>");
        xml_data = sb.toString();
        return xml_data;
    }
    /*
    public void printAll(String cacheName){
    	Cache cache = manager.getCache(cacheName);
    	List list_key=cache.getKeys();	
    	System.out.println("条数："+cache.getSize()+" 占用内存："+cache.calculateInMemorySize());
    	if(null!=list_key&&list_key.size()>0){
    		for(Object key:list_key){
    			System.out.println(cache.get(key));
    		}
    	}
    	System.out.println("*******************************************************************************");    	
    } 
    */
}  