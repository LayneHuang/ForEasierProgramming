---
title: Codeforces 823 div2
date: 2022-09-26 21:00:00
categories: acm
tag: Codeforces
---

{% link '#823 div2' https://codeforces.com/contest/1717 [title] %}
### B
```python
import heapq
import operator


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


def solve():
    n = int(input().strip())
    a = list(map(int, input().strip().split()))
    t = list(map(int, input().strip().split()))

    if n == 1:
        print(a[0])
        return

    nodes = [(a[i], t[i]) for i in range(n)]

    nodes.sort(key=operator.itemgetter(0, 1))
    # print(nodes)

    l_que = MyPriorityQueue()
    r_que = MyPriorityQueue()

    for i, node in enumerate(nodes):
        a, ti = node
        r_que.push((-a - ti, i))

    # r_que.show()
    ans_cost = 0
    ans = -1

    for i, node in enumerate(nodes):
        if i == n - 1:
            break
        a, ti = node
        l_que.push((a - ti, i))
        while not r_que.is_empty() and r_que.peek()[1] <= i:
            r_que.pop()

        l_top = l_que.peek()
        r_top = r_que.peek()

        # l_que.show()
        # r_que.show()

        l_id = l_top[1]
        r_id = r_top[1]

        l_a, l_t = nodes[l_id]
        r_a, r_t = nodes[r_id]

        cost = ((r_t + r_a - a) + (l_t + a - l_a)) / 2

        point = a + cost - (l_t + a - l_a)

        # print('before', point)

        if point < a:
            point = a
        if point > nodes[i + 1][0]:
            point = nodes[i + 1][0]

        cost = max(l_t + (point - l_a), r_t + (r_a - point))
        # print(i, node, nodes[i + 1], point, cost)

        if ans == -1 or cost < ans_cost:
            # print(point, cost)
            ans = point
            ans_cost = cost

    print(ans)


if __name__ == '__main__':
    t = int(input().strip())
    for _ in range(t):
        solve()

```
