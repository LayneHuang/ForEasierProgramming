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
    // 自定义权限通过管理
    @Autowired
    private MyAccessDecisionManager accessDecisionManager;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        PasswordEncoder passwordEncoder = passwordEncoder();
        // 登陆信息获取
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
    }

    @Override
    protected void configure(HttpSecurity http) {
        http.formLogin()
                // 登陆成功返回
                .successHandler(getLoginSuccessHandler())
                // 或者用这个并在 Controller 上实现对应接口, 也OK
//                .successForwardUrl("/login/success")
                // 用户状态不正确(启用禁用等)
                .failureHandler(getFailureHandler())
                .and().exceptionHandling()
                // 处理未登陆
                .authenticationEntryPoint(getUnLoginHandler())
                // 处理权限校验失败
                .accessDeniedHandler(getAccessDeniedHandler())
                .and().logout().permitAll()
                // 处理登出返回
                .logoutSuccessHandler(getLogoutSuccessHandler())
                .and().authorizeRequests()
                // 使用默认权限匹配
//                .antMatchers("/login**").permitAll()
                // 自定义权限匹配
                .withObjectPostProcessor(new ObjectPostProcessor<FilterSecurityInterceptor>() {
                    @Override
                    public <O extends FilterSecurityInterceptor> O postProcess(O object) {
                        object.setAccessDecisionManager(accessDecisionManager);
                        object.setSecurityMetadataSource(securityMetaDataSource);
                        return object;
                    }
                })
                .anyRequest().authenticated()
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

URL角色获取 （实现 FilterInvocationSecurityMetadataSource 接口, 通过 URL 获取该 URL 需要哪些角色访问）

```java

@Configuration
public class MySecurityMetaDataSource implements FilterInvocationSecurityMetadataSource {

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    /**
     * 比如要通过一些 swagger 的 url
     * @param url 当前请求url
     * @return 是否通过
     */
    private boolean isPermitAll(String url) {
        List<String> permitAllList = new ArrayList<>();
        permitAllList.add("/webjars/springfox-swagger-ui/**");
        permitAllList.add("/swagger**");
        permitAllList.add("/*/api-docs");
        return permitAllList.stream().anyMatch(pattern -> antPathMatcher.match(pattern, url));
    }

    @Override
    public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {
        String requestURI =
                ((FilterInvocation) object).getRequest().getRequestURI();
        List<String> roles = new ArrayList<>();
        roles.add("ROLE_ADMIN");
        roles.add("ROLE_USER");
        // Todo: 要处理一下, 当权限列表为空时抛出异常
        if (CollectionUtils.isEmpty(roles) && !isPermitAll(requestURI)) {
            throw new IllegalArgumentException("url not exist");
        }
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

自定义权限通过管理

```java

@Component
public class MyAccessDecisionManager implements AccessDecisionManager {
    @Override
    public void decide(Authentication authentication, Object object, Collection<ConfigAttribute> configAttributes) throws AccessDeniedException, InsufficientAuthenticationException {

    }

    @Override
    public boolean supports(ConfigAttribute attribute) {
        return false;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return false;
    }
}
```

### 6.管理员失效某用户Session

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

应用通过 SessionRepositoryFilter 把请求(HttpServletRequest)中的 Session 进行存储

Session 中存储的内容  
{% img /images/pic_session.png %}