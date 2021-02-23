---
title: Java 并发编程 (基础篇)
date: 2020-04-27 20:44:31
tags:
categories: Java
---

## 1.可见性、原子性和有序性
CPU、内存和IO设备的处理能力的不一致，使得我们要通过一些策略去更加高效利用上层资源(处理能力较快的设备)。  
1.CPU增加缓存，以均衡与内存的速度差异。  
2.操作系统增加进程、线程，来分时复用CPU。进而均衡CPU与IO设备的速度差异。  
3.编译程序优化指令执行次序，似的缓存能够得到更加合理的利用。(如JAVA虚拟机的指令排序)  

### 1.1 可见性
一个线程对共享资源变量的修改，另外一个线程能否立即看到，称为可见性。  
在多核的情况下，线程占用的是不同的CPU资源。
  
<img src="https://github.com/LayneHuang/ForEasyCode/blob/master/images/pic_concurrent_program1.png" width="500">  

### 1.2 原子性
我们把一个或者多个操作在CPU执行的过程中不被中断的特性成为原子性。
其实在我的理解中是一段连续的操作（内部的资源）只能被单个线程所占用。

### 1.3 有序性（可能是最容易被忽略的）
编译器为了性能优化，不影响程序运行结果的前提下，可能会调整一些语句的顺序。  
比较经典的例子就是双重检查创建单例对象。  
正常顺序:  
1.分配内存  
2.在内存上初始化对象  
3.把M的地址赋值给instance  
优化后，将2.跟3.调换了。结果先拿到锁的线程在2.之后就释放锁，另外一个线程发现对象非空，调用就会发生空指针错误。

## 2.Java内存模型，解决可见性和有序性问题
导致可见性的原因是缓存，导致有序性的原因是编译优化，但是通过直接禁用的方式去解决问题，就会影响程序的性能。所以要按需禁用。  
Java内存模型规范了JVM如何提供按需禁用缓存和编译优化的方法。具体来说，这些方法包括**volatile**、**synchronized**和**final**3个关键字、以及6项**Happens-Before规则**。  

### 2.1 volatile
它的意义就是禁用CPU缓存。例如声明了一个对象 volatile int x = 0; 它的表达意义是：告诉编译器，对这个变量的读写必须从内存中读取或者写入。

### 2.2 Happens-Before规则
1.在一个线程中，前面的操作Happens-Before于后续的任意操作。  
2.volatile的写操作Happens-Before与后续对这个volatile的读操作。  
3.传递性，A Happens-Before于 B，B Happens-Before于 C ，那么A Happens-Before于 C。  
4.管程中的锁规则，对一个锁的解锁Happens-Before于后续对这个锁的加锁。synchronized是Java里对管程的实现。  
5.线程start()规则，start() Happens-Before于线程中的任意操作。  
6.线程join()规则，这条操作关于线程等待，线程A等待线程B完成，当线程B完成后（主线程A从join()中返回），线程A能够看到线程B的操作（**共享变量的操作**）。

### 2.3 final
final修饰变量时，就告诉编译器，这个变量生而不变，可以进行优化。

## 3.互斥锁：解决原子性问题
跟我之前所理解的是一样，**一段连续的操作（内部的资源）只能被单个线程所占用**。  
synchronized就是java所提供的锁技术。  
**当修饰静态方法的时候，锁定的就是当前类的Class对象。当修饰非静态方法时，锁定的是当前实例对象this。**

### 3.1 锁和受保护资源的关系
锁和受保护资源的关系是1：n的关系。（一个相同的资源不能被多把锁保护）  
```java
class SafeCalc {
    static long value = 0L;
    synchronized long get() {
        return value;
    }
    synchronized static void addOne() {
        value += 1;
    }
}
```
你会发现这段代码其实是用两个锁保护一个资源。这个受保护的资源就是静态变量 value，两个锁分别是 this 和 SafeCalc.class。我们可以用
下面这幅图来形象描述这个关系。  
<img src="https://github.com/LayneHuang/ForEasyCode/blob/master/images/pic_concurrent_program2.png" width="500">  
由于临界区 get() 和 addOne() 是用两个锁保护的，因此这两个临界区没有互斥关系，临界区 addOne() 对 value 的修改对临界区 get() 也
没有可见性保证，这就导致并发问题了。  

