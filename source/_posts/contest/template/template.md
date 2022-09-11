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

线段树单点更新，区间求最
```python
t = []
v = []


def init(n):
    global t
    global v
    t = [0 for i in range(n + 1)]
    v = [0 for i in range(n + 1)]


def up(root):
    t[root] = max(t[root << 1], t[root << 1 | 1])


def build(root, lx, rx):
    if lx == rx:
        t[root] = v[lx]
    mid = (lx + rx) >> 1
    build(root << 1, lx, mid)
    build(root << 1 | 1, mid + 1, rx)
    up(root)


def update(root, lx, rx, pos, value):
    if lx == rx:
        t[root] = max(t[root], value)
        return

    mid = (lx + rx) >> 1
    if pos <= mid:
        update(root << 1, lx, mid, pos, value)
    if pos > mid:
        update(root << 1 | 1, mid + 1, rx, pos, value)
    up(root)


def query_max(root, lx, rx, ql, qr):
    if ql <= lx and rx <= qr:
        return t[root]
    now_max = 0
    mid = (lx + rx) >> 1
    if ql <= mid:
        now_max = max(now_max, query_max(root << 1, lx, mid, ql, qr))
    if qr > mid:
        now_max = max(now_max, query_max(root << 1 | 1, mid + 1, rx, ql, qr))
    return now_max
```


KMP
```java
public class Main {
    public static int[] getNext(String ps) {
        char[] p = ps.toCharArray();
        int[] next = new int[p.length];
        next[0] = -1;
        int j = 0;
        int k = -1;
        while (j < p.length - 1) {
            if (k == -1 || p[j] == p[k]) {
                if (p[++j] == p[++k]) { // 当两个字符相等时要跳过
                    next[j] = next[k];
                } else {
                    next[j] = k;
                }
            } else {
                k = next[k];
            }
        }
        return next;
    }
    public static int KMP(String ts, String ps) {
        char[] t = ts.toCharArray();
        char[] p = ps.toCharArray();
        int i = 0; // 主串的位置
        int j = 0; // 模式串的位置
        int[] next = getNext(ps);
        while (i < t.length && j < p.length) {
            if (j == -1 || t[i] == p[j]) { // 当j为-1时，要移动的是i，当然j也要归0
                i++;
                j++;
            } else {
                j = next[j]; // j回到指定位置
            }
        }
        if (j == p.length) {
            return i - j;
        } else {
            return -1;
        }
    }
}
```