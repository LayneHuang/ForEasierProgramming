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
第二章主要讲了一下为什么要重构。对整个系统的好处。道理讲得比较多...  
一些持续集成，测试相关的内容。

## 三.代码的坏味道
抽了其中的一部分

### 1.神秘命名
深思熟虑如何给函数、模块、变量和类命名，是它们能清晰地表明自己的功能和用法。好的命名能节省未来用的猜谜尚的大把时间。

### 2.重复代码
最简单的方法就是提炼函数。再而就是把重复代码段位于同一个超类，把不同的代码再在子类处理。

### 3.过长函数
最终的效果：你应该积极地分解函数。遵循一条原则：每当感觉需要注释来说明点什么的时候，我们就把需要说明的东西写进一个独立函数中，并以其用途命名。  
条件表达式和循环常常也是提炼的信号。你可以使用分解条件表达式处理条件表达式。对于庞大的switch语句，其中的每个分支都应该通过提炼函数变成独立的函数调用。如果有多个switch语句基于同一个条件进行分支选择，就应该使用以多态取代条件表达式。  
对于函数参数过长的话，通过**引入参数对象**和**保持对象完整**，来让参数列表变得简洁。

### 4.过长参数列表
1.如果可以向某个参数发起查询而获得另一个参数的值，就可以用查询去取代第二个参数。  
2.如果可以从现有的数据结构中抽出很多数据项，可以考虑直接传入原来的数据结构，并用保持对象完整的手法(暂时不知道什么手法？判断吗？)。  
3.函数式编程

### 5.全局数据
1.变量封装（防止数据被污染），控制其作用域。  

### 6.可变数据
1.函数式编程，建立在“数据永不改变”的基础上：如果要更新一个数据结构，就返回一份新的数据副本，旧的数据仍保持不变。  
2.依然是封装变量。拆分变量，查询与修改分离等。

### 7.发散式变化
两个不同的上下文（比如说数据库交互和金融逻辑处理），就要将他们分别搬到各自独立的模块中（这不是微服务的日常操作吗？）。

### 8.散弹式修改
如果遇到某种变化，你都必须在许多不同的类内做出许多小修改，你锁面临的怀味道就是散弹式修改。  
这种情况下，你应该使用搬移函数和搬移字段吧需要修改的代码放进同一个模块里。如果有很多函数都在操作相似的数据，可以使用函数组合成类。  
面对散弹式修改，一个常用的策略就是使用与内联相关的重构，如内联函数或者内联类，把本不该分散的逻辑拽会一处。完成内联之后，如果类过大，在将其提炼成更小的函数和类。

### 9.依恋情结
所谓模块化，就是力求将代码分出区域，最大化区域内部的交互，最小化跨区域的交互。  
如果发现一个函数跟另一个模块中的函数或数据交流格外频繁，远胜于在自己所处模块内部的交流（正常来说不太可能），这就是依恋情结。  
解决方法：将其提炼分解后，放到属于它的模块。  
有几个复杂精巧的模式破坏了这条规则。策略模式，访问者模式和Self Delegation模式。这些模式是为了对抗发散式变化这一坏味道。最根本的原则是：将总是一起变化的东西放在一块。

### 10.数据泥团

### 11.基本类型偏执
你可以运用以对象取代基本类型，将原本独立存在的数据替换为对象。（如手机号码等，有逻辑，不单单只是字符串）

### 12.switch语句
更多地使用多态

### 13.循环语句
可以使用以管道取代循环。（filter 和 map...）

### 14.夸夸其谈通用性
如果某个抽象类其实没太大作用，请运用折叠继承。不必要的委托可以运用内联函数和内联类除掉。

### 15.过长的消息链
如果你看到用户向一个对象请求另一个对象，然后再向后者请求另一个对象，然后再请求另一个对象....这就是消息链。  
这个时候应该使用隐藏委托关系（？？）。理论上你可以重构消息链上的所有对象，但这么做就会把所有中间对象变成“中间人”。

### 16.中间人
对象的基本特征之一就是封装 —— 对外部世界隐藏其内部细节。封装往往伴随着委托。比如你问主管是否有时间参加一个会议，他就把这个消息“委托”给他的记事簿，然后才能回答你。然而你没必要知道他到底使用传统记事簿还是电子记事簿，或是他的秘书来记录自己的约会。  
但是人们可能过度运用委托。你也许会看到某个类的接口有一半的函数都委托给其他类，这样就是过度运用。这个时候应该移除中间人，直接和真正负责的对象打交道。

### 17.内幕交易
在实际情况中，一定的数据交换不可避免，但我们必须尽量减少这种情况，并把这种交换都放到明面上来。  
如果两个模块总是在咖啡机旁边窃窃私语，就应该用搬移函数和搬移字段减少他们的私下交流。如果两个模块有共同的兴趣，可以尝试再新建一个模块，把共同的数据放在一个管理良好的地方。或者用隐藏委托关系，把另一个模块编程两者的中介。

### 18.过大的类

### 19.异曲同工的类

### 20.被拒绝的遗赠
如果子类复用了超类的行为，却又不愿意支持超类的接口。应该以委托取代子类或者委托取代超类彻底划清界限。

