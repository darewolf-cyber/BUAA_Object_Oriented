import com.oocourse.TimableOutput;
import com.oocourse.elevator1.PersonRequest;
import java.util.concurrent.LinkedBlockingQueue;

public class Main
{
    private static final LinkedBlockingQueue<PersonRequest> queue =
            new LinkedBlockingQueue<>();

    public static void main(String[] args)
    {
        TimableOutput.initStartTimestamp();

        WhenToStop state = new WhenToStop();
        InputThread inputThread = new InputThread(queue, state);
        Elevator elevator = new Elevator(queue, state);

        inputThread.start();
        elevator.start();
    }
}
