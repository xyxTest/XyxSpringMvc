package com.xyx.spring.ServiceImpl;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.xyx.spring.Dao.UserDao;
import com.xyx.spring.Enums.ErrorCodeEnum;
import com.xyx.spring.Enums.UserTypeEnum;
import com.xyx.spring.Model.Files;
import com.xyx.spring.Model.User;
import com.xyx.spring.Model.Pojo.UserPojo;
import com.xyx.spring.Service.FileService;
import com.xyx.spring.Service.UserService;
import com.xyx.spring.Utils.BCrypt;
import com.xyx.spring.Utils.DataWrapper;
import com.xyx.spring.Utils.SessionManager;


@Service("userService")
public class UserServiceImpl implements UserService {
	@Autowired
	UserDao userDao;
	@Autowired
	FileService fileService;
	private String filePath="files";
    private Integer fileType=5;
    private SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
	@Override
	public DataWrapper<Void> register(User user) {
		// TODO Auto-generated method stub
		DataWrapper<Void> dataWrapper = new DataWrapper<>();
		if (user.getUserName() == null ||user.getUserName().equals("")
				|| user.getPassword() == null || user.getPassword().equals("")
				|| user.getRealName() == null || user.getRealName().equals("")) {
			dataWrapper.setErrorCode(ErrorCodeEnum.Empty_Inputs);
		} else if(userDao.getByUserName(user.getUserName()) != null) {
			dataWrapper.setErrorCode(ErrorCodeEnum.User_Existed);
		} else {
			user.setId(null);
			String passwordnews=BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
			user.setPassword(passwordnews);
			user.setUserType(UserTypeEnum.User.getType());
			//user.setUserType(user.getUserType());
			user.setRegisterDate(new Date());
			if(!userDao.addUser(user)) {
				dataWrapper.setErrorCode(ErrorCodeEnum.Error);
			}
			
		}
		return dataWrapper;
	}

	@Override
	public DataWrapper<UserPojo> login(String userName, String password,Integer system) {
		// TODO Auto-generated method stub
		DataWrapper<UserPojo> dataWrapper = new DataWrapper<UserPojo>();
		if (userName == null || password == null 
				||userName.equals("") || password.equals("")) {
			dataWrapper.setErrorCode(ErrorCodeEnum.Empty_Inputs);
		} else {
			User user = userDao.getByUserName(userName);
			if (user == null) {
				dataWrapper.setErrorCode(ErrorCodeEnum.User_Not_Existed);
			} else if(BCrypt.checkpw(password, user.getPassword())) {
				dataWrapper.setErrorCode(ErrorCodeEnum.Password_Error);
			} else {
				SessionManager.removeSessionByUserId(user.getId());
				String token = SessionManager.newSession(user);
				dataWrapper.setToken(token);
				UserPojo users=new UserPojo();
				users.setUserName(user.getUserName());
				users.setWorkName(user.getWorkName());
				users.setRealName(user.getRealName());
				users.setUserType(user.getUserType());
				users.setId(user.getId());
				if(user.getUserIcon()!=null){
					Files file=new Files();
					file=fileService.getById(user.getUserIcon());
					if(file!=null){
						users.setUserIconUrl(file.getUrl());
					}
				}
				dataWrapper.setData(users);
			}
		}
		
		return dataWrapper;
	}

	@Override
	public DataWrapper<Void> updateUser(User user, String token) {
		// TODO Auto-generated method stub
		DataWrapper<Void> dataWrapper = new DataWrapper<Void>();
		User userInMemory = SessionManager.getSession(token);
		if (userInMemory != null) {
			User userInDB = userDao.getById(userInMemory.getId());
			if (userInDB != null) {
				if(user.getRealName() != null && !user.getRealName().equals("")) {
					userInDB.setRealName(user.getRealName());
				}
				if (user.getEmail() != null && !user.getEmail().equals("")) {
					userInDB.setEmail(user.getEmail());
				}
				if (user.getTel() != null && !user.getTel().equals("")) {
					userInDB.setTel(user.getTel());
				}
				if (!userDao.updateUser(userInDB)) {
					dataWrapper.setErrorCode(ErrorCodeEnum.Error);
				}
			} else {
				dataWrapper.setErrorCode(ErrorCodeEnum.User_Not_Existed);
			}
		} else {
			dataWrapper.setErrorCode(ErrorCodeEnum.User_Not_Logined);
		}
		return dataWrapper;
	}
	
