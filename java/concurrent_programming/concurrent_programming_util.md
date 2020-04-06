# 二.并发工具类
来到的挺关键的一章，实战上能用到的都在这~  

## 1.Lock & Condition
跟之前所说的一样，并发领域要解决两类问题：互斥，同步。  
而JAVA SDK并发包**通过 Lock 和 Condition 两个接口来实现管程，Lock用于解决互斥问题，Condition用于解决同步问题。**

### 1.1 再造管程的理由 
 locK 相比于 synchronized 有3个好处：  
#### 1.能够响应中断
synchronized 的问题是，持有锁A后，如果尝试获取锁B失败，那么线程就进入拥塞状态，一旦发生死锁，就没有任何机会来唤醒拥塞的线程。
如果我们给拥塞的线程发送中断信号时，能够唤醒它，它就有机会释放锁A。就可以破坏掉不可抢占的条件了。  
#### 2.支持超时
如果线程在一段时间内没有获取到锁，不是进入阻塞状态，而是返回一个错误，那这个线程也有机会释放曾经持有的锁。  
#### 3.非拥塞地获取锁*
跟2.不多，没获取到锁就直接返回错误。  

```java
// 支持中断的API
void lockInterruptibly() 
  throws InterruptedException;

// 支持超时的API
boolean tryLock(long time, TimeUnit unit) 
  throws InterruptedException;

// 支持非阻塞获取锁的API
boolean tryLock();
```

### 1.2 如何保证可见性
它利用了volatile相关的Happen-Before规则。（如ReentrantLock，它内部持有一个volatile的成员变量state）

### 1.3 可重入锁
可重入锁，指的是线程可以重复获取同一把锁。
```java
class X {
  private final Lock rtl =
  new ReentrantLock();
  int value;
  public int get() {
    // 获取锁
    rtl.lock();
    try {
      return value;
    } finally {
      // 保证锁能释放
      rtl.unlock();
    }
  }
  public void addOne() {
    // 获取锁
    rtl.lock();  
    try {
      value = 1 + get();
    } finally {
      // 保证锁能释放
      rtl.unlock();
    }
  }
}
```

#### 1.4 公平锁与非公平锁
ReentrantLock 有两个构造函数。
```java
//无参构造函数：默认非公平锁
public ReentrantLock() {
    sync = new NonfairSync();
}
//根据公平策略参数创建锁
public ReentrantLock(boolean fair){
    sync = fair ? new FairSync() 
                : new NonfairSync();
}
```
公平锁：谁等待时间长，就唤醒谁。(队列)   
非公平锁：有可能等待时间短的线程先被唤醒。   

#### 1.5 锁的最佳实践
1.永远只在更新对象的成员变量时加锁  
2.永远只在访问可变的成员变量时加锁  
3.永远不在调用其他对象的方法时加锁  

#### 1.6 多条件变量
Java语言内置的管程里只有一个条件变量，而Lock & Condition实现的管程支持多个条件变量。
```java
public class BlockedQueue<T>{
  final Lock lock =
    new ReentrantLock();
  // 条件变量：队列不满  
  final Condition notFull =
    lock.newCondition();
  // 条件变量：队列不空  
  final Condition notEmpty =
    lock.newCondition();

  // 入队
  void enq(T x) {
    lock.lock();
    try {
      while (队列已满){
        // 等待队列不满
        notFull.await();
      }  
      // 省略入队操作...
      //入队后,通知可出队
      notEmpty.signal();
    }finally {
      lock.unlock();
    }
  }
  // 出队
  void deq(){
    lock.lock();
    try {
      while (队列已空){
        // 等待队列不空
        notEmpty.await();
      }  
      // 省略出队操作...
      //出队后，通知可入队
      notFull.signal();
    }finally {
      lock.unlock();
    }  
  }
}
```

## 2.Semaphore(信号量)
信号量模型还是很简单的：一个计算器，一个等待队列，三个方法。<br/>
<img src="https://github.com/LayneHuang/ForEasyCode/blob/master/images/pic_concurrent_program10.png" width="300"><br/>
这里提到的 init()、down() 和 up() 三个方法都是原子性的，并且这个原子性是由信号量模型的实现方保证的。<br/>
在 Java SDK 并发包里，down() 和 up() 对应的则是 acquire() 和 release()。<br/>

### 2.1 信号量能快速实现一个限流器
它跟Lock的区别是：**Semaphore可以允许多个线程访问一个临界区**。<br/>
比较常见的需求就是各种池化资源，例如连接池、对象池、线程池等等。
```java
class ObjPool<T, R> {
  final List<T> pool;
  // 用信号量实现限流器
  final Semaphore sem;
  // 构造函数
  ObjPool(int size, T t){
    // 这里Vector能保证线程安全
    pool = new Vector<T>(){};
    for(int i=0; i<size; i++){
      pool.add(t);
    }
    sem = new Semaphore(size);
  }
  // 利用对象池的对象，调用func
  R exec(Function<T,R> func) {
    T t = null;
    sem.acquire();
    try {
      t = pool.remove(0);
      return func.apply(t);
    } finally {
      pool.add(t);
      sem.release();
    }
  }
}
```

