package com.smart.test.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smart.framework.annotation.Service;
import com.smart.test.controller.LoginController;
import com.smart.test.service.IUserService;

/**
 * ²âÊÔService
 * @author yueming.zhang
 *
 */
@Service("userService")
public class UserServiceImpl implements IUserService {
	
	private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

	public boolean checkUserByLoginNameAndPwd(String userName, String pwd) {
		
		logger.info("Srevice : UserServiceImpl --> Method : checkUserByLoginNameAndPwd ");
		
		if(userName.equals("admin") && pwd.equals("123")){
			return true;
		}
		return false;
	}

}
