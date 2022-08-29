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