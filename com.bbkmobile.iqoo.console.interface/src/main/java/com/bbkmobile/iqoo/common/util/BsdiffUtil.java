package com.bbkmobile.iqoo.common.util;

public class BsdiffUtil {
    
    static {
        System.loadLibrary("Bsdiff"); 
    }
    // 所有native关键词修饰的都是对本地的声明
    public native int getPatchByBsdiff(String old_apk, String new_apk , String patch);

}
