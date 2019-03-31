import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main
{
    private static final BigInteger ZERO = new BigInteger("0");
    private static final BigInteger ONE = new BigInteger("1");
    private static final BigInteger NEGONE = new BigInteger("-1");
    private static final String NUMBER = "[-+]?\\d+";
    private static final String POWER = "x(?:[ \t]*\\^[ \t]*" + NUMBER + ")?";
    private static final String SINX =
            "sin[ \t]*\\([ \t]*x[ \t]*\\)(?:[ \t]*\\^[ \t]*" + NUMBER + ")?";
    private static final String COSX =
            "cos[ \t]*\\([ \t]*x[ \t]*\\)(?:[ \t]*\\^[ \t]*" + NUMBER + ")?";
    private static Matcher[] matchers = new Matcher[4];
    private static final Pattern[] PATTERNS = {
            Pattern.compile(POWER),
            Pattern.compile(SINX),
            Pattern.compile(COSX),
            Pattern.compile(NUMBER)};
    private static String rawExpr;

    private static void printWrongFormat()
    {
        System.out.println("WRONG FORMAT!");
        System.exit(0);
    }

    private static class Term
    {
        private BigInteger indexOfX;
        private BigInteger indexOfSin;
        private BigInteger indexOfCos;

        Term(BigInteger ix, BigInteger isin, BigInteger icos)
        {
            indexOfX = ix;
            indexOfSin = isin;
            indexOfCos = icos;
        }

        @Override
        public int hashCode()
        {
            String str = "x^" + indexOfX.toString()
                    + "*sin(x)^" + indexOfSin.toString()
                    + "*cos(x)^" + indexOfCos.toString();
            return str.hashCode();
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj.getClass() != Term.class) {
                throw new UnsupportedOperationException();
            }
            Term term = (Term) obj;
            return term.indexOfX.equals(indexOfX)
                    && term.indexOfSin.equals(indexOfSin)
                    && term.indexOfCos.equals(indexOfCos);
        }
    }

    private static final Term CONST = new Term(ZERO, ZERO, ZERO);

    private static void
        putTerm(HashMap<Term, BigInteger> target, Term key, BigInteger value)
    {
        if (target.containsKey(key)) {
            target.put(key, value.add(target.get(key)));
        } else {
            target.put(key, value);
        }
    }

    private static HashMap<Term, BigInteger> createExpr()
    {
        HashMap<Term, BigInteger> expr = new HashMap<>();
        String[] arr = rawExpr.split("\\+");
        int i = 0;
        final int l = arr.length;
        while (arr[i].equals("")) {
            i++;
        }
        while (i < l) {
            String[] fact = arr[i].split("\\*");
            Term key = new Term(ZERO, ZERO, ZERO);
            BigInteger value = ONE;
            int j = 0;
            int ll = fact.length;
            while (j < ll) {
                if (PATTERNS[3].matcher(fact[j]).matches()) {
                    BigInteger abc = new BigInteger(fact[j]);
                    if (!abc.equals(ZERO)) {
                        value = value.multiply(abc);
                        j++;
                        continue;
                    } else {
                        value = ZERO;
                        break;
                    }
                }
                String[] temp = fact[j].split("\\^");
                BigInteger bi;
                if (temp.length == 1) {
                    bi = ONE;
                } else {
                    bi = new BigInteger(temp[1]);
                }
                if (temp[0].equals("x")) {
                    key.indexOfX = key.indexOfX.add(bi);
                } else if (temp[0].equals("sin(x)")) {
                    key.indexOfSin = key.indexOfSin.add(bi);
                } else {
                    key.indexOfCos = key.indexOfCos.add(bi);
                }
                j++;
            }
            if (!value.equals(ZERO) && !key.equals(CONST)) {
                putTerm(expr, key, value);
            }
            i++;
        }
        return expr;
    }

    private static HashMap<Term, BigInteger>
        qiuDao(HashMap<Term, BigInteger> expr)
    {
        HashMap<Term, BigInteger> result = new HashMap<>();
        for (Term term : expr.keySet())
        {
            Term key;
            BigInteger fact = expr.get(term);
            BigInteger value;
            if (fact.equals(ZERO)) {
                continue;
            }
            BigInteger ix = term.indexOfX;
            BigInteger isin = term.indexOfSin;
            BigInteger icos = term.indexOfCos;
            if (!ix.equals(ZERO)) {
                value = fact.multiply(ix);
                ix = ix.subtract(ONE);
                key = new Term(ix, isin, icos);
                putTerm(result, key, value);
                ix = term.indexOfX;
            }
            if (!isin.equals(ZERO)) {
                value = fact.multiply(isin);
                isin = isin.subtract(ONE);
                icos = icos.add(ONE);
                key = new Term(ix, isin, icos);
                putTerm(result, key, value);
                isin = term.indexOfSin;
                icos = term.indexOfCos;
            }
            if (!icos.equals(ZERO)) {
                value = fact.multiply(icos).negate();
                isin = isin.add(ONE);
                icos = icos.subtract(ONE);
                key = new Term(ix, isin, icos);
                putTerm(result, key, value);
            }
        }
        return result;
    }

    private static String gTmD(boolean bbb, String str)
    {
        if (bbb) {
            return str + "*";
        } else {
            return str;
        }
    }

    private static void output(HashMap<Term, BigInteger> result)
    {
        LinkedList<String> list = new LinkedList<>();
        int cnt = 0;
        for (Term key : result.keySet()) {
            BigInteger value = result.get(key);
            if (!value.equals(ZERO)) {
                String ans = "";
                boolean jkl;
                if (jkl = !key.indexOfX.equals(ZERO)) {
                    ans += "x";
                    if (!key.indexOfX.equals(ONE)) {
                        ans += "^" + key.indexOfX.toString();
                    }
                }
                if (!key.indexOfSin.equals(ZERO)) {
                    ans = gTmD(jkl, ans);
                    ans += "sin(x)";
                    if (!key.indexOfSin.equals(ONE)) {
                        ans += "^" + key.indexOfSin.toString();
                    }
                    jkl = true;
                }
                if (!key.indexOfCos.equals(ZERO)) {
                    ans = gTmD(jkl, ans);
                    ans += "cos(x)";
                    if (!key.indexOfCos.equals(ONE)) {
                        ans += "^" + key.indexOfCos.toString();
                    }
                    jkl = true;
                }
                if (!jkl) {
                    ans = value.toString();
                } else if (!value.equals(ONE) && !value.equals(NEGONE)) {
                    ans = value.toString() + "*" + ans;
                } else if (value.equals(NEGONE)) {
                    ans = "-" + ans;
                }
                if (value.compareTo(ZERO) > 0) {
                    if (cnt > 0) {
                        ans = "+" + ans;
                    }
                    System.out.print(ans);
                    cnt++;
                } else {
                    list.add(ans);
                }
            }
        }
        for (String str : list) {
            System.out.print(str);
            cnt++;
        }
        if (cnt <= 0) {
            System.out.println("0");
        } else {
            System.out.println();
        }
    }

    private static int[] lastStart = {-1, -1, -1, -1};
    private static int[] lastEnd = {0, 0, 0, 0};
    private static boolean flag;
    private static int start;
    private static int plus;
    private static boolean star;

    private static int
        nextStart(int currentEnd, int length, boolean exit)
    { // TODO: KEY
        int i = currentEnd;
        plus = 0;
        star = false;
        while (i < length)
        {
            char c = rawExpr.charAt(i);
            if (c != ' ' && c != '\t') {
                if (star || plus >= 2) {
                    break;
                } else if (c == '+' || c == '-') {
                    plus++;
                } else if (c == '*') {
                    if (plus == 0) {
                        star = true;
                    } else {
                        printWrongFormat();
                    }
                } else if (plus == 0 && exit) {
                    printWrongFormat();
                } else {
                    break;
                }
            }
            i++;
        }
        return i;
    }

    private static void func(int index, int length)
    {
        if (lastStart[index] != start) {
            if (flag = matchers[index].find(start)) { //found in the string
                lastStart[index] = matchers[index].start();
                lastEnd[index] = matchers[index].end();
                flag = lastStart[index] == start; // found since start
            }
            else {
                lastStart[index] = length;
            }
        } else {
            flag = true;
        }
        if (flag) {
            start = nextStart(lastEnd[index], length, true);
        }
    }

    private static void judge()
    {
        final int l = rawExpr.length();
        start = nextStart(0, l, false);
        if (start >= l) {
            printWrongFormat();
        }
        while (start < l) {
            flag = false;
            if (lastStart[0] <= start) {
                func(0, l);
            }
            if (!flag && lastStart[1] <= start) {
                func(1, l);
            }
            if (!flag && lastStart[2] <= start) {
                func(2, l);
            }
            if (!flag && lastStart[3] <= start) {
                func(3, l);
            }
            if (!flag) {
                printWrongFormat();
            }
        }
        if (star || plus != 0) {
            printWrongFormat();
        }
    }

    public static void main(String[] args)
    {
        Scanner cin = new Scanner(System.in);
        if (cin.hasNextLine()) {
            rawExpr = cin.nextLine();
        }
        else {
            printWrongFormat();
        }
        matchers[0] = PATTERNS[0].matcher(rawExpr);
        matchers[1] = PATTERNS[1].matcher(rawExpr);
        matchers[2] = PATTERNS[2].matcher(rawExpr);
        matchers[3] = PATTERNS[3].matcher(rawExpr);
        judge();

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
        rawExpr = rawExpr.replace("*+", "*");
        if (rawExpr.charAt(0) == '*') {
            printWrongFormat();
        }

        HashMap<Term, BigInteger> expr = createExpr();
        expr = qiuDao(expr);
        output(expr);
        cin.close();
    }
}
