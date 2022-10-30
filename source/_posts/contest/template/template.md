---
title: acm模板
date: 2022-07-22 10:47:00
categories: acm
---

切题用到的 python 模板, leetcode, codeforces 用

<!-- more -->

### 输入模板
```python
import sys

input = lambda: sys.stdin.readline().rstrip()

# for _ in range(int(input()))
# n = int(input())
# n, k = map(int, input().split())
# a = list(map(int, input().split()))
# a = [list(map(int, input().split())) for i in range(n)]

```

### 优先队列

```python
import heapq

class MyPriorityQueue:
    que: list = None

    def __init__(self, arr=None):
        if arr is None:
            arr = []
        self.que = arr
        heapq.heapify(self.que)

    def push(self, val):
        heapq.heappush(self.que, val)

    def pop(self):
        return heapq.heappop(self.que)

    def peek(self):
        return self.que[0]

    def is_empty(self):
        return len(self.que) == 0

    def show(self):
        print('que:', self.que)
```
### GCD

```python
def GCD(a , b) :
    return b if a == 0 else GCD(b%a, a) 
```

### 质数筛

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

### 统计 n 以内互质的数的个数

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

### 数组离散
```python
def li_san(nums):
    mp = {}

    if len(nums) == 0:
        return mp

    nums.sort()
    cnt = 0

    for i, x in enumerate(nums):
        if i > 0 and x == nums[i - 1]:
            continue
        cnt += 1
        mp[x] = cnt

    return mp
```

### 树离散(DFS序)
```pyhon
def dfs(now, in_dx, out_dx):
    global tt
    if now:
        v = now.val
        tt += 1
        in_dx[v] = tt
        dfs(now.left, in_dx, out_dx)
        dfs(now.right, in_dx, out_dx)
        out_dx[v] = tt
```

### 线段树（区间更新，区间最值）
```python
t = []
lazy = []

def init(n):
    global t, lazy
    t = [0 for i in range(n * 4 + 1)]
    lazy = [-1 for i in range(n * 4 + 1)]

def up(now):
    global t
    t[now] = max(t[now << 1], t[now << 1 | 1])


def build(now, lx, rx):
    if lx == rx:
        return
    mid = (lx + rx) >> 1
    build(now << 1, lx, mid)
    build(now << 1 | 1, mid + 1, rx)
    up(now)


def down(now):
    global lazy, t
    if lazy[now] == -1:
        return
    t[now << 1] = lazy[now]
    lazy[now << 1] = lazy[now]
    t[now << 1 | 1] = lazy[now]
    lazy[now << 1 | 1] = lazy[now]
    lazy[now] = -1


def update(now, l, r, a, b, v):
    if a <= l and r <= b:
        global lazy, t
        t[now] = v
        lazy[now] = v
        return

    down(now)
    mid = (l + r) >> 1
    if b <= mid:
        update(now << 1, l, mid, a, b, v)
    elif a > mid:
        update(now << 1 | 1, mid + 1, r, a, b, v)
    else :
        update(now << 1, l, mid, a, b, v)
        update(now << 1 | 1, mid + 1, r, a, b, v)
    up(now)


def query(now, l, r, a, b):
    if a <= l and r <= b:
        global t
        return t[now]
    down(now)
    mid = (l + r) >> 1
    if b <= mid:
        return query(now << 1, l, mid, a, b)
    elif a > mid:
        return query(now << 1 | 1, mid + 1, r, a, b)
    else:
        return max(query(now << 1, l, mid, a, b), query(now << 1 | 1, mid + 1, r, a, b))
```
### 线段树（单点更新，区间求最）
```python
t = []
v = []


def init(n):
    global t
    global v
    t = [0 for i in range((n + 1) << 2)]
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

### 线段树（单点更新, 区间求和）
```python
t = []


def init(n):
    global t
    t = [0 for i in range((n + 1) << 2)]


def up(root):
    t[root] = t[root << 1] + t[root << 1 | 1]


def update(root, lx, rx, pos, value):
    if lx == rx:
        t[root] += value
        return

    mid = (lx + rx) >> 1
    if pos <= mid:
        update(root << 1, lx, mid, pos, value)
    if pos > mid:
        update(root << 1 | 1, mid + 1, rx, pos, value)
    up(root)


def query_sum(root, lx, rx, ql, qr):
    if ql <= lx and rx <= qr:
        return t[root]
    now_sum = 0
    mid = (lx + rx) >> 1
    if ql <= mid:
        now_sum += query_sum(root << 1, lx, mid, ql, qr)
    if qr > mid:
        now_sum += query_sum(root << 1 | 1, mid + 1, rx, ql, qr)
    return now_sum

```

### KMP

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

### 字典树

```python
class TreeNode:
    def __init__(self):
        self.val = 0
        self.son = [None for i in range(26)]


def add(now, word, p):
    now.val += 1
    if p + 1 < len(word):
        ch = word[p + 1]
        idx = ord(ch) - ord('a')
        # print(ch, idx)
        if now.son[idx] is None:
            now.son[idx] = TreeNode()
        add(now.son[idx], word, p + 1)


def cnt(now, word):
    ans = 0
    for ch in word:
        idx = ord(ch) - ord('a')
        now = now.son[idx]
        ans += now.val

    return ans

if __name__ == '__main__':
    root = TreeNode()
    add(root, "abcd", -1)
```

### 并查集

```python
def init(n):
    return [i for i in range(n)]

def find(p, fa):
    if p == fa[p]:
        return p
    fa[p] = find(fa[p], fa)
    return fa[p]


def unit(p, q, fa):
    fa_p = find(p, fa)
    fa_q = find(q, fa)
    if fa_p != fa_q:
        fa[fa_q] = fa_p
```