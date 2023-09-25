---
title: Power Mockito 单元测试
date: 2020-11-24 14:20:00
categories: Unit Test
---

{% link 'github地址' https://github.com/powermock/powermock/wiki/Mockito %}

当需要 Mock 一些 static, final, private 的方法时需要加上注解:

```
@RunWith(PowerMockRunner.class)
@PrepareForTest(Static.class)
```

### 运行测试时，idea 不检查其他类的错误

参考: https://blog.csdn.net/AiAShao/article/details/80105093  
1.Java Compiler 用 Eclipse  
2.Run Configurations 中配置 Build,no error check  
亲测 OK~

### 调用 private 方法

比如说调用 Person.class 的 getStatus(String person) 方法

```java
import java.lang.reflect.Method;

public class UnitTest {
    @InjectMocks
    Person person;

    @Test
    public void testGetStatus() {
        String personId = "123";
        Method method = PowerMockito.method(Person.class, "getStatus");
        String status = (String) method.invoke(person, personId);
        Assert.assertEqual("expectStatus", status);
    }
}
```