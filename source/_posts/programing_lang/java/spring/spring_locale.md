---
title: Spring Boot 国际化
date: 2020-04-02 01:48:31
categories: [ Spring ]
---

在处理不同语言环境的情况下，需要不同的字段显示。
（通常就是一份中文显示配置，一份英文显示配置）

### 1.创建国际化文件

{% img /images/pic_spring_locale.png %}

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

我在实践的时候发现直接 Autowired MessageSource 是不行的（会报 Empty Resource，Spring Boot 并没有默认实现）  
可能 Spring Boot 没加载到 MessageResource 作为 Bean。（MessageResource 只是个接口）  
然后的话暂时自定义一个 Component 类去解决。   
**还有一各要十分注意的一个事情就是**：  
Locale 的使用，要注意国家码是否有定义（比如 Locale.CHINA 与 Locale.CHINESE 是有区别的，实际上对应的是.properties文件的后缀）。

```java

@Slf4j
@Component
public class MyMessageResource {

    @Value("${spring.messages.basename}")
    private String path;

    private ReloadableResourceBundleMessageSource messageSource;

    public MyMessageResource() {
        // 注意 Locale 的设置
        messageSource = new ReloadableResourceBundleMessageSource();
    }

    @PostConstruct
    void init() {
        messageSource.setCacheSeconds(-1);
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
        messageSource.setBasenames(path);
        messageSource.setDefaultLocale(Locale.US);
        messageSource.setUseCodeAsDefaultMessage(true);
    }

    public String getMessage(String result, Object[] params) {
        return messageSource.getMessage(result, params, Locale.US);
    }

    public String getMessage(String result, Object[] params, Locale locale) {
        String message = "";
        try {
            messageSource.setDefaultLocale(locale);
            message = messageSource.getMessage(result, params, locale);
        } catch (Exception e) {
            log.error(e.toString());
        }
        return message;
    }
}
```

如果Spring 版本比较旧不会自动设置进 LocaleContextHolder 的话，要增加一个配置项

```java

@Configuration
@EnableAutoConfiguration
@ComponentScan
public class LocaleConfig implements WebMvcConfigurer {

    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver localeResolver = new CookieLocaleResolver();
        localeResolver.setCookieName("localeCookie");
        localeResolver.setDefaultLocale(Locale.US);
        localeResolver.setCookieMaxAge(3600);
        return localeResolver;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang");
        return lci;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}
```

### 4.追加国际化配置

MessageSource 只能读取配置，而不能写入。  
找了一些资料，Java 工具包（java.util）中提供了一个 Properties 类，可以用于处理其读写。  
需要写入的时候可以选择使用这种方式。我的实现方式是用一个静态工厂类处理不同的 Locale。

1 文件路径配置在 Spring Boot 系统配置的 .properties 文件中

```properties
# 国际化配置
locale.path=D:/workspace/DemoService/src/main/resources
spring.messages.basename=/i18n/query-messages
```

2.启动时读入国际化到缓存中的 MyProperties 中  
（用 MyProperties 的原因是 Properties 类追加时没有首先进行文末换行，所以我继承后重写了一下 store 方法）

3.读写锁（ReadWriteLock）处理并发写入

4.创建时重复校验，properties 文件的配置 key 名，不能重复的。

样例代码 Demo：

```java

@Slf4j
@Component
public class MyMessageResource {

    private static final String PROPERTIES_SUFFIX = ".properties";

    @Value("${locale.path}")
    private String path;

    @Value("${spring.messages.basename}")
    private String baseName;

    private final HashMap<Locale, MyProperties> properties = new HashMap<>();

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @PostConstruct
    void init() {
        loadProperties(Locale.US);
        loadProperties(Locale.CHINA);
    }

    private void loadProperties(Locale locale) {
        MyProperties prop = new MyProperties();
        InputStream in;
        try {
            in = new BufferedInputStream(new FileInputStream(getFilePath(locale)));
            prop.load(in);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            properties.put(locale, prop);
        }
    }

    public boolean containKey(String key) {
        lock.readLock().lock();
        try {
            for (MyProperties prop : properties.values()) {
                if (prop.containsKey(key)) {
                    return true;
                }
            }
            return false;
        } finally {
            lock.readLock().unlock();
        }
    }

    public String getMessage(String key, Locale locale) {
        lock.readLock().lock();
        try {
            MyProperties prop = properties.get(locale);
            if (prop == null) {
                return key;
            }
            return prop.getProperty(key, key);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 添加到本地缓存
     */
    private void setMessage(String key, String value, Locale locale) {
        if (properties.containsKey(locale)) {
            properties.get(locale).setProperty(key, value);
        }
    }

    /**
     * 添加到缓存并保存到文件
     */
    public void saveMessage(String key, String value, Locale locale) {
        if (containKey(key)) {
            return;
        }
        lock.writeLock().lock();
        MyProperties prop = new MyProperties();
        String filePath = getFilePath(locale);
        try {
            // 追加打开
            FileOutputStream oFile = new FileOutputStream(filePath, true);
            prop.setProperty(key, value);
            prop.store(oFile, null);
            oFile.close();
            // 放到缓存
            setMessage(key, value, locale);
        } catch (FileNotFoundException e) {
            log.error("{}, save message {} {} {} {} failure, file not found", filePath, key, value,
                    locale.getLanguage(), locale.getCountry());
        } catch (IOException e) {
            log.error(e.toString());
        } finally {
            lock.writeLock().unlock();
        }
    }

    private String getFilePath(Locale locale) {
        return path + baseName + "_" + locale.getLanguage() + "_" + locale.getCountry() + PROPERTIES_SUFFIX;
    }

}
```

附上测试 Test:

 ```java
public class MyMessageResourceTest {
    @Test
    void testAddMessage() {
        String key = "test_key";
        String value = "test_value";
        messageSource.saveMessage(key, value, Locale.CHINA);
        String nowValue = messageSource.getMessage("test_key", Locale.CHINA);
        assertEquals(value, nowValue);
    }
}
```

### Idea Resource Bundle plugins

{% img /images/pic_spring_i18n_1.png %}
