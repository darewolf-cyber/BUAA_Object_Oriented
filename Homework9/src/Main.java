import com.oocourse.specs1.AppRunner;

public class Main {

    public static void main(String[] args) {
        try {
            AppRunner runner = AppRunner.newInstance(MyPath.class,
                    MyPathContainer.class);
            runner.run(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
