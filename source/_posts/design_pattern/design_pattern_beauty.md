---
title: 设计模式之美
date: 2020-04-02 01:46:00
categories: [ DesignPatterns ]
---

这个也是来自于极客时间《设计模式之美》,以及《Head First设计模式》的阅读笔记。

这个课程有100讲。主要包含：  
1.面向对象  
2.设计原则  
3.规范与重构  
4.设计模式与范式(实战例子)  

2020年过年期间看了前面两章，一些原理的东西，总结的时候还是要重新回顾一下。

## [一.面向对象](https://github.com/LayneHuang/ForEasyCode/blob/master/normal/design_pattern/design_pattern_beauty_oop.md)
第一章讲的大多是概念性的东西，主要是面向对象编程的一些好处。  
与面向过程编程的对比，抽象类和接口等等  
抽取其中部分总结

## [二.设计原则](https://github.com/LayneHuang/ForEasyCode/blob/master/normal/design_pattern/design_pattern_beauty_rule.md)
设计原则包括 SOLID, KISS, YAGNI, DRY, LOD 等  
SOLID：单一职责原则、开闭原则、里式替换原则、接口隔离原则、依赖反转原则  

## 三.规范与重构
这一章先略过，主要内容先看[《重构第二版》]((https://github.com/LayneHuang/ForEasyCode/blob/master/normal/restructure.md))  
大重构：系统、模块、代码结构、类与类之间的关系。重构手段：分层、模块化、解耦、抽象可复用组件等等。  
小重构：类、函数、变量等代码级。手段：掌握各种函数规范。  
静态代码分析工具：CheckStyle、FindBugs、PMD（自动发现代码中的问题，然后针对性地进行重构优化）  

## 四.设计模式与范式
一些比较熟悉的模式，就直接记其特别的点。  
根据不同的功能分类：  

### [1.创建型：单例模式、工厂模式、建造者模式](https://github.com/LayneHuang/ForEasyCode/blob/master/normal/design_pattern/design_pattern_beauty_create.md)

### [2.结构型：代理模式、装饰器模式、享元模式](https://github.com/LayneHuang/ForEasyCode/blob/master/normal/design_pattern/design_pattern_beauty_structure.md)
（一些类或对象组合在一起的经典结构）

### [3.行为型：观察者模式](https://github.com/LayneHuang/ForEasyCode/blob/master/normal/design_pattern/design_pattern_beauty_action.md)  

