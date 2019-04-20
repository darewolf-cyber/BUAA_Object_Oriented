import com.oocourse.TimableOutput;
import com.oocourse.elevator3.PersonRequest;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Elevator extends Thread
{
    // static members
    private static final Object outputLock = new Object();

    private static final long TIME_DOOR = 200L;

    public static int realFloor(int fff) {
        if (fff < 0) {
            return fff + 1;
        } else {
            return fff;
        }
    }

    static boolean isEmpty(HashMap<Integer,
            LinkedBlockingQueue<PersonRequest>> map) {
        for (int i = -2; i <= 20; i++) {
            if (map.containsKey(i) && map.get(i).size() != 0) {
                return false;
            }
        }
        return true;
    }

    private final int maxPassengerNum;
    private final char id;
    private final long timePerFloor;
    private int floor = 1;
    private boolean doorOpened = false;
    private boolean upOrDown = true;
    private int passengerNumber = 0;
    private final HashMap<Integer, LinkedBlockingQueue<PersonRequest>>
            passengers = new HashMap<>();
    private final SyncInteger reqNum;

    private void syncOutput(String str) {
        synchronized (outputLock) {
            TimableOutput.println(str + "-" + id);
        }
    }

    // shared objects
    private final HashMap<Integer,
            LinkedBlockingQueue<PersonRequest>> selfRequests;
    private final HashMap<Integer,
            LinkedBlockingQueue<PersonRequest>> allRequests;
    private final ReentrantLock inputLock;
    private final Condition inputCondition;
    private final ThreadRunning inputState;

    public Elevator(char cc,
                    HashMap<Integer,
                            LinkedBlockingQueue<PersonRequest>> selfMap,
                    HashMap<Integer,
                            LinkedBlockingQueue<PersonRequest>> allMap,
                    ReentrantLock lock, Condition condition,
                    SyncInteger num,
                    ThreadRunning inputting) {
        int i;
        id = cc;
        selfRequests = selfMap;
        allRequests = allMap;
        inputLock = lock;
        inputCondition = condition;
        reqNum = num;
        inputState = inputting;
        switch (id) {
            case 'A':
                timePerFloor = 400L;
                maxPassengerNum = 6;
                for (i = -2; i <= 1; i++) {
                    passengers.put(i, new LinkedBlockingQueue<>());
                }
                for (i = 15; i <= 20; i++) {
                    passengers.put(i, new LinkedBlockingQueue<>());
                }
                break;
            case 'B':
                timePerFloor = 500L;
                maxPassengerNum = 8;
                for (i = -1; i <= 2; i++) {
                    passengers.put(i, new LinkedBlockingQueue<>());
                }
                for (i = 4; i <= 15; i++) {
                    passengers.put(i, new LinkedBlockingQueue<>());
                }
                break;
            case 'C':
                timePerFloor = 600L;
                maxPassengerNum = 7;
                for (i = 1; i <= 15; i += 2) {
                    passengers.put(i, new LinkedBlockingQueue<>());
                }
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    private int getFloor() {
        if (floor >= 1) {
            return floor;
        } else {
            return floor - 1;
        }
    }

    private void openDoor() {
        if (!doorOpened) {
            doorOpened = true;
            syncOutput(String.format("OPEN-%d", getFloor()));
            try {
                Thread.sleep(TIME_DOOR);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeDoor() {
        if (doorOpened) {
            do {
                try {
                    Thread.sleep(TIME_DOOR);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (getOn());
            doorOpened = false;
            syncOutput(String.format("CLOSE-%d", getFloor()));
        }
    }

    private void upOneFloor() {
        if (floor < 20) {
            try {
                Thread.sleep(timePerFloor);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            floor++;
            syncOutput(String.format("ARRIVE-%d", getFloor()));
        }
    }

    private void downOneFloor() {
        if (floor > -2) {
            try {
                Thread.sleep(timePerFloor);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            floor--;
            syncOutput(String.format("ARRIVE-%d", getFloor()));
        }
    }

    private void toNextFloor() {
        do {
            if (upOrDown) {
                upOneFloor();
            } else {
                downOneFloor();
            }
        } while (!passengers.containsKey(floor));
    }

    private int fixedFloor(PersonRequest request) { // TODO: 3F
        int toFloor = realFloor(request.getToFloor());
        if (id == 'B' && toFloor == 3) {
            if (floor > 3) {
                return 5;
            } else if (floor < 3) {
                return 1;
            } else {
                return 3;
            }
        }
        if (id == 'C' && floor == 3) {
            if (toFloor == 4) {
                return 5;
            } else if (toFloor < 3) {
                return 1;
            }
        }
        if (toFloor > floor) {
            for (int i = toFloor; i >= floor; i--) {
                if (passengers.containsKey(i)) {
                    return i;
                }
            }
        } else {
            for (int i = toFloor; i <= floor; i++) {
                if (passengers.containsKey(i)) {
                    return i;
                }
            }
        }
        return floor;
    }

    private boolean getOn() {
        if (passengerNumber >= maxPassengerNum
                || !passengers.containsKey(floor)) {
            return false;
        }
        LinkedBlockingQueue<PersonRequest> list = selfRequests.get(floor);
        LinkedBlockingQueue<PersonRequest> temp = new LinkedBlockingQueue<>();
        boolean passengerGotOn = false;
        while (list.size() != 0) {
            PersonRequest request = list.remove();
            int fff = fixedFloor(request);
            if ((fff > floor) != upOrDown || fff == floor
                    || passengerNumber >= maxPassengerNum) {
                try {
                    temp.put(request);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            passengerGotOn = true;
            openDoor();
            try {
                passengers.get(fff).put(request);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            passengerNumber++;
            syncOutput(String.format(
                    "IN-%d-%d", request.getPersonId(), getFloor()));
        }
        selfRequests.put(floor, temp);
        if (passengerNumber >= maxPassengerNum) {
            return false;
        } else {
            return passengerGotOn;
        }
    }

    private void getOff() {
        LinkedBlockingQueue<PersonRequest> list = passengers.get(floor);
        while (list.size() != 0) {
            openDoor();
            PersonRequest request = list.remove();
            syncOutput(String.format(
                    "OUT-%d-%d", request.getPersonId(), getFloor()));
            if (realFloor(request.getToFloor()) != floor) {
                try {
                    inputLock.lock();
                    allRequests.get(floor).put(request);
                    inputCondition.signalAll();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    inputLock.unlock();
                }
            } else {
                reqNum.getValue(-1);
            }
            passengerNumber--;
        }
    }

    private boolean checkDirection() {
        int i;
        if (passengerNumber != 0) {
            return false;
        }
        if (upOrDown) {
            for (i = floor + 1; i <= 20; i++) {
                if (selfRequests.containsKey(i)
                        && !selfRequests.get(i).isEmpty()) {
                    return false;
                }
            }
        } else {
            for (i = floor - 1; i >= -2; i--) {
                if (selfRequests.containsKey(i)
                        && !selfRequests.get(i).isEmpty()) {
                    return false;
                }
            }
        }
        if (!selfRequests.containsKey(floor)
                || selfRequests.get(floor).isEmpty()) {
            return true;
        } else {
            for (PersonRequest request : selfRequests.get(floor)) {
                int fff = fixedFloor(request);
                if ((fff > floor) == upOrDown) {
                    return false;
                }
            }
            return true;
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                inputLock.lock();
                while ((inputState.isRunning() || reqNum.getValue(0) != 0)
                        && passengerNumber == 0 && isEmpty(selfRequests)) {
                    inputCondition.await();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                inputLock.unlock();
            }
            if (passengerNumber == 0 && isEmpty(selfRequests)
                    && !inputState.isRunning() && reqNum.getValue(0) == 0) {
                closeDoor();
                inputLock.lock();
                inputCondition.signalAll();
                inputLock.unlock();
                return;
            }
            if (checkDirection()) {
                upOrDown = !upOrDown;
            }
            getOn();
            closeDoor();
            toNextFloor();
            getOff();
        }
    }
}
