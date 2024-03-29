---
title: 深入拆解 Java 虚拟机
date: 2020-04-27 20:44:31
categories: [ Java ]
---
来自于极客时间《深入拆解 Java 虚拟机》。

作者将专栏分成了四大模块：  
1.**基本原理**：剖析 Java 虚拟机的运行机制。  
2.**高效实现**：探索 Java 编译器，以及内嵌 Java 虚拟机中的即时编译器，帮助你更好地理解 Java 语言特性。  
3.**代码优化**：介绍如何利用工具定位并解决代码中的问题，以及在已有工具不适用的情况下，如何打造专属轮子。  
（感觉这个十分重要，面试考试的主要内容，也是有意思的地方）
4.**虚拟机黑科技**：介绍甲骨文实验室近年来的前沿工作之一 GraalVM。包括如何在 JVM 上高效运行其他语言，如何混到这些语言，实现 Polyglot。  
（这一部分感觉就没那么容易用得上了）

## 1.Java 代码是怎样运行的
1.JRE：仅包含运行 Java 程序的必须组件，包括 Java 虚拟机以及 Java 核心类库等。  
2.JDK：包含了 JRE ，并且还附带了一系列开发、诊断工具。  