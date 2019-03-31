import java.math.BigInteger;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main
{
    private static final String NUMBER = "[-+]?\\d+";
    private static final String INDEX = "[ \t]*\\^[ \t]*" + NUMBER;
    private static final String POWER = "x(?:" + INDEX + ")?";
    private static final String SIN = "sin[ \t]*";
    private static final String COS = "cos[ \t]*";
    private static final Pattern numberPattern = Pattern.compile(NUMBER);
    private static final Pattern powerPattern = Pattern.compile(POWER);
    private static final Pattern sinPattern = Pattern.compile(SIN);
    private static final Pattern cosPattern = Pattern.compile(COS);
    private static final Pattern indexPattern = Pattern.compile(INDEX);

    private static final BigInteger TENTHOUSAND = new BigInteger("10000");
    private static final BigInteger NEG_TENTHOUSAND = new BigInteger("-10000");

    private static void wrongFormat()
    {
        System.out.println("WRONG FORMAT!");
        System.exit(0);
    }

    private static int[] brackets(String str)
    {
        final int l = str.length();
        int[] arr = new int[l];
        int i = 0;
        while (i < l) {
            arr[i++] = -1;
        }
        i = 0;
        int[] stack = new int[l];
        int top = -1;
        while (i < l)
        {
            if (str.charAt(i) == '(') {
                stack[++top] = i;
            } else if (str.charAt(i) == ')') {
                if (top < 0) {
                    wrongFormat();
                } else {
                    arr[stack[top--]] = i;
                }
            }
            i++;
        }
        if (top >= 0) {
            wrongFormat();
        }
        return arr;
    }

    private static boolean flag;
    private static int plus;
    private static boolean star;

    private static int
        nextStart(String str, int currentEnd, int length, boolean exit)
    {
        int i = currentEnd;
        plus = 0;
        star = false;
        while (i < length) {
            char c = str.charAt(i);
            if (c != ' ' && c != '\t') {
                if (star || plus >= 2) {
                    break;
                } else if (c == '+' || c == '-') {
                    plus++;
                } else if (c == '*') {
                    if (plus == 0) {
                        star = true;
                    } else {
                        wrongFormat();
                    }
                } else if (plus == 0 && exit) {
                    wrongFormat();
                } else {
                    break;
                }
            }
            i++;
        }
        return i;
    }

    private static int[] find(Matcher matcher, int start,
                              int lastStart, int lastEnd, int l)
    {
        flag = false;
        if (lastStart >= start) {
            if (start < l && lastStart == start) {
                flag = true;
            }
            return new int[]{lastStart, lastEnd};
        } else {
            if (flag = matcher.find(start)) {
                flag = matcher.start() == start;
                return new int[]{matcher.start(), matcher.end()};
            } else {
                return new int[]{l, l};
            }
        }
    }

    private static int judge(String str) {
        final int l = str.length();
        int cnt = 0;
        Matcher[] matchers = {powerPattern.matcher(str),
                sinPattern.matcher(str), cosPattern.matcher(str),
                numberPattern.matcher(str), indexPattern.matcher(str)};
        int[] ls = {-1, -1, -1, -1, -1};
        int[] le = {-1, -1, -1, -1, -1};
        int i;
        final int[] bra = brackets(str);
        int cur = nextStart(str, 0, l, false);
        if (star || plus != 0) {
            cnt++;
        }
        if (cur >= l) {
            wrongFormat();
        }
        while (cur < l) {
            if (str.charAt(cur) == '(') {
                judge(str.substring(cur + 1, bra[cur]));
                cur = bra[cur] + 1;
            } else {
                for (i = 0, flag = false; i < 4 && !flag; i++) {
                    int[] tmp = find(matchers[i], cur, ls[i], le[i], l);
                    ls[i] = tmp[0];
                    le[i] = tmp[1];
                }
                if (!flag) {
                    wrongFormat();
                }
                cur = le[--i];
                switch (order[i]) {
                    case 0:
                    case 1:
                        break;
                    case 2:
                    case 3:
                        String sss = str.substring(cur + 1, bra[cur]);
                        if (bra[cur] < 0 || (judge(sss) != 1
                                && !sss.matches("[ \t]*[-+]?\\d+[ \t]*"))) {
                            wrongFormat();
                        }
                        cur = bra[cur] + 1;
                        int[] tmp = find(matchers[4], cur, ls[4], le[4], l);
                        ls[4] = tmp[0];
                        le[4] = tmp[1];
                        if (flag) {
                            cur = le[4];
                        }
                        break;
                    default:
                } }
            cur = nextStart(str, cur, l, true);
            cnt++;
        }
        if (star || plus > 0) {
            wrongFormat();
        }
        return cnt;
    }

    private static Expr mul(Expr e1, Expr e2)
    {
        if (e1 != null) {
            return e1.multiply(e2);
        } else {
            return e2;
        }
    }

    private static Expr add(Expr e1, Expr e2)
    {
        if (e1 != null) {
            return e1.add(e2);
        } else {
            return e2;
        }
    }

    private static final int[] order = {1, 2, 3, 0};

    private static void tooBigIndex(BigInteger bigInteger)
    {
        if (bigInteger.compareTo(TENTHOUSAND) > 0
                || bigInteger.compareTo(NEG_TENTHOUSAND) < 0) {
            wrongFormat();
        }
    }

    private static BigInteger gI(String str, int judge)
    {
        switch (judge) {
            case 0:
                return new BigInteger(str);
            case 1:
                String[] arr = str.split("\\^");
                BigInteger bigInteger;
                if (numberPattern.matcher(str).find()) {
                    bigInteger = new BigInteger(arr[1]);
                } else {
                    bigInteger = BigInteger.ONE;
                }
                tooBigIndex(bigInteger);
                return bigInteger;
            case 2:
            case 3:
                bigInteger = new BigInteger(str.substring(1));
                tooBigIndex(bigInteger);
                return bigInteger;
            default:
                throw new UnsupportedOperationException();
        }
    }

    private static Expr create(String str) {
        Expr expr = null;
        Expr mid = null;
        final int[] bra = brackets(str);
        final int l = str.length();
        int cur = 0;
        if (str.charAt(cur) == '+') {
            cur++;
        }
        Matcher[] matchers = {powerPattern.matcher(str),
                sinPattern.matcher(str), cosPattern.matcher(str),
                numberPattern.matcher(str), indexPattern.matcher(str)};
        int[] ls = {-1, -1, -1, -1, -1};
        int[] le = {-1, -1, -1, -1, -1};
        int i;
        while (cur < l) {
            if (str.charAt(cur) == '(') {
                mid = mul(mid, create(str.substring(cur + 1, bra[cur])));
                cur = bra[cur] + 1;
            } else {
                for (i = 0; i < 4; i++) {
                    int[] tmp = find(matchers[i], cur, ls[i], le[i], l);
                    ls[i] = tmp[0];
                    le[i] = tmp[1];
                    if (flag) {
                        break;
                    } }
                String res = str.substring(ls[i], cur = le[i]);
                switch (order[i]) {
                    case 0:
                    case 1:
                        mid = mul(mid, new Basic(order[i], gI(res, order[i])));
                        break;
                    case 2:
                    case 3:
                        Expr outer;
                        Expr inner = create(str.substring(cur + 1, bra[cur]));
                        cur = bra[cur] + 1;
                        int[] tmp = find(matchers[4], cur, ls[4], le[4], l);
                        if (flag) {
                            res = str.substring(tmp[0], cur = tmp[1]);
                            outer = new Basic(order[i], gI(res, order[i]));
                        } else {
                            outer = new Basic(order[i], Expr.ONE);
                        }
                        mid = mul(mid, outer.composite(inner));
                        ls[4] = tmp[0];
                        le[4] = tmp[1];
                        break;
                    default:
                } }
            if (cur >= l || str.charAt(cur++) == '+') {
                expr = add(expr, mid);
                mid = null;
            } }
        return expr;
    }

    private static boolean isSin(Expr expr)
    {
        return expr.getClass() == Basic.class
                && ((Basic) expr).getExpClass() == 2;
    }

    private static void conditionalPrint(Expr expr, String str)
    {
        if (expr.getClass() == AddSub.class) {
            System.out.print(str);
        }
    }

    private static void output(Expr expr) {
        if (expr.getClass() == Basic.class) {
            Basic tmp = (Basic) expr;
            if (tmp.getExpClass() == 0) {
                System.out.print(tmp.getPara());
            } else {
                System.out.print(
                        Basic.FUNC_NAME[tmp.getExpClass()]);
                if (!tmp.getPara().equals(BigInteger.ONE)
                        && !tmp.getPara().equals(BigInteger.ZERO)) {
                    System.out.print("^" + tmp.getPara());
                }
            }
        } else if (expr.getClass() == AddSub.class) {
            AddSub tmp = (AddSub) expr;
            output(tmp.getExpr1());
            if (tmp.isAddOrSub()) {
                System.out.print("+");
            } else {
                System.out.print("-");
            }
            output(tmp.getExpr2());
        } else if (expr.getClass() == Multiply.class) {
            Multiply tmp = (Multiply) expr;
            conditionalPrint(tmp.getExpr1(), "(");
            output(tmp.getExpr1());
            conditionalPrint(tmp.getExpr1(), ")");
            System.out.print("*");
            conditionalPrint(tmp.getExpr2(), "(");
            output(tmp.getExpr2());
            conditionalPrint(tmp.getExpr2(), ")");
        } else if (expr.getClass() == Composite.class) {
            Composite tmp = (Composite) expr;
            if (isSin(tmp.getOuter())) {
                System.out.print("sin(");
            } else {
                System.out.print("cos(");
            }
            if (tmp.getInner().getClass() == AddSub.class
                    || tmp.getInner().getClass() == Multiply.class) {
                System.out.print("(");
            }
            output(tmp.getInner());
            if (tmp.getInner().getClass() == AddSub.class
                    || tmp.getInner().getClass() == Multiply.class) {
                System.out.print(")");
            }
            System.out.print(")");
            Basic basic = (Basic) tmp.getOuter();
            if (!basic.getPara().equals(BigInteger.ONE)
                    && !basic.getPara().equals(BigInteger.ZERO)) {
                System.out.print("^" + basic.getPara());
            }
        }
    }

    public static void main(String[] args)
    {
        String rawExpr = "";
        Scanner cin = new Scanner(System.in);
        if (cin.hasNextLine()) {
            rawExpr = cin.nextLine();
        } else {
            wrongFormat();
        }
        judge(rawExpr);

        rawExpr = rawExpr.replaceAll("[ \t]+", "");
        rawExpr = rawExpr.replace("+++", "+");
        rawExpr = rawExpr.replace("---", "+-");
        rawExpr = rawExpr.replace("++-", "+-");
        rawExpr = rawExpr.replace("+--", "+");
        rawExpr = rawExpr.replace("+-+", "+-");
        rawExpr = rawExpr.replace("--+", "+");
        rawExpr = rawExpr.replace("-+-", "+");
        rawExpr = rawExpr.replace("-++", "+-");
        rawExpr = rawExpr.replace("++", "+");
        rawExpr = rawExpr.replace("--", "+");
        rawExpr = rawExpr.replace("^+", "^");
        rawExpr = rawExpr.replace("-+", "+-");
        rawExpr = rawExpr.replace("-", "+-");
        rawExpr = rawExpr.replace("++-", "+-");
        rawExpr = rawExpr.replace("^+-", "^-");
        rawExpr = rawExpr.replace("*+-", "*-");
        rawExpr = rawExpr.replace("+-", "+-1*");
        if (rawExpr.charAt(0) == '*') {
            wrongFormat();
        }

        Expr expr = create(rawExpr);
        expr = expr.qiuDao();
        output(expr);
    }
}
