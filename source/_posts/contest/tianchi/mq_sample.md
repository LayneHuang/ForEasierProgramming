---
title: 针对冷热读写场景的RocketMQ存储系统设计
date: 2021-11-01 11:58:00
categories: 天池比赛
---

{% link '赛题链接' https://tianchi.aliyun.com/competition/entrance/531922/information [title] %}
{% link '代码地址' https://github.com/LayneHuang/mq-sample/tree/layne [title] %}

### 优化点
1.由于MQ收到的每条消息都需要Force到磁盘才能返回。  
题目给出了40个线程，单个线程消息大小是随机到17KB的，每次直接Force到磁盘的话，Force次数太多，磁盘IO也不满。  
这块想到的两个优化点：  
A.某个线程写到WAL的 buffer 之后，进入等待，达到一定数量再落盘(借助Lock, Condition异步转同步)  
B.越接近落盘数量的写线程先唤醒
2.用上傲腾，让体积较小的消息放到傲腾上，使得查询SSD时IO更少
{% img /images/pic_tianchi_mq_context.png %}
3.同步操作步骤尽量拆分细，锁粒度更小，并发更高。
4.本题目能够做到消息地址全索引，可惜的地方是没有合并处理查询

### 收获点
1.ThreadLocal使得线程同步时效率更高。
2.多使用Unsafe进行buffer,byte数组的拷贝(阳仔的处理)
3.JNC的组件确实用得更6了

### 大佬的优化点
1.因为消息生产和消费符合滑动窗口(最后是用上了)
2.4K对齐[最初实现就是等4K对齐落盘(代码里头的Encoder)，写拉了，后面还是把等磁盘大小改成等线程个数]
3.文件预分配，因为比赛计时是在append开始的，所以可以在初始化函数做更多事情（创建文件，填充0字节等）
4.查询阶段的并发查询(后来未加上傲腾之前，没搞出效果，被我去掉了，有傲腾的情况下，应该有效果)

### 感谢队友
感谢耿爷前期的调测，阳仔后期的疯狂输出