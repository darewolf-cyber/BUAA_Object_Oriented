import com.oocourse.elevator2.ElevatorInput;
import com.oocourse.elevator2.PersonRequest;
import java.io.IOException;
import java.util.HashMap;
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
        while ((request = cin.nextPersonRequest()) != null) {
            reentrantLock.lock();
            try {
                int realFloor = Elevator.realFloor(request.getFromFloor());
                outerRequests.get(realFloor).put(request);
                condition.signalAll();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                reentrantLock.unlock();
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