	@Override
	public DataWrapper<Void> updateUserByAdmin(User user, String token,MultipartFile file,HttpServletRequest request) {
		// TODO Auto-generated method stub
		DataWrapper<Void> dataWrapper = new DataWrapper<Void>();
		User adminInMemory = SessionManager.getSession(token);
		if (adminInMemory != null && adminInMemory.getUserType() == UserTypeEnum.Admin.getType()) {
			User userInDB = userDao.getById(user.getId());
			if (userInDB != null) {
				if(file !=null){
						String path=filePath+"/"+"userIcons"+"/";
						Files newfile=fileService.uploadFile(path, file,fileType,request);
						userInDB.setUserIcon(newfile.getId());
				}
				if(user.getUserName() != null && !user.getUserName().equals("")) {
					userInDB.setUserName(user.getUserName());
				}
				if(!user.getPassword().equals(userInDB.getPassword())){
					userInDB.setPassword(BCrypt.hashpw(userInDB.getPassword(), BCrypt.gensalt()));
				}
				if(user.getRealName() != null && !user.getRealName().equals("")) {
					userInDB.setRealName(user.getRealName());
				}
				if(user.getEmail() != null && !user.getEmail().equals("")) {
					userInDB.setEmail(user.getEmail());
				}
				if (user.getWorkName() != null && !user.getWorkName().equals("")) {
					userInDB.setWorkName(user.getWorkName());
				}
				if (user.getTel() != null && !user.getTel().equals("")) {
					userInDB.setTel(user.getTel());
				}
				if(user.getUserType() !=null && !user.getUserType().equals(""))
				{
					userInDB.setUserType(user.getUserType());
				}
				if (!userDao.updateUser(userInDB)) {
					dataWrapper.setErrorCode(ErrorCodeEnum.Error);
				}
			} else {
				dataWrapper.setErrorCode(ErrorCodeEnum.User_Not_Existed);
			}
		} else {
			dataWrapper.setErrorCode(ErrorCodeEnum.AUTH_Error);
		}
		return dataWrapper;
	}
	
	@Override
	public DataWrapper<Void> updateUserBySelf(String oldPs, String newPs, String token) {
		// TODO Auto-generated method stub
		DataWrapper<Void> dataWrapper = new DataWrapper<Void>();
		User adminInMemory = SessionManager.getSession(token);
		int flag=-1;
		if (adminInMemory != null) {
			if(oldPs!=null && newPs!=null)
			{
				if(adminInMemory.getPassword().equals(oldPs)){
					flag=1;
					adminInMemory.setPassword(BCrypt.hashpw(newPs, BCrypt.gensalt()));
				}else{
					dataWrapper.setErrorCode(ErrorCodeEnum.Password_Not_Fit);
				}
			}else{
				dataWrapper.setErrorCode(ErrorCodeEnum.Empty_Inputs);
			}
			if(flag>0){
				if (!userDao.updateUser(adminInMemory)) {
					dataWrapper.setErrorCode(ErrorCodeEnum.Error);
				}
			}
		} else {
			dataWrapper.setErrorCode(ErrorCodeEnum.User_Not_Logined);
		}
		return dataWrapper;
	}
	@Override
	public DataWrapper<User> getUserDetails(String token) {
		// TODO Auto-generated method stub
		DataWrapper<User> dataWrapper = new DataWrapper<User>();
		User userInMemory = SessionManager.getSession(token);
		if (userInMemory != null) {
			User userInDB = userDao.getById(userInMemory.getId());
			userInDB.setPassword(null);
			dataWrapper.setData(userInDB);
		} else {
			dataWrapper.setErrorCode(ErrorCodeEnum.User_Not_Logined);
		}
		return dataWrapper;
	}

