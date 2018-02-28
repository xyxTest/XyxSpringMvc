package com.xyx.spring.DaoImpl;

import com.xyx.spring.Dao.BaseDao;
import com.xyx.spring.Dao.FileDao;
import com.xyx.spring.Enums.ErrorCodeEnum;
import com.xyx.spring.Model.Files;
import com.xyx.spring.Utils.DataWrapper;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;
@Repository
public class FileDaoImpl extends BaseDao<Files> implements FileDao {

    @Override
    public boolean addFiles(Files File) {
        return save(File);
    }
    @Override
    public boolean deleteFiles(Long id) {
        return delete(get(id));
    }


    @SuppressWarnings("unchecked")
	@Override
    public DataWrapper<List<Files>> getFilesList() {
        DataWrapper<List<Files>> retDataWrapper = new DataWrapper<List<Files>>();
        List<Files> ret = new ArrayList<Files>();
        Session session = getSession();
        Criteria criteria = session.createCriteria(Files.class);
//        criteria.addOrder(Order.desc("publishDate"));
        try {
            ret = criteria.list();
        }catch (Exception e){
            e.printStackTrace();
        }
        retDataWrapper.setData(ret);
        return retDataWrapper;
    }

	@Override
	public Files getById(Long id) {
		// TODO Auto-generated method stub
		return get(id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public DataWrapper<Files> getByName(String name) {
		DataWrapper<Files> dataWrapper = new DataWrapper<Files>();
		String sql="select * from file where name="+name;
		Session session=getSession();
		 try{
	            Query query=session.createSQLQuery(sql).addEntity(Files.class);
	            List<Files> test=query.list();
	            dataWrapper.setData(test.get(0));
	        }catch(Exception e){
	            e.printStackTrace();
	            dataWrapper.setErrorCode(ErrorCodeEnum.Target_Not_Existed);
	        }
		 
		return dataWrapper;
	}
}
