# Java并发编程
阅读笔记，图文记录自极客时间《Java并发编程实战》

## [一.并发编程基础](https://github.com/LayneHuang/ForEasyCode/blob/master/java/concurrent_programming/concurrent_programming_base.md)
第一章讲解的主要是并发编程的一些基础知识：
  
1.并发编程Bug的源头：可见性、原子性、有序性  
2.互斥锁（最基本的锁）  
3.死锁  
4.“等待-通知”机制（线程的同步）
5.安全性、活跃性以及性能问题  
6.管程（锁的底层实现）  
7.Java线程（线程的创建、局部变量的线程安全原理）  

## [二.并发工具类](https://github.com/LayneHuang/ForEasyCode/blob/master/java/concurrent_programming/concurrent_programming_util.md)
来到的挺关键的一章，实战上能用到的都在这~（JAVA并发包中的工具）  
如果当时做第五届阿里云中间比赛时，会用这些并发工具的话，可能就事半功倍了~

1.Lock, Condition（锁，条件变量）  
2.Semaphore(信号量)  
3.ReadWriteLock(读写锁)  
4.StampedLock(带乐观读的读写锁)  
5.CountDownLatch、CyclicBarrier(多线程的同步等待执行)  
6.原子类  
7.Executor与线程池  
8.Future
9.CompletableFuture
10.CompletableService