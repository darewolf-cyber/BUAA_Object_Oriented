import java.math.BigInteger;
import java.util.Scanner;

public class Main {

    public static void main(String[] args)
    {
        Scanner cin = new Scanner(System.in);
        String str = cin.nextLine();
        str = "0+" + str;
        str = str.replace(" ", "");
        str = str.replace("++", "+");
        str = str.replace("--", "+");
        str = str.replace("-+", "+-");
        str = str.replace("-", "+-");
        str = str.replace("++-", "+-");
        String[] arr = str.split("\\+");
        BigInteger sum = new BigInteger("0");
        BigInteger ele;
        int i = 0;
        while (i < arr.length)
        {
            ele = new BigInteger(arr[i]);
            ele = new BigInteger(arr[i]); //biginteger
            sum = sum.add(ele);
            i++;
        }
        System.out.println(sum);
    }
}
