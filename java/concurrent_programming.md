# Java并发编程
阅读笔记，记录自极客时间《Java并发编程实战》
## 一.并发编程基础
### 1.可见性、原子性和有序性
CPU、内存和IO设备的处理能力的不一致，使得我们要通过一些策略去更加高效利用上层资源(处理能力较快的设备)。<br/>
1.CPU增加缓存，以均衡与内存的速度差异。<br/>
2.操作系统增加进程、线程，来分时复用CPU。进而均衡CPU与IO设备的速度差异。<br/>
3.编译程序优化指令执行次序，似的缓存能够得到更加合理的利用。(如JAVA虚拟机的指令排序)<br/>
#### 1.1 可见性
一个线程对共享资源变量的修改，另外一个线程能否立即看到，称为可见性。<br/>
在多核的情况下，线程占用的是不同的CPU资源。
<br/>
<img src="https://github.com/LayneHuang/ForEasyCode/blob/master/images/pic_concurrent_program1.png" width="300"><br/>
#### 1.2 原子性
我们把一个或者多个操作在CPU执行的过程中不被中断的特性成为原子性。
其实在我的理解中是一段连续的操作（内部的资源）只能被单个线程所占用。

#### 1.3 有序性（可能是最容易被忽略的）
编译器为了性能优化，不影响程序运行结果的前提下，可能会调整一些语句的顺序。<br/>
比较经典的例子就是双重检查创建单例对象。<br/>
正常顺序:<br/>
1.分配内存<br/>
2.在内存上初始化对象<br/>
3.把M的地址赋值给instance<br/>
优化后，将2.跟3.调换了。结果先拿到锁的线程在2.之后就释放锁，另外一个线程发现对象非空，调用就会发生空指针错误。

### 2.Java内存模型，解决可见性和有序性问题
导致可见性的原因是缓存，导致有序性的原因是编译优化，但是通过直接禁用的方式去解决问题，就会影响程序的性能。所以要按需禁用。<br/>
Java内存模型规范了JVM如何提供按需禁用缓存和编译优化的方法。具体来说，这些方法包括**volatile**、**synchronized**和**final**3个关键字、以及6项**Happens-Before规则**。<br/>
#### 2.1 volatile
它的意义就是禁用CPU缓存。例如声明了一个对象 volatile int x = 0; 它的表达意义是：告诉编译器，对这个变量的读写必须从内存中读取或者写入。

#### 2.2 Happens-Before规则
1.在一个线程中，前面的操作Happens-Before于后续的任意操作。<br/>
2.volatile的写操作Happens-Before与后续对这个volatile的读操作。<br/>
3.传递性，A Happens-Before于 B，B Happens-Before于 C ，那么A Happens-Before于 C。<br/>
4.管程中的锁规则，对一个锁的解锁Happens-Before于后续对这个锁的加锁。synchronized是Java里对管程的实现。<br/>
5.线程start()规则，start() Happens-Before于线程中的任意操作。<br/>
6.线程join()规则，这条操作关于线程等待，线程A等待线程B完成，当线程B完成后（主线程A从join()中返回），线程A能够看到线程B的操作（**共享变量的操作**）。

#### 2.3 final
final修饰变量时，就告诉编译器，这个变量生而不变，可以进行优化。

### 3.互斥锁：解决原子性问题
跟我之前所理解的是一样，**一段连续的操作（内部的资源）只能被单个线程所占用**。<br/>
synchronized就是java所提供的锁技术。<br/>
**当修饰静态方法的时候，锁定的就是当前类的Class对象。当修饰非静态方法时，锁定的是当前实例对象this。**
#### 3.1 锁和受保护资源的关系
锁和受保护资源的关系是N：1的关系。（一个相同的资源不能被多把锁保护）<br/>
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
下面这幅图来形象描述这个关系。<br/>
<img src="https://github.com/LayneHuang/ForEasyCode/blob/master/images/pic_concurrent_program2.png" width="300"><br/>
由于临界区 get() 和 addOne() 是用两个锁保护的，因此这两个临界区没有互斥关系，临界区 addOne() 对 value 的修改对临界区 get() 也
没有可见性保证，这就导致并发问题了。<br/>

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
加锁本质是在对象头中写入当前线程id，但是new Object每次在内存中都是新对象（就是所说的多把锁锁同一资源），所以加锁无效。<br/>

#### 3.2 细粒度锁
所谓细粒度锁，讲的就是在使用锁的时候，如果两个资源之间的操作互不相关，他们应该使用不同的锁，这样他们操作就能并行。（容易理解）

#### 3.3 保护有关联的多个资源
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
（确实是保护到了本对象，但实际上也不太可能这样做，因为可能Account中并不仅只有balance这个变量，如果我去处理的话，可能粒度会更细一点，不会使用this做锁）。<br/>
另一方面，target这个对象并没有保护到，它可被随时修改，有并发问题。<br/>
实际上它们需要使用同一把锁，如果使用同一个object，或者类锁，都会串行所有的转账操作，效率极低，不可行。

#### 3.4 