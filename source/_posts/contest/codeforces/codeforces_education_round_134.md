---
title: Codeforces education round 134
date: 2022-03-29 15:39:00
categories: acm
tag: Codeforces
---

{% link '#education round 134' https://codeforces.com/contest/1721 [title] %}

# D题 
```java
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
2
5
4 2 7 11 25
27 29 24 20 6
3
27 30 0
0 5 9
 *
 * <p>
 * 31
 * 9
 */
public class Main {
    private static final FastReader cin = new FastReader(System.in);


    private static int count(int num, int bit) {
        return (num & (1 << bit)) > 0 ? 1 : 0;
    }


    private static void decode(int num, int[] bits) {
        int idx = 0;
        while (num > 0) {
            bits[idx++] = num % 2;
            num /= 2;
        }
    }

    private static void solve() {
        int n = cin.nextInt();
        int[] cnt = new int[31];
        int[][][] bits = new int[2][n][31];
        int[][] sum = new int[2][31];


        List<Integer> a = new ArrayList<>();
        List<Integer> b = new ArrayList<>();
        List<List<Integer>> aa = new ArrayList<>();
        List<List<Integer>> bb = new ArrayList<>();
        for (int i = 0; i < n; ++i) a.add(cin.nextInt());
        for (int i = 0; i < n; ++i) b.add(cin.nextInt());

        List<Integer> idxA = new ArrayList<>();
        List<Integer> idxB = new ArrayList<>();
        for (int i = 0; i < n; ++i) {
            decode(a.get(i), bits[0][i]);
            decode(b.get(i), bits[1][i]);
            idxA.add(i);
            idxB.add(i);
        }

        for (int k = 0; k < 2; ++k) {
            for (int i = 0; i < 31; ++i) {
                for (int j = 0; j < n; ++j) {
                    sum[k][i] += bits[k][j][i];
                }
            }
        }

        aa.add(idxA);
        bb.add(idxB);

        for (int bit = 30; bit >= 0; --bit) {

            if (sum[0][bit] != n - sum[1][bit]) continue;

            for (int i = 0; i < aa.size(); ++i) {
                List<Integer> nowA = aa.get(i);
                List<Integer> nowB = bb.get(i);
                int cntA = 0;
                int cntB = 0;
                for (int j = 0; j < nowA.size(); ++j) {
                    int idA = nowA.get(j);
                    int idB = nowB.get(j);
                    int bitA = bits[0][idA][bit];
                    cntA += bitA;
                    int bitB = bits[1][idB][bit];
                    cntB += 1 ^ bitB;
                }
                if (cntA == cntB) cnt[bit] += nowA.size();
                else break;
            }

            if (cnt[bit] == n) {
                List<List<Integer>> nAA = new ArrayList<>();
                List<List<Integer>> nBB = new ArrayList<>();
                for (int i = 0; i < aa.size(); ++i) {
                    List<Integer> nowA = aa.get(i);
                    List<Integer> nowB = bb.get(i);
                    List<Integer> na0 = new ArrayList<>();
                    List<Integer> na1 = new ArrayList<>();
                    List<Integer> nb0 = new ArrayList<>();
                    List<Integer> nb1 = new ArrayList<>();
                    for (int j = 0; j < nowA.size(); ++j) {

                        int idA = nowA.get(j);
                        int idB = nowB.get(j);

                        int bitA = bits[0][idA][bit];
                        int bitB = bits[1][idB][bit];

                        if (bitA == 0) na0.add(nowA.get(j));
                        else na1.add(nowA.get(j));

                        if (bitB == 0) nb0.add(nowB.get(j));
                        else nb1.add(nowB.get(j));
                    }
                    nAA.add(na0);
                    nAA.add(na1);
                    nBB.add(nb1);
                    nBB.add(nb0);
                }
                aa = nAA;
                bb = nBB;
            }
        }
        int ans = 0;
        for (int i = 30; i >= 0; --i) if (cnt[i] == n) ans |= (1 << i);
        System.out.println(ans);
    }

    public static void main(String[] args) {
        int t = cin.nextInt();
        for (int tt = 0; tt < t; ++tt) solve();
    }

    private static class FastReader {
        private final InputStream stream;

        private final byte[] buf = new byte[1024];

        private int curChar;

        private int numChars;

        FastReader(InputStream stream) {
            this.stream = stream;
        }

        int read() {
            if (numChars == -1) {
                throw new InputMismatchException();
            }
            if (curChar >= numChars) {
                curChar = 0;
                try {
                    numChars = stream.read(buf);
                } catch (IOException e) {
                    throw new InputMismatchException();
                }
                if (numChars <= 0) {
                    return -1;
                }
            }
            return buf[curChar++];
        }

        int nextInt() {
            int c = read();
            while (isSpaceChar(c)) {
                c = read();
            }
            int sgn = 1;
            if (c == '-') {
                sgn = -1;
                c = read();
            }
            int res = 0;
            do {
                if (c < '0' || c > '9') {
                    throw new InputMismatchException();
                }
                res *= 10;
                res += c - '0';
                c = read();
            } while (!isSpaceChar(c));
            return res * sgn;
        }

        public long nextLong() {
            long ret = 0;
            int c = read();
            while (c <= ' ') {
                c = read();
            }
            boolean neg = (c == '-');
            if (neg) {
                c = read();
            }
            do {
                ret = ret * 10 + c - '0';
            } while ((c = read()) >= '0' && c <= '9');
            if (neg) {
                return -ret;
            }
            return ret;
        }

        public String next() {
            int c = read();
            while (isSpaceChar(c)) {
                c = read();
            }
            StringBuilder res = new StringBuilder();
            do {
                res.appendCodePoint(c);
                c = read();
            } while (!isSpaceChar(c));
            return res.toString();
        }

        boolean isSpaceChar(int c) {
            return c == ' ' || c == '\n' || c == '\r' || c == '\t' || c == -1;
        }
    }
}

```