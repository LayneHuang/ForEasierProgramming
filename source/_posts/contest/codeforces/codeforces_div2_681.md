---
title: Codeforces 681 div2
date: 2020-11-05 11:09:00
tag: Codeforces
---

{% link '#681 div2' http://codeforces.com/contest/1443 [title] %}

### A题
输入 n , 要求输出 n 个范围在（ 1 ~ 4n ）的数字，满足一下规则：  
1. GCD( A , B ) != 1  
2. A, B 相互不能够整除

我的做法是先筛选出素数，对其每个素数乘2，这样每个数都符合上面两条规则，而且又足够的多。  
然后把数字丢到队列中，只要它还能乘上素因子，就继续让它乘，这样能够产生更多符合要求的数。(虽然不知道原理是为何)  
```java
public class Main1 {
 
    private static final int N = 410;
 
    private static int[] pri = new int[N];
    private static int size = 0;
 
    private static void init() {
        boolean[] vis = new boolean[N];
        for (int i = 2; i < N; i++) {
            if (vis[i]) continue;
            pri[size++] = i;
            for (int j = i; j < N; j += i) {
                vis[j] = true;
            }
        }
    }
 
    private static void solve() {
        int n = in.nextInt();
        Queue<Integer> ans = new PriorityQueue<>();
        for (int i = 0; 2 * pri[i] <= 4 * n; ++i) {
            ans.add(2 * pri[i]);
        }
        while (ans.peek() < 2 * n) {
            int now = ans.poll();
            for (int i = 0; i < size; ++i) {
                if (now * pri[i] > 4 * n) break;
                if (ans.contains(now)) continue;
                ans.add(now * pri[i]);
            }
        }
        int lst = -1;
        int cnt = 0;
        while (!ans.isEmpty()) {
            int now = ans.poll();
            if (now == lst) continue;
            if (cnt == n) break;
            out.print(now + " ");
            lst = now;
            cnt++;
        }
        out.println();
    }
 
    private static void solveT() {
        init();
        int T = in.nextInt();
        for (int t = 0; t < T; ++t) {
            solve();
        }
    }
}
```

### B题
对中间没放地雷的做贪心就OK
```java
public class Main {
    private static Scanner cin = new Scanner(System.in, StandardCharsets.UTF_8.name());

    private static void solve() {
        int a = cin.nextInt();
        int b = cin.nextInt();
        int lst = -1;
        int zeroCount = 0;
        int ans = 0;
        String str = cin.next();
        for (int i = 0; i < str.length(); ++i) {
            if (str.charAt(i) == '0') {
                zeroCount++;
            } else {
                if (zeroCount > 0 && lst != -1) {
                    ans += Math.min(zeroCount * b, a);
                    zeroCount = 0;
                }
                if (lst == -1) {
                    ans += a;
                    zeroCount = 0;
                }
                lst = i;
            }
        }
        System.out.println(ans);
    }

    public static void main(String[] args) {
        int T = cin.nextInt();
        for (int t = 0; t < T; ++t) {
            solve();
        }
    }
}
```

### C题
不自己实现快读过不了
```java
public class Main {
    private static FastReader cin = new FastReader(System.in);

    private static void solve() {
        long ans = Long.MAX_VALUE;
        int n = cin.nextInt();
        List<Node> list = new ArrayList<>(n);
        for (int i = 0; i < n; ++i) {
            Node node = new Node();
            node.a = cin.nextInt();
            list.add(node);
        }
        for (int i = 0; i < n; ++i) {
            list.get(i).b = cin.nextInt();
        }
        list.sort((o1, o2) -> {
            int cmp = Integer.compare(o1.a, o2.a);
            if (cmp != 0) {
                return -cmp;
            }
            return Integer.compare(o1.b, o2.b);
        });
        long sum = 0;
        for (int i = 0; i < n; ++i) {
            ans = Math.min(ans, Math.max(sum, list.get(i).a));
            sum += list.get(i).b;
        }
        ans = Math.min(ans, sum);
        System.out.println(ans);
    }

    public static void main(String[] args) {
        int T = cin.nextInt();
        for (int t = 0; t < T; ++t) {
            solve();
        }
    }

    private static class Node {
        int a;

        int b;
    }
}
```

### D题
左右需要分别递减，所以贪心一下，从左往右构造递减的时候，尽量减得多，这样使得右边的起点也尽量小。
```java
public class Main {
    private static FastReader cin = new FastReader(System.in);

    private static void solve() {
        int n = cin.nextInt();
        List<Integer> list = new ArrayList<>(n);
        for (int i = 0; i < n; ++i) {
            list.add(cin.nextInt());
        }

        boolean[] isDown = new boolean[n];
        for (int i = n - 1; i >= 0; --i) {
            if (i == n - 1 || list.get(i) <= list.get(i + 1)) {
                isDown[i] = true;
            } else {
                break;
            }
        }

        int leftNum = Integer.MAX_VALUE;
        int rightNum = 0;
        for (int i = 0; i < n; ++i) {
            int now = list.get(i);
            if (rightNum <= now && isDown[i]) {
                System.out.println("YES");
                return;
            }
            if (rightNum > now) {
                break;
            }
            int nowLeftNum = now - rightNum;
            if (nowLeftNum > leftNum) {
                rightNum += (nowLeftNum - leftNum);
            } else {
                leftNum = nowLeftNum;
            }
        }
        System.out.println("NO");
    }
}
```
