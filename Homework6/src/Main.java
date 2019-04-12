import com.oocourse.TimableOutput;
import com.oocourse.elevator2.PersonRequest;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Main
{
    private static long timeStamp;

    public static long getTimeStamp() {
        return timeStamp;
    }

    public static void main(String[] args)
    {
        TimableOutput.initStartTimestamp();
        timeStamp = System.currentTimeMillis();

        final ReentrantLock reentrantLock = new ReentrantLock();
        final Condition condition = reentrantLock.newCondition();

        final HashMap<Integer, LinkedBlockingQueue<PersonRequest>> outerRequests
                = new HashMap<>();
        for (int i = -2; i <= 16; i++) {
            outerRequests.put(i, new LinkedBlockingQueue<>());
        }
        final InputtingState state = new InputtingState();

        InputThread inputThread = new InputThread(outerRequests,
                reentrantLock, condition, state);
        Elevator elevator = new Elevator(outerRequests,
                reentrantLock, condition, state);

        elevator.start();
        inputThread.start();
    }
}
