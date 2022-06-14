import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.InputMismatchException;

public class Main {
    private static FastReader cin = new FastReader(System.in);

    static int[] dirX = {-1, 1};
    static int[] dirY = {1, -1};

    public static int[] findDiagonalOrder(int[][] mat) {
        int m = mat.length;
        int n = mat[0].length;
        int[] result = new int[n * m];
        int total = 0;
        int nowX = 0, nowY = 0;
        int up = 0;
        for (; ; ) {
            result[total++] = mat[nowX][nowY];
            if (nowX == m - 1 && nowY == n - 1) {
                break;
            }
            if (nowX == 0 && nowX + dirX[up] < 0 && nowY + 1 < n) {
                nowY++;
                up ^= 1;
            } else if (nowY == 0 && nowY + dirY[up] < 0 && nowX + 1 < m) {
                nowX++;
                up ^= 1;
            } else if (nowX == m - 1 && nowX + dirX[up] >= m && nowY + 1 < n) {
                nowY++;
                up ^= 1;
            } else if (nowY == n - 1 && nowY + dirY[up] >= n && nowX + 1 < m) {
                nowX++;
                up ^= 1;
            } else {
                nowX += dirX[up];
                nowY += dirY[up];
            }
        }
        return result;
    }


    public static void main(String[] args) {
        int[][] mat = {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        };
//        int[][] mat = {
//                {1, 2},
//                {3, 4},
//        };
        Arrays.stream(findDiagonalOrder(mat)).forEach(x -> System.out.print(" " + x));
        System.out.println();
    }


    private static class FastReader {
        private InputStream stream;

        private byte[] buf = new byte[1024];

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
