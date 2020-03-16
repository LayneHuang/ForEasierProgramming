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

这里的重构写法感觉有点像《设计模式之美》里面所说的一种是面向过程编程，一种是面向对象编程。

#### 1.函数单一职责（提炼函数）
#### 2.内联变量
#### 3.搬移函数
#### 4.以多态取代条件表达式

## 二.重构的原则
第二章主要讲了一下为什么要重构。对整个系统的好处。道理讲得比较多...<br/>
一些持续集成，测试相关的内容。<br/>

## 三.代码的坏味道

### 1.神秘命名
深思熟虑如何给函数、模块、变量和类命名，是它们能清晰地表明自己的功能和用法。好的命名能节省未来用的猜谜尚的大把时间。

### 2.重复代码
最简单的方法就是提炼函数。再而就是把重复代码段位于同一个超类，把不同的代码再在子类处理。

### 3.过长函数
最终的效果：你应该积极地分解函数。遵循一条原则：每当感觉需要注释来说明点什么的时候，我们就把需要说明的东西写进一个独立函数中，并以其用途命名。<br/>
条件表达式和循环常常也是提炼的信号。你可以使用分解条件表达式处理条件表达式。对于庞大的switch语句，其中的每个分支都应该通过提炼函数变成独立的函数调用。如果有多个switch语句基于同一个条件进行分支选择，就应该使用以多态取代条件表达式。<br/>
对于函数参数过长的话，通过**引入参数对象**和**保持对象完整**，来让参数列表变得简洁。

### 4.过长参数列表
1.如果可以向某个参数发起查询而获得另一个参数的值，就可以用查询去取代第二个参数。<br/>
2.如果可以从现有的数据结构中抽出很多数据项，可以考虑直接传入原来的数据结构，并用保持对象完整的手法(暂时不知道什么手法？判断吗？)。<br/>
3.函数式编程

### 5.全局数据
1.变量封装（防止数据被污染），控制其作用域。<br/>

### 6.可变数据
1.函数式编程，建立在“数据永不改变”的基础上：如果要更新一个数据结构，就返回一份新的数据副本，旧的数据仍保持不变。<br/>
2.依然是封装变量。拆分变量，查询与修改分离等。

### 7.发散式变化
两个不同的上下文（比如说数据库交互和金融逻辑处理），就要将他们分别搬到各自独立的模块中（这不是微服务的日常操作吗？）。

### 8.散弹式修改
如果遇到某种变化，你都必须在许多不同的类内做出许多小修改，你锁面临的怀味道就是散弹式修改。<br/>
这种情况下，你应该使用搬移函数和搬移字段吧需要修改的代码放进同一个模块里。如果有很多函数都在操作相似的数据，可以使用函数组合成类。<br/>
面对散弹式修改，一个常用的策略就是使用与内联相关的重构，如内联函数或者内联类，把本不该分散的逻辑拽会一处。完成内联之后，如果类过大，在将其提炼成更小的函数和类。

