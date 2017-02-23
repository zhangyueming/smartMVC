package com.smart.test.service;

public interface IUserService {
	
	/**
	 * 用户测试接口 判断用户有效性
	 * @date 2017-2-20 
	 * @author yueming.zhang
	 * @param userName 
	 * @param pwd 
	 * @return
	 */
	public boolean checkUserByLoginNameAndPwd(String userName, String pwd);
	
}