直接用new Object()来进行加锁又有什么问题呢？（第一眼就可以看出这肯定不是同一把锁）
```java
class SafeCalc {
  long value = 0L;
  long get() {
    synchronized (new Object()) {
      return value;
    }
  }
  void addOne() {
    synchronized (new Object()) {
      value += 1;
    }
  }
}
```
加锁本质是在对象头中写入当前线程id，但是new Object每次在内存中都是新对象（就是所说的多把锁锁同一资源），所以加锁无效。  

### 3.2 细粒度锁
所谓细粒度锁，讲的就是在使用锁的时候，如果两个资源之间的操作互不相关，他们应该使用不同的锁，这样他们操作就能并行。（容易理解）

### 3.3 保护有关联的多个资源
当业务中需要进行转账操作时（涉及两个账户金额操作），文中给出的Sample是这样的：
```java
class Account {
    private int balance;
    // 转账
    synchronized void transfer(
        Account target, int amt){
        if (this.balance > amt) {
            this.balance -= amt;
            target.balance += amt;
        }
    } 
}
```
可能一开始并未理解能有什么问题，但是立即就可以发现synchronized修饰在方法上，实际上持有的是this这个对象的锁，它对于this这个对象的其他所有操作都进行了阻塞
（确实是保护到了本对象，但实际上也不太可能这样做，因为可能Account中并不仅只有balance这个变量，如果我去处理的话，可能粒度会更细一点，不会使用this做锁）。  
另一方面，target这个对象并没有保护到，它可被随时修改，有并发问题。  
实际上它们需要使用同一把锁，如果使用同一个object，或者类锁，都会串行所有的转账操作，效率极低，不可行。

## 4.死锁问题

### 4.1 死锁的例子
对于上面那个问题，实际上我们可以直接去锁定两个对象去提高并行度。
```java
class Account {
    private int balance;
    // 转账
    void transfer(
        Account target, int amt){
        synchronized(this) {
            synchronized(target) {
                if (this.balance > amt) {
                    this.balance -= amt;
                    target.balance += amt;
                }
            }
        }
    } 
}
```
但是会存在一种情况，就是（1）A要转账给B，（2）B也要转账给A，同时发生。  
假设是线程1去完成（1）操作，线程2去完成（2）操作  
同一时间，线程1占有了A的锁，线程2占有了B的锁。然后互相都拥塞在第二个同步上了，出现了死锁。

### 4.2 如何预防死锁
一个叫Coffman的人提出，只有下列4个条件同时出现的时候才会出现死锁：  
1.互斥，共享资源 X 和 Y 只能被一个线程占用。（无法破坏，用锁为的就是互斥）  
2.占有且等待，线程T1已经取得共享资源X，在等待共享资源Y的时候，不释放共享资源X。（所有资源一并申请）  
3.不可抢占，其他线程不能强行抢占线程T1占有的资源。（申请不到后续资源时，就释放当前资源）  
4.循环等待，线程T1等待线程T2占有的资源，线程T2等待线程T1占有的资源，就是循环等待。（有序申请）  
**也就是说只要破坏其中一个，就能避免死锁**

## 5.等待-通知机制
synchronized可以配合wait()、notyfy()、notyfyAll()这三个方法实现等待通知机制。  
<img src="https://github.com/LayneHuang/ForEasyCode/blob/master/images/pic_concurrent_program3.png" width="500">  
1.当线程进入临界区，由于某些条件不满足的情况下，调用wait()方法就会进入等待状态。同时当前线程被拥塞，进入右边的等待队列中，并且释放锁。  
2.锁被释放后，左边等待队列（临界区入口）中的线程就有机会获得锁。  
3.当条件满足的时候，调用notify()或notifyAll()方法即可唤醒右边等待队列中的线程。  
**尽量使用notifyAll()**  
notify()会随机通知等待队列中的一个线程，而notifyAll()会通知等待队列中所有的线程。  
notify()可能会导致某个线程永远得不到通知，虽然notifyAll()会唤醒所有线程，而实际上能进入临界区的只有一个。

## 6.安全性、活跃性以及性能问题
并发编程中要注意的问题有很多，主要有三方面的问题**安全性、活跃性以及性能问题**

### 6.1 安全性问题

#### 1.什么是线程安全？  
其实本质上是正确性，而正确性的含义就是**程序按照我们期望的执行**。(本来我想修改描述为"多线程下正确性"，不过感觉上单线程也可以用线程安全描述吧)  

