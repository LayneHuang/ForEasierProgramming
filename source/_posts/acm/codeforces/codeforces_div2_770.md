---
title: Codeforces 770 div2
date: 2022-03-29 15:39:00
categories: [acm]
tag: [Codeforces]
---

{% link '#770 div2' https://codeforces.com/contest/1634 [title] %}

# D题 find zero
需要进行反问，从3个数字当中可得到 max(a,b,c) - min(a,b,c)
最终判断出数字 0 可能存在的 2 个位置

<!-- more -->

```python
import collections

mp = collections.defaultdict(int)


def ans(list):
    list.sort(key=lambda x: int(x))
    query = '? ' + ' '.join(list)
    global mp
    pre_ans = mp.get(query)
    if pre_ans is not None:
        return pre_ans
    print(query)
    now_ans = int(input())
    mp.setdefault(query, now_ans)
    return now_ans


def query_max_diff(list):
    diff = []
    ans1 = ans([list[0], list[1], list[2]])
    diff.append((list[3], ans1))
    ans2 = ans([list[0], list[1], list[3]])
    diff.append((list[2], ans2))
    ans3 = ans([list[0], list[2], list[3]])
    diff.append((list[1], ans3))
    if ans1 == ans2 and ans2 == ans3 and ans1 == 0:
        return list[0], list[1], list[2], list[3], ans1, True

    ans4 = ans([list[1], list[2], list[3]])
    diff.append((list[0], ans4))

    diff.sort(key=lambda x: x[1])
    # min, sc_min, max-diff, max
    all_eq = True
    now = diff[0][1]
    for item in diff:
        if item[1] != now:
            all_eq = False
    return diff[0][0], diff[1][0], diff[2][0], diff[3][0], diff[0][1], all_eq


def solve():
    global mp
    mp.clear()
    n = int(input())
    list = []
    for i in range(1, n + 1):
        list.append(str(i))

    next_list = []
    a, b, c, d, diff = '1', '2', '3', '4', 0
    while len(list) >= 4:
        query_list = []
        for pos in list:
            query_list.append(pos)
            if len(query_list) == 4:
                a, b, c, d, diff, all_eq = query_max_diff(query_list)
                query_list.clear()
                if not all_eq:
                    next_list.append(a)
                    next_list.append(b)

        if len(query_list) > 0:
            for pos in query_list:
                next_list.append(pos)

        if len(next_list) == 3:
            next_list.append(c)

        # print(next_list)

        list.clear()
        for pos in next_list:
            list.append(pos)
        next_list.clear()

    if len(list) == 1:
        if list[0] == 1:
            print('!', list[0], n)
        else:
            print('! 1', list[0])
    else:
        print('!', list[0], list[1])


if __name__ == '__main__':
    t = int(input())
    for _ in range(t):
        solve()
```

# F题
这个题目更加蹊跷，需要把矩阵中的数字分别分割到 L,R 两个集合当中
要求每一行在L,R的数量相同，并且最终L,R集合的值一致


```python
import collections
 
 
def solve():
    m = int(input().strip())
    cnt = []
    item = [[] for _ in range(m)]
    result = []
 
    for i in range(m):
        n = int(input().strip())
        cnt.append(n)
        item[i] = list(map(int, input().strip().split()))
        result.append(['L' for _ in range(n)])
 
    cnt = collections.Counter(a for row in item for a in row)
    if any(_ % 2 == 1 for _ in cnt.values()):
        print("NO")
        return
 
    idx_mp = collections.defaultdict(set)
    row_mp = collections.defaultdict(set)
    for i, row in enumerate(item):
        for j, a in enumerate(row):
            idx_mp[a].add((i, j))
            row_mp[i].add((a, j))
 
    for i in range(m):
        idx = i
        while row_mp[idx]:
            a, j = row_mp[idx].pop()
            result[idx][j] = 'L'
            idx_mp[a].remove((idx, j))
            idx, j = idx_mp[a].pop()
            result[idx][j] = 'R'
            row_mp[idx].remove((a, j))
 
    print("YES")
    for x in result:
        print(''.join(x))
 
 
if __name__ == '__main__':
    solve()
```