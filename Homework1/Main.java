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
    private static final String ERROR = "WRONG FORMAT!";

    private static int[] lastStart = {-1, -1};
    private static int[] lastEnd = {0, 0};

    private static String rawPoly;
    private static Matcher[] matcher = new Matcher[2];
    private static boolean flag;
    private static int start;

    private static void exitError()
    {
        System.out.println(ERROR);
        System.exit(0);
    }

    private static HashMap<BigInteger, BigInteger> createPoly()
    {
        HashMap<BigInteger, BigInteger> poly = new HashMap<>();
        String[] arr = rawPoly.split("\\+");
        int i = 0;
        while (arr[i].equals(""))
        {
            i++;
        }
        final int l = arr.length;

        while (i < l)
        {
            String[] temp = arr[i].split("\\*x\\^?");
            BigInteger key;
            Pattern pattern = Pattern.compile("\\*x\\^?");
            Matcher matcher = pattern.matcher(arr[i]);
            if (!matcher.find()) {
                key = ZERO;
            } else if (temp.length == 1 || temp[1].equals("")) {
                key = ONE;
            } else {
                key = new BigInteger(temp[1]);
            }
            BigInteger value = new BigInteger(temp[0]);
            if (poly.containsKey(key))
            {
                value = value.add(poly.get(key));
            }
            poly.put(key, value);
            i++;
        }
        return poly;
    }

    private static HashMap<BigInteger, BigInteger>
        qiuDao(HashMap<BigInteger, BigInteger> poly)
    {
        HashMap<BigInteger, BigInteger> result = new HashMap<>();

        for (BigInteger key : poly.keySet())
        {
            BigInteger value = poly.get(key);
            if (value.equals(ZERO) || key.equals(ZERO))
            {
                continue;
            }
            value = value.multiply(key);
            key = key.subtract(ONE); // (x^a)' = a*x^(a-1)
            if (result.containsKey(key))
            {
                value = value.add(result.get(key));
            }
            result.put(key, value);
        }
        return result;
    }

    private static void output(HashMap<BigInteger, BigInteger> poly)
    {
        int cnt = 0;
        LinkedList<String> list = new LinkedList<>();
        for (BigInteger key : poly.keySet())
        {
            BigInteger value = poly.get(key);
            if (value.equals(ZERO)) {
                continue;
            }
            String ans;
            if (key.equals(ZERO)) {
                ans = value.toString();
            } else {
                if (value.equals(ONE)) {
                    ans = "x";
                } else {
                    ans = value.toString() + "*x";
                }
                if (!key.equals(ONE)) {
                    ans += "^" + key.toString();
                }
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
        for (String str : list)
        {
            System.out.print(str);
            cnt++;
        }
        if (cnt <= 0) {
            System.out.println("0");
        } else {
            System.out.println();
        }
    }

    private static boolean cnt;

    private static int
        nextStart(int currentEnd, int length, boolean exit)
    {
        int i = currentEnd;
        cnt = false;
        while (i < length)
        {
            char c = rawPoly.charAt(i);
            if (c != ' ' && c != '\t') {
                if (cnt) {
                    break;
                } else if (c == '+' || c == '-') {
                    cnt = true;
                } else if (exit) {
                    exitError();
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
            if (flag = matcher[index].find(start)) { //found in the string
                lastStart[index] = matcher[index].start();
                lastEnd[index] = matcher[index].end();
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
        final int l = rawPoly.length();
        start = nextStart(0, l, false);
        if (start >= l) {
            exitError();
        }

        while (start < l) {
            flag = false;
            if (lastStart[0] <= start) {
                func(0, l);
            }
            if (!flag && lastStart[1] <= start) {
                func(1, l);
            }
            if (!flag) {
                exitError();
            }
        }
        if (cnt) {
            exitError();
        }
    }

    public static void main(String[] args)
    {
        Scanner cin = new Scanner(System.in);
        rawPoly = cin.nextLine();

        final String number = "[-+]?\\d+";
        final String term1 = number
                + "(?:[ \t]*\\*[ \t]*x[ \t]*(?:\\^[ \t]*" + number + ")?)?";
        final String term4 = "[-+]?[ \t]*x(?:[ \t]*\\^[ \t]*" + number + ")?";
        matcher[0] = Pattern.compile(term1).matcher(rawPoly);
        matcher[1] = Pattern.compile(term4).matcher(rawPoly);
        judge();

        rawPoly = rawPoly.replaceAll("[ \t]+", "");
        rawPoly = rawPoly.replace("++", "+");
        rawPoly = rawPoly.replace("--", "+");
        rawPoly = rawPoly.replace("^+", "^");
        rawPoly = rawPoly.replace("-+", "+-");
        rawPoly = rawPoly.replace("-", "+-");
        rawPoly = rawPoly.replace("++-", "+-");
        rawPoly = rawPoly.replace("^+-", "^-");
        rawPoly = rawPoly.replace("-x", "-1*x");
        rawPoly = rawPoly.replace("+x", "+1*x");
        if (rawPoly.charAt(0) == 'x')
        {
            rawPoly = "1*" + rawPoly;
        }

        HashMap<BigInteger, BigInteger> poly = createPoly();
        poly = qiuDao(poly);
        output(poly);

    }
}
