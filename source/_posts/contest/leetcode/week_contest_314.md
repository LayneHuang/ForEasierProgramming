---
title: leetcode 314
date: 2022-10-09 21:00:00
categories: acm
tag: leetcode
---

# 4
```python
def get_pos(x, y, m):
    return x * m + y


def solve(grid, K):
    n = len(grid)
    m = len(grid[0])
    limit = n * m
    dp = [[0 for i in range(K)] for j in range(limit)]
    MOD = 10 ** 9 + 7
    dp[get_pos(0, 0, m)][grid[0][0] % K] = 1

    for i in range(n):
        for j in range(m):
            for k in range(K):
                if i < n - 1:
                    dp[get_pos(i + 1, j, m)][(k + grid[i + 1][j]) % K] += dp[get_pos(i, j, m)][k]
                    dp[get_pos(i + 1, j, m)][(k + grid[i + 1][j]) % K] %= MOD
                if j < m - 1:
                    dp[get_pos(i, j + 1, m)][(k + grid[i][j + 1]) % K] += dp[get_pos(i, j, m)][k]
                    dp[get_pos(i, j + 1, m)][(k + grid[i][j + 1]) % K] %= MOD

    return dp[get_pos(n - 1, m - 1, m)][0]


class Solution(object):
    def numberOfPath(self, grid, k):
        return solve(grid, k)

```