package com.smart.test.controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smart.framework.annotation.Controller;
import com.smart.framework.annotation.Qualifier;
import com.smart.framework.annotation.RequestMapping;
import com.smart.framework.annotation.RequestMethod;
import com.smart.test.service.IUserService;

@Controller
@RequestMapping("loginController")
public class LoginController {
	
	private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
	
	@Qualifier("userService")
	private IUserService userService;
	
	
	@RequestMapping(value="login",method=RequestMethod.GET)
	public void login(HttpServletRequest request, HttpServletResponse response, String userName, String pwd) {
		PrintWriter writer = null;
		
		try {
			logger.info("Controller : LoginController --> Method : login ");
			writer = response.getWriter();
			if(userService.checkUserByLoginNameAndPwd(userName, pwd)){
				writer.write("LOGIN SUCCESS HELLO SMARTMVC!");
			}else{
				writer.write("LOGIN FAIL TRY AGAIN!");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			writer.close();
		}
		
	}
}
