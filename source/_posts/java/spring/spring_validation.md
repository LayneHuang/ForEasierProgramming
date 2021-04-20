---
title: Spring 校验器
date: 2021-04-19 11:49:31
categories: Spring
---
由于在普通的 Spring MVC 项目中 Hibernate Validator 配置了很多次都没有生效，
所以想对 Spring-Validation 的原理分析分析。

{% link 'hibernate文档' https://docs.jboss.org/hibernate/validator/4.2/reference/zh-CN/html_single/#validator-usingvalidator-annotate %}
(看不大懂)

### Bean Validation 与 Validator 的适配

1.核心组件: org.springframework.validation.beanvalidation.LocalValidatorFactoryBean  
2.依赖 Bean Validation - JSR-303 or JSR-349 provider (这玩意有啥用呢?)  
3.Bean 方法参数校验 - org.springframework.validation.beanvalidation.MethodValidationPostProcessor

LocalValidatorFactoryBean 继承了 SpringValidatorAdapter, SpringValidatorAdapter 用于适配 Hibernate Validator  

引入 Hibernate Validator 的依赖, （还有EL Manager 的依赖）
```xml
<dependency>
	<groupId>org.hibernate.validator</groupId>
	<artifactId>hibernate-validator</artifactId>
	<version>6.0.13.Final</version>
</dependency>
```
```xml
<dependency>
	<groupId>org.glassfish</groupId>
	<artifactId>jakarta.el</artifactId>
	<version>4.0.0</version>
</dependency>
```

APP 启动后日志会打印出 Hibernate Validator 的版本信息
```text
HV000001: Hibernate Validator 6.0.13.Final
```

Spring 启动配置中添加: 
```xml
<beans>
    <mvc:annotation-driven validator="validator"/>
    <bean id="validator" class="org.springframework.validation.beanvalidation.LocalValidationFactoryBean">
        <property name="providerClass" value="org.hibernate.validator.HibernateValidator"/>
    </bean>
    <bean class="org.springframework.validation.beanvalidation.MethodValidationPostProcessor">
        <property name="validator" ref="validator"/>
    </bean>
</beans>
```

直接在方法参数前面加 @Validated 没有生效
```java
@RestController
public class MyController {
    @PostMapping(value = "/save")
    public Object save(@RequestBody @Validated MyBean param) {
        try {
            log.info("ID: {}", param.getId());
        } catch (Exception e) {
            return "inner e";
        }
        return "OK";
    }
}
```

要把 @Validated 放到类上(要分组的话放到方法上), @Valid 放到入参上, 最后成功跑出来, 而 Spring Boot 没有这种问题  
```java
@RestController
public class MyController {
    @PostMapping(value = "/save")
    @Validated
    public Object save(@RequestBody @Valid MyBean param) {
        try {
            log.info("ID: {}", param.getId());
        } catch (Exception e) {
            return "inner e";
        }
        return "OK";
    }
}
```
在普通 Spring MVC 框架上可以捕获到 ConstrainViolationException, 在 ControllerAdvice 中统一处理 

但后面又发现一个问题, 就是子对象加上 @Valid 之后，原来的父对象会失效~ (太麻瓜了), Spring Boot 没有这个问题