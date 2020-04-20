# Spring Boot 国际化
在处理不同语言环境的情况下，需要不同的字段显示。
（通常就是一份中文显示配置，一份英文显示配置）

### 1.创建国际化文件 
<img src="https://github.com/LayneHuang/ForEasyCode/blob/master/images/pic_spring_locale.png" width="300">

### 2.Spring Boot 启动配置
在 .properties 中定义国际化文件路径,这个可以设置 basenames 来定义多个文件  
(注意前缀要跟 1. 中创建的文件前缀要一样)
```properties
spring.messages.basename=/i18n/messages
```

### 3.获取国际化配置

#### 3.1 通过 MessageSource 获取配置信息
Spring Boot 注入 MessageSource，调用 getMessage(String arg, Object[] objects, Locale locale) 方法，
就可以获取到对应语言环境配置的字段了。 
 
#### 3.2 若 MessageSource 为 Empty
我在实践的时候发现直接 Autowired MessageSource 是不行的（会报 Empty Resource）  
可能 Spring Boot 没加载到 MessageResource 作为 Bean。  
然后的话暂时自定义一个 Component 类去解决
```java
@Slf4j
@Component
public class MyMessageResource {

    @Value("${spring.messages.basename}")
    private String path;

    private ReloadableResourceBundleMessageSource messageSource;

    public MyMessageResource() {
        messageSource = new ReloadableResourceBundleMessageSource();
    }

    @PostConstruct
    void init() {
        messageSource.setCacheSeconds(-1);
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
        messageSource.setBasename(path);
    }

    public String getMessage(String result, Object[] params) {
        String message = "";
        try {
            Locale locale = LocaleContextHolder.getLocale();
            message = messageSource.getMessage(result, params, locale);
        } catch (Exception e) {
            log.error(e.toString());
        }
        return message;
    }
}
```