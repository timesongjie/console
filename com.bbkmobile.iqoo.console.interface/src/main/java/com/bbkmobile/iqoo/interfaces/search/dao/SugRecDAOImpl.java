package com.bbkmobile.iqoo.interfaces.search.dao;

import java.util.List;

import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import com.bbkmobile.iqoo.common.appinfo.AppInfoFilter;
import com.bbkmobile.iqoo.console.dao.modelmgr.Model;
import com.bbkmobile.iqoo.interfaces.common.AnnotationBaseDao;
import com.bbkmobile.iqoo.interfaces.search.vo.SugRecApp;

@Repository
public class SugRecDAOImpl extends AnnotationBaseDao implements SugRecDAO{

	@SuppressWarnings("unchecked")
	@Override
	public List<SugRecApp> getSugRecApps(final String key,Model model) throws Exception {
		Session session = getSession();
		return new AppInfoFilter(session,SugRecApp.class) {
			@Override
			public String preSql() {
				return "select ";
			}
			
			@Override
			public String postSql() {
				return " from t_search_word_relation this_,t_app_info appinfo1_ where this_.app_id = appinfo1_.id and relation like :relation";
			}
			
			@Override
			public String orderSq() {
				return null;
			}

			@Override
			public void queryAssign() {
				this.getQuery().setString("relation","%"+key+"%");
			}
			
			
		}.execute(null, null);//TODO 新增机型判断
	}

}
