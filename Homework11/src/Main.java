import com.oocourse.specs3.AppRunner;
import work.MyPath;
import work.MyRailwaySystem;

public class Main {
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        try {
            AppRunner runner =
                    AppRunner.newInstance(MyPath.class, MyRailwaySystem.class);
            runner.run(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.err.println(System.currentTimeMillis() - start);
    }
}