#### 2.如何写出线程安全程序？
并发Bug主要的源头就是之前所讲到的**可见性、原子性和有序性问题**。理论上线程安全的程序，要避免这三个问题。  

**实际上，多个线程会同时读写同一数据**，才需要排查线程安全问题。  
如果数据能够不共享或者数据状态不发生变化，就能保证线程的安全性了，有不少技术都是基于这个理论的，如线程本地存储（Thread Local Storage,TLS）、不变模式等等。  

#### 3.竞态条件
所谓竞态条件，指的是程序执行结果依赖程序执行顺序。  
比如转账操作，转账操作上有一个判断就是，转出金额不能大于账户余额。  
在并发环境中，如果不加控制，当多个线程同时对一个账户执行转出操作就会出现超额转出问题。  
```java
class Account {
  private int balance;
  // 转账
  void transfer(
      Account target, int amt){
    if (this.balance > amt) {
      this.balance -= amt;
      target.balance += amt;
    }
  } 
}
```
其实，在遇到竞态条件时，把判断一同加到互斥模块内就行了。  

### 6.2 活跃性问题
所谓活跃性问题，指的是某个操作无法执行下去。“死锁”就是典型的活跃性问题，此外还有“活锁”和“饥饿”。  
#### 1.活锁
路人A要从左手边**出**门，路人B要从右手边**进**门，相撞了。路人A让路走右手边，路人B让路走左手边。一直让下去...虽然没有阻塞，但成了程序执行不下去的“活锁”。  
解决方法:让A，B分别等待一个随机时间再执行，那他们再次相撞的可能性就极低了。  
#### 2.饥饿
“饥饿”指的是线程因无法访问所需资源而无法执行下去的情况。  
比如某个线程优先级太低，一直得不到调用，又或者是某个持有锁的线程。

### 6.3 性能问题
使用锁要非常小心，但是小心过度，也可能导致性能问题。过度使用可能导致串行化的范围过大。  

#### 1.无锁的算法和数据结构
既然锁会带来性能问题，那最好就是使用无锁的算法和数据结构。  
例如线程本地储存（TLS）、写时复制（Copy On Write,COW）、乐观锁（好像JAVA是用Campare And Sawp，CAS来完成的，之前跟COW有点混淆，不过后面有说明）等；  
Java并发包里面的原子类也是无锁的数据结构，Disruptor则是无锁的内存队列（没用过）。  
#### 2.减少锁持有的时间
使用细粒度的锁，例如JAVA并发包中的ConcurrentHashMap(分段锁，好像后面更新成单节点锁了)。  
还可以使用读写锁，读时无锁，只有写时才会互斥。（貌似是不需要读百分百准确的场景）  
#### 3.性能的指标
3.1 吞吐量：指的是单位时间内能处理的请求量。吞吐量越高，性能越好。  
3.2 延迟：指的是从发出请求到收到响应的时间。延迟越小，性能越好。  
3.3 并发量：指的是同时处理的请求数量，一般来说随着并发量的增加，延迟也会增加。  

## 7.管程
所谓管程，指的是管理共享变量的操作过程，让他们支持并发。  
并发编程领域有两大核心问题：一个是互斥（同一时间只允许一个线程访问共享变量），另外一个是同步（线程之间如何通信，协作）。  

### 7.1 管程（MESA）处理互斥
把共享变量及共享变量操作统一封装起来，只保证一个线程进入管程。

### 7.2 管程（MESA）处理同步
把共享变量及共享变量操作是被封装起来的，最外层的框就代表封装的意思。  
管程里还引入条件变量的概念，**每个条件变量都对应有一个等待队列**  
<img src="https://github.com/LayneHuang/ForEasyCode/blob/master/images/pic_concurrent_program4.png" width="500">  

类比管程入口：（感觉还是有点不大一样吧）  
```java
public class BlockedQueue<T> {
    final Lock lock = new ReentrantLock();
    // 条件变量：队列不满
    final Condition notFull = lock.newCondition();
    // 条件变量：队列不空
    final Condition notEmpty = lock.newCondition();

    void enq(T x) {
        lock.lock();
        try {
            while (队列已满) {
                // 等待队列不满
                notFull.await();
            }
            // 省略入队操作
            // 入队后，通知可出队
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    void deq(T x) {
        lock.lock();
        try {
            while (队列已空) {
                // 等待队列不空
                notEmpty.await();
            }
            // 省略出队操作
            // 入队后，通知可出队
            notFull.signal();
        } finally {
            lock.unlock();
        }
    }
    
}
```

