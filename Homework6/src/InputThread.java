import com.oocourse.elevator2.ElevatorInput;
import com.oocourse.elevator2.PersonRequest;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class InputThread extends Thread
{

    private final HashMap<Integer,
            LinkedBlockingQueue<PersonRequest>> outerRequests;
    private final ReentrantLock reentrantLock;
    private final Condition condition;
    private final InputtingState state;

    public InputThread(HashMap<Integer, LinkedBlockingQueue<PersonRequest>> map,
                       ReentrantLock lock,
                       Condition c,
                       InputtingState state) {
        outerRequests = map;
        reentrantLock = lock;
        condition = c;
        this.state = state;
    }

    @Override
    public void run() {
        ElevatorInput cin = new ElevatorInput(System.in);
        PersonRequest request;
        Random random = new Random();
        /*try {
            System.setErr(new PrintStream("inputdata.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }*/
        while ((request = cin.nextPersonRequest()) != null) {
            System.err.println(String.format("[%.1f]%s",
                    (double)(System.currentTimeMillis()
                            - Main.getTimeStamp()) / 1000,
                    request.toString()));
            reentrantLock.lock();
            try {
                int realFloor = Elevator.realFloor(request.getFromFloor());
                outerRequests.get(realFloor).put(request);
                condition.signalAll();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                reentrantLock.unlock();
                try {
                    Thread.sleep(random.nextInt(7200));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        state.stopInputting();
        reentrantLock.lock();
        condition.signalAll();
        reentrantLock.unlock();
        try {
            cin.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
