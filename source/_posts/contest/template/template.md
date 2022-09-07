---
title: acm模板
date: 2022-07-22 10:47:00
categories: acm
---

质数筛

```python
    vis = [False for i in range(n + 1)]
    pri = []
    for i in range(2, n + 1):
        if not vis[i]:
            pri.append(i)
            for j in range(i, n + 1, i):
                vis[j] = True
```

```python
def get_gcd_list(num):
    res = []
    for i in range(2, num):
        if i * i > num:
            break
        if num % i == 0:
            res.append(i)
            while num % i == 0:
                num //= i

    if num > 1:
        res.append(num)

    return res
```

统计 n 以内互质的数的个数
```python
    vis = [False for i in range(n + 1)]
    pri_cnt = [i for i in range(n + 1)]

    pri = [0, 1]
    for i in range(2, n + 1):
        if not vis[i]:
            pri.append(i)
            for j in range(i, n + 1, i):
                vis[j] = True
                pri_cnt[j] -= pri_cnt[j] // i
```

区间最值, 简易模板

```java
public class Main {

    static final int N = 100010;
    static long[] t = new long[N << 2];
    static long[] v = new long[N];

    static void up(int root) {
        t[root] = Math.max(t[root << 1], t[root << 1 | 1]);
    }

    static void build(int root, int l, int r) {
        if (l == r) {
            t[root] = v[l];
            return;
        }
        int mid = (l + r) / 2;
        build(root << 1, l, mid);
        build(root << 1 | 1, mid + 1, r);
        up(root);
    }

    static long queryMax(int root, int l, int r, int qL, int qR) {
        if (qL <= l && r <= qR) return t[root];
        long nowMax = 0;
        int mid = (l + r) / 2;
        if (qL <= mid) nowMax = Math.max(nowMax, queryMax(root << 1, l, mid, qL, qR));
        if (qR > mid) nowMax = Math.max(nowMax, queryMax(root << 1 | 1, mid + 1, r, qL, qR));
        return nowMax;
    }
}
```