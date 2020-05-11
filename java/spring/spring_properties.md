# Spring 配置

### idea 多环境配置
Edit configuration 下有 program arguments，配置 --spring.profiles.active=dev 即优先使用 dev 环境的配置参数。  
当然 Spring 也有其他注解可以在程序内进行配合。  

### 打包后配置
用 maven 将项目打包成 .jar 之后，在项目路径下创建 /config 文件夹，在 config 内放入 .properties 文件，就会优先应用其中的配置。 