## 3.ReadWriteLock
有一种非常普遍的场景：读多写少场景。  
在实际工作中，为了优化性能，经常会使用缓存，例如缓存元数据、缓存基础数据。（一个重要的条件就是，缓存里的数据一定是读多写少的）  
读写锁就是针对这个场景下使用的，它有三条基本原则：  
1.允许多个线程同时读共享变量。  
2.只允许一个线程写共享变量。  
3.如果线程正在写操作，此时禁止读线程共享变量。  

### 3.1 懒加载缓存
假设缓存的源头是数据库，如果缓存在没有命中的，那么需要从数据库中加载，然后写入缓存，写缓存时就需要用写锁。  
要注意的是，在获取写锁之后，并没有直接去查数据库，而是重新验证一次缓存是否存在。（高并发时，可能会有多个线程竞争写锁）
```java
public class Cache<K, V> {
    final Map<K, V> m = new HashMap<>();
    final ReadWriteLock rwl = new ReentrantReadWriteLock();
    final Lock r = rwl.readLock();
    final Lock w = rwl.writeLock();

    V get(K key) {
        V v = null;
        // 读缓存
        r.lock();
        try {
            v = m.get(key);
        } finally {
            r.unlock();
        }
        // 缓存命中
        if (v != null) {
            return v;
        }
        // 缓存未命中，查询数据库
        w.lock();
        try {
            // 再次验证
            v = m.get(key);
            if (v == null) {
                v = getFromDataBase();
                m.put(key, v);
            }
        } finally {
            w.unlock();
        }
        return v;
    }
}
```

### 3.2 读写锁的升级与降级
读写锁是**不允许锁的升级的**，但是允许锁的降级。在读锁未释放时获取写锁，会导致写锁永久等待。导致相关线程都被阻塞。
```java
class CachedData {
    Object data;
    volatile boolean cacheValid;
    final ReadWriteLock rwl =
            new ReentrantReadWriteLock();
    // 读锁  
    final Lock r = rwl.readLock();
    //写锁
    final Lock w = rwl.writeLock();

    void processCachedData() {
        // 获取读锁
        r.lock();
        if (!cacheValid) {
            // 释放读锁，因为不允许读锁的升级
            r.unlock();
            // 获取写锁
            w.lock();
            try {
                // 再次检查状态  
                if (!cacheValid) {
                    data = ...
                    cacheValid = true;
                }
                // 释放写锁前，降级为读锁
                // 降级是可以的
                r.lock(); ①
            } finally {
                // 释放写锁
                w.unlock();
            }
        }
        // 此处仍然持有读锁
        try {
            use(data);
        } finally {
            r.unlock();
        }
    }
}
```

## 4.StampedLock
它的性能比ReadWriteLock还要好一些，它支持三种模式：写锁、悲观读锁和乐观读。  
悲观读锁、写锁跟ReadWriteLock的读写锁差不多，不同的是，加锁成功后，StampedLock会返回一个stamp。（就类似版本号的东西呗，CAS原理）
```java
public class Demo {
    final StampedLock sl = new StampedLock();
    private void func() {
        // 获取/释放悲观读锁示意代码
        long stamp = sl.readLock();
        try {
          //省略业务相关代码
        } finally {
          sl.unlockRead(stamp);
        }
        
        // 获取/释放写锁示意代码
        long stamp = sl.writeLock();
        try {
          //省略业务相关代码
        } finally {
          sl.unlockWrite(stamp);
        }
    }
}
```
StempedLock的性能之所以比ReadWriteLock的性能好，其关键是StampedLock支持乐观读。  
ReadWriteLock在多线程同时读的时候，所有写操作都会被阻塞。而StampedLock提供的乐观读，是允许一个线程获取写锁的。  
```java

class Point {
    private int x, y;
    final StampedLock sl = new StampedLock();

    //计算到原点的距离  
    int distanceFromOrigin() {
        // 乐观读
        long stamp = sl.tryOptimisticRead();
        // 读入局部变量，
        // 读的过程数据可能被修改
        int curX = x, curY = y;
        //判断执行读操作期间，
        //是否存在写操作，如果存在，
        //则sl.validate返回false
        if (!sl.validate(stamp)) {
            // 升级为悲观读锁
            stamp = sl.readLock();
            try {
                curX = x;
                curY = y;
            } finally {
                //释放悲观读锁
                sl.unlockRead(stamp);
            }
        }
        return Math.sqrt(curX * curX + curY * curY);
    }
}
```
在上面代码的示例，如果执行乐观读操作的期间，存在写操作，**会把乐观读升级为悲观读锁。**否则你就需要在一个循环里反复执行乐观读（相当于自己实现自旋了），浪费CPU。

