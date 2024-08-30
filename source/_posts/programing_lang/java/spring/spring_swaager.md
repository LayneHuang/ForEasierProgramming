---
title: Spring Swagger
date: 2024-08-29 21:00
categories: [ Spring ]
---



## Swagger3集成



### 兼容处理

因为SpringBoot使用的版本号是2.7.x的，在集成swagger2的时候服务启动会出现NPE问题。

```yaml
spring:
  mvc:
    pathmatch:
      matching-strategy: ant_path_matcher
```



### 配置

```JAVA
@Configuration
public class SwaggerConfig {

    @Bean
    public Docket createRestApi() {
        // 配置docket以配置Swagger具体参数
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                // .apis(RequestHandlerSelectors.basePackage("com.layne.demo.auth.controller"))
           		.apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("DemoService")
                .description("-")
                .version("v1.0")
                .contact(new Contact("laynehuang", "", ""))
                .build();
    }
}
```

```JAVA

@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 解决静态资源无法访问
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
        // 解决swagger无法访问
        registry.addResourceHandler("swagger-ui.html", "doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        // 解决swagger的js文件无法访问
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
        super.addResourceHandlers(registry);
    }
}
```



[启动后默认地址](http://localhost:8090/auth/swagger-ui.html)