package com.smart.framework.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ע�� ע��
 * @author yueming.zhang
 *
 */
@Target(ElementType.FIELD) //�����ʾ������ע��
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Qualifier {
	String value() default "";
}
