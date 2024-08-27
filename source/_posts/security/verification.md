---
title: Verification
date: 2024-08-24 19:00
categories: [ Security ]
---

### Spring Bean Validation

#### Define inside Java Bean

放在类定义的注解，每次执行校验的时候都会自动运行。重复执行可能带来额外的副作用。

通常一些简单的格式校验注解可以用这种方式。同时尽量只定义在`VO`当中。

同时`javax.validation.constraints`给出了许多常用注解，如`@Eamil`,`@NotBlank`等，还包含各国多语言配置，无需自己重定义。

```java
public class UserVO {
    @NotBlank(groups = {CreateValidator.class}, message = "resp.msg.user.phone.not.blank")
    @Pattern(
            groups = {CreateValidator.class, UpdateValidator.class},
            regexp = "^1(3\\d|4[5-9]|5[0-35-9]|6[567]|7[0-8]|8\\d|9[0-35-9])\\d{8}$",
            message = "resp.msg.user.phone.pattern"
    )
    private String phone;

    @Email(groups = {CreateValidator.class,UpdateValidator.class})
    private String email;
}
```



#### Define in Controller

```java
public class UserController {
	@Autowired
    UserService userService;

    @PostMapping("/register")
    public ResponseData<UserInfo> register(@RequestBody 
        								@Validated({CreateValidator.class})
        								@Phone UserDto dto) {
        return new ResponseData<>(ReturnCode.OK.getCode(), userService.register(param));
    }
}
```

