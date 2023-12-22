---
title: Java 并发编程 (工具篇)
date: 2020-04-27 20:44:31
categories: [ Java ]
---

来到的挺关键的一章，实战上能用到的都在这~

从 java.util.concurrent 命名中可以大概区分为 Concurrent*, CopyOnWrite 和 Block 这3类。
Concurrent 没有类似 CopyOnWrite 之类容器相对较重的修改开销。

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
public class Demo {
    // 支持中断的API
    void lockInterruptibly() throws InterruptedException;

    // 支持超时的API
    boolean tryLock(long time, TimeUnit unit) throws InterruptedException;

    // 支持非阻塞获取锁的API
    boolean tryLock();
}
```

### 1.2 如何保证可见性

它利用了volatile相关的Happen-Before规则。（如ReentrantLock，它内部持有一个volatile的成员变量state）

### 1.3 可重入锁

可重入锁，指的是线程可以重复获取同一把锁。  
（递归调用到相同函数的时候）

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
public class Demo {
    //无参构造函数：默认非公平锁
    public ReentrantLock() {
        sync = new NonfairSync();
    }

    //根据公平策略参数创建锁
    public ReentrantLock(boolean fair) {
        sync = fair ? new FairSync()
                : new NonfairSync();
    }
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
public class BlockedQueue<T> {
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
            while (队列已满) {
                // 等待队列不满
                notFull.await();
            }
            // 省略入队操作...
            //入队后,通知可出队
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    // 出队
    void deq() {
        lock.lock();
        try {
            while (队列已空) {
                // 等待队列不空
                notEmpty.await();
            }
            // 省略出队操作...
            //出队后，通知可入队
            notFull.signal();
        } finally {
            lock.unlock();
        }
    }
}
```

## 2.Semaphore(信号量)

信号量模型还是很简单的：一个计算器，一个等待队列，三个方法。  
{% img /images/pic_concurrent_program10.png %}  
这里提到的 init()、down() 和 up() 三个方法都是原子性的，并且这个原子性是由信号量模型的实现方保证的。<br/>
在 Java SDK 并发包里，down() 和 up() 对应的则是 acquire() 和 release()。<br/>

### 2.1 信号量能快速实现一个限流器

它跟Lock的区别是：**Semaphore可以允许多个线程访问一个临界区**。  
比较常见的需求就是各种池化资源，例如连接池、对象池、线程池等等。

