---
title: Codeforces education round 134
date: 2022-09-02 21:00:00
categories: [acm]
tag: [Codeforces]
---

{% link '#education round 134' https://codeforces.com/contest/1721 [title] %}

# D题
我直接思路：
1.从大到小循环每个位(贪心)
2.循环当前分组(贪心)
3.统计当前位的所有分组 0,1 的数量，若a中1的数量与b中0的相等则加当前长度
4.当前位总和为 n 则真正地再进行拆分，否则继续

正解思路：
a & ans 与 ~b & ans 需要相等, 进行贪心

```python
import collections


def solve():
    n = int(input().strip())
    a = list(map(int, input().strip().split()))
    b = list(map(int, input().strip().split()))
    ans = 0
    bit = (1 << 29)
    while bit > 0:
        now = ans | bit
        if sorted(i & now for i in a) == sorted(~i & now for i in b):
            ans = now
        bit >>= 1
    print(ans)


if __name__ == '__main__':
    t = int(input().strip())
    for _ in range(t):
        solve()
```

# E
题意：  
给一个字符串s，然后再有q个查询，每个查询有一个t串。  
对于每一个查询，问 s + (t[0]...t[n]) 串的最长相同前后缀有多长。  

思路：
这个相当于 kmp 算法中的 getNext(), 在 s 串中第 i 个字符不相等后，前面还能有多少个相同的字符跳过匹配（换句话就是前后缀相同）。

```python
def get_next(j, k, nxt, p):
    while p[j] != '$':
        if k == -1 or p[j] == p[k]:
            j += 1
            k += 1
            if p[j] == p[k]:
                nxt[j] = nxt[k]
            else:
                nxt[j] = k
        else:
            k = nxt[k]
    return j, k, nxt
 
 
def solve():
    s = input().strip()
 
    len_s = len(s)
    ns = [ch for ch in s]
    for i in range(11):
        ns.append('$')
 
    # print(ns)
    j, k, nxt = get_next(0, -1, [-1 for i in range(len(ns))], ns)
 
    q = int(input().strip())
    for _ in range(q):
        t = input().strip()
        ans = []
 
        for i in range(10):
            ns[i + len_s] = '$'
 
        for i in range(len(t)):
            ns[i + len_s] = t[i]
            # print(ns)
            nj, nk, n_nxt = get_next(j, k, nxt, ns)
            # print(n_nxt)
            ans.append(n_nxt[len_s + i + 1])
        print(' '.join(map(str, ans)))
 
 
if __name__ == '__main__':
    # t = int(input().strip())
    # for _ in range(t):
    solve()
```