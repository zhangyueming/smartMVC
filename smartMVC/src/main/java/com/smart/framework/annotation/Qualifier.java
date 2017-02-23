package com.smart.framework.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注入 注解
 * @author yueming.zhang
 *
 */
@Target(ElementType.FIELD) //这里表示是属性注解
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Qualifier {
	String value() default "";
}
