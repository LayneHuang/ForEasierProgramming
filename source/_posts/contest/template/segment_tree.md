---
title: 线段树
date: 2022-07-22 10:47:00
categories: ACM模板
---

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