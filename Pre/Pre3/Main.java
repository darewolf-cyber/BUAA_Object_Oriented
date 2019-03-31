import java.math.BigInteger;
import java.util.Scanner;

public class Main {

    public static void main(String[] args)
    {
        Scanner cin = new Scanner(System.in);
        String str = cin.nextLine();
        str = str.trim();
        String[] arr = str.split("[ ]+");
        int aa = Integer.valueOf(arr[0]);
        final BigInteger a = new BigInteger(arr[1], aa);
        str = cin.nextLine();
        str = str.trim();
        arr = str.split("[ ]+");
        int bb = Integer.valueOf(arr[0]);
        BigInteger b = new BigInteger(arr[1], bb);
        int cc = cin.nextInt();
        b = a.add(b);
        System.out.println(b.toString(cc));
    }
}
