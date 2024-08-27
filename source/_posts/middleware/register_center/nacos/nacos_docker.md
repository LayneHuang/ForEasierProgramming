---
title: Nacos Docker
date: 2024-08-27 21:30
categories: [ nacos ]
---



## DB User initialization

```mysql
CREATE USER 'nacos'@'%' IDENTIFIED BY 'your_password_here';
CREATE DATABASE IF NOT EXISTS nacos_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;  
GRANT ALL PRIVILEGES ON nacos_db.* TO 'nacos'@'%';
FLUSH PRIVILEGES;
```

