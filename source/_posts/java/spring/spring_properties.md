---
title: Spring 配置
date: 2020-07-02 19:57:31
categories: Spring
---

### 1.idea 多环境配置
Edit configuration 下有 program arguments，配置 --spring.profiles.active=dev 即优先使用 dev 环境的配置参数。  
当然 Spring 也有其他注解可以在程序内进行配合。  

### 2.打包后配置
用 maven 将项目打包成 .jar 之后，在项目路径下创建 /config 文件夹，在 config 内放入 .properties 文件，就会优先应用其中的配置。

### 3.XML 配置

#### 3.1 @XmlElementWrapper
设置xml 对应 的java bean的时候，是列表属性的有些要加一个外层类。
比如 :
```xml
<params>
    <param name="key1">value1</param>   
    <param name="key2">value2</param>   
<params/>
```
在 JAXB，@XmlElementWrapper注解可以生成这个包装元素。  
要是它没有其他属性，Bean 里面就可以减少这个类。有可能会用得上

#### 3.2 @XmlType
可以改变 JAXB 输出时候的顺序

### 4.单个请求文件大小配置

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 100MB
      max-request-size: 1000MB
```

在 GlobalExceptionHandler 中可以处理统一返回
```
@ExceptionHandler(value = {MaxUploadSizeExceededException.class})
```