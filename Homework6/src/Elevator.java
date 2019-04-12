import com.oocourse.TimableOutput;
import com.oocourse.elevator2.PersonRequest;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Elevator extends Thread
{
    // static members
    private static final long TIME_PER_FLOOR = 400L;
    private static final long TIME_DOOR = 200L;

    public static int realFloor(int fff) {
        if (fff < 0) {
            return fff + 1;
        } else {
            return fff;
        }
    }

    private static boolean isEmpty(HashMap<Integer,
            LinkedBlockingQueue<PersonRequest>> map) {
        for (int i = -2; i <= 16; i++) {
            if (map.get(i).size() != 0) {
                return false;
            }
        }
        return true;
    }

    // properties
    private int floor = 1;
    private boolean doorOpened = false;
    private boolean upOrDown = true;
    private int passengerNumber = 0;
    private final HashMap<Integer, LinkedBlockingQueue<PersonRequest>>
            passengers = new HashMap<>();

    // shared objects
    private final HashMap<Integer,
            LinkedBlockingQueue<PersonRequest>> outerRequests;
    private final ReentrantLock reentrantLock;
    private final Condition condition;
    private final InputtingState state;

    public Elevator(HashMap<Integer, LinkedBlockingQueue<PersonRequest>> map,
                    ReentrantLock lock,
                    Condition c,
                    InputtingState inputtingState) {
        int i;
        for (i = -2; i <= 16; i++) {
            passengers.put(i, new LinkedBlockingQueue<>());
        }
        outerRequests = map;
        reentrantLock = lock;
        condition = c;
        state = inputtingState;
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
            TimableOutput.println(String.format("OPEN-%d", getFloor()));
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
            TimableOutput.println(String.format("CLOSE-%d", getFloor()));
        }
    }

    private void upOneFloor() {
        if (floor < 16) {
            try {
                Thread.sleep(TIME_PER_FLOOR);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            floor++;
            TimableOutput.println(String.format("ARRIVE-%d", getFloor()));
        }
    }

    private void downOneFloor() {
        if (floor > -2) {
            try {
                Thread.sleep(TIME_PER_FLOOR);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            floor--;
            TimableOutput.println(String.format("ARRIVE-%d", getFloor()));
        }
    }

    private void moveOneFloor() {
        if (upOrDown) {
            upOneFloor();
        } else {
            downOneFloor();
        }
    }

    private boolean getOn() {
        LinkedBlockingQueue<PersonRequest> list = outerRequests.get(floor);
        LinkedBlockingQueue<PersonRequest> temp = new LinkedBlockingQueue<>();
        boolean passengerGotOn = false;
        while (list.size() != 0) {
            PersonRequest request = list.remove();
            int fff = realFloor(request.getToFloor());
            if ((fff > floor) != upOrDown) {
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
            TimableOutput.println(String.format(
                    "IN-%d-%d", request.getPersonId(), getFloor()));
        }
        outerRequests.put(floor, temp);
        return passengerGotOn;
    }

    private void getOff() {
        LinkedBlockingQueue<PersonRequest> list = passengers.get(floor);
        while (list.size() != 0) {
            openDoor();
            PersonRequest request = list.remove();
            TimableOutput.println(String.format(
                    "OUT-%d-%d", request.getPersonId(), getFloor()));
            passengerNumber--;
        }
    }

    private boolean checkDirection() {
        int i;
        if (passengerNumber != 0) {
            return false;
        }
        if (upOrDown) {
            for (i = floor + 1; i <= 16; i++) {
                if (!outerRequests.get(i).isEmpty()) {
                    return false;
                }
            }
        } else {
            for (i = floor - 1; i >= -2; i--) {
                if (!outerRequests.get(i).isEmpty()) {
                    return false;
                }
            }
        }
        if (outerRequests.get(floor).isEmpty()) {
            return true;
        } else {
            for (PersonRequest request : outerRequests.get(floor)) {
                int fff = realFloor(request.getToFloor());
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
            reentrantLock.lock();
            try {
                while (state.isInputting() && passengerNumber == 0
                        && isEmpty(outerRequests)) {
                    condition.await();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                reentrantLock.unlock();
            }
            if (passengerNumber == 0
                    && isEmpty(outerRequests) && !state.isInputting()) {
                closeDoor();
                return;
            }
            if (checkDirection()) {
                upOrDown = !upOrDown;
            }
            getOn();
            closeDoor();
            moveOneFloor();
            getOff();
        }
    }
}
