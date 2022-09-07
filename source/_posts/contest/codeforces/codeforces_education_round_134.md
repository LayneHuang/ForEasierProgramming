---
title: Codeforces education round 134
date: 2022-09-02 21:00:00
categories: acm
tag: Codeforces
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