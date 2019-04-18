import com.oocourse.elevator3.PersonRequest;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Dispatcher extends Thread
{
    private final ReentrantLock inputLock;
    private final Condition inputCondition;
    private final HashMap<Integer,
            LinkedBlockingQueue<PersonRequest>> allRequests;
    private final ThreadRunning inputState;
    private final SyncInteger reqNum;

    private final HashMap<Integer, LinkedBlockingQueue<PersonRequest>>[]
            selfRequests;

    public Dispatcher(HashMap<Integer, LinkedBlockingQueue<PersonRequest>>
                              selfMap1,
                      HashMap<Integer, LinkedBlockingQueue<PersonRequest>>
                              selfMap2,
                      HashMap<Integer, LinkedBlockingQueue<PersonRequest>>
                              selfMap3,
                      HashMap<Integer, LinkedBlockingQueue<PersonRequest>>
                              allMap,
                      ReentrantLock lock,
                      Condition condition,
                      ThreadRunning state,
                      SyncInteger num) {
        selfRequests = new HashMap[]{selfMap1, selfMap2, selfMap3};
        allRequests = allMap;
        inputLock = lock;
        inputCondition = condition;
        inputState = state;
        reqNum = num;
    }

    private int fixedFloor(int num, int fromFloor, int toFloor) {
        if (num == 1 && toFloor == 3) {
            if (fromFloor > 3) {
                return 5;
            } else if (fromFloor < 3) {
                return 1;
            } else {
                return 3;
            }
        }
        if (num == 2 && fromFloor == 3) {
            if (toFloor == 4) {
                return 5;
            } else if (toFloor < 3) {
                return 1;
            } else if (toFloor == 3) {
                return 3;
            }
        }
        if (toFloor > fromFloor) {
            for (int i = toFloor; i >= fromFloor; i--) {
                if (selfRequests[num].containsKey(i)) {
                    return i;
                }
            }
        } else {
            for (int i = toFloor; i <= fromFloor; i++) {
                if (selfRequests[num].containsKey(i)) {
                    return i;
                }
            }
        }
        return fromFloor;
    }

    private final Random random = new Random();
    private final int[][] arr = {
            {0, 1, 2}, {0, 2, 1},
            {1, 0, 2}, {1, 2, 0},
            {2, 0, 1}, {2, 1, 0}
    };

    private void dispatch(PersonRequest request, int fromFloor)
            throws InterruptedException { // TODO: 3F
        int i;
        int toFloor = Elevator.realFloor(request.getToFloor());
        int[] tmp = arr[random.nextInt(6)];
        for (i = 0; i <= 2; i++) {
            if (selfRequests[tmp[i]].containsKey(fromFloor)
                    && fixedFloor(tmp[i], fromFloor, toFloor) != fromFloor) {
                selfRequests[tmp[i]].get(fromFloor).put(request);
                return;
            }
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                inputLock.lock();
                while ((inputState.isRunning() || reqNum.getValue(0) != 0)
                        && Elevator.isEmpty(allRequests)) {
                    inputCondition.await();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                inputLock.unlock();
            }
            if (Elevator.isEmpty(allRequests) && !inputState.isRunning()
                    && reqNum.getValue(0) == 0) {
                return;
            }
            int i;
            for (i = -2; i <= 20; i++) {
                LinkedBlockingQueue<PersonRequest>
                        requests = allRequests.get(i);
                while (!requests.isEmpty()) {
                    try {
                        inputLock.lock();
                        dispatch(requests.take(), i);
                        inputCondition.signalAll();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        inputLock.unlock();
                    }
                } // while (!requests.isEmpty())
            } // for
        } // while (true)
    } // run()
}
