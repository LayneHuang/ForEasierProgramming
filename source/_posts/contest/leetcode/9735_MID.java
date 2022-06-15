import java.io.IOException;
import java.io.InputStream;
import java.util.InputMismatchException;

public class Main {
    private static FastReader cin = new FastReader(System.in);

    public static class ListNode {
        int val;
        ListNode next;

        ListNode() {
        }

        ListNode(int val) {
            this.val = val;
        }

        ListNode(int val, ListNode next) {
            this.val = val;
            this.next = next;
        }
    }

    public static ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        if (l1 == null && l2 == null) {
            return null;
        }
        ListNode now = null;
        ListNode root = null;
        ListNode pre = null;
        ListNode p1 = l1;
        ListNode p2 = l2;
        int res = 0;
        for (; p1 != null || p2 != null; now = now.next) {
            if (p1 != null) {
                res += p1.val;
                p1 = p1.next;
            }
            if (p2 != null) {
                res += p2.val;
                p2 = p2.next;
            }
            now = new ListNode();
            now.next = null;
            if (pre != null) {
                pre.next = now;
            }
            if (root == null) {
                root = now;
            }
            pre = now;
            now.val = res % 10;
            res /= 10;
        }
        while (res > 0) {
            now = new ListNode();
            pre.next = now;
            now.val = res % 10;
            res /= 10;
            pre = now;
        }
        return root;
    }

    public static void main(String[] args) {
        addTwoNumbers(root1, root2);
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
