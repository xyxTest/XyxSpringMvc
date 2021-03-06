package com.xyx.spring.Service;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.multipart.MultipartFile;

import com.xyx.spring.Model.User;
import com.xyx.spring.Model.Pojo.UserPojo;
import com.xyx.spring.Utils.DataWrapper;

public interface UserService {
	DataWrapper<Void> register(User user);
	DataWrapper<UserPojo> login(String userName,String password, Integer systemId);
	DataWrapper<Void> updateUser(User user,String token);
	DataWrapper<User> getUserDetails(String token);
	DataWrapper<UserPojo> getUserDetailsByAdmin(Long userId,String token);
	DataWrapper<List<UserPojo>> getUserList(Integer pageIndex, Integer pageSize,User user,String token);
	DataWrapper<Void> changeUserTypeByAdmin(Long userId, Integer userType, String token);
	DataWrapper<User> findUserLike(User user,String token);
	DataWrapper<Void> deleteUser(Long userId, String token);
	DataWrapper<Void> addUser(User user,String token, MultipartFile file, HttpServletRequest request);
	DataWrapper<Void> updateUserByAdmin(User user,String token, MultipartFile file,HttpServletRequest request);
	DataWrapper<User> FindPs(User user);
	DataWrapper<Void> updateUserBySelf(String oldPs,String newPs,String token);
	DataWrapper<List<UserPojo>> getUserTeam(String token,Long projectId);
	DataWrapper<List<UserPojo>> getUserLists(Integer pageIndex, Integer pageSize, User user, String token);
}
