package com.smart.framework.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ������ע����
 * @author yueming.zhang
 *
 */
@Target(ElementType.TYPE) //��ʾ����ע��
@Retention(RetentionPolicy.RUNTIME)//��ʾ����������java����ʱ
@Documented //javadoc��������� 
public @interface Controller {
	
}
