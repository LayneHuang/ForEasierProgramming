---
title: Codeforces education round 135
date: 2022-09-13 21:00:00
categories: [acm]
tag: [Codeforces]
---

{% link '#education round 135' https://codeforces.com/contest/1721 [title] %}

# D
```python
def get_lst_pos(beg, ed, lst_pos):
    if lst_pos == 0:
        return beg - 1
    else:
        return ed + 1


def solve():
    s = input().strip()
    n = len(s)

    dp = [[[2 for k in range(2)] for j in range(n)] for i in range(n)]

    for i in range(n):
        if i > 0:
            ch = s[get_lst_pos(i, i, 0)]
            if ord(s[i]) < ord(ch):
                dp[i][i][0] = 1
            elif ord(s[i]) > ord(ch):
                dp[i][i][0] = 0
        if i < n - 1:
            ch = s[get_lst_pos(i, i, 1)]
            if ord(s[i]) < ord(ch):
                dp[i][i][1] = 1
            elif ord(s[i]) > ord(ch):
                dp[i][i][1] = 0

    for s_len in range(2, n + 1):
        for beg in range(0, n - 1):
            ed = beg + s_len - 1
            if ed >= n:
                break
            for k in range(2):
                if ed == n - 1 and k == 1:
                    continue
                if beg == 0 and k == 0:
                    continue

                if dp[beg + 1][ed][0] == 1 and dp[beg][ed - 1][1] == 1:
                    dp[beg][ed][k] = 0
                elif dp[beg + 1][ed][0] == 0 or dp[beg][ed - 1][1] == 0:
                    dp[beg][ed][k] = 1
                elif s_len % 2 == 1:
                    ch = s[get_lst_pos(beg, ed, k)]
                    if dp[beg + 1][ed][0] == 2:
                        if ord(s[beg]) < ord(ch):
                            dp[beg][ed][k] = 1
                        elif ord(s[beg]) > ord(ch):
                            dp[beg][ed][k] = 0
                    if dp[beg][ed - 1][1] == 2:
                        if ord(s[ed]) < ord(ch):
                            dp[beg][ed][k] = 1
                        if ord(s[ed]) > ord(ch):
                            dp[beg][ed][k] = 0

    # print(dp)

    # print(dp[0][n - 1])

    if dp[1][n - 1][0] == 0 or dp[0][n - 2][1] == 0:
        print('Alice')
    elif dp[1][n - 1][0] == 1 and dp[0][n - 2][1] == 1:
        print('Bob')
    else:
        print('Draw')


if __name__ == '__main__':
    t = int(input().strip())
    for _ in range(t):
        solve()

```