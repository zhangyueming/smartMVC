package com.smart.framework.servlet;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smart.framework.annotation.Controller;
import com.smart.framework.annotation.Qualifier;
import com.smart.framework.annotation.RequestMapping;
import com.smart.framework.annotation.Service;
import com.smart.test.controller.LoginController;


/**
 * 核心入口类
 * @author yueming.zhang
 *
 */
@SuppressWarnings("serial")
public class DispatcherServlet extends HttpServlet{
	
	private static final Logger logger = LoggerFactory.getLogger(DispatcherServlet.class);
	
	public static final String INCLUDE_REQUEST_URI_ATTRIBUTE = "javax.servlet.include.request_uri";
	
	private List<String> packageNames = new ArrayList<String>();
	
	private Map<String, Object> instanceMap = new HashMap<String, Object>(); 
	
	private Map<String, Object> handlerMap = new HashMap<String, Object>();
	
	public void init() throws ServletException {
		logger.info(" DispatcherServlet : init   --> start");
		
		System.out.println(" DispatcherServlet : init   --> start");
		
		//扫描基包 获得基包下的全部类
		scanPackage("com.smart");
		
		//实例化这些类
		try {
			processClassAndInstance();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		//ioc 
		ioc();
		
		//创建反应映射
		processHandlerMapping();
		
	}
	
	/**
	 * 将请求的路径与方法进行映射 并存放在缓存中
	 * 
	 * @date 2017-2-21 
	 * @author yueming.zhang
	 */
	private void processHandlerMapping(){
		if(instanceMap.size() <= 0){
			return;
		}
		for(Map.Entry<String, Object> entry:instanceMap.entrySet()){
			//只有类是Controller的时候才处理映射
			try {
				if(entry.getValue().getClass().isAnnotationPresent(Controller.class)){
					RequestMapping rqm = (RequestMapping)entry.getValue().getClass().getAnnotation(RequestMapping.class);
					String ctrPath = "";
					if(rqm != null){
						ctrPath = rqm.value();
					}else{
						throw new RuntimeException("Controller 没有 配置对应的 requestMapping属性");
					}
					//TODO Controller可以不配置RequestMapping 直接使用Controller标签方式 这种方式默认会也type的形式进行装配后续补充
//				else{
//				}
					
					Method[] methods = entry.getValue().getClass().getMethods();
					for(Method method:methods){
						if(method.isAnnotationPresent(RequestMapping.class)){
							RequestMapping rqs = method.getAnnotation(RequestMapping.class);
							String rqsPath = rqs.value();
							
							//存储方法映射
							handlerMap.put("/"+ctrPath+"/"+rqsPath, method);
						}
					}
				}
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 将类中带有qualifier标签的属性进行注入
	 * 
	 * @date 2017-2-21 
	 * @author yueming.zhang
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	private void ioc() {
		if(instanceMap.size() <= 0){
			return;
		}
		for(Map.Entry<String, Object> entry:instanceMap.entrySet()){
			//获得实例中全部的属性
			Field[] fields = entry.getValue().getClass().getDeclaredFields();
			
			for(Field field:fields){
				if(field.isAnnotationPresent(Qualifier.class)){//判断是否带有qualifier标签
					Qualifier qlf = field.getAnnotation(Qualifier.class);
					
					String fieldName = qlf.value();//获得类注册名
					//将实例注册到当前的类中 
					field.setAccessible(true); //允许私有属性修改
					try {
						field.set(entry.getValue(), instanceMap.get(fieldName));
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	/**
	 * 解析类对象标签注解的类， 并且进行实例化 缓存备用
	 * 
	 * @date 2017-2-21 
	 * @author yueming.zhang
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	private void processClassAndInstance() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		if(packageNames.size() <= 0){
			return;
		}
		for(String packageName:packageNames){
			System.out.println("packageName==="+packageName);
			Class<?> clazz = Class.forName(packageName.replace(".class", ""));
			if(clazz.isAnnotationPresent(Controller.class)){//判断是否是Controller标签注解的类
				if(clazz.isAnnotationPresent(RequestMapping.class)){//如果是Controller类那么继续获得request
					Object instance = clazz.newInstance();//创建对象实例
					RequestMapping rmp = (RequestMapping)clazz.getAnnotation(RequestMapping.class);//获得requestmapping注解对象并获得value
					String key = rmp.value();
					instanceMap.put(key, instance);//奖类的注册名与类实例进行缓存
				}
				//TODO 这里需要可以兼容 Controller默认requestMapping的形式
//				else{
//					
//				}
			}else if(clazz.isAnnotationPresent(Service.class)){//判断service标注注解的类
				Object instance = clazz.newInstance();//创建对象实例
				Service sc = (Service)clazz.getAnnotation(Service.class);//获得Service注解对象并获得value
				String key = sc.value();
				instanceMap.put(key, instance);//奖类的注册名与类实例进行缓存
			}else{
				continue;
			}
		}
	}

	/**
	 * 扫描基包下的全部类 并且实例化这些类  这里我们的基包是（com.smartmvc） 
	 * 当然这里应该从配置文件进行获取，这里当加入xml解析的时候进行出补充
	 * 获得对象的完整限定名之后进行存储
	 */
	private void scanPackage(String basePackage){
		URL url = this.getClass().getClassLoader().getResource("/"+replaceToPath(basePackage));
		String filePath = url.getFile();//获得文件的路径
		File file = new File(filePath);//获得这些类文件
		String[] files = file.list();
		for(String path:files){
			File eachFile = new File(filePath+path); 
			if(eachFile.isDirectory()){
				scanPackage(basePackage + "." + eachFile.getName());
			}else{
				packageNames.add(basePackage + "." + eachFile.getName());
			}
 		}
		
	}
	
	private String replaceToPath(String path){
		return path.replaceAll("\\.", "/");
	}
	
	
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}
	
	/**
	 * 在方法的属性上 method 为post的时候进入此方法  这里目前没有做处理，默认所有请求进入到post方法中来
	 * 后面进行补充
	 */
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		//获得请求地址
		String uri = getUri(req);
		String contextPath = req.getContextPath();
		String path = uri.replace(contextPath, "");//去掉请求中的congtextPath
		
		String regex = ".do$";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(path);
		
		if(m.find()){
			String methodPath = path.substring(0,path.indexOf(".do"));
			Method method = (Method)handlerMap.get(methodPath);
			
			try {
				method.invoke(instanceMap.get(methodPath.split("/")[1]), new Object[]{req,resp,"admin","1234"});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			throw new RuntimeException("url不正确");
		}
	}
	
	//获得请求的uri优化处理
	private String getUri(HttpServletRequest req){
		String uri = (String) req.getAttribute(INCLUDE_REQUEST_URI_ATTRIBUTE);
		if(uri == null){
			uri = req.getRequestURI();
		}
		return uri;
	}
	
	
}
