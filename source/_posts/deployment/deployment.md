---
title: Deployment
date: 2023-8-15 21:00:00
categories: [ Deployment ]
---

### SSL Certificate Generation

{% link '
AliyunSSL' https://help.aliyun.com/zh/ssl-certificate/user-guide/install-certificates/?spm=a2c4g.11186623.0.0.40c470b0no85tf [title] %}

证书签名请求(需要提交到CA机构验证): csr.pem

```shell
openssl req -utf8 -out csr.pem -key cakey.pem -new -sha256
```

```shell 
openssl req -new -x509 -key cakey.pem -days 394
```

Format change

```shell
openssl x509 -in cert.crt -out cert.pem -outform PEM
```

```nginx
FROM nginx:v2
COPY dist.zip /usr/local/nginx/html/
COPY ./nginx.conf /usr/local/nginx/conf
WORKDIR /usr/local/nginx/html/
RUN unzip dist.zip
RUN rm -rf dist.zip
EXPOSE 80
WORKDIR /usr/local/nginx/sbin
CMD ["./nginx","-g","daemon off;"]
```

```
server {
    listen 443 ssl http2;
    server_name  ${xxx}.com www.${xxx}.com;

    ssl_certificate           /etc/nginx/ssl/cert.pem;
    ssl_certificate_key       /etc/nginx/ssl/cakey.pem;
    ssl_protocols             TLSv1.1 TLSv1.2 TLSv1.3;
    ssl_ciphers               EECDH+CHACHA20:EECDH+CHACHA20-draft:EECDH+AES128:RSA+AES128:EECDH+AES256:RSA+AES256:EECDH+3DES:RSA+3DES:!MD5;
    ssl_prefer_server_ciphers on;
    ssl_session_cache         shared:SSL:10m;
    ssl_session_timeout       10m;

	client_max_body_size 1024m;

    location / {
        proxy_set_header HOST $host;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_pass       {target-host}:{target-port}; 
    }
}

server {
    listen 80;
    server_name  ${xxx}.com www.${xxx}.com;
	return 301   https://www.${xxx}.com;
```