import com.oocourse.specs2.AppRunner;
import work.MyGraph;
import work.MyPath;

public class Main {

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        try {
            AppRunner runner =
                    AppRunner.newInstance(MyPath.class, MyGraph.class);
            runner.run(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.err.println(System.currentTimeMillis() - start);
    }
}
