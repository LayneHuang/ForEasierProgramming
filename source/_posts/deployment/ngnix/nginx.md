---
title: nginx
date: 2022-8-11 21:40:00
categories: [ nginx ]
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

### Docker

{% link 'docker部署指南' https://www.cnblogs.com/tod4/p/16659260.html [title] %}    
{% link '配置' https://blog.csdn.net/qq_46312987/article/details/118895520 [title] %}

```shell
mkdir -p /home/nginx/{conf,log,html}
docker cp nginx:/etc/nginx/nginx.conf /home/nginx/conf/nginx.conf
docker cp nginx:/etc/nginx/conf.d /home/nginx/conf/conf.d
docker cp nginx:/usr/share/nginx/html /home/nginx/gs/
```

```shell
docker run -d --name nginx -p 80:80 \
-v /home/nginx/conf/nginx.conf:/etc/nginx/nginx.conf \
-v /home/nginx/conf/conf.d:/etc/nginx/conf.d \
-v /home/nginx/html:/usr/share/nginx/html \
-v /home/nginx/log:/var/log/nginx \
--privileged=true nginx
```
