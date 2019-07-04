import com.oocourse.uml1.interact.AppRunner;

public class Main {

    public static void main(String[] args) {

        long start = System.currentTimeMillis();
        AppRunner appRunner;
        try {
            appRunner = AppRunner.newInstance(MyUmlInteraction.class);
            appRunner.run(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.err.println(System.currentTimeMillis() - start);
    }
}
