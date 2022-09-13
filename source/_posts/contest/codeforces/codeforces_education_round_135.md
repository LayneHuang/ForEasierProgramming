---
title: Codeforces education round 135
date: 2022-09-13 21:00:00
categories: acm
tag: Codeforces
---

{% link '#education round 135' https://codeforces.com/contest/1721 [title] %}

# D
```python
def dfs(s, beg, ed, dp):
    if dp[beg][ed] != -1:
        return dp[beg][ed]
    result = 2
    if beg + 1 == ed:
        if s[beg] != s[ed]:
            result = 1
        dp[beg][ed] = result
        return result

    op1 = dfs(s, beg + 1, ed, dp)
    op2 = dfs(s, beg, ed - 1, dp)
    if op1 == 0 or op2 == 0:
        result = 1
    else:
        result = 0

    dp[beg][ed] = result
    return result


def solve():
    s = input().strip()
    n = len(s)
    dp = [[-1 for j in range(n)] for i in range(n)]
    dfs(s, 0, n - 1, dp)


if __name__ == '__main__':
    t = int(input().strip())
    for _ in range(t):
        solve()

```