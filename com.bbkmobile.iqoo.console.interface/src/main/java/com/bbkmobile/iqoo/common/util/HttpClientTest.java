package com.bbkmobile.iqoo.common.util;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;

public class HttpClientTest {

	public static void main(String[] args) {
		HttpClient client = new HttpClient();
		String url = "http://m.baidu.com/api?action=redirect&token=vivo&from=563i&type=app&dltype=new&tj=soft_4647827_703516_%E7%89%9B%E6%B4%A5%E9%AB%98%E9%98%B6%E8%8B%B1%E8%AF%AD%E8%AF%8D%E5%85%B8&blink=665c687474703a2f2f7777772e636f6f6c61706b2e636f6d2f646c3f763d5156424c4e6a4d774f574d324d6d78705a4126683d35363533376333323232353436643935393836393764303261333766346339302666726f6d3d62616964759a53&crversion=1&cfrom=35&module_id=4&imei=012345678987654&model=vivo+Xplay3S&elapsedtime=55138866&cs=0&target=baidu&update=0&related=0";
		String url2 = "http://dl.coolapk.com/dl2.php?dl=apk/8995/com.slovoed.oald/%E7%89%9B%E6%B4%A5%E9%AB%98%E9%98%B6%E8%8B%B1%E8%AF%AD%E8%AF%8D%E5%85%B8_Oxford_Advanced_Learner%27s_8/3.5.20/2013%2F1031%2Fcom.slovoed.oald_3.5.20.apk&v=1wVgVB";
		String url3 = "http://www.coolapk.com/dl?v=QVBLNjMwOWM2MmxpZA&h=56537c3222546d9598697d02a37f4c90&from=baidu";
		String url4 = "http://www.coolapk.com/dl?v=QVBLNmFjMjhjZWlodA&h=c0fe77";
		GetMethod get = new GetMethod(url4);
		try {
			get.setRequestHeader("User-Agent", "IQooAppstore");
			int status = client.executeMethod(get);
			long length = get.getResponseContentLength();
			System.out.println("响应 ：" + status + ",获取数据长度：" + length);//响应 ：200,获取数据长度：3554148
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