	@Override
	public DataWrapper<UserPojo> getUserDetailsByAdmin(Long userId, String token) {
		// TODO Auto-generated method stub
		DataWrapper<UserPojo> dataWrappers = new DataWrapper<UserPojo>();
		DataWrapper<User> dataWrapper = new DataWrapper<User>();
		User adminInMemory = SessionManager.getSession(token);
		if (adminInMemory != null) {
			User adminInDB = new User();
			if(userId!=null && (adminInMemory.getUserType()==0 || adminInMemory.getWorkName().equals("总经理"))){
				adminInDB = userDao.getById(userId);
			}else{
				adminInDB = userDao.getById(adminInMemory.getId());
			}
			dataWrapper.setData(adminInDB);
			if(dataWrapper.getData()!=null){
				UserPojo userpojo=new UserPojo();
				userpojo.setEmail(dataWrapper.getData().getEmail());
				userpojo.setId(dataWrapper.getData().getId());
				userpojo.setPassword(dataWrapper.getData().getPassword());
				userpojo.setRealName(dataWrapper.getData().getRealName());
				 
				userpojo.setRegisterDate(sdf.format(dataWrapper.getData().getRegisterDate()));
				userpojo.setTel(dataWrapper.getData().getTel());
				userpojo.setUserName(dataWrapper.getData().getUserName());
				userpojo.setUserType(dataWrapper.getData().getUserType());
				userpojo.setUserIcon(dataWrapper.getData().getUserIcon());
				userpojo.setWorkName(dataWrapper.getData().getWorkName());
				if(dataWrapper.getData().getUserIcon()!=null){
					Files file=new Files();
					file=fileService.getById(dataWrapper.getData().getUserIcon());
					if(file!=null){
						userpojo.setUserIconUrl(file.getUrl());
					}
				}
				dataWrappers.setData(userpojo);
			}
		} else {
			dataWrappers.setErrorCode(ErrorCodeEnum.User_Not_Logined);
		}
		return dataWrappers;
	}

