# ForEasyCode

## 前端
#### [1.React & Redux](https://github.com/LayneHuang/ForEasyCode/blob/master/website/react/react.md)

## Java后台
#### [Effective Java 2.0](https://github.com/LayneHuang/ForEasyCode/blob/master/java/effectivejava.md)
#### [并发编程](https://github.com/LayneHuang/ForEasyCode/blob/master/java/concurrent_programming.md)

## 数据分析

## 简录
```text
[2020.2.4]
工作两年有余，17年~19年都比较迷茫，不知道究竟向着什么方向努力
经过之前的一些面试知道了自己的一些弱势点在哪里，了解的知识是不够全面。
目前的做法是通过看书（kindle电子书）和极客时间的专栏结合（有音频，有Sample可以加深印象）
然后自己再做总结去再进一步加深印象提升。
2020年的春节假期因为武汉肺炎病毒的事情延长了许久。刚好有时间提前回来去做总结。
之后的目标一方面是向着能把一些编程比赛（codeforces）的分打高一点（纯兴趣）。
另一方面得是工程技术得到全面的学习（工作的基础嘛），同时向着自己兴趣方面去靠拢（数据分析）。
最近因为Kobe的去世，看了很多很多关于他的记录片（我的青春结束了）。
他对篮球的热爱，坚持，不断学习进步，不畏惧挑战。
对自己职业生涯的理解，对自身（臂展，弹跳，得分技巧）和比赛的理解都做到很极致。
曼巴精神~~~我希望之后自己也能把自己喜欢的事情做到极致。

其中他讲到有一段话印象很深刻：
我会跟他说专注于当下的事情就好
把注意力放在当下做好眼前的事情
你可以这样来想
如果我想要成为NBA的最佳投手
那我要怎么做
我会每天一醒来就投1000次篮
星期一1000次
星期二1000次
星期三1000次
我只专注于做好每天的事情
但等到一年过去之后我就是这世界上的最佳投手
但要完成这个目标你必须要一步一个脚印做好
你不用去担心过去或者是未来
```
附上:[Dear Basketball](https://www.bilibili.com/video/av16997700?from=search&seid=1465929823314277301)


# 重构第二版

## 一.第一个示例

重构前：
```javascript
function statement(invoice, plays) {
    let totalAmount = 0;
    let volumeCredits = 0;
    let result = `Statement for ${invoice.customer}\n`;
    const format = new Intl.NumberFormat("en-US", {
        style: "currency",
        currency: "USD",
        minimumFractionDigits: 2
    }).format;
    for (let perf of invoice.performances) {
        const play = plays[perf.playID];
        let thisAmount = 0;
        switch (play.type) {
            case "tragedy":
                thisAmount = 40000;
                if (perf.audience > 30) {
                    thisAmount += 1000 * (perf.audience - 30);
                }
                break;
            case "comedy":
                thisAmount = 30000;
                if (perf.audience > 20) {
                    thisAmount += 10000 + 500 * (perf.audience - 20);
                }
                thisAmount += 300 * perf.audience;
                break;
            default:
                throw new Error(`unknown type: ${play.type}`);
        }

        // add volume credits
        volumeCredits += Math.max(perf.audience - 30, 0);
        // add extra credit for every ten comedy attendees
        if ("comedy" === play.type) volumeCredits += Math.floor(perf.audience / 5);
        // print line for this order
        result += ` ${play.name}: ${format(thisAmount / 100)} (${perf.audience} seats)\n`;
        totalAmount += thisAmount;
    }
    result += `Amount owed is ${format(totalAmount / 100)}\n`;
    result += `You earned ${volumeCredits} credits\n`;
    return result;
}

```

重构后:
```javascript
export default function createStatementData(invoice, plays) {
    const result = {};
    result.customer = invoice.customer;
    result.performances = invoice.performances.map(enrichPerformance);
    result.totalAmount = totalAmount(result);
    result.totalVolumeCredits = totalVolumeCredits(result);
    return result;
}

function enrichPerformance(aPerformance) {
    const calculator = createPerformanceCalculator(aPerformance, playFor(aPerformance));
    const result = Object.assign({}, aPerformance);
    result.play = calculator.play;
    result.amount = calculator.amount;
    result.volumeCredits = calculator.volumeCredits;
    return result;
}

function playFor(aPerformance) {
    return plays[aPerformance.playID];
}

function totalAmount(data) {
    return data.performances.reduce((total, p) = > total + p.amount, 0);
}

function totalVolumeCredits(data) {
    return data.performances.reduce((total, p) = > total + p.volumeCredits, 0);
}

function createPerformanceCalculator(aPerformance, aPlay) {
    switch (aPlay.type) {
        case "tragedy":
            return new TragedyCalculator(aPerformance, aPlay);
        case "comedy" :
            return new ComedyCalculator(aPerformance, aPlay);
        default:
            throw new Error(`unknown type: ${aPlay.type}`);
    }
}

class PerformanceCalculator {
    constructor(aPerformance, aPlay) {
        this.performance = aPerformance;
        this.play = aPlay;
    }

    getAmount() {
        throw new Error('subclass responsibility');
    }

    getVolumeCredits() {
        return Math.max(this.performance.audience - 30, 0);
    }
}

class TragedyCalculator extends PerformanceCalculator {
    getAmount() {
        let result = 40000;
        if (this.performance.audience > 30) {
            result += 1000 * (this.performance.audience - 30);
        }
        return result;
    }
}

class ComedyCalculator extends PerformanceCalculator {
    getAmount() {
        let result = 30000;
        if (this.performance.audience > 20) {
            result += 10000 + 500 * (this.performance.audience - 20);
        }
        result += 300 * this.performance.audience;
        return result;
    }

    getVolumeCredits() {
        return super.volumeCredits + Math.floor(this.performance.audience / 5);
    }
}
```

感觉有点像《设计模式之美》里面所说的一种是面向过程编程，一种是面向对象编程。

### 1.函数单一职责（提炼函数）
### 2.内联变量
### 3.搬移函数
### 4.以多态取代条件表达式

## 二.重构的原则
第二章主要讲了一下为什么要重构。对整个系统的好处。<br/>
一些持续集成，测试相关的内容。<br/>

## 三.代码的坏味道

### 1.神秘命名
深思熟虑如何给函数、模块、变量和类命名，是它们能清晰地表明自己的功能和用法。好的命名能节省未来用的猜谜尚的大把时间。

### 2.重复代码
最简单的方法就是提炼函数。再而就是把重复代码段位于同一个超类，把不同的代码再在子类处理。

### 3.过长函数
最终的效果：你应该积极地分解函数。遵循一条原则：每当感觉需要注释来说明点什么的时候，我们就把需要说明的东西写进一个独立函数中，并以其用途命名。<br/>
条件表达式和循环常常也是提炼的信号。你可以使用分解条件表达式处理条件表达式。对于庞大的switch语句，其中的每个分支都应该通过提炼函数变成独立的函数调用。如果有多个switch语句基于同一个条件进行分支选择，就应该使用以多态取代条件表达式。<br/>

