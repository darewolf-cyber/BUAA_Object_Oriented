import com.oocourse.uml2.interact.AppRunner;

public class Main {

    public static void main(String[] args) {

        long start = System.currentTimeMillis();
        try {
            AppRunner appRunner =
                    AppRunner.newInstance(MyUmlGeneralInteraction.class);
            appRunner.run(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.err.println(System.currentTimeMillis() - start);
    }
}