	@Override
	public DataWrapper<List<UserPojo>> getUserList(Integer pageIndex, Integer pageSize, User user, String token) {
		// TODO Auto-generated method stub
		DataWrapper<List<UserPojo>> dataWrapper = new DataWrapper<List<UserPojo>>();
		List<UserPojo> userpojoList=new ArrayList<UserPojo>();
		DataWrapper<List<User>> userList=new DataWrapper<List<User>>();
		User adminInMemory = SessionManager.getSession(token);
		if (adminInMemory != null) {
			//if (adminInMemory.getUserType() == UserTypeEnum.Admin.getType() || adminInMemory.getTeamId()!=null || adminInMemory.getWorkName().equals("总经理")) {
			if(adminInMemory.getUserType()==0){
				userList=userDao.getUserList(pageSize, pageIndex,user);
			}else{
				userList=userDao.getUserLists(pageSize, pageIndex,user);
			}
			
			if(userList.getData().size()>0){
				for(int i=0;i<userList.getData().size();i++){
					UserPojo userpojo=new UserPojo();
					userpojo.setEmail(userList.getData().get(i).getEmail());
					userpojo.setId(userList.getData().get(i).getId());
					userpojo.setPassword(userList.getData().get(i).getPassword());
					userpojo.setRealName(userList.getData().get(i).getRealName());
					userpojo.setTel(userList.getData().get(i).getTel());
					userpojo.setRegisterDate(sdf.format(userList.getData().get(i).getRegisterDate()));
					userpojo.setUserName(userList.getData().get(i).getUserName());
					userpojo.setUserType(userList.getData().get(i).getUserType());
					userpojo.setUserIcon(userList.getData().get(i).getUserIcon());
					userpojo.setWorkName(userList.getData().get(i).getWorkName());
					if(userList.getData().get(i).getUserIcon()!=null){
						Files file=fileService.getById(userList.getData().get(i).getUserIcon());
						if(file!=null){
							userpojo.setUserIconUrl(file.getUrl());
						}
					}
					userpojoList.add(i, userpojo);
				}
				dataWrapper.setData(userpojoList);
				dataWrapper.setCallStatus(userList.getCallStatus());
				dataWrapper.setCurrentPage(userList.getCurrentPage());
				dataWrapper.setErrorCode(userList.getErrorCode());
				dataWrapper.setNumberPerPage(userList.getNumberPerPage());
				dataWrapper.setTotalNumber(userList.getTotalNumber());
				dataWrapper.setTotalPage(userList.getTotalPage());
			}
				
			/*} else {
				dataWrapper.setErrorCode(ErrorCodeEnum.AUTH_Error);
			}*/
		} else {
			dataWrapper.setErrorCode(ErrorCodeEnum.User_Not_Logined);
		}
		return dataWrapper;
	}
	@Override
	public DataWrapper<List<UserPojo>> getUserLists(Integer pageIndex, Integer pageSize, User user, String token) {
		// TODO Auto-generated method stub
		DataWrapper<List<UserPojo>> dataWrapper = new DataWrapper<List<UserPojo>>();
		List<UserPojo> userpojoList=new ArrayList<UserPojo>();
		DataWrapper<List<User>> userList=new DataWrapper<List<User>>();
		User adminInMemory = SessionManager.getSession(token);
		if (adminInMemory != null) {
			userList=userDao.getUserLists(pageSize, pageIndex,user);
			if(userList.getData().size()>0){
				for(int i=0;i<userList.getData().size();i++){
					UserPojo userpojo=new UserPojo();
					userpojo.setEmail(userList.getData().get(i).getEmail());
					userpojo.setId(userList.getData().get(i).getId());
					userpojo.setPassword(userList.getData().get(i).getPassword());
					userpojo.setRealName(userList.getData().get(i).getRealName());
					userpojo.setTel(userList.getData().get(i).getTel());
					userpojo.setRegisterDate(sdf.format(userList.getData().get(i).getRegisterDate()));
					userpojo.setUserName(userList.getData().get(i).getUserName());
					userpojo.setUserType(userList.getData().get(i).getUserType());
					userpojo.setUserIcon(userList.getData().get(i).getUserIcon());
					userpojo.setWorkName(userList.getData().get(i).getWorkName());
					if(userList.getData().get(i).getUserIcon()!=null){
						Files file=fileService.getById(userList.getData().get(i).getUserIcon());
						if(file!=null){
							userpojo.setUserIconUrl(file.getUrl());
						}
					}
					userpojoList.add(i, userpojo);
				}
				dataWrapper.setData(userpojoList);
				dataWrapper.setCallStatus(userList.getCallStatus());
				dataWrapper.setCurrentPage(userList.getCurrentPage());
				dataWrapper.setErrorCode(userList.getErrorCode());
				dataWrapper.setNumberPerPage(userList.getNumberPerPage());
				dataWrapper.setTotalNumber(userList.getTotalNumber());
				dataWrapper.setTotalPage(userList.getTotalPage());
			}
				
		} else {
			dataWrapper.setErrorCode(ErrorCodeEnum.User_Not_Logined);
		}
		return dataWrapper;
	}
	@Override
	public DataWrapper<List<UserPojo>> getUserTeam(String token,Long projectId) {
		// TODO Auto-generated method stub
		DataWrapper<List<UserPojo>> dataWrapper = new DataWrapper<List<UserPojo>>();
		List<UserPojo> userpojoList=new ArrayList<UserPojo>();
		DataWrapper<List<User>> userList=new DataWrapper<List<User>>();
		User adminInMemory = SessionManager.getSession(token);
		if (adminInMemory != null) {
				userList=userDao.getUserTeam(null, -1,projectId);
				if(userList.getData().size()>0){
					for(int i=0;i<userList.getData().size();i++){
						UserPojo userpadpojo=new UserPojo();
						if(userpadpojo.getId()>0){
							userpadpojo.setRealName(userList.getData().get(i).getRealName());
							userpadpojo.setTel(userList.getData().get(i).getTel());
							userpadpojo.setWorkName(userList.getData().get(i).getWorkName());
							userpojoList.add(userpadpojo);
						}
					}
					dataWrapper.setData(userpojoList);
					dataWrapper.setCallStatus(userList.getCallStatus());
					dataWrapper.setCurrentPage(userList.getCurrentPage());
					dataWrapper.setErrorCode(userList.getErrorCode());
					dataWrapper.setNumberPerPage(userList.getNumberPerPage());
					dataWrapper.setTotalNumber(userList.getTotalNumber());
					dataWrapper.setTotalPage(userList.getTotalPage());
				}
		} else {
			dataWrapper.setErrorCode(ErrorCodeEnum.User_Not_Logined);
		}
		return dataWrapper;
	}

