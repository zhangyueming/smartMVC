package com.smart.test.service;

public interface IUserService {
	
	/**
	 * �û����Խӿ� �ж��û���Ч��
	 * @date 2017-2-20 
	 * @author yueming.zhang
	 * @param userName 
	 * @param pwd 
	 * @return
	 */
	public boolean checkUserByLoginNameAndPwd(String userName, String pwd);
	
}
