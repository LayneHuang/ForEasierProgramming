---
title: Codeforces 823 div2
date: 2022-09-26 21:00:00
categories: acm
tag: Codeforces
---

{% link '#823 div2' https://codeforces.com/contest/1730 [title] %}
### B
题意：  
x 轴上有 n 个人，大家都要去要一个相同的点开会。每个人都需要一个ti时间穿衣服，以及 abs(xi - ans) 时间移动。
找出一个点让到达时间最小。

思路：  
对于任意一个点 x，它必定为它左边或右边最远的点（距离+穿衣时间）。  
对于左边的点的距离为 ti + x - xi ， 实际上排序由 ti - xi 决定  
对于右边的点的距离为 ti + xi - x ， 实际上排序由 ti + xi 决定  

那么思路就是枚举坐标轴上的点，分别用左，右两个优先队列维护最远点的坐标。  
（注意取答案时，值不能超过下一个点的值域，且越居中越好）  

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
