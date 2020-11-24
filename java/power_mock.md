# Power Mock 用作单元测试
github地址：https://github.com/powermock/powermock/wiki/Mockito  

当需要 Mock 一些 static, final, private 的方法时需要加上注解:  
```java
@RunWith(PowerMockRunner.class)
@PrepareForTest(Static.class)
```

### 运行测试时，idea 不检查其他类的错误
参考: https://blog.csdn.net/AiAShao/article/details/80105093  
1.Java Compiler 用 Eclipse  
2.Run Configurations 中配置 Build,no error check  
亲测 OK~
