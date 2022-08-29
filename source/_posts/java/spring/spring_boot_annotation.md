---
title: Spring Boot 注解
date: 2022-08-15 21:00:00
categories: Spring
---

关于各种注解的用法

### 1.自定义注解

可参考《Spring Boot编程思想》第八章


#### 自定义注解类
```java

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Component
@Import(ConsumersRegister.class)
public @interface Consumer {
    String[] packages() default {};
}
```

#### 实现 ImportBeanDefinitionRegistrar 
将已经注解 @Consumer 的类扫描到 Spring 中
(也可以去掉package属性,及下面的 getPackages 方法, 只要在目标类上面增加了注解, 都能扫到)

```
@Slf4j
@Component
public class ConsumersRegister implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Set<String> packages = getPackages(importingClassMetadata);
        ReactClientDefinitionScanner scanner = new ReactClientDefinitionScanner(registry);
        scanner.scan(packages.toArray(new String[]{}));
    }

    /**
    * 可选
    */
    private Set<String> getPackages(AnnotationMetadata metadata) {
        AnnotationAttributes attributes =
                AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(Consumer.class.getName()));
        String[] packages = attributes.getStringArray("packages");
        Set<String> packagesToScan = new LinkedHashSet<>(Arrays.asList(packages));
        if (packagesToScan.isEmpty()) {
            packagesToScan.add(ClassUtils.getPackageName(metadata.getClassName()));
        }
        return packagesToScan;
    }

    private static class ReactClientDefinitionScanner extends ClassPathBeanDefinitionScanner {

        public ReactClientDefinitionScanner(BeanDefinitionRegistry registry) {
            super(registry);
            addIncludeFilter(new AnnotationTypeFilter(Consumer.class));
        }
    }
}
```

#### 通过 application context 获取实例

```
@Component
@Slf4j
public class Factory {

    @Autowired
    ApplicationContext applicationContext;

    @PostConstruct
    public void init() {
        Map<String, Object> maps = applicationContext.getBeansWithAnnotation(Consumer.class);
    }
}
```

### 2.ExceptionHandler
@RestControllerAdvice 注解在全局类上, 配合 @ExceptionHandler(value = {Throwable.class}) 注解在方法上。
可以全局统一地处理异常，添加了全局异常后，Controller 抛出的 RuntimeException 就无须捕获了

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = {Throwable.class})
    public Object commonException(Throwable e) {
        // todo: do something
    }
    /**
     * DTO 校验异常
     */
    @ExceptionHandler(value = {org.springframework.web.bind.MethodArgumentNotValidException.class})
    public Object paramCheckException(MethodArgumentNotValidException e) {
        // todo: do something
    }
}
```