## 四.构建测试体系
用Mocha讲了几种测试方式，已读，要了解还是直接看Java测试相关的吧。

## 五.介绍重构名录

## 六.第一组重构

### 1.拆分阶段

```javascript
const orderData = orderString.split(/\s+/); 
const productPrice = priceList[orderData[0].split("-")[1]]; 
const orderPrice = parseInt(orderData[1]) * productPrice;
```
become:
```javascript
const orderRecord = parseOrder(order); 
const orderPrice = price(orderRecord, priceList);
function parseOrder(aString) {
    const values = aString.split(/\s+/);
    return ({ 　　
        productID: values[0].split("-")[1],
        quantity: parseInt(values[1]), 　
    }); 
} 
function price(order, priceList) { 　
    return order.quantity * priceList[order.productID]; 
}
```
## 七.封装

### 1.封装集合
```javascript
class Person {  
    getCourses() {return this._courses;}  
    setCourses(aList) {this._courses = aList;}
}
```
become:
```javascript
class Person {
    getCourses() {return this._courses.slice();}
    addCourse(aCourse) { ... }   
    removeCourse(aCourse) { ... }
}
```

### 2.提炼类
```javascript
class Person {  
    getOfficeAreaCode() {return this._officeAreaCode;} 　
    getOfficeNumber()   {return this._officeNumber;}
}
```
become:
```javascript
class Person { 　
    getOfficeAreaCode() {return this._telephoneNumber.areaCode;}
    geOfficeNumber() {return this._telephoneNumber.number;} 
}

class TelephoneNumber {
    getAreaCode() {return this._areaCode;}
    getNumber() {return this._number;} 
}
```
一个类应该是一个清晰的抽象，只处理一些明确的职责。

### 3.内联类
就跟提炼类反过来。  
类不在承担足够的责任，不再有单独存在的理由。

### 4.隐藏委托关系
```javascript
manager = aPerson.department.manager
```
become:
```javascript
manager = aPerson.manager; 

class Person { 
    getManager() {return this.department.manager;}
}
```

### 5.移除中间人
就跟隐藏委托关系反过来。  
每当客户端要使用受委托的新特性时，你就必须在服务端添加一个简单委托函数。随着受委托类的特性（功能）越来越多，更多的转发函数就会使人烦躁。

## 八.搬移特性
通过搬移函数，在类与其他模块之间搬移函数，对于字段可用搬移字段。  
有时还需要单独对语句进行搬移。调整他们的顺序。  
有时一些语句做的事已有现成的函数代替，那就能以函数调用取代内联代码消除重复。  

对于循环：  
1.拆分循环，可以确保每个循环只做一件事。  
2.以管道取代循环，可以直接消灭整个循环。  

**最后就是移除死代码（无用代码）。**

### 1.移动语句
```javascript
const pricingPlan = retrievePricingPlan();
const order = retreiveOrder();
let charge;
const chargePerUnit = pricingPlan.unit;
```
become:
```javascript
const pricingPlan = retrievePricingPlan();
const chargePerUnit = pricingPlan.unit;
const order = retreiveOrder();
let charge;
```
让存在关联的东西一起出现，可以使代码更容易理解。 

## 九.重新组织数据
将一个值用与多个不同的用途，就会催生混乱和bug。  
一旦看见这种情况，就会拆分变量，将不同用途分开。有多余的变量最好是彻底消除掉，比如通过以查询代替派生变量。

### 1.拆分变量
```javascript
let temp = 2 * (height + width);
console.log(temp); 
temp = height * width;
console.log(temp);
```
become:
```javascript
const perimeter = 2 * (height + width); 
console.log(perimeter); 
const area = height * width; 
console.log(area);
```

### 2.将引用对象改为值对象
```javascript
class Product {  
    applyDiscount(arg) {
        this._price.amount -= arg;
    }
}
```
become:
```javascript
class Product {
    applyDiscount(arg) {
        this._price = new Money(this._price.amount - arg, this._price.currency);  
    }
}
```

### 3.将值对象改为引用对象
```javascript
let customer = new Customer(customerData);
// become
let customer = customerRepository.get(customerData.id);
```
一个数据结构中可能包含多个记录，而这些记录都关联到同一个逻辑数据结构。  
过多的数据复杂会造成内存占用问题。  
如果共享的数据需要更新，将其复制多分的做法就会遇到巨大的困难。此时我必须找到所有的副本，更新所有对象。
只要漏掉一个副本没有更新，就会遭遇麻烦的数据不一致。这种情况下可以考虑将多分数据副本变成单一的引用。

## 十.简化条件逻辑
程序大部分威力来自条件逻辑，但很不幸，程序的复杂度也大多来自条件逻辑。  
我常用分解条件表达式处理复杂的条件表达式，用合并条件表达式理清逻辑组合。  
会以卫语句取代嵌套条件表达式清晰表达“在主要处理逻辑之前先做检查”的意图。如果发现一处switch逻辑处理了几种情况，就可以考虑以多态取代条件表达式的重构方法。

