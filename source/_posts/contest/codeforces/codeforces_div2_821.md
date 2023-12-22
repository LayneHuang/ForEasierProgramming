---
title: Codeforces 821 div2
date: 2022-09-23 21:00:00
categories: [acm]
tag: [Codeforces]
---

{% link '#821 div2' https://codeforces.com/contest/1717 [title] %}
### D2
题意：  
给定2个字符串 a 和 b，都是由0与1组成。并且你有2中操作：
1.将相邻的数字取反花费是x    
2.非相邻数字取反花费是y    
求在最少花费的情况下把 a 变成 b。  

思路：
c = a ^ b 得带 c 之后，其实目的就是要把 c 数字都变为 0。  
因为当x<y的时候，需要取反i,i+k时，可以通过i,i+1,...,i+k连续执行操作1达到取反不相邻数字的效果。  
同时当x>2*y的时候，需要取反i,i+1时，可以通过取反另外一个与i,i+1不相邻的数字达到效果

同时对于任意一个位置i，有5种情况：  
0.数字全都是0  
1.第i位置数字为1  
2.前i-1位置上有1个1，第i位是0  
3.前i-1位置上有1个1，第i位是1  
4.前i-1位置上有2个1，第i位是0  

这几种情况就可以满足各种交换的地递推。通过dp[i][j]表示第i个位置目前处于情况j
```python
for _ in range(int(input().strip())):
    n, x, y = map(int, input().strip().split())
    s = input().strip()
    a = [int(ch) for ch in s]
    s = input().strip()
    b = [int(ch) for ch in s]
    c = [(a[i] ^ b[i]) for i in range(n)]

    # print(c)

    mx = 1 << 63
    dp = [[mx for j in range(5)] for i in range(n)]

    for i, num in enumerate(c):
        if i == 0:
            dp[0][num] = 0
            continue
        if num == 0:
            dp[i][0] = dp[i - 1][0]
            dp[i][1] = min(dp[i - 1][1] + min(x, 2 * y), dp[i - 1][2] + y)
            dp[i][2] = min(dp[i - 1][1], dp[i - 1][2])
            dp[i][3] = min(dp[i - 1][0] + min(x, y),
                           dp[i - 1][3] + min(x, y),
                           dp[i - 1][4] + y)
            dp[i][4] = min(dp[i - 1][4], dp[i - 1][3])
        else:
            dp[i][0] = min(dp[i - 1][1] + min(x, 2 * y), dp[i - 1][2] + y)
            dp[i][1] = dp[i - 1][0]
            dp[i][2] = min(dp[i - 1][0] + min(x, y),
                           dp[i - 1][3] + min(x, y),
                           dp[i - 1][4] + y)
            dp[i][3] = min(dp[i - 1][1], dp[i - 1][2])
            dp[i][4] = min(dp[i - 1][1] + min(y, 2 * x), dp[i - 1][2] + min(x, y))

    print(-1 if dp[n - 1][0] >= mx else dp[n - 1][0])

```

### E

来自大佬的代码：
```cpp
#include <bits/stdc++.h>
using namespace std;
 
typedef long long ll;
 
const int Maxn = 120;
 
int Q;
ll t;
int x, y;
ll B[Maxn][Maxn];
bool odd[Maxn][Maxn];
 
int main()
{
    scanf("%d", &Q);
    while (Q--) {
        scanf("%I64d %d %d", &t, &x, &y);
        t -= (x + y);
        if (t < 0) { printf("NO\n"); continue; }
        B[0][0] = t;
        for (int i = 0; i < Maxn; i++)
            for (int j = 0; j < Maxn; j++) {
                odd[i][j] = B[i][j] % 2;
                if (B[i][j]) {
                    if (j + 1 < Maxn) B[i][j + 1] += (B[i][j] + 1) / 2;
                    if (i + 1 < Maxn) B[i + 1][j] += B[i][j] / 2;
                    B[i][j] = 0;
                }
            }
        int r = 0, c = 0;
        int steps = x + y;
        for (int i = 0; i < steps && r < Maxn && c < Maxn; i++)
            if (odd[r][c]) r++;
            else c++;
        printf("%s\n", r == x && c == y? "YES": "NO");
    }
    return 0;
}
```