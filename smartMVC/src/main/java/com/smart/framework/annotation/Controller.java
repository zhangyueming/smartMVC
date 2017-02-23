package com.smart.framework.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 控制器注解类
 * @author yueming.zhang
 *
 */
@Target(ElementType.TYPE) //表示是类注解
@Retention(RetentionPolicy.RUNTIME)//表示生命周期是java运行时
@Documented //javadoc打包起作用 
public @interface Controller {
	
}
