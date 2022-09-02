---
title: Codeforces education round 134
date: 2022-03-29 15:39:00
categories: acm
tag: Codeforces
---

{% link '#education round 134' https://codeforces.com/contest/1721 [title] %}

# D题

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

直接模拟 TLE

```python
import collections


def decode(num):
    res = [0 for _ in range(31)]
    idx = 0
    while num > 0:
        res[idx] = num % 2
        idx += 1
        num >>= 1;
    return res


def solve():
    n = int(input().strip())
    a = list(map(int, input().strip().split()))
    b = list(map(int, input().strip().split()))
    # print(a)
    # print(b)
    idxA = [i for i in range(n)]
    idxB = [i for i in range(n)]

    bits = [[], []]
    for i in range(n):
        bits[0].append(decode(a[i]))
        bits[1].append(decode(b[i]))

    aa = [idxA]
    bb = [idxB]
    cnt = [0 for _ in range(31)]

    for bit in range(30, -1, -1):
        # print(bit)
        naa = []
        nbb = []
        for i in range(len(aa)):
            nowA = aa[i]
            nowB = bb[i]
            # print('nowA', nowA)
            # print('nowB', nowB)
            na0 = []
            nb0 = []
            na1 = []
            nb1 = []
            cntA = 0
            cntB = 0
            for j in range(len(nowA)):
                idA = nowA[j]
                idB = nowB[j]
                bitA = bits[0][idA][bit]
                bitB = bits[1][idB][bit]
                if bitA == 0:
                    na0.append(idA)
                else:
                    cntA += 1
                    na1.append(idA)
                if bitB == 0:
                    cntB += 1
                    nb0.append(idB)
                else:
                    nb1.append(idB)

            if cntA == cntB:
                cnt[bit] += len(nowA)
            else:
                break
            naa.append(na0)
            naa.append(na1)
            nbb.append(nb1)
            nbb.append(nb0)
        if cnt[bit] == n:
            aa = naa
            bb = nbb

    ans = 0
    for i in range(31):
        if cnt[i] == n: ans |= (1 << i)
    print(ans)


if __name__ == '__main__':
    t = int(input().strip())
    for _ in range(t):
        solve()

```