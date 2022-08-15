---
title: Spring Boot 注解
date: 2022-08-15 21:00:00
categories: Spring
---

### 1.自定义注解

自定义注解类

```java

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Component
public @interface Consumer {
    String[] packages() default {};
}
```

实现 ImportBeanDefinitionRegistrar 将其扫描到 Spring 中
(也可以去掉package属性,及下面的 getPackages 方法, 只要在目标类上面增加了注解, 都能扫到)

```
@Slf4j
@Component
public class MqttConsumersRegister implements ImportBeanDefinitionRegistrar {

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

通过 application context 获取实例

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