### 1.合并条件表达式
```javascript
if (anEmployee.seniority < 2) return 0; 
if (anEmployee.monthsDisabled > 12) return 0; 
if (anEmployee.isPartTime) return 0;
```
become:
```javascript
if (isNotEligibleForDisability()) return 0; 
function isNotEligibleForDisability() {
    return ((anEmployee.seniority < 2) 　　　　　
        || (anEmployee.monthsDisabled > 12) 　　　　　
        || (anEmployee.isPartTime)); 
}
```

### 2.以卫语句取代嵌套条件表达式
```javascript
function getPayAmount() { 　
    let result;
    if (isDead)
        result = deadAmount();
    else {
        if (isSeparated)
            result = separatedAmount();
     　　else {
            if (isRetired)
                result = retiredAmount();
            else
                result = normalPayAmount();
        }
    }
    return result;
}
```
become:
```javascript
function getPayAmount() {
    if (isDead) return deadAmount();
    if (isSeparated) return separatedAmount();
    if (isRetired) return retiredAmount();
    return normalPayAmount(); 
}
```
如果某个条件极其罕见，就应该单独检查该条件，并且该条件为真时立刻从函数中返回。这样的单独检查常常被称为“**卫语句**”。  
以卫语句取代嵌套条件表达式的精髓就是：  
给某一条分支以特别的重视。如果使用if-then-else结构，你对if分支 和else分支的重视是同等的。
这样的代码结构传递给阅读者的消息就是：各个分支有同样的重要性。  
卫语句就不同了，它告诉阅读者：“这种情况不是本函数的核心逻辑所关心的，如果它真发生了，请做一些必要的整理工作，然后退出。”

### 3.多态取代条件表达式
```javascript
switch (bird.type) {
    case 'EuropeanSwallow':
        return "average";
    case 'AfricanSwallow':
        return (bird.numberOfCoconuts > 2) ? "tired" : "average";
    case 'NorwegianBlueParrot':
        return (bird.voltage > 100) ? "scorched" : "beautiful";
    default:
        return "unknown";
}
```
become:
```javascript
class EuropeanSwallow {
    getPlumage() {
        return "average";
    }
}

class AfricanSwallow {
    getPlumage() {
        return (this.numberOfCoconuts > 2) ? "tired" : "average";
    }
}

class NorwegianBlueParrot {
    getPlumage() {
        return (this.voltage > 100) ? "scorched" : "beautiful";
    }
}
```
（感觉这里switch也挺好呀）  
主要多态的作用是**处理复杂的逻辑**(所以说比较简单的话肯定还是条件语句啦)，然后也可以把基础逻辑放进超类。

## 十一.重构API
好的API会把更新数据的函数与只是读取数据的函数清晰分开。如果我看到这两类操作被混在一起，就会用将查询函数和修改函数分离,将它们分开。  
如果两个函数的功能非常相似、只有一些数值不同，我可以用函数参数化将其统一。

### 1.以工厂模式取代构造函数

### 2.以命令取代函数
```javascript
function score(candidate, medicalExam, scoringGuide) {   
    let result = 0;  
    let healthLevel = 0;
    // long body code
}
```
become:
```javascript
class Scorer {
    constructor(candidate, medicalExam, scoringGuide) {
        this._candidate = candidate;
        this._medicalExam = medicalExam;
        this._scoringGuide = scoringGuide;  
    }
    execute() {
        this._result = 0;
        this._healthLevel = 0; 
        // long body code
    }
}
```
与普通函数相比，命令对象提供了更大的控制灵活性和更强的表达能力。除了函数调用本身，命令对象还可以支持附加操作。

### 3.以函数取代命令
跟上面反过来，命令对象为处理复杂计算提供了强大的机制。
借助命令对象，可以轻松地将原本复杂的函数拆解为多个方法，彼此之间通过字段共享状态；
拆解后的方法可以分别调用；开始调用之前的数据状态也可以逐步构建。

## 十二.处理继承关系
一些常用的方式：函数上下移动，字段上下移动，构造方法上移等。

### 1.以委托类取代子类
```javascript
class Order {
    get daysToShip() {
        return this._warehouse.daysToShip;
    }
}

class PriorityOrder extends Order {
    get daysToShip() {
        return this._priorityPlan.daysToShip;
    }
}
```
become:
```javascript
class Order {
    get daysToShip() {
        return (this._priorityDelegate) 
            ? this._priorityDelegate.daysToShip 
            : this._warehouse.daysToShip;
    }
}

class PriorityOrderDelegate {
    get daysToShip() {
        return this._priorityPlan.daysToShip
    }
}
```
继承有其短板：  
1.继承只能继承一次。  
2.继承给类之间引入非常紧密的关系。超类上做的任何修改，都可能破坏子类。  
而这两个问题委托都能解决。对于不同的变化原因，委托给不同的类。
委托是对象之间的常规关系，与继承相比，使用委托关系时接口更清晰、耦合更少。  
有一条流行的原则：**对象组合优于类继承**（“组合”跟“委托”是同一回事）。（这里讲得跟Effective Java说的继承有点类似）
