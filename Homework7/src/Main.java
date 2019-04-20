import com.oocourse.TimableOutput;
import com.oocourse.elevator3.PersonRequest;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Main {

    public static void main(String[] args) {
        TimableOutput.initStartTimestamp();

        int i;

        HashMap<Integer, LinkedBlockingQueue<PersonRequest>>
                allRequests = new HashMap<>();
        for (i = -2; i <= 20; i++) {
            allRequests.put(i, new LinkedBlockingQueue<>());
        }

        HashMap<Integer, LinkedBlockingQueue<PersonRequest>>
                selfRequests1 = new HashMap<>();
        for (i = -2; i <= 1; i++) {
            selfRequests1.put(i, new LinkedBlockingQueue<>());
        }
        for (i = 15; i <= 20; i++) {
            selfRequests1.put(i, new LinkedBlockingQueue<>());
        }

        HashMap<Integer, LinkedBlockingQueue<PersonRequest>>
                selfRequests2 = new HashMap<>();
        for (i = -1; i <= 2; i++) {
            selfRequests2.put(i, new LinkedBlockingQueue<>());
        }
        for (i = 4; i <= 15; i++) {
            selfRequests2.put(i, new LinkedBlockingQueue<>());
        }

        HashMap<Integer, LinkedBlockingQueue<PersonRequest>>
                selfRequests3 = new HashMap<>();
        for (i = 1; i <= 15; i += 2) {
            selfRequests3.put(i, new LinkedBlockingQueue<>());
        }

        ReentrantLock inputLock = new ReentrantLock();
        Condition condition = inputLock.newCondition();
        ThreadRunning inputState = new ThreadRunning();
        SyncInteger reqNum = new SyncInteger(0);

        InputThread inputThread = new InputThread(allRequests,
                inputLock, condition, reqNum, inputState);
        Dispatcher dispatcher = new Dispatcher(selfRequests1, selfRequests2,
                selfRequests3, allRequests, inputLock, condition,
                inputState, reqNum);
        inputThread.start();
        dispatcher.start();

        Elevator elevator1 = new Elevator('A', selfRequests1,
                allRequests, inputLock, condition, reqNum, inputState);
        elevator1.start();
        Elevator elevator2 = new Elevator('B', selfRequests2,
                allRequests, inputLock, condition, reqNum, inputState);
        elevator2.start();
        Elevator elevator3 = new Elevator('C', selfRequests3,
                allRequests, inputLock, condition, reqNum, inputState);
        elevator3.start();
    }
}
