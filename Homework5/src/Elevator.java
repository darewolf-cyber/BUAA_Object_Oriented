import com.oocourse.TimableOutput;
import com.oocourse.elevator1.PersonRequest;
import java.util.NoSuchElementException;
import java.util.concurrent.LinkedBlockingQueue;

public class Elevator extends Thread
{
    private final LinkedBlockingQueue<PersonRequest> requests;
    private WhenToStop state;
    private PersonRequest request;

    private int floor = 1;
    private boolean doorOpened = false;

    private static final long TIME_PER_FLOOR = 500L;
    private static final long TIME_DOOR = 250L;

    public Elevator(LinkedBlockingQueue<PersonRequest> queue,
                    WhenToStop condition) {
        requests = queue;
        state = condition;
    }

    private void openDoor() {
        if (!doorOpened) {
            doorOpened = true;
            TimableOutput.println(String.format("OPEN-%d", floor));
            try {
                Thread.sleep(TIME_DOOR);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeDoor() {
        if (doorOpened) {
            try {
                Thread.sleep(TIME_DOOR);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            doorOpened = false;
            TimableOutput.println(String.format("CLOSE-%d", floor));
        }
    }

    private long absSub(int a, int b) {
        if (a >= b) {
            return (long)(a - b);
        } else {
            return (long)(b - a);
        }
    }

    private void toFloor(int target) {
        if (floor != target) {
            closeDoor();
            try {
                Thread.sleep(absSub(target, floor) * TIME_PER_FLOOR);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            floor = target;
        }
    }

    private void getOn() {
        TimableOutput.println(
                String.format("IN-%d-%d", request.getPersonId(), floor));
    }

    private void getOff() {
        TimableOutput.println(
                String.format("OUT-%d-%d", request.getPersonId(), floor));
    }

    @Override
    public void run() {
        while (state.isInputting() || !requests.isEmpty()) {
            try {
                request = requests.remove();
            } catch (NoSuchElementException e) {
                continue;
            }
            toFloor(request.getFromFloor());
            openDoor();
            getOn();
            toFloor(request.getToFloor());
            openDoor();
            getOff();
            closeDoor();
        }
    }
}
