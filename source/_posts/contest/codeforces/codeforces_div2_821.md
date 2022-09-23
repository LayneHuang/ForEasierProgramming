---
title: Codeforces 821 div2
date: 2022-09-23 21:00:00
categories: acm
tag: Codeforces
---

{% link '#821 div2' https://codeforces.com/contest/1717 [title] %}
### D
```python
import heapq
import operator


def solve():
    n, x, y = map(int, input().strip().split())
    s = input().strip()
    a = [int(ch) for ch in s]
    s = input().strip()
    b = [int(ch) for ch in s]
    n = len(a)
    c = [(a[i] ^ b[i]) for i in range(n)]

    # print(c)

    cnt1 = 0
    pair = 0
    pre = -10
    for i in range(n):
        if c[i] == 1:
            cnt1 += 1
            if pre + 1 == i:
                pair += 1
                pre = -10
            else:
                pre = i

    if cnt1 % 2 == 1:
        print(-1)
        return

    not_pair = (cnt1 // 2) - pair
    if y < x and (cnt1 > 2 or pair > 1):
        print(cnt1 // 2 * y)
    elif x <= y * 2:
        print((x * pair + y * not_pair))
    else:
        cost = cnt1 // 2

        if pair > 0:
            cost = max(cost, 2)

        print(cost * y)


if __name__ == '__main__':
    t = int(input().strip())
    for _ in range(t):
        solve()

```
6
5 8 9
01001
00101
6 2 11
000001
100000
5 7 2
01000
11011
7 8 3
0111001
0100001
6 3 4
010001
101000
5 10 1
01100
01100
