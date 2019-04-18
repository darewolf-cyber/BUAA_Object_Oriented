import com.oocourse.elevator3.ElevatorInput;
import com.oocourse.elevator3.PersonRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class InputThread extends Thread
{

    private final HashMap<Integer,
            LinkedBlockingQueue<PersonRequest>> allRequests;
    private final ReentrantLock inputLock;
    private final Condition inputCondition;
    private final ThreadRunning inputState;
    private final SyncInteger reqNum;

    public InputThread(HashMap<Integer, LinkedBlockingQueue<PersonRequest>>
                               map,
                       ReentrantLock lock, Condition c, SyncInteger num,
                       ThreadRunning state) {
        allRequests = map;
        inputLock = lock;
        inputCondition = c;
        inputState = state;
        reqNum = num;
    }

    @Override
    public void run() {
        ElevatorInput cin = new ElevatorInput(System.in);
        PersonRequest request;
        while ((request = cin.nextPersonRequest()) != null) {
            int realFloor = Elevator.realFloor(request.getFromFloor());
            try {
                inputLock.lock();
                allRequests.get(realFloor).put(request);
                reqNum.getValue(1);
                inputCondition.signalAll();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                inputLock.unlock();
            }
        }
        inputState.stopRunning();
        inputLock.lock();
        inputCondition.signalAll();
        inputLock.unlock();
        try {
            cin.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
