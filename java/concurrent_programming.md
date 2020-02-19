# Java并发编程

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