# [Codeforces #676 div2](http://codeforces.com/contest/1421)

### B题
这题我暴力模拟，代码写了一大堆。  
一看别人的代码，真的简洁  
因为问题保证了有答案，所以特判起点终点的边缘值，保证出入口是两个相反值即可。
```java
import java.util.Scanner;

public class Main
{	
	public static boolean check(String x,String y,String[][] s)
	{
		int n=s.length-1;
		int a=s[0][1].equals(y)?1:0;
		int b=s[1][0].equals(y)?1:0;
		int c=s[n][n-1].equals(x)?1:0;
		int d=s[n-1][n].equals(x)?1:0;
		int res=a+b+c+d;
		if(res<=2)
		{
			System.out.println(res);
			if(a==1)
				System.out.println("1 2");
			if(b==1)
				System.out.println("2 1");
			if(c==1)
				System.out.println((n+1)+" "+n);
			if(d==1)
				System.out.println(n+" "+(n+1));
			return true;
		}
		return false;
	}
	
	public static void main(String[] args)
	{
		Scanner sc=new Scanner(System.in);
		int t=Integer.parseInt(sc.next());
		for(int i=0;i<t;i++)
		{
			int n=Integer.parseInt(sc.next());
			String[][] s=new String[n][n];
			for(int j=0;j<n;j++)
				s[j]=sc.next().split("");
			if(!check("0","1",s))
				check("1","0",s);
		}
	}
}
```

### C题
字符串边缘的回文可以构造出来

### D题
蜜蜂窝形态的地图，实质上符合贪心的原则，必定是往3个直线或斜线方向走然后再水平走。

### F题
给 n 个数，每次操作使得其中两个连续的数取反后相加放回原来位置，问 n - 1 次操作后最后的数值最大的可能值。  
想了2天，不太会怎么DP可以推到所有情况。  
其实相当于每个位可以是取反奇数次，偶数次然后决定最终值。只不过 n - 1 次操作怎么正确分配每个位呢？

```cpp
#include <bits/stdc++.h>
using namespace std;
const int N = 2e5 + 5;
int n, a[N]; long long f[N][3][2][2];
int main() {
	cin >> n, memset(f, - 0x3f, sizeof f);
	for (int i = 1; i <= n; i ++) cin >> a[i];
	if (n == 1) return cout << a[1] << '\n', 0;
	f[1][1][0][1] = a[1], f[1][2][0][0] = - a[1];
	for (int i = 1; i < n; i ++)
		for (int j = 0; j < 3; j ++)
			for (int k = 0; k < 2; k ++)
				for (int l = 0; l < 2; l ++)
					for (int t = 0; t < 2; t ++) {
						int nxt = (j + (t == 1 ? 1 : 2)) % 3;
						long long value = f[i][j][k][l] + (t == 1 ? a[i + 1] : - a[i + 1]);
						f[i + 1][nxt][k | (l == t)][t] = max(f[i + 1][nxt][k | (l == t)][t], value);
					}
	return cout << max(f[n][1][1][0], f[n][1][1][1]) << '\n', 0;
}
```