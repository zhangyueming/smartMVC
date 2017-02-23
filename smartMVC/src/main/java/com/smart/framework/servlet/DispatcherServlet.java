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
 * ���������
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
		
		//ɨ����� ��û����µ�ȫ����
		scanPackage("com.smart");
		
		//ʵ������Щ��
		try {
			processClassAndInstance();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		//ioc 
		ioc();
		
		//������Ӧӳ��
		processHandlerMapping();
		
	}
	
	/**
	 * �������·���뷽������ӳ�� ������ڻ�����
	 * 
	 * @date 2017-2-21 
	 * @author yueming.zhang
	 */
	private void processHandlerMapping(){
		if(instanceMap.size() <= 0){
			return;
		}
		for(Map.Entry<String, Object> entry:instanceMap.entrySet()){
			//ֻ������Controller��ʱ��Ŵ���ӳ��
			try {
				if(entry.getValue().getClass().isAnnotationPresent(Controller.class)){
					RequestMapping rqm = (RequestMapping)entry.getValue().getClass().getAnnotation(RequestMapping.class);
					String ctrPath = "";
					if(rqm != null){
						ctrPath = rqm.value();
					}else{
						throw new RuntimeException("Controller û�� ���ö�Ӧ�� requestMapping����");
					}
					//TODO Controller���Բ�����RequestMapping ֱ��ʹ��Controller��ǩ��ʽ ���ַ�ʽĬ�ϻ�Ҳtype����ʽ����װ���������
//				else{
//				}
					
					Method[] methods = entry.getValue().getClass().getMethods();
					for(Method method:methods){
						if(method.isAnnotationPresent(RequestMapping.class)){
							RequestMapping rqs = method.getAnnotation(RequestMapping.class);
							String rqsPath = rqs.value();
							
							//�洢����ӳ��
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
	 * �����д���qualifier��ǩ�����Խ���ע��
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
			//���ʵ����ȫ��������
			Field[] fields = entry.getValue().getClass().getDeclaredFields();
			
			for(Field field:fields){
				if(field.isAnnotationPresent(Qualifier.class)){//�ж��Ƿ����qualifier��ǩ
					Qualifier qlf = field.getAnnotation(Qualifier.class);
					
					String fieldName = qlf.value();//�����ע����
					//��ʵ��ע�ᵽ��ǰ������ 
					field.setAccessible(true); //����˽�������޸�
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
	 * ����������ǩע����࣬ ���ҽ���ʵ���� ���汸��
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
			if(clazz.isAnnotationPresent(Controller.class)){//�ж��Ƿ���Controller��ǩע�����
				if(clazz.isAnnotationPresent(RequestMapping.class)){//�����Controller����ô�������request
					Object instance = clazz.newInstance();//��������ʵ��
					RequestMapping rmp = (RequestMapping)clazz.getAnnotation(RequestMapping.class);//���requestmappingע����󲢻��value
					String key = rmp.value();
					instanceMap.put(key, instance);//�����ע��������ʵ�����л���
				}
				//TODO ������Ҫ���Լ��� ControllerĬ��requestMapping����ʽ
//				else{
//					
//				}
			}else if(clazz.isAnnotationPresent(Service.class)){//�ж�service��עע�����
				Object instance = clazz.newInstance();//��������ʵ��
				Service sc = (Service)clazz.getAnnotation(Service.class);//���Serviceע����󲢻��value
				String key = sc.value();
				instanceMap.put(key, instance);//�����ע��������ʵ�����л���
			}else{
				continue;
			}
		}
	}

	/**
	 * ɨ������µ�ȫ���� ����ʵ������Щ��  �������ǵĻ����ǣ�com.smartmvc�� 
	 * ��Ȼ����Ӧ�ô������ļ����л�ȡ�����ﵱ����xml������ʱ����г�����
	 * ��ö���������޶���֮����д洢
	 */
	private void scanPackage(String basePackage){
		URL url = this.getClass().getClassLoader().getResource("/"+replaceToPath(basePackage));
		String filePath = url.getFile();//����ļ���·��
		File file = new File(filePath);//�����Щ���ļ�
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
	 * �ڷ����������� method Ϊpost��ʱ�����˷���  ����Ŀǰû��������Ĭ������������뵽post��������
	 * ������в���
	 */
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		//��������ַ
		String uri = getUri(req);
		String contextPath = req.getContextPath();
		String path = uri.replace(contextPath, "");//ȥ�������е�congtextPath
		
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
			throw new RuntimeException("url����ȷ");
		}
	}
	
	//��������uri�Ż�����
	private String getUri(HttpServletRequest req){
		String uri = (String) req.getAttribute(INCLUDE_REQUEST_URI_ATTRIBUTE);
		if(uri == null){
			uri = req.getRequestURI();
		}
		return uri;
	}
	
	
}
