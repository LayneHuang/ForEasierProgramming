---
title: Codeforces 821 div2
date: 2022-09-23 21:00:00
categories: acm
tag: Codeforces
---

{% link '#821 div2' https://codeforces.com/contest/1717 [title] %}
### D
```python
def solve():
    n, x, y = map(int, input().strip().split())
    s = input().strip()
    a = [int(ch) for ch in s]
    s = input().strip()
    b = [int(ch) for ch in s]
    n = len(a)
    c = [(a[i] ^ b[i]) for i in range(n)]

    # print(c)

    mx = 1 << 31
    dp = [[mx for j in range(3)] for i in range(n)]

    if c[0] == 0:
        dp[0][0] = 0
    else:
        dp[0][1] = 0

    for i, num in enumerate(c):
        if i == 0:
            continue
        if num == 0:
            dp[i][0] = min(dp[i][0], dp[i - 1][0])
            dp[i][1] = min(dp[i][1], dp[i - 1][1] + x)
            dp[i][1] = min(dp[i][1], dp[i - 1][2] + y)
            dp[i][2] = min(dp[i - 1][1], dp[i - 1][2])
        else:
            dp[i][0] = min(dp[i][0], dp[i - 1][1] + min(x, 2 * y))
            dp[i][0] = min(dp[i][0], dp[i - 1][2] + y)
            dp[i][1] = min(dp[i][1], dp[i - 1][0])
            if i > 1 and c[i - 1] == 1:
                dp[i][2] = min(dp[i][2], min(dp[i - 2][1], dp[i - 2][2]) + y)

        # print(i, dp[i][0], dp[i][1], dp[i][2])
    print(-1 if dp[n - 1][0] >= mx else dp[n - 1][0])


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
