---
title: Codeforces 770 div2
date: 2022-09-07 21:00:00
categories: acm
tag: Codeforces
---

{% link '#818 div2' https://codeforces.com/contest/1717 [title] %}

# A
题意：  
查找 n 之内 有多少对 (a,b) 使得 lcm(a,b) / gcd(a,b) <= 3,  
设 gcd(a, b) 为 i。  
思路：  
实际就是, (i , i) , (i , 2 * i), (2 * i, i), (i , 3 * i), (3 * i, i) 有多少对

```java
public class Main {
    private static void solve() {
        int n = cin.nextInt();
        long ans = n + (n / 2) * 2 + (n / 3) * 2;
        System.out.println(ans);
    }
}
```

# B
题意：  
给定一个 n * n 大小的矩阵，里面只有两种元素`X`与`.` ，且位置(r,c)中有点为`X`。  
给定一个 k，构造出一个矩阵，使得任意行和列中，连续k个单元格只有1个X。

思路：  
构造出从 (0,0) 位置为 X 且符合答案的数组（行内偏移为k，列偏移为1）。  
然后按照偏移去输出。

```java
public class Main {
    private static int get(int r, int c, int n) {
        return r * n + c;
    }
 
    private static void solve() {
        int n = cin.nextInt();
 
        int limit = n * n;
        boolean[] vis = new boolean[limit];
        int k = cin.nextInt();
 
        int diff = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j += k) {
                vis[get(i, (j + diff) % n, n)] = true;
            }
            diff++;
        }
 
        int r = cin.nextInt() - 1;
        int c = cin.nextInt() - 1;
 
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                int rr = ((i - r) + n) % n;
                int cc = ((j - c) + n) % n;
                if (vis[get(rr, cc, n)]) System.out.print('X');
                else System.out.print('.');
            }
            System.out.println();
        }
    }
}
```

# C
题意：  
给定2个长度为n的数组a，b。
a可以有一种操作：
在 a[i] <= a[(i+1)%n] 的情况下，a[i] 变成 a[i]+1。
问 a 在任意操作后能否变成 b。

思路：  
a数组在任意情况下都可以整个数组都变成a中的最大值。
同时，最大值也可以+1，所以a数组能变成任意一个值。

但，只有2种情况不符合条件
1.a中某个值大于b对应位置的值
2.既然a整体都小于等于b,只有当b中某个值b[i]大于前面一个值b[i+1]+1，且a[i] != b[i]时。a[i]无法通过操作增长。

```python
def solve():
    n = int(input().strip())
    a = list(map(int, input().strip().split()))
    b = list(map(int, input().strip().split()))
 
    for i in range(len(a)):
        if a[i] > b[i]:
            print('NO')
            return
 
    b.append(b[0])
 
    for i in range(n):
        if (b[i] > b[i + 1] + 1) and (a[i] != b[i]):
            print('NO')
            return
 
    print('YES')
 
 
if __name__ == '__main__':
    t = int(input().strip())
    for _ in range(t):
        solve()
```

# D
题意：  
2^n 个人参与一个比赛。(实际上进行n场比赛)
投资方能够修改某场比赛结果 k 次。
问投资方能控制能够获胜的参赛者的个数。

思路：  
对于某个参赛者而言，把某场比赛的胜利设为1，失败设为0
即相当于一个二进制数字 => 1010010
如果想要某个玩家胜利，需要把所有0都变成1。
就相当于计算 2^n 内有多少个数字的二进制表示中，0的数目小于等于K个。
ans = C(0,n) + C(1,n) + ... + C(k,n)

包含内容:  
组合数学、逆元

```python
def solve():
    mod = 1000000007
    n, k = map(int, input().strip().split())
 
    res = pow(2, n, mod)
 
    if k >= n:
        print(res)
    elif k + 1 == n:
        print(res - 1)
    else:
        f = [1 for i in range(n + 1)]
        fi = [1 for i in range(n + 1)]
 
        for i in range(1, n + 1):
            f[i] = f[i - 1] * i % mod
 
        fi[n] = pow(f[n], mod - 2, mod)
        for i in range(n - 1, 0, -1):
            fi[i] = fi[i + 1] * (i + 1) % mod
 
        ans = 0
        for i in range(0, k + 1):
            ans += f[n] * fi[n - i] * fi[i] % mod
            ans %= mod
        print(ans)
 
 
if __name__ == '__main__':
    solve()
```

# E
题意：  
给定n, 在 a+b+c=n 的条件下，求所有 lcm(a,gcd(b,c)) 的和。

思路：  
第一维可以对a进行枚举。
因为 b % gcd(b, c) == 0, c % gcd(b, c) == 0。  
所以有： (b + c) % gcd(b , c) == 0。     
即第二维再对(b + c)的因子[即gcd(b , c)]，设为p, 进行枚举。
并且统计出 (b + c)/p 内有多少对数字(x,y)且 x+y = (b + c)/p 互为质数，使得b,c的gcd为p。  
算出后，将 个数 * lcm(a, gcd(b, c)) 累加到答案即可。

包含内容:  
质数对数、因子筛

```python
import math
 
 
def solve():
    n = int(input().strip())
 
    vis = [False for i in range(n + 1)]
    pri_cnt = [i for i in range(n + 1)]
 
    pri = [0, 1]
    for i in range(2, n + 1):
        if not vis[i]:
            pri.append(i)
            for j in range(i, n + 1, i):
                vis[j] = True
                pri_cnt[j] -= pri_cnt[j] // i
 
    # print('pri', pri)
    # print('pri_cnt', pri_cnt)
 
    ans = 0
    mod: int = 10 ** 9 + 7
 
    divs = [[] for i in range(n + 1)]
 
    for i in range(2, n + 1):
        for j in range(i + i, n + 1, i):
            divs[j].append(i)
 
    for a in range(1, n - 1):
        bc = n - a
        # print(bc, gcd_bc_list)
        temp = 0
        total = bc - 1
        for gcd_bc in divs[bc]:
            cnt = pri_cnt[bc // gcd_bc] % mod
            temp += cnt
            ans += (cnt * math.lcm(a, gcd_bc)) % mod
            ans %= mod
        ans += a * (total - temp) % mod
        ans %= mod
        # print(a, 'temp', temp, 'ans', ans)
 
    return ans
 
 
if __name__ == '__main__':
    # t = int(input().strip())
    # for _ in range(t):
    print(solve())
```