---
title: Codeforces education round 135
date: 2022-09-13 21:00:00
categories: acm
tag: Codeforces
---

{% link '#education round 135' https://codeforces.com/contest/1721 [title] %}

# D
```python
def dfs(s, beg, ed, dp, lst_pos):
    # print(beg, ed, lst_pos)
    if dp[beg][ed][lst_pos] != -1:
        return dp[beg][ed][lst_pos]

    dp[beg][ed][lst_pos] = 2

    if beg + 1 == ed:
        if s[beg] != s[ed]:
            dp[beg][ed][lst_pos] = 1
    else:
        s_len = ed - beg + 1
        if s_len % 2 == 0:
            if dfs(s, beg + 1, ed, dp, 0) == 0 or dfs(s, beg, ed - 1, dp, 1) == 0:
                dp[beg][ed][lst_pos] = 1
            elif dfs(s, beg + 1, ed, dp, 0) == 1 and dfs(s, beg, ed - 1, dp, 1) == 1:
                dp[beg][ed][lst_pos] = 0
            elif (dfs(s, beg + 1, ed - 1, dp, 0) == 2 or dfs(s, beg + 1, ed - 1, dp, 1)) and s[beg] != s[ed]:
                dp[beg][ed][lst_pos] = 1
            else:
                dp[beg][ed][lst_pos] = 2
        else:
            ch = ''
            if lst_pos == 0:
                ch = s[beg - 1]
            else:
                ch = s[ed + 1]

            if dfs(s, beg + 1, ed, dp, 0) == 2 and ord(s[beg]) > ord(ch):
                dp[beg][ed][lst_pos] = 1
            elif dfs(s, beg, ed - 1, dp, 1) == 2 and ord(s[ed]) > ord(ch):
                dp[beg][ed][lst_pos] = 1
            elif dfs(s, beg + 1, ed, dp, 0) == 0 or dfs(s, beg, ed - 1, dp, 1) == 0:
                dp[beg][ed][lst_pos] = 1
            elif dfs(s, beg + 1, ed, dp, 0) == 1 and dfs(s, beg, ed - 1, dp, 1) == 1:
                dp[beg][ed][lst_pos] = 0
            # print(dfs(s, beg + 1, ed, dp, 0), dfs(s, beg, ed - 1, dp, 1))
            # print(beg, ed, lst_pos, dp[beg][ed][lst_pos])

    return dp[beg][ed][lst_pos]


def solve():
    s = input().strip()
    n = len(s)
    if n == 2:
        if ord(s[0]) != ord(s[1]):
            print('Alice')
        else:
            print('Draw')

    dp = [[[-1 for k in range(2)] for j in range(n)] for i in range(n)]

    if dfs(s, 1, n - 1, dp, 0) == 0 or dfs(s, 0, n - 2, dp, 1) == 0:
        print('Alice')
    elif dfs(s, 1, n - 1, dp, 0) == 1 and dfs(s, 0, n - 2, dp, 1) == 1:
        print('Bob')
    else:
        print('Draw')


if __name__ == '__main__':
    t = int(input().strip())
    for _ in range(t):
        solve()

```