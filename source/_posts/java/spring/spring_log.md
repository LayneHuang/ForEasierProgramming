---
title: Spring Boot 日志
date: 2020-04-02 01:48:31
categories: Spring
---

### slf4j
很多情况下需要按照日志级别去控制日志输出。
#### 日志分级输出
(.properties 文件同理)
```yaml
#slf4j日志配置
logging:
  # 配置级别
  level:
    #分包配置级别，即不同的目录下可以使用不同的级别
    com: debug
```
