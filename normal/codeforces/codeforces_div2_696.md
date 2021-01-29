# D题 Cleaning
http://codeforces.com/contest/1474/problem/D  
题意：  
有长度为 n 的一组石头堆，每堆石头上有 ai 个石头。  
有两种操作：  
1.移除相邻的石头堆上各一个石头  
2.在游戏开始前交换相邻的石头堆，只能进行一次  
问: 能否把所有石头都移走？  

思路：
1.在不使用第2种操作的情况下，想要把所有石头都清除，你必须从前往后操作且需要把前面的石头堆都清零，那么  
把 dp[i] 看成操作后剩余的石头数目，每次操作实质上就是 dp[i] = a[i] - dp[i-1]  
对于整个

```java
public class Main {
    private static FastReader cin = new FastReader(System.in);
 
    private static boolean solve2(List<Integer> a) {
        int n = a.size();
        long sum0 = 0;
        long sum1 = 0;
        for (int i = 0; i < n; ++i) {
            if (i % 2 == 0) {
                sum0 += a.get(i);
            } else {
                sum1 += a.get(i);
            }
        }
        long diff = sum1 - sum0;
        int[][] dp = new int[n][2];
        dp[0][0] = a.get(0);
        dp[0][1] = -1;
        for (int i = 0; i < n - 1; ++i) {
            int flag = (i + 1) % 2 == 1 ? 1 : -1;
            if (dp[i][0] != -1 && a.get(i + 1) - dp[i][0] >= 0) {
                dp[i + 1][0] = a.get(i + 1) - dp[i][0];
            } else {
                dp[i + 1][0] = -1;
            }
            if (dp[i][0] != -1 && 2 * a.get(i) - a.get(i + 1) - dp[i][0] >= 0
                && dp[i][0] - a.get(i) + a.get(i + 1) >= 0
                && (flag * diff - 2 * (a.get(i + 1) - a.get(i)) == 0)) {
                dp[i + 1][1] = 2 * a.get(i) - a.get(i + 1) - dp[i][0];
            } else if (dp[i][1] != -1 && a.get(i + 1) - dp[i][1] >= 0) {
                dp[i + 1][1] = a.get(i + 1) - dp[i][1];
            } else {
                dp[i + 1][1] = -1;
            }
        }
        return dp[n - 1][0] == 0 || dp[n - 1][1] == 0;
    }
 
    private static void solve() {
        int n = cin.nextInt();
        List<Integer> a = new ArrayList<>();
        List<Integer> reverseA = new ArrayList<>();
        for (int i = 0; i < n; ++i) {
            int num = cin.nextInt();
            a.add(num);
            reverseA.add(num);
        }
        Collections.reverse(reverseA);
        System.out.println((solve2(a) || solve2(reverseA)) ? "YES" : "NO");
    }
}
```
