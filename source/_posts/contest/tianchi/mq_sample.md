---
title: 2021第二届云原生编程挑战赛1：针对冷热读写场景的RocketMQ存储系统设计
date: 2021-11-01 11:58:00
tag: 天池比赛
---

{% link '赛题链接' https://tianchi.aliyun.com/competition/entrance/531922/information [title] %}
{% link '代码地址' https://github.com/LayneHuang/mq-sample/tree/layne [title] %}

### 优化点
1.由于MQ收到的每条消息都需要Force到磁盘才能返回。
题目给出了40个线程，单线程
