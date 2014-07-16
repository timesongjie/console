package com.bbkmobile.iqoo.common.http;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class SessionListener implements HttpSessionListener{

	@Override
	public void sessionCreated(HttpSessionEvent event) {
		System.out.println("session created");
		event.getSession().invalidate();
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent event) {
		
	}

}