### 7.3 管程模型
1.Hasen模型，要求notify()放在代码最后，这样T2通知完T1之后,T2就结束了，然后在执行T1，保证同一时刻只有一个线程执行。  
2.Hoare模型，T2通知完T1之后,T2阻塞，然后在执行T1，T1执行完唤醒T2，也能保证同一时刻只有一个线程执行。但比Hasen模型多一次阻塞唤醒操作。  
3.MESA，T2通知完T1之后，T2继续执行，T1并不立即执行，仅仅从条件变量等待队列进入到入口等待队列中。这样notify()不用放到代码最后，T2也没有多余的阻塞唤醒操作。  

## 8.Java线程

### 8.1 生命周期
<img src="https://github.com/LayneHuang/ForEasyCode/blob/master/images/pic_concurrent_program5.png" width="500">  

Java语言中线程共有6种状态  
```java
// 来自Thread.java类
public enum State {
    NEW,            // 初始化状态
    RUNNABLE,       // 可运行状态
    BLOCKED,        // 阻塞状态
    WAITING,        // 无时限等待状态
    TIMED_WAITING,  // 有时限等待状态
    TERMINATED;     // 终止状态
}
```
在操作系统层面，Java线程的BLOCKED、WAITING、TIME_WAITING是一种状态，即休眠状态。只要JAVA线程处于这三种状态之一，那么这个线程就永远没有CPU的使用权。  
可简化图:  
<img src="https://github.com/LayneHuang/ForEasyCode/blob/master/images/pic_concurrent_program6.png" width="500">  

#### 1.RUNNABLE与BLOCKED的状态转换
只有一种场景会触发这种转换，synchronized的隐式锁（那并发工具里面的lock那些呢），代码块同一时刻只允许一个线程执行。等待的线程就会从RUNNABLE转换到BLOCKED状态。而等待线程获取到锁就从BLOCKED转换到RUNNABLE状态。  
如果熟悉操作系统生命周期的话，可能有个疑问：线程调用阻塞式API时，是否会转到BLOCKED状态？在**系统层面**线程是会转到休眠状态的，但是在JVM层面，JAVA线程状态不会变化，依然保持RUNNABLE状态。**JVM层面不关心操作系统调度相关的状态**，在JVM看来，等待CPU使用权与等到I/O没有区别，都是在等待某个资源，所以都归入RUNNABLE状态。（所以说，JVM层阻塞跟系统层的阻塞大有不同的）

#### 2.RUNNABLE与WAITTING的状态转换
总体来说有3种场景  
1.获取synchronized隐式锁的线程调用Object.wait()方法。  
2.Thread.join(),其中join()是一种线程同步方法，例如有一个线程对象Thread A，当前Thread B调用 A.join()的时候，Thread B会等待Thread A执行完，Thread B的状态会从RUNNABLE转换到WAITING,当Thread A执行完，Thread B状态会从WAITING转换到RUNNABLE。  
3.调用LockSupport.park()方法。其实并发包中的锁，都是基于LockSupport实现的。调用LockSupport.park()方法，当前线程会阻塞，调用调用LockSupport.unpark(Thread thread)可唤醒目标线程。  

#### 3.RUNNABLE与TIMED_WAITTING的状态转换
这类状态转换通常就是上一类的函数中带个时间参数。  
1.Thread.sleep(long millis)。  
2.synchronized隐式锁的线程调用Object.wait(long timeout)方法。  
3.等等。。。。  

#### 4.NEW与RUNNABLE的状态转换
其实就是调用Thread类的start()函数，正式被系统调度。  

#### 5.RUNNABLE与TERMINATED的状态转换
1.run()运行结束  
2.调用stop()、interrupt()。(stop方法会令线程强制终止，可能导致一些它持有的锁资源未被释放，已被废弃)  

