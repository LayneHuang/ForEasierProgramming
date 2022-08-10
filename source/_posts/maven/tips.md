---
title: Maven Tips
date: 2020-05-25 16:23:00
categories: Maven
---

通常用 Spring Initializr 初始化项目时，选用 Maven 作为依赖工具后，一般情况都会配置好了。  
在对应项目下快捷键 Alt + F1 中可以定位到 Maven 项。

### 1.编译指令
```shell
mvn clean install -Dmaven.test.skip=true -s "file_path" -fae
```
file_path 可替换默认的依赖配置  
[指令参数参考](https://www.cnblogs.com/zz0412/p/3767146.html)

### 2.跳过单元测试
Spring Boot 在 install 的时候回自动跑上测试，如果我们想跳过单元测试，在 pom.xml 配置中添加：
```xml
<properties>
    <skipTests>true</skipTests>
</properties>
```

### 3.本地包导入
当依赖一些 maven 仓库上没有的包资源时，可能会用到

#### 1.将 .jar 文件放到本地目录
比如说 /resources/lib 下

#### 2.pom.xml 文件中配置，引入 jar
假设本地 jar 包为 abc.jar
```xml
<project>
    <dependencies>
        <dependency>
            <groupId>自定义</groupId>
            <artifactId>自定义</artifactId>
            <version>自定义</version>
            <scope>system</scope>
            <systemPath>${project.basedir}/src/main/resources/lib/abc.jar</systemPath>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <!--能把本地包导入 jar包-->
                    <includeSystemScope>true</includeSystemScope>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <source>8</source>
                    <target>8</target>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### 3.修改打包目录
目前直接用拷贝的方式去实现
```xml
<plugin>
    <artifactId>maven-antrun-plugin</artifactId> <!-- 拷贝插件 -->
    <executions>
        <execution>
            <id>copy</id>
            <phase>install</phase>
            <configuration>
                <tasks>
                    <!--  文件将要输出目录 在parent pom中统一配置，此处引用即可-->
                    <copy todir="${project.basedir}"> 
                        <!-- 待拷贝文件的目录，默认目录在项目的target下-->
                        <fileset dir="${project.build.directory}">
                            <!-- 待拷贝文件，可以使用下面的通配符，还可以是其他目录的文件。此处需要与project.bulid.finalName对应，否则可能找不到文件 -->
                            <include name="${project.artifactId}-${project.version}.jar"/> 
                            <!-- <include name="*.jar" /> -->
                        </fileset>
                    </copy>
                </tasks>
            </configuration>
            <goals>
                <goal>run</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```