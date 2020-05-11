# Spring Boot Maven
通常用 Spring Initializr 初始化项目时，选用 Maven 作为依赖工具后，一般情况都会配置好了。  
在对应项目下快捷键 Alt + F1 中可以定位到 Maven 项。

### 1.跳过单元测试
Spring Boot 在 install 的时候回自动跑上测试，如果我们想跳过单元测试，在 pom.xml 配置中添加：
```xml
<properties>
    <skipTests>true</skipTests>
</properties>
```