#### 被interrupt的线程是如何收到通知的呢？
1.异常，当线程A处于 WAITING,TIMED_WAITING 状态时，如果其他线程调用线程A的interrupt()方法，会使线程A返回RUNNABLE状态（蛮奇怪的），同时线程A的代码会触发 InterruptedException 异常。
2.主动检测，当线程A处于 RUNNABLE 状态时，并且阻塞在java.nio.chnnels.InterruptibleChannel上时，如果其他线程调用线程A的interrput()方法，线程A会触发java.nio.channels.ClosedByInterruptException这个异常；而阻塞在java.nio.channels.Selector上时，如果其他线程调用线程A的interrupt()，线程A的java.nio.channels.Selector会立即返回。（虽然这样说，其实我也不知道是什么实际情况才用到）。

思考:
```java
public class Demo {
    public void run() {
        Thread th = Thread.currentThread();
        while(true) {
            if( th.isInterrupted() ) {
                break;
            }
            try {
                Thread.sleep(100);
            } catch( InterruptedException e ) {
                e.printStackTrace();
            }
        }
    }
}
```

### 8.2 创建多少线程才是合适的？
多线程本质上就是提升I/O利用率和CPU利用率。  
假设在单线程的情况下，I/O操作和CPU计算的耗时比为1:1，它们交叉执行的方式运行。  
<img src="https://github.com/LayneHuang/ForEasyCode/blob/master/images/pic_concurrent_program7.png" width="500">  
这个时候，CPU利用率和I/O利用率都是50%。  

如果有两个线程的时候，A线程执行CPU计算的时候，B线程执行I/O操作，A线程执行I/O操作的时候，B线程执行CPU计算，这样双方的利用率都可以到达100%。（如果CPU和I/O利用率都很低的情况下可以尝试增加线程来提升吞吐量）  
<img src="https://github.com/LayneHuang/ForEasyCode/blob/master/images/pic_concurrent_program8.png" width="500">  

在单核时代，多线程主要是用来平衡CPU和I/O设备的。如果程序只有CPU计算，而没有I/O计算的话，性能反而会变得更差（增加了线程切换的成本）。  
而在多核时代，它是可以提升性能的。比如计算sum[1,100e]。当你有4核时，可以分别用4个线程去求sum[1,25e),sum[25e,50e),sum[50e,75e),sum[75e,100e],得到结果再做一次求和（实质上就是类似分治的思想吧）。  

#### 1.CPU密集型计算
线程数量 = CPU核数 + 1 (因为偶尔的内存失效或其他原因导致阻塞时，这个额外的线程可以顶上，从而保证CPU利用率)  

#### 2.I/O密集型计算
最佳线程数 = 1 + (I/O耗时 ÷ CPU耗时) [其实就是公式左边的1表示1个线程进行I/O的情况下，公式右边保证了CPU要打满]  
<img src="https://github.com/LayneHuang/ForEasyCode/blob/master/images/pic_concurrent_program9.png" width="500">  


在多核CPU的情况下：  
最佳线程数 = CPU核数 * [ 1 + (I/O耗时 ÷ CPU耗时) ]   

### 8.3 为什么局部变量是线程安全的
其实这个问题跟JVM的内存模型有关。当前线程对方法的调用，其实方法的调用栈是线程独立的（线程间不共享）。  
虽然说 new 语句产生的对象是在堆中的，但引用的栈持有的。

### 8.4 线程异常处理
推荐使用线程池，线程池(包括ScheduledThreadPoolExecutor)的线程 setUncaughtExceptionHandler 是无效的。（线程池执行时可能不是提交时的线程了）  
但如果有些场景不得不独立创建Thread时，遵循本规则。   
Java 多线程程序中，所有线程都不允许抛出未捕获的 checked exception，也就是说各个线程需要自己把自己的 checked exception 处理掉。  
但是无法避免的是 unchecked exception， 也就是 RuntimeException，当抛出异常时子线程会结束。  
但主线程不会知道，因为主线程通过 try catch 是无法捕获子线程异常的。 Thread 对象提供了 setUncaughtExceptionHandler 方法用来获取线程中产生的异常。  
而且还可使用 Thread.setDefaultUncaughtExceptionHandler，为所有线程设置默认异常捕获方法。   
程序员应注意的是，在执行周期性任务例如 ScheduledExecutorService时，为了健壮性，可考虑在提交的 Runnable 的 run 方法内捕获高层级的异常。  
ScheduledExecutorService的各种schedule方法，可以通过其返回的 ScheduledFuture 对象获取其异常。  