```java
class ObjPool<T, R> {
    final List<T> pool;
    // 用信号量实现限流器
    final Semaphore sem;

    // 构造函数
    ObjPool(int size, T t) {
        // 这里Vector能保证线程安全
        pool = new Vector<T>() {
        };
        for (int i = 0; i < size; i++) {
            pool.add(t);
        }
        sem = new Semaphore(size);
    }

    // 利用对象池的对象，调用func
    R exec(Function<T, R> func) {
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
                    data = "something";
                    cacheValid = true;
                }
                // 释放写锁前，降级为读锁
                // 降级是可以的
                r.lock();
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
**所以在使用StampedLock一定不要调用中断操作，如果需要支持中断功能，一定使用可中断的悲观读锁readLockInterruptibly()
和写锁writeLockInterruptibly()**

## 5.CountDownLatch 和 CyclicBarrier

CountDownLatch: 等待多个线程并行都执行任务完了，再往下执行。

```java
// 创建2个线程的线程池
public class Demo {
    public void run() {
        Executor executor = Executors.newFixedThreadPool(2);
        while (存在未对账订单) {
            // 计数器初始化为2
            CountDownLatch latch = new CountDownLatch(2);
            // 查询未对账订单
            executor.execute(() -> {
                pos = getPOrders();
                latch.countDown();
            });
            // 查询派送单
            executor.execute(() -> {
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
    }
}

```

CyclicBarrier: 等待多个线程并行都执行任务完了，调用一个回调方法，并循环执行。  
CyclicBarrier 的计数器有自动重置的功能，当减到 0 的时候，会自动重置你设置的初始值。
CyclicBarrier 最后一个线程执行回调。

```java
public class Demo {
    // 订单队列
    Vector<P> pos;
    // 派送单队列
    Vector<D> dos;
    // 执行回调的线程池 
    Executor executor = Executors.newFixedThreadPool(1);
    final CyclicBarrier barrier =
            new CyclicBarrier(2, () -> {
                executor.execute(() -> check());
            });

    void check() {
        P p = pos.remove(0);
        D d = dos.remove(0);
        // 执行对账操作
        diff = check(p, d);
        // 差异写入差异库
        save(diff);
    }

    void checkAll() {
        // 循环查询订单库
        Thread T1 = new Thread(() -> {
            while (存在未对账订单) {
                // 查询订单库
                pos.add(getPOrders());
                // 等待
                barrier.await();
            }
        });
        T1.start();
        // 循环查询运单库
        Thread T2 = new Thread(() -> {
            while (存在未对账订单) {
                // 查询运单库
                dos.add(getDOrders());
                // 等待
                barrier.await();
            }
        });
        T2.start();
    }
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
        while (idx++ < 10000) {
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
public class Demo {
    final long getAndIncrement() {
        return unsafe.getAndAddLong(this, valueOffset, 1L);
    }
}
```

## 7.Executor与线程池

虽然在Java语言中创建线程看上去就像创建对象一样简单，只需要new Thread()
就可以了，但实际上创建线程远不是创建一个对象那么简单。  
创建对象知识在JVM堆中分配一块内存而已。  
而创建一个线程却需要调用操作系统内核的API，然后操作系统要为线程分配一系列资源，这个成本就很高了。  
所以**线程是一个重量级的对象，应该避免频繁创建和销毁**

### 7.1 线程池是一种生产者-消费者模式

它并不是通过 execute(Runnable target) 这种方式去执行的。（说实话，一开始我以为是这样的）  
目前业界的线程池设计，普遍采用**生产者 - 消费者模式**。线程池的使用方是生产者，线程池本身是消费者。

```java
//简化的线程池，仅用来说明工作原理
class MyThreadPool {
    //利用阻塞队列实现生产者-消费者模式
    BlockingQueue<Runnable> workQueue;
    //保存内部工作线程
    List<WorkerThread> threads
            = new ArrayList<>();

    // 构造方法
    MyThreadPool(int poolSize,
                 BlockingQueue<Runnable> workQueue) {
        this.workQueue = workQueue;
        // 创建工作线程
        for (int idx = 0; idx < poolSize; idx++) {
            WorkerThread work = new WorkerThread();
            work.start();
            threads.add(work);
        }
    }

    // 提交任务
    void execute(Runnable command) {
        workQueue.put(command);
    }

    // 工作线程负责消费任务，并执行任务
    class WorkerThread extends Thread {
        public void run() {
            //循环取任务并执行
            while (true) {
                Runnable task = workQueue.take();
                task.run();
            }
        }
    }
}

public class Demo {
    /**
     * 下面是使用示例
     **/
    public void run() {
        // 创建有界阻塞队列
        BlockingQueue<Runnable> workQueue =
                new LinkedBlockingQueue<>(2);
        // 创建线程池
        MyThreadPool pool = new MyThreadPool(
                10, workQueue);
        // 提交任务
        pool.execute(() -> {
            System.out.println("hello");
        });
    }
}
```

在 MyThreadPool 的内部，我们维护了一个阻塞队列 workQueue 和一组工作线程    
工作线程个数由构造函数中的 poolSize 来指定。  
通过调用 execute() 方法来提交 Runnable 任务。execute() 方法仅仅将任务放入 workQueue 中。（而非真正执行）  
MyThreadPool 内部工作线程会消费 workQueue 中的任务并执行任务。
（其实线程池的实现也没有很复杂）

### 7.2 如何使用 Java 中的线程池

Java 提供的线程池当中最核心的是 **ThreadPoolExecutor**，通过名字也可能看出来，它强调的是 Executor (有什么区别呢？)
，而不是一般的池化资源。

TheadPoolExecutor 的构造函数
（一开始理解起来可能很复杂，但是之前做完阿里云中间件比赛之后，就着实理解了池化到底干了什么事情）

```java
public class Demo {
    ThreadPoolExecutor(
            int corePoolSize,
            int maximumPoolSize,
            long keepAliveTime,
            TimeUnit unit,
            BlockingQueue<Runnable> workQueue,
            ThreadFactory threadFactory,
            RejectedExecutionHandler handler) {
    }
}
```

可以把线程池类比成一个项目组，而线程就是项目组的成员（可以，这个类比有点意思）

#### corePoolSize

表示线程池保有的最少线程数。有些项目很闲，但至少有coolPoolSize个人在待命。

#### maximumPoolSize

表示线程池创建的最大线程数。当项目很忙时，就需要加人，但是也不能无限制地加。  
最多加到 maximumPoolSize 个人。当项目闲下来时，就要撤走，最多能撤到 corePoolSize 个。

#### keepAliveTime & unit

一个线程如果在一段时间内，都没有执行任务，说明很闲，keepAliveTime 和 unit 就是用来定义这个“一段时间”的参数。  
也就是说，如果一个线程空闲了keepAliveTime & unit这么久，而且线程池的线程数大于 corePoolSize ，那么这个空闲的线程就要被回收了。

#### workQueue

工作队列，和上面示例代码的工作队列同义。

#### threadFactory

通过这个参数你可以自定义如何创建线程，例如你可以给线程指定一个有意义的名字。

#### handler

通过这个参数你可以自定义任务的拒绝策略。

### 7.3 使用线程池要注意的

因为 ThreadPoolExecutor 的构造函数实在有些复杂，所有 Java 并发包提供了一个线程池的静态工厂类 Executors。  
但是 Executors 默认提供的都是无界的 LinkedBlockQueue，在高负载下会导致OOM的发生。

使用有界队列，当任务过多时，线程池会触发执行 默认拒绝策略。如果线程池处理的任务非常重要，**建议自定义拒绝策略**。

如果 execute() 方法提交任务是，如果任务在执行的过程中出现运行时异常，会导致执行的任务线程终止；  
不过最致命的是任务虽然异常了，但是没获取到任何通知，就会让人误以为执行得很正常。  
最简单的方法还是捕获所有的异常并按需处理。

```java
public class Demo {
    void run() {
        try {
            //业务逻辑
        } catch (RuntimeException x) {
            //按需处理
        } catch (Throwable x) {
            //按需处理
        }
    }
}
```

## 8.Future

利用 ThreadPoolExecutor 的 void executor(Runnable command) 方法虽然可以提交任务，但是却没有办法获取任务的执行结果。

Java 通过 ThreadPoolExecutor 提供的3个 submit() 方法和1个 FutureTask 工具类来支持获得任务执行结果的需求。

```java
public class Demo {
    // 提交Runnable任务
    Future<?> submit(Runnable task);

    // 提交Callable任务
    <T> Future<T> submit(Callable<T> task);

    // 提交Runnable任务及结果引用
    <T> Future<T> submit(Runnable task, T result);
}
```

(Callable能返回结果)
而 Future 接口提供了5个方法（截取自 concurrent 包）：

```java
public interface Future<V> {
    // 取消任务
    boolean cancel(boolean mayInterruptIfRunning);

    // 判断任务是否已取消
    boolean isCancelled();

    // 判断任务是否已结束
    boolean isDone();

    // 获得任务执行结果
    V get() throws InterruptedException, ExecutionException;

    // 获得任务执行结果，支持超时
    V get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException;
}
```

### 8.1 三个submit()方法的区别

1.submit(Runnable task)： 这个方法的参数是一个 Runnable 接口，Runnable 接口的 run() 方法是没有返回值得，
所以 submit(Runnable task) 方法返回的 Future 仅可以用来断言任务已经结束了，类似于 Thread.join()。  
（...说实话 Thread.join() 的具体我也不大清楚）

2.submit(Callable<T> task)： 这个方法的参数是一个 Callable 接口，它只有一个 call() 方法，并且是有返回值的，
所以这个方法返回的 Future 对象可以通过调用其 get() 方法来获取任务的执行结果。

3.submit(Runnable task, T result)： 假设这个方法返回的 Future 对象是 f，f.get() 的返回值就是传给 submit()
方法的参数 result。  
需要注意的是 Runnable 接口的实现类 Task 声明一个有参构造函数 Task(Result r)。  
创建 Task 对象的时候传入 result 对象，就能在类 Task 的 run() 方法中对 result 进行各种操作了，
通过它，主线程和子线程可以共享数据（...还能这样？也算是一种同步吗）

```java
class Demo {
    void run() {
        ExecutorService executor = Executors.newFixedThreadPool(1);
        // 创建Result对象r
        Result r = new Result();
        r.setAAA(a);
        // 提交任务
        Future<Result> future = executor.submit(new Task(r), r);
        Result fr = future.get();
        // 下面等式成立
        fr == r;
        fr.getAAA() == a;
        fr.getXXX() == x;
    }
}

class Task implements Runnable {
    Result r;

    //通过构造函数传入result
    Task(Result r) {
        this.r = r;
    }

    void run() {
        //可以操作result
        a = r.getAAA();
        r.setXXX(x);
    }
}
```

（其实就是一个变量 Result 可以在各个线程之间给其赋值）  
而 FutureTask 实现了 Runnable 和 Future 接口，由于实现了 Runnable 接口，所以可以将 FutureTask 对象作为任务交给
ThreadPoolExecutor
去执行

### 8.2 用 Future 实现烧水泡茶

文章中给出的流程图是这样的：  
{% img /images/pic_concurrent_program11.png %}  
前面说到了并发编程可以总结为3个核心的问题（分工、同步和互斥）：  
分工指得就是高效地拆解任务并分配给线程。（同步互斥就不再解释了）  
目前来讲吧下面的3道工序分配给第二个线程去完成。  
{% img /images/pic_concurrent_program12.png %}

#### 实现步骤：

1.创建2个 FutureTask ，ft1 完成洗水壶，烧开水，泡茶的任务，ft2 完成其余步骤。  
2.ft1 这个任务执行在泡茶任务之前，需要等待 ft2 把茶叶拿来，所以 ft1 内部需要引用 ft2，并在执行泡茶前，
调用 ft2 的 get() 方法实现等待。  
（代码有一丢丢长，不过不难看懂）

```java
class Demo {
    // 创建任务T2的FutureTask
    FutureTask<String> ft2 = new FutureTask<>(new T2Task());

    // 创建任务T1的FutureTask
    FutureTask<String> ft1 = new FutureTask<>(new T1Task(ft2));

    // 线程T1执行任务ft1
    Thread T1 = new Thread(ft1);
    T1.start();

    // 线程T2执行任务ft2
    Thread T2 = new Thread(ft2);
    T2.start();
    // 等待线程T1执行结果
    System.out.println(ft1.get());
}

// T1Task需要执行的任务：
// 洗水壶、烧开水、泡茶
class T1Task implements Callable<String> {
    FutureTask<String> ft2;

    // T1任务需要T2任务的FutureTask
    T1Task(FutureTask<String> ft2) {
        this.ft2 = ft2;
    }

    @Override
    String call() throws Exception {
        System.out.println("T1:洗水壶...");
        TimeUnit.SECONDS.sleep(1);

        System.out.println("T1:烧开水...");
        TimeUnit.SECONDS.sleep(15);
        // 获取T2线程的茶叶  
        String tf = ft2.get();
        System.out.println("T1:拿到茶叶:" + tf);

        System.out.println("T1:泡茶...");
        return "上茶:" + tf;
    }
}

// T2Task需要执行的任务:
// 洗茶壶、洗茶杯、拿茶叶
class T2Task implements Callable<String> {
    @Override
    String call() throws Exception {
        System.out.println("T2:洗茶壶...");
        TimeUnit.SECONDS.sleep(1);

        System.out.println("T2:洗茶杯...");
        TimeUnit.SECONDS.sleep(2);

        System.out.println("T2:拿茶叶...");
        TimeUnit.SECONDS.sleep(1);
        return "龙井";
    }
}
/*
 * 一次执行结果：
 * T1:洗水壶...
 * T2:洗茶壶...
 * T1:烧开水...
 * T2:洗茶杯...
 * T2:拿茶叶...
 * T1:拿到茶叶:龙井
 * T1:泡茶...
 * 上茶:龙井
 */
```

## 9.CompletableFuture 异步编程

Java 在 1.8 版本提供了 CompletableFuture 来支持异步编程，
CompletableFuture 有可能是你见过的最复杂的工具类了，不过功能也着实让人感到震撼。

### 9.1 CompletableFuture 的优势

1.无需手工维护线程，没有繁琐的手工维护线程的工作，非任务分配线程的工作也不需要我们关注。  
2.语义更清晰，如 f3 = f1.thenCombine(f2,()->{}) 能够清晰表述“任务3要等待任务1,2完成后才开始”。  
3.代码更加简练且专注于业务逻辑。

```java
class Demo {
    void solve() {
        //任务1：洗水壶->烧开水
        CompletableFuture<Void> f1 = CompletableFuture.runAsync(() -> {
            System.out.println("T1:洗水壶...");
            sleep(1, TimeUnit.SECONDS);

            System.out.println("T1:烧开水...");
            sleep(15, TimeUnit.SECONDS);
        });
        //任务2：洗茶壶->洗茶杯->拿茶叶
        CompletableFuture<String> f2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("T2:洗茶壶...");
            sleep(1, TimeUnit.SECONDS);

            System.out.println("T2:洗茶杯...");
            sleep(2, TimeUnit.SECONDS);

            System.out.println("T2:拿茶叶...");
            sleep(1, TimeUnit.SECONDS);
            return "龙井";
        });
        //任务3：任务1和任务2完成后执行：泡茶
        CompletableFuture<String> f3 = f1.thenCombine(f2, (__, tf) -> {
            System.out.println("T1:拿到茶叶:" + tf);
            System.out.println("T1:泡茶...");
            return "上茶:" + tf;
        });
        //等待任务3执行结果
        System.out.println(f3.join());

        void sleep ( int t, TimeUnit u){
            try {
                u.sleep(t);
            } catch (InterruptedException e) {
            }
        }
    }
}
/*
 * 一次执行结果：
 * T1:洗水壶...
 * T2:洗茶壶...
 * T1:烧开水...
 * T2:洗茶杯...
 * T2:拿茶叶...
 * T1:拿到茶叶:龙井
 * T1:泡茶...
 * 上茶:龙井
 */
```

可能不知道 runAsync 跟 supplyAsync 有什么区别，但是明显看起来代码更加短，更加容易理解了。

### 9.2 CompletableFuture 的创建

supplyAsync 跟 runAsync 的区别主要是前者有返回值。  
而 CompletableFuture 的创建方法可以指定线程池。  
（不设置默认为 ForkJoinPool , 默认创建线程数为 CPU 个数，如果所有 CompletableFuture 都共用它，I/O操作将会很慢）  
**所以，建议根据不同的业务类型，自己设置线程池，避免相互干扰**

### 9.3 CompletionStage 接口

CompletionStage 接口可以清晰地描述任务之间的时序关系。  
例如前面 f3 = f1.thenCombine(f2,()->{}) 描述了一种汇聚关系（AND）。
那也会有一种 OR 聚合关系，描述依赖的任务只要有一个完成就可以执行当前任务。
（现在暂时没用过这个工具，所以就不介绍各种接口了，真正用到再说，貌似也不难看懂）

## 10.CompletionService 批量执行异步任务

这个类提供的创建函数主要是一个线程池 和 Future 的拥塞队列。  
原型还是 生产者 - 消费者 模式。  
跟 Future 的区别是它最后线程任务完成后会经过队列，消费者从队列中取即可。（C）