### 4.1 理解乐观读
乐观读其原理其实就是CAS，而返回的stamp就是其版本号。

### 4.2 StampedLock的注意事项
1.StampedLock不支持可重入  
2.StampedLock的悲观读锁，写锁都不支持条件变量  
3.线程阻塞在StampedLock的readLock()或者是WriteLock()上时，调用该阻塞线程的interrupt()方法，会导致CPU飙升。  
**所以在使用StampedLock一定不要调用中断操作，如果需要支持中断功能，一定使用可中断的悲观读锁readLockInterruptibly()和写锁writeLockInterruptibly()**

## 5.CountDownLatch 和 CyclicBarrier
CountDownLatch: 等待多个线程并行都执行任务完了，再往下执行。  
```java
// 创建2个线程的线程池
Executor executor = Executors.newFixedThreadPool(2);
while(存在未对账订单){
    // 计数器初始化为2
    CountDownLatch latch = new CountDownLatch(2);
    // 查询未对账订单
    executor.execute(()-> {
        pos = getPOrders();
        latch.countDown();
    });
    // 查询派送单
    executor.execute(()-> {
        dos = getDOrders();
        latch.countDown();
    });

    // 等待两个查询操作结束
    latch.await();

    // 执行对账操作
    diff = check(pos, dos);
    // 差异写入差异库
    save(diff);
}
```
CyclicBarrier: 等待多个线程并行都执行任务完了，调用一个回调方法，并循环执行。  
CyclicBarrier的计数器有自动重置的功能，当减到 0 的时候，会自动重置你设置的初始值。  
```java
// 订单队列
Vector<P> pos;
// 派送单队列
Vector<D> dos;
// 执行回调的线程池 
Executor executor = Executors.newFixedThreadPool(1);
final CyclicBarrier barrier =
        new CyclicBarrier(2, ()->{
            executor.execute(()->check());
        });

void check(){
    P p = pos.remove(0);
    D d = dos.remove(0);
    // 执行对账操作
    diff = check(p, d);
    // 差异写入差异库
    save(diff);
}

void checkAll(){
    // 循环查询订单库
    Thread T1 = new Thread(()->{
        while(存在未对账订单){
            // 查询订单库
            pos.add(getPOrders());
            // 等待
            barrier.await();
        }
    });
    T1.start();
    // 循环查询运单库
    Thread T2 = new Thread(()->{
        while(存在未对账订单){
            // 查询运单库
            dos.add(getDOrders());
            // 等待
            barrier.await();
        }
    });
    T2.start();
}
```
（这两个东西我感觉它们能用到的地方还是很多的）。

## 6.原子类
```java
public class Test {
  AtomicLong count = 
    new AtomicLong(0);
  void add10K() {
    int idx = 0;
    while(idx++ < 10000) {
      count.getAndIncrement();
    }
  }
}
```
原子类是用无锁方案实现的，最大的好处就是性能。  
（而互斥锁为了保证互斥性，需要执行加锁、解锁操作，会消耗性能，同时难不倒锁的线程还会进入阻塞，触发线程切换，也要消耗性能）  

### 6.1 无锁方案的实现
其实原子类性能高的密码很简单，硬件支持而已。**CPU为了解决并发问题，提供了CAS指令（Compare And Swap）**
CAS 指令包含 3 个参数：共享变量的内存地址 A、用于比较的值 B 和共享变量的新值 C  
并且只有当内存中地址 A 处的值等于 B 时，才能将内存中地址 A 处的值更新为新值 C。    
**作为一条 CPU 指令，CAS 指令本身是能够保证原子性的。**

### 6.2 Java如何实现原子化的count+=1
在Java 1.8版本中，getAndIncreament()方法会转调 unsafe.getAndAddLong() 方法。
```java
final long getAndIncrement() {
  return unsafe.getAndAddLong(this, valueOffset, 1L);
}
```

## 7.Executor与线程池
虽然在Java语言中创建线程看上去就像创建对象一样简单，只需要new Thread()就可以了，但实际上创建线程远不是创建一个对象那么简单。  
创建对象知识在JVM堆中分配一块内存而已。  
而创建一个线程却需要调用操作系统内核的API，然后操作系统要为线程分配一系列资源，这个成本就很高了。  
所以**线程是一个重量级的对象，应该避免频繁创建和销毁**

### 7.1 线程池是一种生产者-消费者模式