	@Override
	public DataWrapper<Void> changeUserTypeByAdmin(Long userId, Integer userType, String token) {
		// TODO Auto-generated method stub
		DataWrapper<Void> dataWrapper = new DataWrapper<Void>();
		User adminInMemory = SessionManager.getSession(token);
		if (adminInMemory != null) {
			User adminInDB = userDao.getById(adminInMemory.getId());
			if (adminInDB.getUserType() == UserTypeEnum.Admin.getType()) {
				User userInDB = userDao.getById(userId);
				userInDB.setUserType(userType);
				if (!userDao.updateUser(userInDB)) {
					dataWrapper.setErrorCode(ErrorCodeEnum.Error);
				}
			} else {
				dataWrapper.setErrorCode(ErrorCodeEnum.AUTH_Error);
			}
		} else {
			dataWrapper.setErrorCode(ErrorCodeEnum.User_Not_Logined);
		}
		return dataWrapper;
	}

	@Override
	public DataWrapper<User> findUserLike(User user, String token) {
		// TODO Auto-generated method stub
		DataWrapper<User> dataWrapper = new DataWrapper<User>();
		User adminInMemory = SessionManager.getSession(token);
		if(adminInMemory!=null){
			if(user!=null){
				dataWrapper=userDao.findUserLike(user);
				if(dataWrapper!=null && dataWrapper.getData()!=null){
					dataWrapper.setErrorCode(ErrorCodeEnum.Error);
				}
			}else{
				dataWrapper.setErrorCode(ErrorCodeEnum.Empty_Inputs);
			}
			
		}else{
			dataWrapper.setErrorCode(ErrorCodeEnum.User_Not_Logined);
		}
		return dataWrapper;
	}

	@Override
	public DataWrapper<Void> deleteUser(Long userId, String token) {
		DataWrapper<Void> dataWrapper=new DataWrapper<Void>();
		User userInMemory=SessionManager.getSession(token);
		if(userInMemory!=null){
			User adminInDB = userDao.getById(userInMemory.getId());
			if (adminInDB.getUserType() == UserTypeEnum.Admin.getType()) {
				if(userDao.deleteUser(userId)){
					dataWrapper.setErrorCode(ErrorCodeEnum.No_Error);
				}else{
					dataWrapper.setErrorCode(ErrorCodeEnum.Target_Not_Existed);
				}
			}else{
				dataWrapper.setErrorCode(ErrorCodeEnum.AUTH_Error);
			}
		}else{
			dataWrapper.setErrorCode(ErrorCodeEnum.User_Not_Logined);
		}
		return dataWrapper;
	}

	@Override
	public DataWrapper<Void> addUser(User user,String token,MultipartFile file, HttpServletRequest request) {
		DataWrapper<Void> dataWrapper = new DataWrapper<>();
		User userInMemory=SessionManager.getSession(token);
		if(userInMemory!=null){
			User adminInDB = userDao.getById(userInMemory.getId());
			if (adminInDB.getUserType() == UserTypeEnum.Admin.getType()) {
				if (user.getUserName() == null ||user.getUserName().equals("")
						|| user.getPassword() == null || user.getPassword().equals("")
						|| user.getRealName() == null || user.getRealName().equals("")) {
					dataWrapper.setErrorCode(ErrorCodeEnum.Empty_Inputs);
				} else if(userDao.getByUserName(user.getUserName()) != null) {
					dataWrapper.setErrorCode(ErrorCodeEnum.User_Existed);
				} else {
					if(file !=null){
						String path=filePath+"/"+"userIcons"+"/";
						Files newfile=fileService.uploadFile(path, file,fileType,request);
						user.setUserIcon(newfile.getId());
					}
					user.setId(null);
					user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
					//user.setUserType(user.getUserType());
					user.setRegisterDate(new Date());
					if(!userDao.addUser(user)) {
						dataWrapper.setErrorCode(ErrorCodeEnum.Error);
					}
					
				}
			}else{
				dataWrapper.setErrorCode(ErrorCodeEnum.AUTH_Error);
			}
		}else{
			dataWrapper.setErrorCode(ErrorCodeEnum.User_Not_Logined);
		}
		
		return dataWrapper;
	}

	@Override
	public DataWrapper<User> FindPs(User user) {
		// TODO Auto-generated method stub
		DataWrapper<User> dataWrapper = new DataWrapper<User>(); 
		dataWrapper=userDao.findUserLike(user);
		return dataWrapper;
	}

}
