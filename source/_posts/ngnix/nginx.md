---
title: Nginx Tips
date: 2022-8-11 21:40:00
categories: Nginx
---


### 1.前端 History 模式重定向配置
```config
location / {
    root html;
    index index.html index.htm;
    try_files $uri $uri/ @rewrites;
}
location @rewrites {
    rewrite ^(.+)$ /index.html last;
}
```
