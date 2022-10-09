---
title: UUID 引入
date: 2022-08-19 21:00:00
categories: UUID
---

因为要用到 baidu 的long类型的UUID生成器，目前github上只提供本地包引入的方式

1.首先从GITHUB上将项目clone下来
```shell
git clone https://github.com/baidu/uid-generator.git
```

2.打包
```shell
mvn clean install
```

3.按照引入方式将本地包引入
放到相应目录后，添加 maven 依赖，本地包导入方式文章列表中也有
```xml
<dependency>
    <groupId>com.generator</groupId>
    <artifactId>uid-generator</artifactId>
    <version>1.0</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/src/main/resources/lib/uid-generator-1.0.0-SNAPSHOT.jar</systemPath>
</dependency>
```

4.在使用到的项目上进行构建
注意要加上 `<includeSystemScope>true</includeSystemScope>` 标签，才能把本地包最终打进服务的包  
并且 mapper 的依赖也要导进来
```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <configuration>
                <includeSystemScope>true</includeSystemScope>
                <classifier>exec</classifier>
                <skip>true</skip>
            </configuration>
        </plugin>
    </plugins>
    <resources>
        <resource>
            <directory>src/main/java</directory>
            <includes>
                <include>**/*.xml</include>
            </includes>
        </resource>
        <resource>
            <directory>src/main/resources</directory>
        </resource>
    </resources>
</build>
```

依赖的组件
```xml
<dependency>
    <groupId>commons-lang</groupId>
    <artifactId>commons-lang</artifactId>
    <version>2.6</version>
</dependency>
```

5.配置UUID要用到的bean

因为 UUID 要用到用到数据库，所以它要扫描到 WorkerNodeDAO 以及 WORKER_NODE.xml。

WORKER_NODE.xml 这个文件要加到我们自身的 mapper xml文件目录下，或者 mybatis 能够扫描到这个文件

```java
@Configuration
@MapperScan(value = {"com.baidu.fsg.uid.worker.dao"})
public class UidConfig {

    @Bean("disposableWorkerIdAssigner")
    public DisposableWorkerIdAssigner initDisposableWorkerIdAssigner() {
        return new DisposableWorkerIdAssigner();
    }

    @Bean
    public UidGenerator defaultUidGenerator(WorkerIdAssigner workerIdAssigner) {
        DefaultUidGenerator defaultUidGenerator = new DefaultUidGenerator();
        defaultUidGenerator.setWorkerIdAssigner(workerIdAssigner);
        defaultUidGenerator.setTimeBits(28);
        defaultUidGenerator.setWorkerBits(22);
        defaultUidGenerator.setSeqBits(13);
        defaultUidGenerator.setEpochStr("2016-09-20");
        return defaultUidGenerator;

    }
}
```

要注意一下的是 UidConfig 加上 @MapperScan(value = {"com.baidu.fsg.uid.worker.dao"}) 会影响 SpringBoot 配置自身的 mapper 扫描。
估计是因为 注解 优先级 大于 外部化properties配置，所以在SpringBootApplication主启动类加上扫描注解。
```java
@SpringBootApplication
@MapperScan(value = {"com.demo.mapper"})
public class DemoApplication extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

7.其余建表相关参考github官方文档即可