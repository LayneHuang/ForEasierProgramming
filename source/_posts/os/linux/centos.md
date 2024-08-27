---
title: centos
date: 2024-08-23 19:00:00
tags: [ linux ]
---

### Yum镜像源切换到国内

```
cd /etc/yum.repos.d
// backup
sudo cp CentOS-Base.repo CentOS-Base.repo.bak
sudo curl -o /etc/yum.repos.d/CentOS-Base.repo http://mirrors.aliyun.com/repo/Centos-7.repo

sudo yum clean all
sudo yum makecache
sudo yum update -y
```

