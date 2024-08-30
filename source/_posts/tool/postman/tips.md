---
title: Postman
date: 2021-02-22 11:10:31
tags: [ PostMan ]
---

### 1.公用 Cookies 配置

在 Collections -> Edit -> Variables 中可设置

{% img /images/pic_postman1.jpg %}

### 2.上传文件测试

请求 Body 选择 form-data, 参数配上 file

{% img /images/pic_postman2.png %}

### 3.APP端抓包工具

Fiddler {% link '配置教程' https://www.cnblogs.com/woaixuexi9999/p/9247705.html %}

### 4.全局 Header

目前使用的是 8.8.0 版本, 带有一个 Environments, 可以保存起公用的请求变量  
{% img /images/pic_postman3.png %}  
使用的时候, 用 ```{{变量名}}``` 代替变量
{% img /images/pic_postman4.png %}  

