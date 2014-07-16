package com.bbkmobile.iqoo.interfaces.index.dao;

import java.sql.SQLException;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.stereotype.Repository;

import com.bbkmobile.iqoo.common.appinfo.AppInfoFilter;
import com.bbkmobile.iqoo.common.page.PageVO;
import com.bbkmobile.iqoo.common.vo.NewAppsResultAppInfo;
import com.bbkmobile.iqoo.console.dao.modelmgr.Model;
import com.bbkmobile.iqoo.console.index.dao.IndexModelSubTitle;
import com.bbkmobile.iqoo.interfaces.common.AnnotationBaseDao;
@Repository
public class AmustDaoImpl extends AnnotationBaseDao implements AmustDao {

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getAmustSubTitles(char tag) throws Exception {
		String sql = "select subtitle from t_index_model_subtitle where tag = ? order by show_order ";
		return getSession().createSQLQuery(sql).setCharacter(0, tag).list();
	}

	@Override
	public List<NewAppsResultAppInfo> getAmustAppInfo(final IndexModelSubTitle title,
			final Model model, final PageVO page) throws Exception {
		return getHibernateTemplate().execute(new HibernateCallback<List<NewAppsResultAppInfo>>() {
			@SuppressWarnings("unchecked")
			@Override
			public List<NewAppsResultAppInfo> doInHibernate(Session session)
					throws HibernateException, SQLException {
				List<NewAppsResultAppInfo>  list = new AppInfoFilter(session,NewAppsResultAppInfo.class) {
					@Override
					public String preSql() {
						return "select appinfo1_.updateDate as upload_time , appinfo1_.app_remark,";
					}
					
					@Override
					public String postSql() {
						if(model != null && model.getSeries_id() > 0) {
							return " from t_index_model_subtitle subtitle_,t_index_model_apps this_ ,t_app_info appinfo1_ "
									+ " where this_.app_id = appinfo1_.id and this_.sub_id = subtitle_.id and subtitle_.subtitle = :subtitle ";
						}else{
							return " from t_index_model_subtitle subtitle_,t_index_model_apps this_ ,t_app_info appinfo1_ "
									+ " where this_.app_id = appinfo1_.id and this_.sub_id = subtitle_.id and subtitle_.subtitle = :subtitle ";
						}
					}
					@Override
					public String orderSq() {
						return " order by this_.show_order asc ";
					}
					
					@Override
					public void queryAssign() {
						this.getQuery().addScalar("upload_time").addScalar("app_remark");
						this.getQuery().setString("subtitle", title.getSubTitle());
						if(page != null){
							this.getQuery().setInteger("size", page.getNumPerPage()+1);
						}
					}
				}.execute(page, model);
				return list;
			}
		});
	}

}
