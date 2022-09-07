---
title: Codeforces 770 div2
date: 2022-09-07 21:00:00
categories: acm
tag: Codeforces
---

{% link '#818 div2' https://codeforces.com/contest/1717 [title] %}

# A
查找 n 之内 有多少对 (a,b) 使得 lcm(a,b) / gcd(a,b) <= 3,  
设 gcd(a, b) 为 i。  
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
给定2个长度为n的数组a，b。
a可以有一种操作：
在 a[i] <= a[(i+1)%n] 的情况下，a[i] 变成 a[i]+1。
问 a 在任意操作后能否变成 b。

思路：

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

#D
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

#E
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