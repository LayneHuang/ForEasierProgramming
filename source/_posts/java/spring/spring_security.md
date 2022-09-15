---
title: Spring Security
date: 2022-08-26 21:00:00
categories: Spring
---

### 1.Spring Security 引入
在 maven 的 pom.xml 文件加入依赖，版本默认依赖 spring-boot-starter-parent 的版本
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

### 2.启动类加入注解
加上注解 `@EnableWebSecurity`
```java
@SpringBootApplication
@EnableWebSecurity
public class DemoApplication extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

### 3.继承 WebSecurityConfigurerAdapter 并且覆盖其中配置方法

```java
@Configuration
@Slf4j
public class MyWebSecurityConfig extends WebSecurityConfigurerAdapter {
    // 自定义用户登陆信息
    @Autowired
    private MyUserDetailsService userDetailsService;
    // 自定义URL的权限列表
    @Autowired
    private MySecurityMetaDataSource securityMetaDataSource;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        PasswordEncoder passwordEncoder = passwordEncoder();
        // 登陆信息获取
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
    }

    @Override
    protected void configure(HttpSecurity http) {
        // 自定义权限
        ApplicationContext applicationContext = http.getSharedObject(ApplicationContext.class);
        http.apply(new UrlAuthorizationConfigurer<>(applicationContext))
                .withObjectPostProcessor(new ObjectPostProcessor<FilterSecurityInterceptor>() {
                    @Override
                    public <O extends FilterSecurityInterceptor> O
                    postProcess(O object) {
                        object.setSecurityMetadataSource(securityMetaDataSource);
                        return object;
                    }
                });

        http.formLogin()
                .successHandler(getLoginSuccessHandler())
                .failureHandler(getFailureHandler())
                .and().exceptionHandling()
                .authenticationEntryPoint(getUnLoginHandler())
                .and().logout().permitAll()
                .logoutSuccessHandler(getLogoutSuccessHandler())
//                .and().authorizeRequests()
//                .anyRequest().authenticated()
                .and().csrf().disable();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### 4.获取自定义获取
通过实现 UserDetailsService.loadUserByUsername() 接口，来自定义返回登陆信息

```java
@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserInfo info = userMapper.selectByName(username);
        if (info == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        List<GrantedAuthority> authorities = new ArrayList<>();
        // 角色
        Integer roleId = info.getRole();
        AuthRoleType role = AuthRoleType.getByCode(roleId);
        authorities.add(new SimpleGrantedAuthority(role.getName()));
        // 登陆返回消息体
        LoginUserInfo loginUserInfo = new LoginUserInfo(
                info.getUsername(),
                info.getPassword(),
                Objects.equals(STATUS_ENABLE, info.getStatus()),
                authorities
        );
        BeanUtils.copyProperties(info, loginUserInfo);
        return loginUserInfo;
    }
}
```

### 5.自定义权限校验
实现 FilterInvocationSecurityMetadataSource 接口, 通过 URL 获取该 URL 需要哪些角色访问

```java
@Configuration
public class MySecurityMetaDataSource implements FilterInvocationSecurityMetadataSource {

    @Override
    public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {
        String requestURI =
                ((FilterInvocation) object).getRequest().getRequestURI();
        List<String> roles = new ArrayList<>();
        roles.add("ROLE_ADMIN");
        roles.add("ROLE_USER");
        return SecurityConfig.createList(roles.toArray(new String[0]));
    }

    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return null;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return FilterInvocation.class.isAssignableFrom(clazz);
    }
}
```

### 6.管理员失效某用户Session

可用于多服务实例分布式管理Session
{% link 'spring-session-data-redis好文' https://blog.csdn.net/shuoyueqishilove/article/details/122244995 [title] %}

应用通过 SessionRepositoryFilter 把请求(HttpServletRequest)中的 Session 进行存储

引入 session redis 依赖
```xml
 <dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.session</groupId>
        <artifactId>spring-session-data-redis</artifactId>
    </dependency>
</dependencies>
```

配置 Spring Security 使用 redis 存储 session
```yaml
spring:
  session:
    store-type: redis
```

清理 redis 中的 session id
```java
@Slf4j
@Service
public class MyUserDetailsService implements UserDetailsService {
    public void logout(String username) {
        Map<String, ? extends Session> userSessions = sessionRepository.findByIndexNameAndIndexValue(
                RedisIndexedSessionRepository.PRINCIPAL_NAME_INDEX_NAME,
                username
        );
        List<String> sessionIds = new ArrayList<>(userSessions.keySet());
        for (String session : sessionIds) {
            sessionRepository.deleteById(session);
        }
    }
}
```

### 7.Cookie 与 Session

{% link 'Cookie与Session详解' https://www.cnblogs.com/qingshangucheng/p/16041147.html [title] %}
文中提到 SessionId 是存在 Cookie 中的，这解析了为啥，请求中只需要传入 Cookie， 服务器依然能够识别用户信息。 

在 HttpServletRequestWrapper 封装请求的过程中，程序会从 Cookie 中读取 SessionId (实现类: CookieHttpSessionIdResolver)


```java
public class LoginTest {

    private static String cookie;

    @BeforeAll
    public static void login() throws Exception {
        RestTemplate template = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("username", "username");
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);

        ResponseEntity<String> res = template.postForEntity(
                "http://localhost:7001/login",
                requestEntity,
                String.class
        );
        // 得到 cookie
        cookie = Objects.requireNonNull(res.getHeaders().get("Set-Cookie")).get(0).split(";")[0];
    }

    @Test
    public void testGetBaseConfig() {
        RestTemplate template = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        // 设置 cookie
        headers.set("Cookie", cookie);
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(new LinkedMultiValueMap<>(), headers);
        ResponseEntity<String> res = template.exchange(
                "http://localhost:7001/doSomething",
                HttpMethod.GET,
                requestEntity,
                String.class
        );
        Assert.notNull(res, "请求体为空");
        Assert.isTrue(HttpStatus.OK == res.getStatusCode(), "请求结果错误");
        System.out.println(JSON.toJSONString(res.getBody()));
    }
}
```